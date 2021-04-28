package com.anguel.dissertation.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.anguel.dissertation.serviceengine.ServiceEngine;
import com.anguel.dissertation.utils.Utils;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

public class GossipWorker extends Worker {

    public GossipWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Trace trace = FirebasePerformance.getInstance().newTrace("gossipWorker");
        trace.start();
        trace.incrementMetric("gossip_worker_starting_gossip", 1);
        trace.stop();
        Utils.getInstance().saveNewLastTimeGossipRan(getApplicationContext(), Utils.getInstance().getTime());
        ServiceEngine.getInstance(getApplicationContext()).startGossipService(getApplicationContext());
        return Result.success();
    }
}