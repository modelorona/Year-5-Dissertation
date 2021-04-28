package com.anguel.dissertation.networking.webrtc2.peerconnection;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.content.ContextCompat;

import com.anguel.dissertation.R;
import com.anguel.dissertation.networking.webrtc2.dcmessage.DCMessage;
import com.anguel.dissertation.networking.webrtc2.dcmessage.DCMessageType;
import com.anguel.dissertation.networking.webrtc2.dcobserver.CustomDataChannelObserver;
import com.anguel.dissertation.networking.webrtc2.peerconnection.peerconnectionobserver.CustomPeerConnectionObserver;
import com.anguel.dissertation.networking.webrtc2.sdpobserver.CustomSdpObserver;
import com.anguel.dissertation.networking.websocket.message.Message;
import com.anguel.dissertation.networking.websocket.message.MessageType;
import com.anguel.dissertation.networking.websocket.signallingserver.CustomWebSocketListener;
import com.anguel.dissertation.persistence.DatabaseAPI;
import com.anguel.dissertation.persistence.converters.SessionWithAppsConverter;
import com.anguel.dissertation.persistence.entity.SessionWithApps;
import com.anguel.dissertation.persistence.entity.app.App;
import com.anguel.dissertation.persistence.entity.session.Session;
import com.anguel.dissertation.serviceengine.ServiceEngine;
import com.anguel.dissertation.utils.Utils;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.AddTrace;
import com.google.firebase.perf.metrics.Trace;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import io.sentry.Sentry;

public class CustomPeerConnection {
    private final String TAG = this.getClass().getName();
    private PeerConnection peerConnection;
    private DataChannel dataChannel;
    private final CustomWebSocketListener client;
    private final Context context;
    private boolean caller;
    // put these here to try to prevent a phone sending back data that it received from the sender. def could do better
    private final long currentTime;
    private final long previousTime;

    public CustomPeerConnection(Context context, CustomWebSocketListener signallingClient) {
        this.client = signallingClient;
        this.context = context;
        currentTime = Utils.getInstance().getTime();
        previousTime = Utils.getInstance().getPreviousTime(currentTime, Utils.getInstance().getHoursSinceLastShare(this.context, currentTime));
        init();
    }


    // hack af, don't like keeping status like this
    private synchronized void setStatus(boolean status) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();

