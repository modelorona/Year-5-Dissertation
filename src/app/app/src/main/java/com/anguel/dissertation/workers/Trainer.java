package com.anguel.dissertation.workers;


import android.app.Notification;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.anguel.dissertation.R;
import com.anguel.dissertation.ml.ModelStats;
import com.anguel.dissertation.ml.ModelType;
import com.anguel.dissertation.ml.smile.classification.RandomForest;
import com.anguel.dissertation.ml.smile.data.DataFrame;
import com.anguel.dissertation.ml.smile.data.formula.Formula;
import com.anguel.dissertation.ml.smile.io.Read;
import com.anguel.dissertation.ml.smile.validation.ClassificationMetrics;
import com.anguel.dissertation.ml.smile.validation.metric.Accuracy;
import com.anguel.dissertation.ml.smile.validation.metric.ConfusionMatrix;
import com.anguel.dissertation.ml.smile.validation.metric.FDR;
import com.anguel.dissertation.ml.smile.validation.metric.FScore;
import com.anguel.dissertation.ml.smile.validation.metric.Fallout;
import com.anguel.dissertation.ml.smile.validation.metric.Precision;
import com.anguel.dissertation.ml.smile.validation.metric.Sensitivity;
import com.anguel.dissertation.ml.smile.validation.metric.Specificity;
import com.anguel.dissertation.persistence.DatabaseAPI;
import com.anguel.dissertation.persistence.entity.SessionWithApps;
import com.anguel.dissertation.persistence.entity.app.App;
import com.anguel.dissertation.utils.Utils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.perf.metrics.AddTrace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.sentry.Sentry;


public class Trainer extends Worker {
    private static final String currentModelPath = "current_model.dat";
    private static final String dataPath = "data.arff";
    private FirebaseFunctions functions;

    public Trainer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private static void onComplete(Task<String> task) {
        if (!task.isSuccessful()) {
            if (task.getException() != null) {
                Sentry.captureException(task.getException());
            }
        }
    }

    @NonNull
    @Override
    @AddTrace(name = "Trainer")
    public Result doWork() {
        try {
            functions = FirebaseFunctions.getInstance("europe-west3");
            boolean overallModel = getInputData().getBoolean("overallModel", false);
            setForegroundAsync(createForegroundInfo(overallModel));
            long currentTime = Utils.getInstance().getTime();

            boolean dataSetUp = setUpData(overallModel, currentTime);
            if (!dataSetUp) {
                throw new Exception("data was not set up");
            }

            if (overallModel) {
                RandomForest overallForest = trainModel();
                sendModelStats(overallForest, ModelType.OVERALL, currentTime)
                        .addOnCompleteListener(Trainer::onComplete);
            } else {
                RandomForest current = trainModel();
                sendModelStats(current, ModelType.DAILY, currentTime)
                        .addOnCompleteListener(Trainer::onComplete);

                RandomForest merged = mergeModels(current, getCurrentSavedModel(), currentTime);
                sendModelStats(merged, ModelType.COMBINED, currentTime)
                        .addOnCompleteListener(Trainer::onComplete);

                if (merged != null) {
                    saveNewModel(merged);
                }
            }
            return closeWorker();
        } catch (Exception e) {
            Sentry.captureException(e);
            return closeWorker();
        }
    }

    private Result closeWorker() {
        removeData();
        return Result.success();
    }

    private String getARFFHeader() {
        StringBuilder result = new StringBuilder();
        result.append("@RELATION sessionWithApps\n\n");
        String[] attributes = {"sessionLength", "totalTimeInForeground", "anxious"};
        for (String attr : Arrays.copyOfRange(attributes, 0, 2)) {
            result.append("@ATTRIBUTE ".concat(attr).concat(" NUMERIC\n"));
        }
        result.append("@ATTRIBUTE anxious {false,true}\n");
        result.append("\n@DATA\n");
        result.trimToSize();
        return result.toString();
    }

    private String convertSessionsToString(SessionWithApps sessionWithApps) {
        StringBuilder result = new StringBuilder();
        boolean anxious = sessionWithApps.getSession().isAnxious();
        long sessionLength = Math.abs(sessionWithApps.getSession().getSessionEnd() - sessionWithApps.getSession().getSessionStart());
        for (App app : sessionWithApps.getSessionApps()) {
            result.append(String.format("%s,%s,%s\n", sessionLength, Math.abs(app.getTotalTimeInForeground()), anxious));
        }
        result.trimToSize();
        return result.toString();
    }

    // extracts the data and saves it in a temporary file
    @AddTrace(name = "setUpData")
    private boolean setUpData(boolean getAllData, long currentTime) {
        boolean success = true;

        try (FileOutputStream fos = getApplicationContext().openFileOutput(dataPath, Context.MODE_PRIVATE)) {
            fos.write(getARFFHeader().getBytes(StandardCharsets.UTF_8));
            List<SessionWithApps> data;
            long prevTime;
            if (getAllData) {
                prevTime = Utils.getInstance().getDataCutOffTime();
            } else {
                prevTime = Utils.getInstance().getPreviousTime(currentTime, 24L);
            }
            data = DatabaseAPI.getInstance().getSessionsInTimePeriod(prevTime, currentTime, getApplicationContext());
            for (SessionWithApps sessionWithApps : data) {
                fos.write(convertSessionsToString(sessionWithApps).getBytes(StandardCharsets.UTF_8));
            }
            fos.flush();
        } catch (Exception e) {
            Sentry.captureException(e);
            success = false;
        }

        return success;
    }

    // makes sure to delete the file when done
    private boolean removeData() {
        return new File(getApplicationContext().getFilesDir(), dataPath).delete();
    }

