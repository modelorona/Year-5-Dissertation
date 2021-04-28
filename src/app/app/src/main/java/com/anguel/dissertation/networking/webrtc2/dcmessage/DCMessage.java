package com.anguel.dissertation.networking.webrtc2.dcmessage;

import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class DCMessage {

    public transient static String sep = "%%%";

    private DCMessageType type;
    private String message; // the deserialised data (List<Apps> + Session)

    @NotNull
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(type.toString()).append(sep);
        result.append(message);

        result.trimToSize();
        return result.toString();
    }

    public static DCMessage parseStringToWebRtcMessage(String message) {
        String[] splitMsg = message.split(sep);
        DCMessage result = new DCMessage();
        result.setType(DCMessageType.valueOf(splitMsg[0]));
        result.setMessage(splitMsg[1]);
        return result;
    }

}