        editor.putBoolean(context.getString(R.string.shpref_prefix) + context.getString(R.string.pref_gossip_enabled), status);
        editor.apply();
    }

    public void increaseTimeInterval() {
        Utils.getInstance().increaseTimeInterval(context);
    }

    public void decreaseTimeInterval() {
        Utils.getInstance().decreaseTimeInterval(context);
    }

    private void init() {
        setStatus(false);
        PeerConnectionFactory.InitializationOptions initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
                .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        PeerConnectionFactory peerConnectionFactory = PeerConnectionFactory.builder().setOptions(options).createPeerConnectionFactory();
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
	// use your own below
        iceServers.add(PeerConnection.IceServer.builder("").setUsername("").setPassword("").createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("").setUsername("").setPassword("").createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("").createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("").createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("").createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("").createIceServer());
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);

        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new CustomPeerConnectionObserver() {

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                client.send(iceCandidate);
                peerConnection.addIceCandidate(iceCandidate);
            }

            @Override
            public void onDataChannel(DataChannel dc) {
                super.onDataChannel(dc);
                dataChannel = dc;

                // this is the initial data sharer
                if (caller) {
                    sendData(false);
                }
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                super.onIceConnectionChange(iceConnectionState);
                if (iceConnectionState != null) {
                    Trace trace = FirebasePerformance.getInstance().newTrace("iceConnectionChange");
                    trace.start();
                    trace.incrementMetric(iceConnectionState.toString(), 1);
                    trace.stop();
                    switch (iceConnectionState) {
                        case CONNECTED:
                            setStatus(true);
                            disconnectFromWS();
                            break;
                        case DISCONNECTED:
                            setStatus(false);
                            close();
                            break;
                        case FAILED:
                            setStatus(false);
                            decreaseTimeInterval();
                            close();
                            break;
                        default:
                            break;
                    }
                }
            }

            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                super.onSignalingChange(signalingState);
                if (signalingState.equals(PeerConnection.SignalingState.HAVE_REMOTE_OFFER)) {
                    createAnswer();
                }
            }
        });

        DataChannel.Init dcInit = new DataChannel.Init();

        if (peerConnection != null) {
            dataChannel = peerConnection.createDataChannel("dataChannel", dcInit);
            dataChannel.registerObserver(getObserver());
        }

    }

    public void saveData(Session session, List<App> apps) {
        Trace trace = FirebasePerformance.getInstance().newTrace("peerConnectionSaveData");
        trace.start();
        DatabaseAPI databaseAPI = DatabaseAPI.getInstance();

        long sessionId = Objects.requireNonNull(databaseAPI).saveSession(session, context);

        if (sessionId == -1L) {
            trace.incrementMetric("session_save_fail", 1);
            trace.stop();
            return;
        }

        apps = apps
                .stream()
                .peek(app -> app.setSessionIdFK(sessionId))
                .collect(Collectors.toList());

        long count = databaseAPI.saveApps(apps, context).size();

        trace.incrementMetric("save_data_app_count", count);
        trace.stop();
    }

    public void sendData(boolean answer) {
        Trace trace = FirebasePerformance.getInstance().newTrace("peerConnectionSendData");
        trace.start();
        DatabaseAPI databaseAPI = DatabaseAPI.getInstance();

        // if database can't be reached, or if there is an issue with processing later on, just cancel and tell the other device to share.
        if (databaseAPI != null) {
            try {
                List<SessionWithApps> sessions = databaseAPI.getSessionsInTimePeriod(previousTime, currentTime, context);
                Executor executor = ContextCompat.getMainExecutor(context);
                executor.execute(() -> {
                    try {
                        List<String> sessionStrings = new ArrayList<>(sessions.size());
                        sessions.forEach(sessionWithApps -> {
                            try {
                                sessionStrings.add(SessionWithAppsConverter.sessionToString(sessionWithApps));
                            } catch (Exception e) {
                                Sentry.captureException(e, TAG.concat(": second try"));
                                sendDcMsg(DCMessageType.ERROR, context.getString(R.string.webrtc_error_msg));
                                decreaseTimeInterval();
                                trace.incrementMetric("send_data_session_string_failure", 1);
                            }
                        });
                        if (sessionStrings.size() != 0) {
                            // SEND EM
                            sessionStrings.forEach(session -> sendDcMsg(DCMessageType.SESSION, session));
                            trace.incrementMetric("send_data_session_string_size", sessionStrings.size());
                            increaseTimeInterval();
                        }
                        // send the FINISH or CLOSE here
                        if (answer) {
                            sendDcMsg(DCMessageType.CLOSE, "close");
                        } else {
                            sendDcMsg(DCMessageType.FINISHED, "finished");
                        }

                    } catch (Exception e) {
                        sendDcMsg(DCMessageType.ERROR, context.getString(R.string.webrtc_error_msg));
                        Sentry.captureException(e, TAG.concat(": first try"));
                        decreaseTimeInterval();
                        if (answer) {
                            sendDcMsg(DCMessageType.CLOSE, "close");
                        } else {
                            sendDcMsg(DCMessageType.FINISHED, "finished");
                        }
                    }
                });
            } catch (Exception e) {
                sendDcMsg(DCMessageType.ERROR, context.getString(R.string.webrtc_error_msg));
                Sentry.captureException(new Throwable(TAG.concat(": database error on getting sessions in time period")));
                decreaseTimeInterval();
                if (answer) {
                    sendDcMsg(DCMessageType.CLOSE, "close");
                } else {
                    sendDcMsg(DCMessageType.FINISHED, "finished");
                }
            }
        } else {
            sendDcMsg(DCMessageType.ERROR, context.getString(R.string.webrtc_error_msg));
            Sentry.captureException(new Throwable(TAG.concat(": databaseAPI is null")));
            decreaseTimeInterval();
            if (answer) {
                sendDcMsg(DCMessageType.CLOSE, "close");
            } else {
                sendDcMsg(DCMessageType.FINISHED, "finished");
            }
        }
        trace.stop();
    }

    private void sendDcMsg(DCMessageType type, String msg) {
        DCMessage message = new DCMessage();
        message.setType(type);
        message.setMessage(msg);

        ByteBuffer buffer = ByteBuffer.wrap(message.toString().getBytes());
        try {
            this.dataChannel.send(new DataChannel.Buffer(buffer, false));
        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }

    public CustomDataChannelObserver getObserver() {
        return new CustomDataChannelObserver(this);
    }


    public void disconnectFromWS() {
        if (client != null) {
            try {
                client.destroy(1000, "Called from CustomPeerConnection", Utils.getInstance().getUserID(this.context));
            } catch (Exception e) {
                Sentry.captureException(e);
            }
        }
    }

    public void connectToWs() {
        Message message = new Message();
        message.setType(MessageType.REQUEST_TO_CONNECT);

        message.setId(Utils.getInstance().getUserID(context));
        client.send(message);
    }

    public void addIceCandidate(IceCandidate candidate) {
        this.peerConnection.addIceCandidate(candidate);
    }

    public void setRemoteDescription(CustomSdpObserver observer, SessionDescription sdp) {
        this.peerConnection.setRemoteDescription(observer, sdp);
    }

    public void createOffer() {
        this.caller = true;
        peerConnection.createOffer(new CustomSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                Trace trace = FirebasePerformance.getInstance().newTrace("peerConnectionCreateOfferSuccess");
                trace.start();
                peerConnection.setLocalDescription(new CustomSdpObserver(), sessionDescription);
                trace.incrementMetric("create_offer_offer", 1);
                trace.stop();
                client.send(sessionDescription);
            }

            @Override
            public void onCreateFailure(String s) {
                super.onCreateFailure(s);
                Trace trace = FirebasePerformance.getInstance().newTrace("peerConnectionCreateOfferFailure");
                trace.start();
                trace.incrementMetric("create_offer_failure", 1);
                trace.stop();
            }
        }, this.getMediaConstraints());
    }

    public void createAnswer() {
        peerConnection.createAnswer(new CustomSdpObserver() {

            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                Trace trace = FirebasePerformance.getInstance().newTrace("peerConnectionCreateAnswer");
                trace.start();
                trace.incrementMetric("create_answer_answer", 1);
                trace.stop();
                peerConnection.setLocalDescription(new CustomSdpObserver(), sessionDescription);
                client.send(sessionDescription);
            }

            @Override
            public void onCreateFailure(String s) {
                super.onCreateFailure(s);
                // Answer creation failed
                Trace trace = FirebasePerformance.getInstance().newTrace("peerConnectionCreateAnswer");
                trace.start();
                trace.incrementMetric("create_answer_failure", 1);
                trace.stop();
            }
        }, this.getMediaConstraints());
    }

    private MediaConstraints getMediaConstraints() {
        MediaConstraints constraints = new MediaConstraints();
        constraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        constraints.optional.add(new MediaConstraints.KeyValuePair("internalSctpDataChannels", "true"));
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"));
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("IceRestart", "true"));
        return constraints;
    }

    //    gotta catch em all!
    @AddTrace(name = "peerConnectionClose")
    public void close() {
        ContextCompat.getMainExecutor(context)
                .execute(() -> {
                    if (dataChannel != null) {
                        try {
                            dataChannel.close();
                        } catch (Exception ignored) {
                        }
                    }

                    if (peerConnection != null) {
                        try {
                            peerConnection.close();
                        } catch (Exception ignored) {
                        }
                    }

                    // just in case
                    this.disconnectFromWS();

                    // again, just in case
                    setStatus(false);

                    // finally, kill the service
                    ServiceEngine.getInstance(context).stopGossipService(context);
                });
    }

}