    // train a new model on the day's current data
    @AddTrace(name = "trainModel")
    private RandomForest trainModel() throws Exception {
        DataFrame originalDf = Read.arff(dataPath, getApplicationContext());
        int split = (int) Math.floor(originalDf.size() * 0.7); // 70% split
        DataFrame train = originalDf.slice(0, split);
        DataFrame test = originalDf.slice(split, originalDf.size());

        if (train.size() == 0 || test.size() == 0) {
            throw new Exception("Train or test is size 0.");
        }

        Properties props = new Properties();
        // this is a quick fix for when the max nodes becomes 1 when there is not enough data
        props.setProperty("smile.random.forest.max.nodes",
                train.size() >= 10 ? String.valueOf(train.size() / 5) : "2");

        Formula anxious = Formula.lhs("anxious");

        RandomForest forest = RandomForest.fit(
                anxious,
                train,
                props
        );

        int[] predictions = forest.predict(test);
        int[] anxiousCol = test.column("anxious").toIntArray();

        ModelStats modelStats = ModelStats.builder()
                .accuracy(Accuracy.of(anxiousCol, predictions))
                .confusionMatrix(ConfusionMatrix.of(anxiousCol, predictions))
                .f1score(FScore.F1.score(anxiousCol, predictions))
                .falseDiscoveryRate(FDR.of(anxiousCol, predictions))
                .falsePositiveRate(Fallout.of(anxiousCol, predictions))
                .precision(Precision.of(anxiousCol, predictions))
                .sensitivity(Sensitivity.of(anxiousCol, predictions))
                .specificity(Specificity.of(anxiousCol, predictions))
                .trainSize(train.size())
                .testSize(test.size())
                .anxiousCountTrain(Arrays.stream(train.column("anxious").toStringArray()).filter(s -> s.equals("true")).count())
                .anxiousCountTest(Arrays.stream(test.column("anxious").toStringArray()).filter(s -> s.equals("true")).count())
                .build();

        forest.setModelStats(modelStats);

        return forest;
    }

    // merge both models, and return the new one
    @AddTrace(name = "mergeModels")
    private RandomForest mergeModels(RandomForest m1, RandomForest m2, long currentTime) {
        Utils.getInstance().setLastTimeModelMerged(getApplicationContext(), currentTime);
        if (m1 == null) {
            return m2;
        } else if (m2 == null) {
            return m1;
        }
        return m1.merge(m2);
    }

    // get the current forest model from the device
    private RandomForest getCurrentSavedModel() {
        RandomForest model = null;
        long lastTimeMerged = Utils.getInstance().getLastTimeModelMerged(getApplicationContext());
        if (lastTimeMerged < Utils.getInstance().getDataCutOffTime()) {
            // ignore the merged model, delete it actually
            new File(getApplicationContext().getFilesDir(), currentModelPath).delete();
            return model;
        }
        try (FileInputStream fis = getApplicationContext().openFileInput(currentModelPath);
             ObjectInputStream objectInputStream = new ObjectInputStream(fis)) {
            model = (RandomForest) objectInputStream.readObject();
        } catch (Exception e) {
            Sentry.captureException(e);
        }

        return model;
    }

    // save the new merged model to the device
    private boolean saveNewModel(RandomForest model) {
        boolean success = true;
        try (FileOutputStream fileOutputStream = getApplicationContext().openFileOutput(currentModelPath, Context.MODE_PRIVATE);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(model);
            objectOutputStream.flush();
        } catch (Exception e) {
            success = false;
            Sentry.captureException(e);
        }

        return success;
    }

    // gets all the classification metrics from the model, and sends them to fb. can be another thread
    private Task<String> sendModelStats(RandomForest model, ModelType type, long currentTime) {
        ModelStats modelStats = model.getModelStats();
        Map<String, Object> data = new HashMap<>(13);
        data.put("id", Utils.getInstance().getUserID(getApplicationContext()));
        data.put("timestamp", currentTime);
        data.put("sensitivity", modelStats.getSensitivity());
        data.put("specificity", modelStats.getSpecificity());
        data.put("precision", modelStats.getPrecision());
        data.put("falseDiscoveryRate", modelStats.getFalseDiscoveryRate());
        data.put("falsePositiveRate", modelStats.getFalsePositiveRate());
        data.put("f1score", modelStats.getF1score());
        data.put("confusionMatrix", modelStats.getConfusionMatrix().toString());
        data.put("modelType", type.toString());
        data.put("testSize", modelStats.getTestSize());
        data.put("trainSize", modelStats.getTrainSize());
        data.put("anxiousCountTrain", modelStats.getAnxiousCountTrain());
        data.put("anxiousCountTest", modelStats.getAnxiousCountTest());
        ClassificationMetrics oob = model.metrics().getSafeSelf();
        data.put("oobFitTime", oob.fitTime);
        data.put("oobScoreTime", oob.scoreTime);
        data.put("oobError", oob.error);
        data.put("oobAccuracy", oob.accuracy);

        return functions.getHttpsCallable("uploadModelStats")
                .call(data)
                .continueWith(task -> (String) task.getResult().getData());

    }

    @NonNull
    private ForegroundInfo createForegroundInfo(boolean overallModel) {
        Context context = getApplicationContext();
        String id = context.getString(R.string.ml_channel_id);
        String title = overallModel ? "Training overall model" : context.getString(R.string.ml_notif_title);

        Notification notification = new NotificationCompat.Builder(context, id)
                .setContentTitle(title)
                .setTicker(title)
                .setSmallIcon(R.drawable.ic_clone_trooper)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .build();

        return new ForegroundInfo(R.integer.ml_channel_notif_id, notification);
    }
}
