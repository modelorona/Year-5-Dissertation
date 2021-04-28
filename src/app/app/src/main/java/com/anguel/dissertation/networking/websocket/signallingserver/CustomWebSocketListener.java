package com.anguel.dissertation.networking.websocket.signallingserver;

import com.anguel.dissertation.networking.webrtc2.peerconnection.CustomPeerConnection;
import com.anguel.dissertation.networking.webrtc2.sdpobserver.CustomSdpObserver;
import com.anguel.dissertation.networking.websocket.message.Message;
import com.anguel.dissertation.networking.websocket.message.MessageType;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.AddTrace;
import com.google.firebase.perf.metrics.Trace;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.sentry.Sentry;
import lombok.NoArgsConstructor;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

@NoArgsConstructor
public class CustomWebSocketListener extends WebSocketListener {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .pingInterval(25, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(1, 5, TimeUnit.MINUTES))
            .build();
    private static final ExecutorService writeExecutor = Executors.newFixedThreadPool(1);
    @lombok.Setter
    private CustomPeerConnection peerConnection;
    private WebSocket ws;

    public void connect() {
	// add your own websocket url
        Request request = new Request.Builder().url("wss://").build();
        this.ws = client.newWebSocket(request, this);
    }

    public void send(Object data) {
        writeExecutor.execute(() -> ws.send(new Gson().toJson(data, data.getClass())));
    }

    public void destroy(int code, String reason, String userID) {
        Message message = new Message(MessageType.WS_CLOSE, userID, reason);
        ws.close(code, new Gson().toJson(message, Message.class));
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        Trace trace = FirebasePerformance.getInstance().newTrace("websocketListenerFailure");
        trace.start();
        trace.incrementMetric("websocket_failure", 1);
        trace.stop();
        Sentry.captureException(t);
        webSocket.close(1000, "error occured");
        if (this.peerConnection != null) {
            this.peerConnection.close();
        }
    }

    @Override
    @AddTrace(name = "socketListenerOnMessage")
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        super.onMessage(webSocket, text);
        JsonObject j = new Gson().fromJson(text, JsonObject.class);
        if (j.has("serverUrl")) {
            peerConnection.addIceCandidate(new Gson().fromJson(j, IceCandidate.class));
        } else if (j.has("type") && (j.get("type")).getAsString().equals("OFFER")) {
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.valueOf(j.get("type").getAsString()),
                    j.get("description").getAsString());

            peerConnection.setRemoteDescription(new CustomSdpObserver(), sdp);

        } else if (j.has("type") && (j.get("type")).getAsString().equals("ANSWER")) {
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.valueOf(j.get("type").getAsString()),
                    j.get("description").getAsString());

            peerConnection.setRemoteDescription(new CustomSdpObserver(), sdp);
        } else if (j.has("response")) {
            if ("SEND_OFFER".equals(j.get("response").getAsString())) {
                this.peerConnection.createOffer();
            }
        }
    }

}
