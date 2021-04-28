package com.anguel.dissertation.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.anguel.dissertation.R;
import com.anguel.dissertation.persistence.DatabaseAPI;
import com.anguel.dissertation.persistence.converters.SessionWithAppsConverter;
import com.anguel.dissertation.persistence.entity.SessionWithApps;
import com.anguel.dissertation.serviceengine.ServiceEngine;
import com.anguel.dissertation.utils.Utils;
import com.anguel.dissertation.workers.Trainer;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.firebase.perf.metrics.AddTrace;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import io.sentry.Sentry;

import static android.content.Context.ACTIVITY_SERVICE;

public class SettingsFragment extends PreferenceFragmentCompat {
    private SwitchPreferenceCompat batteryOpt;
    private SwitchPreferenceCompat usageStatsPms;
    private Utils utils;
    private static final int CREATE_FILE = 1;

    @SuppressLint("QueryPermissionsNeeded")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        batteryOpt = findPreference(getString(R.string.battery_opt_pref));
        usageStatsPms = findPreference(getString(R.string.usage_stats_pref));
        Preference personSias = findPreference(getString(R.string.see_personal_sias_pref));
        Preference licenseInfo = findPreference(getString(R.string.license_pref));
        Preference id = findPreference(getString(R.string.see_personal_id_pref));
        Preference export = findPreference(getString(R.string.see_your_data_pref));
        Preference deleteSettings = findPreference(getString(R.string.delete_settings));
        Preference trainSettings = findPreference(getString(R.string.train_overall_model));
        utils = Utils.getInstance();

//        toggle their values based on the current setting. do it manually as to disable value being modified
        togglePreferenceValues();

        trainSettings.setOnPreferenceClickListener(preference -> {
            Data inputdata = new Data.Builder()
                    .putBoolean("overallModel", true)
                    .build();
            WorkRequest mlTrain = new OneTimeWorkRequest.Builder(Trainer.class)
                    .setInputData(inputdata)
                    .build();

            WorkManager.getInstance(requireContext())
                    .enqueue(mlTrain);

            return true;
        });

        batteryOpt.setOnPreferenceClickListener(preference -> {
            disableBatteryOptimisation();
            return true;
        });

        usageStatsPms.setOnPreferenceClickListener(preference -> {
            requestUsageStatsPermission();
            return true;
        });

