package com.anguel.dissertation.utils;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;

import com.anguel.dissertation.R;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class Utils {

    private static Utils INSTANCE;
    private final FirebaseRemoteConfig firebaseRemoteConfig;

    public static Utils getInstance() {
        if (INSTANCE == null) {
            synchronized (Utils.class) {
                INSTANCE = new Utils();
            }
        }
        return INSTANCE;
    }

    private Utils() {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    }


    public synchronized void saveSias(Context context, int sias) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(context.getString(R.string.shpref_prefix) + "_userSias", sias);
        editor.apply();
    }

    public synchronized int getSias(Context context) {
        return getSharedPreferences(context)
                .getInt(context.getString(R.string.shpref_prefix) + "_userSias", -1);
    }

    public synchronized String getUserID(Context ctx) {
        SharedPreferences sharedPref = getSharedPreferences(ctx);
        String id = sharedPref.getString(ctx.getString(R.string.shpref_prefix) + "_ID", "");
        if (id.equalsIgnoreCase("")) {
            UUID g = UUID.randomUUID();
            id = g.toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(ctx.getString(R.string.shpref_prefix) + "_ID", id);
            editor.apply();
        }
        return id;
    }

    public long getLastTimeGossipRan(Context context) {
        SharedPreferences sharedPref = getSharedPreferences(context);
        return sharedPref.getLong(context.getString(R.string.shpref_prefix) + "_lastTimeGossipRan", getTime());
    }

    public synchronized void saveNewLastTimeGossipRan(Context context, long time) {
        SharedPreferences sharedPref = getSharedPreferences(context);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(context.getString(R.string.shpref_prefix) + "_lastTimeGossipRan", time);
        editor.apply();
    }

    public long getPrevGossipSchedule(Context context) {
        SharedPreferences sharedPref = getSharedPreferences(context);
        return sharedPref.getLong(context.getString(R.string.shpref_prefix) + "_repeatInterval", 60L); // 60 minutes
    }

    private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    public synchronized void saveNewGossipSchedule(Context context, long repeatInterval) {
        // hard reset to an hour if it falls under 0

        if (repeatInterval <= 15L || repeatInterval > (60L * 16L)) {
            repeatInterval = 60L;
        }
        SharedPreferences sharedPref = getSharedPreferences(context);

//        override based on the remote config setting
        if (firebaseRemoteConfig.getBoolean(context.getString(R.string.config_gossip))) {
            repeatInterval = 60L; // once every hour
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(context.getString(R.string.shpref_prefix) + "_repeatInterval", repeatInterval);
        editor.apply();
    }

    public synchronized long getDataCutOffTime() {
        return firebaseRemoteConfig.getLong("data_cut_off");
    }

    public synchronized long getLastTimeModelMerged(Context context) {
        return getSharedPreferences(context).getLong(context.getString(R.string.shpref_prefix) + "_lastTimeModelMerged", 0L);
    }

    public synchronized void setLastTimeModelMerged(Context context, long time) {
        SharedPreferences sharedPref = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(context.getString(R.string.shpref_prefix) + "_lastTimeModelMerged", time);
        editor.apply();
    }

    public synchronized long getDataCutOffTimeInSP(Context context) {
        return getSharedPreferences(context).getLong(context.getString(R.string.shpref_prefix) + "_dataCutOffTime", 0L);
    }

    public synchronized void setDataCutOffTimeInSP(Context context, long time) {
        SharedPreferences sharedPref = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(context.getString(R.string.shpref_prefix) + "_dataCutOffTime", time);
        editor.apply();
    }

    public synchronized long getHoursSinceLastShare(Context context, long currentTime) {
        if (firebaseRemoteConfig.getBoolean(context.getString(R.string.config_data_frame))) {
            return 4L; // default consistent 4 hour frame
        } // else, calculate it based on last time data was shared. if it was 8 hours ago, it'll take 8 hours of data
        long timeLastShared = getSharedPreferences(context).getLong(context.getString(R.string.shpref_prefix) + "_lastTimeGossipRan", currentTime);
        long difference = currentTime - timeLastShared;
        long hours = TimeUnit.MILLISECONDS.toHours(difference);
        if ((hours == Long.MAX_VALUE) || (hours < 0)) {
            hours = 1L; // set min to 1 hour
        }
        return hours;
    }

    public boolean isRecordingData(Context context) {
        return getSharedPreferences(context).getBoolean(context.getString(R.string.shpref_prefix) + context.getString(R.string.pref_data_record), false);
    }

    // run the gossip worker less often
    public void increaseTimeInterval(Context context) {
        Trace trace = FirebasePerformance.getInstance().newTrace("increaseTimeInterval");
        trace.start();
        long repeatInterval = getPrevGossipSchedule(context);
        long upperLimit = 60L & 16L; // 60 min * 16 hour

        if (repeatInterval < upperLimit) {  // upper limit of 16 hours
            repeatInterval = Long.sum(repeatInterval, 30L); // increase by half hour each time after one hour
            trace.incrementMetric("interval_increase_30", 1);
        }

        saveNewGossipSchedule(context, repeatInterval);
        trace.stop();
    }

    // run the gossip worker more often
    public void decreaseTimeInterval(Context context) {
        Trace trace = FirebasePerformance.getInstance().newTrace("decreaseTimeInterval");
        trace.start();
        long repeatInterval = getPrevGossipSchedule(context);

        if (repeatInterval > 15L) { // lower limit of 15 minutes
            repeatInterval = Long.sum(repeatInterval, -15L);
            trace.incrementMetric("interval_decrease_15", 1);
        }

        saveNewGossipSchedule(context, repeatInterval);
        trace.stop();
    }

    public long getTime() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Instant.now().toEpochMilli();
        }
        return org.threeten.bp.Instant.now().toEpochMilli();
    }


    public long getPreviousTime(long currentTime, long timeToSubtract) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant current = Instant.ofEpochMilli(currentTime);
            return current.minus(timeToSubtract, ChronoUnit.HOURS).toEpochMilli();
        }
        org.threeten.bp.Instant current = org.threeten.bp.Instant.ofEpochMilli(currentTime);
        return current.minus(timeToSubtract, org.threeten.bp.temporal.ChronoUnit.HOURS).toEpochMilli();
    }

    public String getHumanReadableDate() {
        final String pattern = "yyyy-MM-dd";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern)
                    .withZone(ZoneId.systemDefault());
            return formatter.format(Instant.now());
        }
        org.threeten.bp.format.DateTimeFormatter formatter = org.threeten.bp.format.DateTimeFormatter.ofPattern(pattern)
                .withZone(org.threeten.bp.ZoneId.systemDefault());
        return formatter.format(org.threeten.bp.Instant.now());
    }

    public boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            mode = Objects.requireNonNull(appOps).checkOpNoThrow("android:get_usage_stats",
                    android.os.Process.myUid(), context.getPackageName());
        } else {
            mode = Objects.requireNonNull(appOps).unsafeCheckOpNoThrow("android:get_usage_stats",
                    android.os.Process.myUid(), context.getPackageName());
        }
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public boolean isBatteryOptDisabled(Context context) {
        PowerManager pm = (PowerManager) Objects.requireNonNull(context).getSystemService(Context.POWER_SERVICE);
        return Objects.requireNonNull(pm).isIgnoringBatteryOptimizations(context.getPackageName());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean areAllPermissionsEnabled(Context context) {
        return hasUsageStatsPermission(context) && isBatteryOptDisabled(context);
    }

}
