package com.anguel.dissertation.persistence.converters;

import com.anguel.dissertation.persistence.entity.SessionWithApps;
import com.anguel.dissertation.persistence.entity.app.App;
import com.anguel.dissertation.persistence.entity.session.Session;
import com.google.firebase.perf.metrics.AddTrace;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// convert the sessionwithapps object to and from plain text
public class SessionWithAppsConverter {

    @AddTrace(name = "converterSessionToString")
    public static String sessionToString(SessionWithApps sessionWithApps) throws Exception {
        Session session = sessionWithApps.session;
        List<App> apps = sessionWithApps.sessionApps;

        JSONObject currentSession = new JSONObject();
        currentSession.put("sessionStart", session.getSessionStart());
        currentSession.put("sessionEnd", session.getSessionEnd());
        currentSession.put("anxious", session.isAnxious());

        JSONArray appArray = new JSONArray();

        for (App app : apps) {
            JSONObject appObject = new JSONObject();
            appObject.put("appCategory", app.getAppCategory());
            appObject.put("name", app.getName());
            appObject.put("packageName", app.getPackageName());
            appObject.put("lastTimeUsed", app.getLastTimeUsed());
            appObject.put("totalTimeInForeground", app.getTotalTimeInForeground());
            appArray.put(appObject);
        }

        currentSession.put("apps", appArray);
        return currentSession.toString();
    }

    // returns it in a csv format
    @AddTrace(name = "converterterSessionToStringCsv")
    public static String sessionToStringCsv(SessionWithApps sessionWithApps) {
        StringBuilder result = new StringBuilder();
        Session session = sessionWithApps.getSession();
        List<App> apps = sessionWithApps.getSessionApps();

        result.append(String.format("%s,%s,%s\n", session.getSessionStart(), session.getSessionEnd(), session.anxious));
        apps.forEach(app -> result.append(String.format("%s,%s,%s,%s,%s\n", app.name, app.appCategory, app.lastTimeUsed, app.totalTimeInForeground, app.packageName)));

        result.trimToSize();
        return result.toString();
    }

    // important: will not save, will just return.
    // array[0] is the session, array[1] is the list of apps.
    // bad but ah well.
    @AddTrace(name = "converterStringToData")
    public static Object[] stringToData(String data) throws Exception {
        Object[] result = new Object[2];

        JSONObject jsonObject = new JSONObject(data);

        Session session = new Session();
        session.setSessionStart(jsonObject.getLong("sessionStart"));
        session.setSessionEnd(jsonObject.getLong("sessionEnd"));
        session.setAnxious(jsonObject.optBoolean("anxious")); // temporary set to optional
        result[0] = session;

        JSONArray apps = jsonObject.getJSONArray("apps");
        List<App> appList = new ArrayList<>(apps.length());

        for (int x = 0; x < apps.length(); x++) {
            JSONObject app = apps.getJSONObject(x);
            App currentApp = new App();
            currentApp.setAppCategory(app.getString("appCategory"));
            currentApp.setName(app.getString("name"));
            currentApp.setPackageName(app.getString("packageName"));
            currentApp.setLastTimeUsed(app.getLong("lastTimeUsed"));
            currentApp.setTotalTimeInForeground(app.getLong("totalTimeInForeground"));
            appList.add(currentApp);
        }

        result[1] = appList;
        return result;
    }
}
