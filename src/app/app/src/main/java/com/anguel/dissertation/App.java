package com.anguel.dissertation;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.anguel.dissertation.serviceengine.ServiceEngine;
import com.anguel.dissertation.utils.Utils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.jakewharton.threetenabp.AndroidThreeTen;


public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
        ServiceEngine.getInstance(this);

        WorkManager.initialize(getApplicationContext(), getWorkManagerConfiguration());

        // this below will save the initial default values if they don't exist
        long repeatInterval = Utils.getInstance().getPrevGossipSchedule(getApplicationContext());
        Utils.getInstance().saveNewGossipSchedule(getApplicationContext(), repeatInterval);

        long lastTimeGossipRan = Utils.getInstance().getLastTimeGossipRan(getApplicationContext());
        Utils.getInstance().saveNewLastTimeGossipRan(getApplicationContext(), lastTimeGossipRan);

        Utils.getInstance().getUserID(getApplicationContext());

        FirebaseApp.initializeApp(getApplicationContext());

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setFetchTimeoutInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        mFirebaseRemoteConfig.fetchAndActivate();
    }

    @NonNull
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .build();
    }
}
