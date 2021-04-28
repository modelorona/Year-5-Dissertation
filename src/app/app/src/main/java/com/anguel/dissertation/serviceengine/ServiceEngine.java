package com.anguel.dissertation.serviceengine;


import android.content.Context;
import android.content.Intent;

import com.anguel.dissertation.R;
import com.anguel.dissertation.serviceengine.services.EventMonitoringService;
import com.anguel.dissertation.serviceengine.services.GossipControllerService;


public class ServiceEngine {

    private static volatile ServiceEngine instance;

    private final Intent gossipIntent;
    private final Intent eventMonitoringIntent;


    private ServiceEngine(Context context) {
        eventMonitoringIntent = new Intent(context.getApplicationContext(), EventMonitoringService.class).setAction(context.getString(R.string.monitor_service));
        gossipIntent = new Intent(context.getApplicationContext(), GossipControllerService.class).setAction(context.getString(R.string.gossip_service));
    }

    public static ServiceEngine getInstance(Context context) {
        if (instance == null) {
            synchronized (ServiceEngine.class) {
                instance = new ServiceEngine(context);
            }
        }
        return instance;
    }

    public void startGossipService(Context context) {
        if (!GossipControllerService.isServiceRunning()) {
            if (gossipIntent != null) {
                context.startService(gossipIntent);
            }
        }
    }

    public void stopGossipService(Context context) {
        if (gossipIntent != null) {
            context.stopService(gossipIntent);
        }
    }

    public void stopGossipService(Context context, boolean startEventOnDeath) {
        if (gossipIntent != null) {
            GossipControllerService.setStartEventOnDeath(startEventOnDeath);
            stopGossipService(context);
        }
    }

    public void stopEventMonitoringService(Context context) {
        if (eventMonitoringIntent != null) {
            context.stopService(eventMonitoringIntent);
        }
    }

    public void startEventMonitoringService(Context context) {
        if (!GossipControllerService.isServiceRunning()) {
            if (eventMonitoringIntent != null) {
                context.startService(eventMonitoringIntent);
            }
        }
    }

}
