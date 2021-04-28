package com.anguel.dissertation;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.anguel.dissertation.utils.Utils;

import java.util.Objects;
import java.util.concurrent.Executor;

import io.sentry.Sentry;

public class QuizActivity extends AppCompatActivity implements View.OnClickListener {


    private int total = 0;
    private int totalQuestionsSoFar = 0;
    private TextView question;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.title);


//        get all buttons
        Button ans1 = findViewById(R.id.ans1);
        Button ans2 = findViewById(R.id.ans2);
        Button ans3 = findViewById(R.id.ans3);
        Button ans4 = findViewById(R.id.ans4);
        Button ans5 = findViewById(R.id.ans5);

//        attach handler to all buttons
        ans1.setOnClickListener(this);
        ans2.setOnClickListener(this);
        ans3.setOnClickListener(this);
        ans4.setOnClickListener(this);
        ans5.setOnClickListener(this);

//        set initial question text
        question = findViewById(R.id.question);
        question.setText(R.string.q1);

    }

    @Override
    public void onClick(View v) {
        int value;
        int vId = v.getId();
        if (vId == R.id.ans2) {
            value = 1;
        } else if (vId == R.id.ans3) {
            value = 2;
        } else if (vId == R.id.ans4) {
            value = 3;
        } else if (vId == R.id.ans5) {
            value = 4;
        } else {
            value = 0;
        }
        if (totalQuestionsSoFar == 4 || totalQuestionsSoFar == 8 || totalQuestionsSoFar == 10) {
            value = 4 - value;
        }
        total += value;
        int amountOfQuestions = 20;
        if (totalQuestionsSoFar++ < amountOfQuestions - 1) {
            updateQuestion(getBaseContext(), totalQuestionsSoFar);
        } else {
            finishQuiz();
        }
    }

    private void updateQuestion(Context context, int index) {
        String resource = String.format("q%s", (index + 1));
        int requestedId = context.getResources().getIdentifier(resource, "string", context.getPackageName());
        question.setText(getString(requestedId));
    }

    private void finishQuiz() {
        try {
            Executor executor = ContextCompat.getMainExecutor(getApplicationContext());
            executor.execute(() -> Utils.getInstance().saveSias(getApplicationContext(), total));

            //        tell the user what has happened. give them chance to read more about the score
            AlertDialog.Builder builder = new AlertDialog.Builder(QuizActivity.this);
            builder.setMessage(String.format("%s %s. %s", getString(R.string.your_score), total, getString(R.string.know_more)))
                    .setTitle(getString(R.string.thanks))
                    .setCancelable(false);

            builder.setPositiveButton(getString(R.string.done), (dialog, which) -> finish());

            Dialog d = builder.create();
            d.setCanceledOnTouchOutside(false);
            d.show();

        } catch (Exception e) {
            Sentry.captureException(e);
            AlertDialog.Builder builder = new AlertDialog.Builder(QuizActivity.this);
            builder.setTitle(getString(R.string.test_save_fail))
                    .setMessage(getString(R.string.save_fail_message))
                    .setCancelable(false);

            builder.setPositiveButton(getString(R.string.try_again), (dialog, which) -> {
                finishQuiz(); // cheeky
            });

            builder.setNeutralButton(getString(R.string.cancel), (dialog, which) -> finish());

            Dialog d = builder.create();
            d.setCanceledOnTouchOutside(false);
            d.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
