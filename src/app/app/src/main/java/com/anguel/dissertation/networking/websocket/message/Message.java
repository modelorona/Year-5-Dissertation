package com.anguel.dissertation.networking.websocket.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    public MessageType type;
    public String id; // device ID

    public String closeRequestReason;
}
