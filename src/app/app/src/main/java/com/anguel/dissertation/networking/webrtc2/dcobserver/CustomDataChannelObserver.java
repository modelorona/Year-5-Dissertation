package com.anguel.dissertation.networking.webrtc2.dcobserver;

import com.anguel.dissertation.networking.webrtc2.dcmessage.DCMessage;
import com.anguel.dissertation.networking.webrtc2.peerconnection.CustomPeerConnection;
import com.anguel.dissertation.persistence.converters.SessionWithAppsConverter;
import com.anguel.dissertation.persistence.entity.app.App;
import com.anguel.dissertation.persistence.entity.session.Session;
import com.google.firebase.perf.metrics.AddTrace;

import org.webrtc.DataChannel;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import io.sentry.Sentry;

public class CustomDataChannelObserver implements DataChannel.Observer {
    private final CustomPeerConnection peerConnection;

    public CustomDataChannelObserver(CustomPeerConnection peerConnection) {
        this.peerConnection = peerConnection;
    }

    @Override
    public void onBufferedAmountChange(long l) {

    }

    @Override
    public void onStateChange() {

    }

    @Override
    @AddTrace(name = "dcObserverOnMessage")
    @SuppressWarnings("unchecked")
    public void onMessage(DataChannel.Buffer buffer) {
        ByteBuffer data = buffer.data;
        byte[] bytes = new byte[data.remaining()];
        data.get(bytes);
        final String message = new String(bytes);
//        convert to WebRtcMessage
        DCMessage DCMessage = com.anguel.dissertation.networking.webrtc2.dcmessage.DCMessage.parseStringToWebRtcMessage(message);
        switch (DCMessage.getType()) {
            case SESSION:
//        convert the WebRtcMessage.message to List<Apps>
                try {
                    Object[] msgArr = SessionWithAppsConverter.stringToData(DCMessage.getMessage());
                    Session session = (Session) msgArr[0];
                    List<App> apps = (ArrayList<App>) msgArr[1];
                    // send to peerconnection to save. avoiding saving here as to not have to pass context
                    this.peerConnection.saveData(session, apps);
                } catch (Exception e) {
                    Sentry.captureException(e);
                }
                break;
            case FINISHED:
                // tell peerconnection to share data
                peerConnection.sendData(true);
                break;
            case CLOSE:
                this.peerConnection.close();
                break;
            case ERROR:
                // do whatever with error, we did not have time to implement robust handling
                break;
        }
    }
}
