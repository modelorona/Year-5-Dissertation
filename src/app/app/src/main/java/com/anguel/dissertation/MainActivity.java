package com.anguel.dissertation;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.anguel.dissertation.serviceengine.ServiceEngine;
import com.anguel.dissertation.settings.SettingsActivity;
import com.anguel.dissertation.utils.Utils;
import com.anguel.dissertation.workers.Trainer;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setUpNotificationChannels();
        }

        Utils utils = Utils.getInstance();
        ToggleButton startDataColButton = findViewById(R.id.startDataCollectionButton);

        startDataColButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!utils.areAllPermissionsEnabled(getApplicationContext())) {
                startDataColButton.setChecked(false);
                showPermissionsMissingDialog();
            } else if (Utils.getInstance().getSias(getApplicationContext()) == -1L) {
                startDataColButton.setChecked(false);
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("")
                        .setMessage("You have not yet taken the SIAS quiz. Please do so before turning on data collection.");

                alert.setPositiveButton("Take quiz", ((dialog, which) -> {
                    Intent quizIntent = new Intent(getApplicationContext(), QuizActivity.class);
                    startActivity(quizIntent);
                }));


                Dialog d = alert.create();
                d.show();
            } else {
                SharedPreferences.Editor editor = this.getSharedPreferences(
                        getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();
                editor.putBoolean(getString(R.string.shpref_prefix) + getString(R.string.pref_data_record), isChecked);
                editor.apply();
                toggleDataCollection(Utils.getInstance().isRecordingData(getApplicationContext()));
            }
        });

        //      start the quiz
        findViewById(R.id.start_test).setOnClickListener(v -> {
//            see if permissions are enabled or not. prevent taking the test unless they are on
            if (!utils.areAllPermissionsEnabled(getApplicationContext())) {
                showPermissionsMissingDialog();
            } else {
                Intent startTestIntent = new Intent(this, QuizActivity.class);
                startActivity(startTestIntent);
            }
        });

        findViewById(R.id.trainModelButton).setOnClickListener(v -> {
            Data inputdata = new Data.Builder()
                    .putBoolean("overallModel", true)
                    .build();
            WorkRequest mlTrain = new OneTimeWorkRequest.Builder(Trainer.class)
                    .setInputData(inputdata)
                    .build();

            WorkManager.getInstance(getApplicationContext())
                    .enqueue(mlTrain);
        });

        toggleDataCollection(Utils.getInstance().isRecordingData(getApplicationContext()));
    }

    private void toggleDataCollection(boolean recordingData) {
        ToggleButton button = findViewById(R.id.startDataCollectionButton);
        button.setChecked(recordingData);
        ServiceEngine engine = ServiceEngine.getInstance(getApplicationContext());
        if (recordingData) {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build();

            PeriodicWorkRequest mlTrain = new PeriodicWorkRequest.Builder(Trainer.class,
                    1L, TimeUnit.DAYS)
                    .setInitialDelay(1L, TimeUnit.DAYS) // delay by a day to allow for data gathering
                    .setConstraints(constraints)
                    .build();

            WorkManager.getInstance(getApplicationContext())
                    .enqueueUniquePeriodicWork("modelTraining",
                            ExistingPeriodicWorkPolicy.KEEP,
                            mlTrain);

            engine.startEventMonitoringService(getApplicationContext());
        } else {
            WorkManager.getInstance(getApplicationContext()).cancelUniqueWork("modelTraining");
            engine.stopEventMonitoringService(getApplicationContext());
        }
    }

    private void showPermissionsMissingDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle(getString(R.string.permissions))
                .setMessage(getString(R.string.enable_permissions));

        alert.setPositiveButton(getString(R.string.go_to_settings), ((dialog, which) -> {
            Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(settingsIntent);
        }));


        Dialog d = alert.create();
        d.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private NotificationChannel createChannel(CharSequence name, String description, int importance, String channelId) {
        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        channel.setDescription(description);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setUpNotificationChannels() {
        NotificationChannel persistentCollectionChannel = createChannel(
                getString(R.string.persistence_notif_name), getString(R.string.persistence_notif_desc),
                NotificationManager.IMPORTANCE_LOW, getString(R.string.persistence_notif_id)
        );

        NotificationChannel gossipNotificationChannel = createChannel(
                getString(R.string.gossip_notif_name), getString(R.string.gossip_notif_desc),
                NotificationManager.IMPORTANCE_LOW, getString(R.string.gossip_notif_id)
        );

        NotificationChannel onBootCollectionChannel = createChannel(
                getString(R.string.collection_on_boot_name), getString(R.string.collection_on_boot_desc),
                NotificationManager.IMPORTANCE_DEFAULT, getString(R.string.collection_on_boot_id)
        );

        NotificationChannel onCollectionChannel = createChannel(
                getString(R.string.on_collection_name), getString(R.string.on_collection_desc),
                NotificationManager.IMPORTANCE_LOW, getString(R.string.on_collection_id)
        );

        NotificationChannel mlChannel = createChannel(
                getString(R.string.ml_channel_name), getString(R.string.ml_channel_desc),
                NotificationManager.IMPORTANCE_LOW, getString(R.string.ml_channel_id)
        );

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
        Objects.requireNonNull(notificationManager).createNotificationChannel(persistentCollectionChannel);
        Objects.requireNonNull(notificationManager).createNotificationChannel(onBootCollectionChannel);
        Objects.requireNonNull(notificationManager).createNotificationChannel(onCollectionChannel);
        Objects.requireNonNull(notificationManager).createNotificationChannel(gossipNotificationChannel);
        Objects.requireNonNull(notificationManager).createNotificationChannel(mlChannel);
    }
}