        deleteSettings.setOnPreferenceClickListener(preference -> {
            try {
                AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());
                alert.setTitle(getString(R.string.data_del_confirm))
                        .setMessage(getString(R.string.data_del_msg))
                        .setCancelable(true);

                alert.setPositiveButton("Delete Data", ((dialog, which) -> {
                    SharedPreferences.Editor editor = requireActivity().getApplicationContext().getSharedPreferences(
                            getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();
                    editor.putBoolean(getString(R.string.shpref_prefix) + getString(R.string.pref_data_record), false);
                    editor.apply();
                    ServiceEngine.getInstance(requireActivity().getApplicationContext()).stopGossipService(requireActivity().getApplicationContext(), false);
                    ServiceEngine.getInstance(requireActivity().getApplicationContext()).stopEventMonitoringService(requireActivity().getApplicationContext());
                    requireActivity().getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit().clear().apply();
                    boolean succ = ((ActivityManager) requireActivity().getApplicationContext().getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
                    if (!succ) {
                        Toast.makeText(requireActivity().getApplicationContext(), getString(R.string.data_delete_succ_toast), Toast.LENGTH_LONG).show();
                    }
                }));

                alert.setNegativeButton("Cancel Deletion", (dialog, which) -> {

                });

                Dialog d = alert.create();
                d.show();
            } catch (Exception ignored) {
            }
            return true;
        });

        requireActivity().runOnUiThread(new Thread(() -> {
            // set sias score
            try {
                int sias = utils.getSias(requireActivity().getApplicationContext());
                Objects.requireNonNull(personSias).setSummary(sias != -1 ? String.valueOf(sias) : "You have not yet taken the test.");
            } catch (Exception e) {
                Sentry.captureException(e);
                Objects.requireNonNull(personSias).setSummary(getString(R.string.not_yet_taken_test));
            }

            // set identifier
            try {
                Objects.requireNonNull(id).setSummary("Your identifier is: " + utils.getUserID(requireActivity().getApplicationContext()) + ". Click to copy it.");
            } catch (Exception e) {
                Objects.requireNonNull(id).setSummary(R.string.pref_no_id);
                Sentry.captureException(e);
            }
        }));

        Objects.requireNonNull(id).setOnPreferenceClickListener(preference -> {
            try {
                ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData userId = ClipData.newPlainText(getString(R.string.cliptext_id), utils.getUserID(requireActivity().getApplicationContext()));
                Objects.requireNonNull(clipboard).setPrimaryClip(userId);

                CharSequence text = getString(R.string.id_copied);
                Toast toast = Toast.makeText(requireActivity().getApplicationContext(), text, Toast.LENGTH_LONG);
                toast.show();
            } catch (Exception e) {
                Sentry.captureException(e);
            }
            return true;
        });

        Objects.requireNonNull(licenseInfo).setOnPreferenceClickListener(preference -> {
            try {
                startActivity(new Intent(requireActivity().getApplicationContext(), OssLicensesMenuActivity.class));
            } catch (Exception e) {
                Sentry.captureException(e);
            }
            return true;
        });

        Objects.requireNonNull(export).setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_TITLE, String.format("data_%s.csv", Utils.getInstance().getHumanReadableDate()));
            startActivityForResult(intent, CREATE_FILE);
            return true;
        });
    }

    @AddTrace(name = "writeDataToFile")
    private void writeDataToFile(Uri uri) {
        try (ParcelFileDescriptor pfd = getActivity().getApplicationContext().getContentResolver().openFileDescriptor(uri, "w");
             FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor())) {
            fileOutputStream.write(
                    ("# The csv file follows the format:\n" +
                            "# session start, session end, session anxious or not\n" +
                            "# list of app name, app category, app last time used, app total time in foreground, app package name\n\n\n\n").getBytes(StandardCharsets.UTF_8));
            List<SessionWithApps> sessionWithAppsList = DatabaseAPI.getInstance().getAllSessions(getActivity().getApplicationContext());
            for (SessionWithApps sessionWithApps : sessionWithAppsList) {
                fileOutputStream.write(SessionWithAppsConverter.sessionToStringCsv(sessionWithApps).getBytes(StandardCharsets.UTF_8));
                fileOutputStream.write("\n".getBytes(StandardCharsets.UTF_8));
            }
            fileOutputStream.flush();
            getActivity().runOnUiThread(() -> Toast.makeText(getActivity().getApplicationContext(), getString(R.string.data_exported), Toast.LENGTH_LONG).show());
        } catch (Exception e) {
            Sentry.captureException(e);
            getActivity().runOnUiThread(() -> Toast.makeText(getActivity().getApplicationContext(), getString(R.string.data_export_error), Toast.LENGTH_LONG).show());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == CREATE_FILE
                && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                final Uri uri = resultData.getData();
                new Thread(() -> writeDataToFile(uri)).start();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        to make sure it cannot be toggled right after
        togglePreferenceValues();
    }

    private void togglePreferenceValues() {
        try {
            if (utils.hasUsageStatsPermission(requireActivity().getApplicationContext())) {
                Objects.requireNonNull(usageStatsPms).setChecked(true);
                Objects.requireNonNull(usageStatsPms).setEnabled(false);
            } else {
                Objects.requireNonNull(usageStatsPms).setChecked(false);
            }

            if (utils.isBatteryOptDisabled(requireActivity().getApplicationContext())) {
                Objects.requireNonNull(batteryOpt).setChecked(true);
                Objects.requireNonNull(batteryOpt).setEnabled(false);
            } else {
                Objects.requireNonNull(batteryOpt).setChecked(false);
            }
        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }

    private void requestUsageStatsPermission() {
        try {
            if (!utils.hasUsageStatsPermission(requireActivity().getApplicationContext())) {
                startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), getActivity().getResources().getInteger(R.integer.request_usage_stats_code));
            }
        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }

    @SuppressLint("BatteryLife")
    private void disableBatteryOptimisation() {
        startActivityForResult(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS), requireActivity().getResources().getInteger(R.integer.request_battery_optimisation_code));
    }

}
