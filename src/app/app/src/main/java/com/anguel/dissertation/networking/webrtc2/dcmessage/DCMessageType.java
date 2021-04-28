package com.anguel.dissertation.networking.webrtc2.dcmessage;

public enum DCMessageType {
    SESSION, // for when sending session data
    FINISHED, // for when done sending all session data
    CLOSE, // for when wanting to close the webrtc connection
    ERROR // if an error occured with the datachannel or else
}
