package com.anguel.dissertation.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.anguel.dissertation.serviceengine.ServiceEngine;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

public class BatteryMonitorReceiver extends BroadcastReceiver {
    private static final String TAG = BatteryMonitorReceiver.class.toString();

    @Override
    public void onReceive(Context context, Intent intent) {
        Trace trace = FirebasePerformance.getInstance().newTrace(BatteryMonitorReceiver.TAG);
        if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
            trace.incrementMetric("battery_low", 1);
            ServiceEngine.getInstance(context).stopGossipService(context, false);
            ServiceEngine.getInstance(context).stopEventMonitoringService(context);
        } else if (intent.getAction().equals(Intent.ACTION_BATTERY_OKAY)) {
            trace.incrementMetric("battery_okay", 1);
            ServiceEngine.getInstance(context).startEventMonitoringService(context);
        }
        trace.stop();
    }
}
