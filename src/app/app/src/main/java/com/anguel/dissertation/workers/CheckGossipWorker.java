package com.anguel.dissertation.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.anguel.dissertation.R;
import com.anguel.dissertation.serviceengine.ServiceEngine;
import com.anguel.dissertation.utils.Utils;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

public class CheckGossipWorker extends Worker {

    public CheckGossipWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Trace trace = FirebasePerformance.getInstance().newTrace("checkGossipWorker");
        trace.start();
        boolean peerConnected = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                .getBoolean(getApplicationContext().getString(R.string.shpref_prefix) + getApplicationContext().getString(R.string.pref_gossip_enabled), false);

        if (!peerConnected) {
            trace.incrementMetric("check_gossip_worker_inactive", 1);
            ServiceEngine.getInstance(getApplicationContext()).stopGossipService(getApplicationContext());
            // decrease the timeinterval between gossip worker tasks
            Utils.getInstance().decreaseTimeInterval(getApplicationContext());
        } else { // else assume that the connection has occured and data is being shared, and that it'll kill itself after finished
            trace.incrementMetric("check_gossip_worker_active", 1);
        }
        trace.stop();
        return Result.success();
    }
}
