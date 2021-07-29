package com.insightsystems.dal.crestron;

import java.util.HashMap;
import java.util.Map;

public class NVX_Constants {
    protected static final String[] deviceInfo = new String[]{
        "BuildDate",
        "DeviceVersion",
        "Model",
        "Name",
        "RebootReason",
        "SerialNumber"
    };
    protected static final String[] receiveStreamStats = new String[]{
        "Status",
        "StreamLocation",
        "AspectRatio",
        "AudioChannels",
        "AudioFormat",
        "AudioMode",
        "Bitrate",
        "Buffer",
        "CodecReady",
        "ElapsedSeconds",
        "HdcpTransmitterMode",
        "InitiatorAddress",
        "IsAutomaticInitiationEnabled",
        "IsPasswordProtectionEnabled",
        "MulticastAddress",
        "NumAudioPacketsDropped",
        "NumAudioPacketsRcvd",
        "NumVideoPacketsDropped",
        "NumVideoPacketsRcvd",
        "Pause",
        "Processing",
        "RtpAudioPort",
        "RtpVideoPort",
        "RtspPort",
        "SessionInitiation",
        "StreamLocation",
        "StreamProfile",
        "StreamType",
        "TcpMode",
        "TransportMode",
        "TsPort",
        "Username",
        "VideoFormat",
        "Volume"
    };
    protected static final String[] transmitStreamStats = new String[]{
        "MultiCastTtl",
        "SnapshotUri",
        "SnapshotFileName",
        "RtspSessionName",
        "RtspStreamFileName",
        "Bitrate",
        "ActiveBitrate",
        "Status",
        "ElapsedSeconds",
        "MulticastAddress",
        "IsPasswordProtectionEnabled",
        "Pause",
        "Processing",
        "SessionInitiation",
        "Start",
        "Stop",
        "StreamLocation",
        "StreamProfile",
        "TransportMode",
        "Username",
        "RtspPort",
        "TsPort",
        "RtpVideoPort",
        "RtpAudioPort",
        "NumVideoPacketsRcvd",
        "NumAudioPacketsRcvd",
        "IsAutomaticInitiationEnabled",
        "AudioMode",
        "IsAdaptiveBitrateMode"
    };
}
