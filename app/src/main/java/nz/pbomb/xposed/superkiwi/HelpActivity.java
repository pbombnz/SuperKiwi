package nz.pbomb.xposed.superkiwi;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        LinearLayout layout = (LinearLayout) this.findViewById(R.id.linearLayout);

        String[] questions = getResources().getStringArray(R.array.helpActivity_questions);
        String[] answers = getResources().getStringArray(R.array.helpActivity_answers);

        for(int i = 0; i< questions.length; i++) {
            LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lparams.gravity = Gravity.CENTER_HORIZONTAL;
            lparams.setMargins(10,10,10,10);


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
    }


}
