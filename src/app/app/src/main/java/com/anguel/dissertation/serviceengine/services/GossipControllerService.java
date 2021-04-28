package com.anguel.dissertation.serviceengine.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.anguel.dissertation.MainActivity;
import com.anguel.dissertation.R;
import com.anguel.dissertation.networking.webrtc2.peerconnection.CustomPeerConnection;
import com.anguel.dissertation.networking.websocket.signallingserver.CustomWebSocketListener;
import com.anguel.dissertation.serviceengine.ServiceEngine;
import com.anguel.dissertation.utils.Utils;
import com.anguel.dissertation.workers.CheckGossipWorker;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;


public class GossipControllerService extends Service {

    @Getter
    private static boolean serviceRunning = false;
    @Setter
    private static boolean startEventOnDeath = true;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && Objects.requireNonNull(intent.getAction()).equals(getString(R.string.gossip_service))) {
            startEventOnDeath = true;
            setUpGossipService();
        }
        return START_NOT_STICKY;
    }

    private void setUpGossipService() {
        if (serviceRunning) return;
        serviceRunning = true;

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setAction(getString(R.string.gossip_service));  // A string containing the action name
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, getString(R.string.gossip_notif_id))
                .setContentTitle(getResources().getString(R.string.gossip_name))
                .setTicker(getResources().getString(R.string.gossip_ticker))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentText(getResources().getString(R.string.gossip_notif_text))
                .setSmallIcon(R.drawable.ic_death_star)
                .setContentIntent(contentPendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .setGroup(getString(R.string.persistence_notif_group))
                .build();

        notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;
        startForeground(R.integer.gossip_keep_alive, notification);
        ServiceEngine.getInstance(getApplicationContext()).stopEventMonitoringService(getApplicationContext());


        CustomWebSocketListener wsListener = new CustomWebSocketListener();
        wsListener.connect();
        CustomPeerConnection peerConnection = new CustomPeerConnection(getApplicationContext(), wsListener);
        wsListener.setPeerConnection(peerConnection);
        peerConnection.connectToWs();

        // hacky
        // set a worker to start in ~5 minutes to see if the connection is made, or if the device is just idling
        WorkRequest checkConnectionStatus = new OneTimeWorkRequest.Builder(CheckGossipWorker.class)
                .setInitialDelay(5, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(getApplicationContext())
                .enqueue(checkConnectionStatus);
    }

    @Override
    public void onDestroy() {
        Trace trace = FirebasePerformance.getInstance().newTrace("gossipControllerServiceOnDestroy");
        trace.start();
        trace.incrementMetric("stopped", 1);
        trace.stop();
        serviceRunning = false;
        if (Utils.getInstance().isRecordingData(getApplicationContext()) && startEventOnDeath)
            ServiceEngine.getInstance(getApplicationContext()).startEventMonitoringService(getApplicationContext());
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
