package com.anguel.dissertation.networking.websocket.message;

public enum MessageType {
    REQUEST_TO_CONNECT, // for when this device wants data from others
    DONE, // for when current connection is done, and ws connection can be closed,
    WS_CLOSE // for when the request is of websocket.close() with reason and code
}
