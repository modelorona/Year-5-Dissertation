package com.anguel.dissertation.workers;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.anguel.dissertation.R;
import com.anguel.dissertation.persistence.DatabaseAPI;
import com.anguel.dissertation.persistence.entity.app.App;
import com.anguel.dissertation.persistence.entity.session.Session;
import com.google.firebase.perf.metrics.AddTrace;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


public class SaveUsageStatsWorker extends Worker {

    @SuppressWarnings("CanBeFinal")
    private final AtomicInteger nId = new AtomicInteger();

    public SaveUsageStatsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @AddTrace(name = "usageStatsWorkerGetAdditionalAppDetails")
    private Map<String, String> getAdditionalAppDetails(String packageName) {
        Map<String, String> details = new HashMap<>();
        details.put(getString(R.string.package_name), packageName);
        try {
            PackageManager packManager = getApplicationContext().getPackageManager();
            ApplicationInfo app = packManager.getApplicationInfo(packageName, 0);
            details.put(getString(R.string.name), packManager.getApplicationLabel(app).toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                damn getCategoryTitle throws a null, got to check that if it's null, the category is UNDEFINED.
                String category = getString(R.string.undefined);
                if (ApplicationInfo.getCategoryTitle(getApplicationContext(), app.category) != null) {
                    category = ApplicationInfo.getCategoryTitle(getApplicationContext(), app.category).toString();
                }
                details.put(getString(R.string.category), category);
            }
        } catch (Exception e) {
            details.put(getString(R.string.name), String.format("%s_%s", getString(R.string.undefined), packageName)); // in this case we have only the package name. the app may have been recently uninstalled. add undefined in front as to differentiate it
            details.put(getString(R.string.category), getString(R.string.undefined));
        }

        return details;
    }

    private String getString(int id) {
        return getApplicationContext().getString(id);
    }


    @NonNull
    @Override
    @AddTrace(name = "usageStatsWorkerDoWork")
    public Result doWork() {
        DatabaseAPI databaseAPI = DatabaseAPI.getInstance();

        UsageStatsManager usm = (UsageStatsManager) getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);

        long startTime = getInputData().getLong(getString(R.string.session_start), -1L);
        long endTime = getInputData().getLong(getString(R.string.session_end), -1L);
        boolean anxious = getInputData().getBoolean(getString(R.string.anxious), false); // do not like giving a default, but have to

        Session currentSession = new Session();
        currentSession.setSessionStart(startTime);
        currentSession.setSessionEnd(endTime);
        currentSession.setAnxious(anxious);


        long sessionId = Objects.requireNonNull(databaseAPI).saveSession(currentSession, getApplicationContext());
        if (sessionId == -1L) {
            return Result.failure();
        }

        List<UsageStats> appList = Objects.requireNonNull(usm).queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, endTime);

        if (appList != null && appList.size() == 0) {
            return Result.failure();
        }

        List<App> sessionApps = new ArrayList<>();

        // sort by the order of last time used, so by time
        appList.sort(Comparator.comparingLong(UsageStats::getLastTimeUsed));

        for (UsageStats s : appList) {
            if (s.getTotalTimeInForeground() == 0L)
                continue; // skip apps that have not been used but still appear (for reasons only google knows about)

            Map<String, String> additionalDetails = getAdditionalAppDetails(s.getPackageName());

            App app = new App();

            app.setAppCategory(additionalDetails.get(getString(R.string.category)));
            app.setLastTimeUsed(s.getLastTimeUsed());
            app.setName(additionalDetails.get(getString(R.string.name)));
            app.setPackageName(s.getPackageName());
            app.setSessionIdFK(sessionId);
            app.setTotalTimeInForeground(s.getTotalTimeInForeground());

            sessionApps.add(app);
        }

        long count = databaseAPI.saveApps(sessionApps, getApplicationContext()).size();

        if (count != sessionApps.size()) {
            return Result.failure();
        }

        // get the default one because that's where the switch preferences save their values
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        boolean success = sharedPref.getBoolean(getApplicationContext().getString(R.string.success_opt_pref), false);

        if (success) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), getApplicationContext().getString(R.string.on_collection_id))
                    .setSmallIcon(R.drawable.ic_smile)
                    .setContentTitle(getString(R.string.collection_occurred))
                    .setAutoCancel(true)
                    .setGroup(getApplicationContext().getString(R.string.on_collection_group))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());

            builder.setContentText(getString(R.string.success));
            notificationManagerCompat.notify(nId.getAndIncrement(), builder.build());
        }

        return Result.success();
    }
}
