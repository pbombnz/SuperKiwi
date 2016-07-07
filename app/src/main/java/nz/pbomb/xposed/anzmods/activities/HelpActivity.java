package nz.pbomb.xposed.anzmods.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import common.PACKAGES;
import common.PREFERENCES;
import nz.pbomb.xposed.anzmods.R;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        LinearLayout layout = (LinearLayout) this.findViewById(R.id.linearLayout);

        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lparams.gravity = Gravity.CENTER_HORIZONTAL;
        lparams.setMargins(10, 10, 10, 10);


        TextView textView = new TextView(this);
        textView.setText("General");
        textView.setLayoutParams(lparams);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        textView.setGravity(Gravity.START);
        layout.addView(textView);

        String[] questions = getResources().getStringArray(R.array.helpQuestions_General);
        String[] answers = getResources().getStringArray(R.array.helpAnswers_General);

        for(int i = 0; i< questions.length; i++) {

            TextView tvQuestion = new TextView(this);
            TextView tvAnswer = new TextView(this);

            tvQuestion.setLayoutParams(lparams);
            tvAnswer.setLayoutParams(lparams);

            tvQuestion.setText(questions[i]);
            tvAnswer.setText(answers[i]);

            tvQuestion.setTypeface(tvQuestion.getTypeface(), Typeface.BOLD_ITALIC);
            tvQuestion.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));

            tvQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            tvAnswer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            tvQuestion.setGravity(Gravity.CENTER);
            tvAnswer.setGravity(Gravity.START);

            layout.addView(tvQuestion);
            layout.addView(tvAnswer);
        }

        textView = new TextView(this);
        textView.setText("ANZ Go Money NZ");
        textView.setLayoutParams(lparams);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        textView.setGravity(Gravity.START);
        layout.addView(textView);

        questions = getResources().getStringArray(R.array.helpQuestions_ANZ);
        answers = getResources().getStringArray(R.array.helpAnswers_ANZ);

        for(int i = 0; i< questions.length; i++) {

            TextView tvQuestion = new TextView(this);
            TextView tvAnswer = new TextView(this);

            tvQuestion.setLayoutParams(lparams);
            tvAnswer.setLayoutParams(lparams);

            tvQuestion.setText(questions[i]);
            tvAnswer.setText(answers[i]);

            tvQuestion.setTypeface(tvQuestion.getTypeface(), Typeface.BOLD_ITALIC);
            tvQuestion.setTextColor(getResources().getColor(android.R.color.darker_gray));

            tvQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            tvAnswer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            tvQuestion.setGravity(Gravity.CENTER);
            tvAnswer.setGravity(Gravity.START);

            layout.addView(tvQuestion);
            layout.addView(tvAnswer);
        }

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        new File("/data/data/"+ PACKAGES.MODULE + "/shared_prefs/" + PREFERENCES.SHARED_PREFS_FILE_NAME + ".xml").setReadable(true,false);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        new File("/data/data/"+ PACKAGES.MODULE + "/shared_prefs/" + PREFERENCES.SHARED_PREFS_FILE_NAME + ".xml").setReadable(true, false);
        super.onPause();
    }
}
