package com.insightsystems.dal.crestron;

import com.avispl.symphony.api.common.error.NotAuthorizedException;
import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.error.CommandFailureException;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.dal.communicator.RestCommunicator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.*;

import static com.insightsystems.dal.crestron.NVX_Constants.*;

public class NVX extends RestCommunicator implements Monitorable, Controller{
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public NVX(){
        this.setAuthenticationScheme(AuthenticationScheme.None);
        this.setContentType("application/json");
        this.setTrustAllCertificates(true);
    }

    @Override
    protected void authenticate() throws Exception {
        try {
            this.doPost("userlogin.html", "login=" + this.getLogin() + "&passwd=" + this.getPassword());
        } catch (CommandFailureException e){
            if (e.getStatusCode() == 403){
                throw new NotAuthorizedException("Username and password combination is invalid",e);
            }
            throw e;
        }
    }

    @Override
    public List<Statistics> getMultipleStatistics() throws Exception {
        ExtendedStatistics extStats = new ExtendedStatistics();
        Map<String,String> stats = new HashMap<>();
        List<AdvancedControllableProperty> controls = new ArrayList<>();

        JsonNode deviceResponse;
        try {
            deviceResponse = objectMapper.readTree(this.doGet("/Device"));
        } catch(Exception e){ //Retry after authentication
            this.authenticate();
            deviceResponse = objectMapper.readTree(this.doGet("/Device"));
        }

        putAllStatistics(stats,deviceResponse.at("/Device/DeviceInfo"),"DeviceInfo#",transmitStreamStats);
        getPreviewInfo(stats,controls,deviceResponse);
        getDeviceConfig(stats,controls,deviceResponse);


        ArrayNode receiveStreams = (ArrayNode) deviceResponse.at("/Device/StreamReceive/Streams");
        for (int i = 0; i < receiveStreams.size(); i++){
            JsonNode stream = receiveStreams.get(i);
            String prefix = "Receive Streams#Stream"+i;
            putAllStatistics(stats,stream,prefix,receiveStreamStats); //Add all stats we want from this section
            String resolution = stream.at("/HorizontalResolution").asText() + "x" + stream.at("/VerticalResolution") + "@" +stream.at("/FramesPerSecond").asText();
            stats.put(prefix+"Resolution",resolution);
        }

        ArrayNode transmitStreams = (ArrayNode) deviceResponse.at("/Device/StreamTransmit/Streams");
        for (int i = 0; i < transmitStreams.size(); i++){
            JsonNode stream = transmitStreams.get(i);
            String prefix = "Transmit Streams#Stream"+i;

            putAllStatistics(stats,stream,prefix,transmitStreamStats); //Add all stats we want from this section

            String resolution = stream.at("/HorizontalResolution").asText() + "x" + stream.at("/VerticalResolution") + "@" +stream.at("/FramesPerSecond").asText();
            String audioMode = stream.at("/AudioChannels").asText()+"CH "+stream.at("/AudioFormat").asText();

            stats.put(prefix+"Resolution",resolution);
            stats.put(prefix+"Audio",audioMode);
        }

        extStats.setStatistics(stats);
        extStats.setControllableProperties(controls);
        return Collections.singletonList(extStats);
    }

    private void getDeviceConfig(Map<String, String> stats, List<AdvancedControllableProperty> controls, JsonNode json) {
        String prefix = "DeviceConfig#";

        JsonNode timezones = json.at("/Device/UserInterfaceConfig/DeviceSupport/TimeZones");
        String[] options = new String[timezones.size()];
        String[] labels = new String[timezones.size()];

        

    }

    private void getPreviewInfo(Map<String, String> stats, List<AdvancedControllableProperty> controls, JsonNode deviceResponse) {
        JsonNode preview = deviceResponse.at("/Device/Preview");
        String prefix = "Preview#";
        boolean enabled = preview.at("/IsPreviewOutputEnabled").asBoolean();

        addSwitchControl(stats,controls,prefix+"PreviewEnabled","offLabel","onLabel",true);

        if (!enabled) return; //Continue only if Preview is enabled


        stats.put(prefix + "StatusMessage", preview.at("/StatusMessage").asText());

        JsonNode images = preview.at("/ImageList"); //Get highest available quality preview image url
        if (images.has("Image3") && images.at("/Image3/IsImageAvailable").asBoolean(false))
            stats.put(prefix + "ImageUrl",images.at("/Image3/IPv4Path").asText());
         else if (images.has("Image2") && images.at("/Image2/IsImageAvailable").asBoolean(false))
            stats.put(prefix + "ImageUrl",images.at("/Image2/IPv4Path").asText());
         else if (images.has("Image1") && images.at("/Image1/IsImageAvailable").asBoolean(false))
            stats.put(prefix + "ImageUrl",images.at("/Image1/IPv4Path").asText());
         else
            stats.put(prefix + "ImageUrl","");
    }

    private static void addSwitchControl(Map<String, String> stats, List<AdvancedControllableProperty> controls,String name, String offLabel, String onLabel, boolean state) {
        AdvancedControllableProperty.Switch toggle = new AdvancedControllableProperty.Switch();
        toggle.setLabelOff(offLabel);
        toggle.setLabelOn(onLabel);

        stats.put(name,state?"1":"0");
        controls.add(new AdvancedControllableProperty(name,new Date(),toggle,state?"1":"0"));
    }

    /**
     * Add a list of statistics by their Json key, each with the string prefix
     * @param stats Statistics Map to add statistics to
     * @param json JsonNode which is a direct parent of the required statistics
     * @param prefix Name prefix to append to the start of each statistic
     * @param statNameArray Array of JsonNode nodes to retrieve text value of
     */
    private static void putAllStatistics(Map<String, String> stats, JsonNode json, String prefix, String[] statNameArray) {
        for (String stat : statNameArray)
            stats.put(prefix+stat,json.at("/"+stat).asText());
    }

    @Override
    public void controlProperty(ControllableProperty controllableProperty) throws Exception {

    }

    @Override
    public void controlProperties(List<ControllableProperty> list) throws Exception {
        for(ControllableProperty cp : list)
            this.controlProperty(cp);
    }

    @Override
    protected HttpHeaders putExtraRequestHeaders(HttpMethod httpMethod, String uri, HttpHeaders headers) throws Exception {

        return headers;
    }

//    public static void main(String[] args) throws Exception {
//        NVX device = new NVX();
//        device.setProtocol("https");
//        device.setHost("10.198.79.243");
//        device.setLogin("admin");
//        device.setPassword("admin");
//        device.init();
//
//        ((ExtendedStatistics)device.getMultipleStatistics().get(0)).getStatistics().forEach((k,v)-> System.out.println(k+" : "+v));
//    }
}
