package com.anguel.dissertation.serviceengine.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.anguel.dissertation.MainActivity;
import com.anguel.dissertation.R;
import com.anguel.dissertation.utils.Utils;
import com.anguel.dissertation.workers.GossipWorker;
import com.anguel.dissertation.workers.SaveUsageStatsWorker;
import com.anguel.dissertation.workers.Trainer;
import com.google.firebase.perf.metrics.AddTrace;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.pranavpandey.android.dynamic.engine.DynamicEngine;
import com.pranavpandey.android.dynamic.engine.model.DynamicAppInfo;

import java.util.concurrent.TimeUnit;

public class EventMonitoringService extends DynamicEngine {

    private long startTime = Utils.getInstance().getTime();
    private static boolean isServiceRunning = false;


    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        startServiceWithNotification();
        return START_STICKY;
    }

    private void startServiceWithNotification() {
        if (isServiceRunning) return;
        isServiceRunning = true;

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setAction(getString(R.string.keep_alive_service));  // A string containing the action name
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, getString(R.string.persistence_notif_id))
                .setContentTitle(getResources().getString(R.string.app_name))
                .setTicker(getResources().getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText(getResources().getString(R.string.alive_notif_text))
                .setSmallIcon(R.drawable.ic_death_star)
                .setContentIntent(contentPendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .setGroup(getString(R.string.persistence_notif_group))
                .build();

        notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;     // NO_CLEAR makes the notification stay when the user performs a "delete all" command
        startForeground(R.integer.keep_alive_notif_channel, notification);

    }

    @Override
    public void onDestroy() {
        isServiceRunning = false;
        stopForeground(true);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onInitialize(boolean charging, boolean headset, boolean docked) {
        super.onInitialize(charging, headset, docked);
    }

    @Override
    public void onCallStateChange(boolean call) {
        super.onCallStateChange(call);
    }

    @Override
    public void onScreenStateChange(boolean screenOff) {
        super.onScreenStateChange(screenOff);
    }

    @Override
    @AddTrace(name = "eventMonitoringServiceOnLockStateChange")
    public void onLockStateChange(boolean locked) {
        super.onLockStateChange(locked);
//        if screen is unlocked, record start time
//        else, record the end time and create a background task to record the data. then reset the variables
        long endTime = Utils.getInstance().getTime();
        if (locked) {
            Data workerData = new Data.Builder()
                    .putLong(getString(R.string.session_start), startTime)
                    .putLong(getString(R.string.session_end), endTime)
                    .putBoolean(getString(R.string.anxious), Utils.getInstance().getSias(getApplicationContext()) >= 34)
                    .build();
            OneTimeWorkRequest saveSessionData = new OneTimeWorkRequest.Builder(SaveUsageStatsWorker.class)
                    .setInputData(workerData)
                    .build();
            WorkManager.getInstance(this)
                    .enqueueUniqueWork("usageStatsWorker".concat(Long.toString(startTime)).concat(Long.toString(endTime)), ExistingWorkPolicy.KEEP, saveSessionData);
        } else {
            startTime = Utils.getInstance().getTime();
        }

        checkGossipService();
    }

    @AddTrace(name = "checkGossipService")
    private void checkGossipService() {
        long lastTimeRan = Utils.getInstance().getLastTimeGossipRan(getApplicationContext()); // milli
        long repeatInterval = Utils.getInstance().getPrevGossipSchedule(getApplicationContext()); // minutes, needs to convert to milli
        repeatInterval = TimeUnit.MILLISECONDS.convert(repeatInterval, TimeUnit.MINUTES); // conversion from min to milli
        long currentTime = Utils.getInstance().getTime(); // milli

        if (currentTime >= lastTimeRan + repeatInterval) {
            // run the gossip service
            OneTimeWorkRequest runGossipWorker = new OneTimeWorkRequest.Builder(GossipWorker.class)
                    .setConstraints(getConstraints())
                    .setInitialDelay(2L, TimeUnit.SECONDS)
                    .build();

            WorkManager.getInstance(getApplicationContext())
                    .enqueueUniqueWork("gossipWorker", ExistingWorkPolicy.KEEP, runGossipWorker);

            FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            mFirebaseRemoteConfig.fetchAndActivate();
        } else {
            long currentCutOffTime = Utils.getInstance().getDataCutOffTimeInSP(getApplicationContext());
            long updatedTime = Utils.getInstance().getDataCutOffTime();

            if (currentCutOffTime < updatedTime) {
                Utils.getInstance().setDataCutOffTimeInSP(getApplicationContext(), updatedTime);

                Data inputdata = new Data.Builder()
                        .putBoolean("overallModel", true)
                        .build();

                OneTimeWorkRequest mlTrain = new OneTimeWorkRequest.Builder(Trainer.class)
                        .setConstraints(new Constraints.Builder().setRequiresBatteryNotLow(true).build())
                        .setInputData(inputdata)
                        .build();

                WorkManager.getInstance(getApplicationContext())
                        .enqueueUniqueWork("trainOverallModel", ExistingWorkPolicy.KEEP, mlTrain);
            }
        }

    }

    private Constraints getConstraints() {
        return new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
    }

    @Override
    public void onHeadsetStateChange(boolean connected) {
        super.onHeadsetStateChange(connected);
    }

    @Override
    public void onChargingStateChange(boolean charging) {
        super.onChargingStateChange(charging);
    }

    @Override
    public void onDockStateChange(boolean docked) {
        super.onDockStateChange(docked);
    }

    @Override
    public void onAppChange(@Nullable DynamicAppInfo dynamicAppInfo) {
        super.onAppChange(dynamicAppInfo);
    }

    @Override
    public void onPackageUpdated(@Nullable DynamicAppInfo dynamicAppInfo, boolean newPackage) {

    }

    @Override
    public void onPackageRemoved(@Nullable String packageName) {

    }


}
