package nz.pbomb.xposed.anzmods.activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import nz.pbomb.xposed.anzmods.preferences.PREFERENCES;
import nz.pbomb.xposed.anzmods.fragments.PrefFragment;
import nz.pbomb.xposed.anzmods.R;


/**
 * A UI for the xposed module that allows the users to toggle preferences and features
 *
 * @author Prashant Bhikhu (PBombNZ)
 */
public class PrefActivity extends AppCompatActivity {
    PrefFragment preferenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initial Creation
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pref);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // We only proceed if we came from the Preference intents created in the MainActivity
        Intent i;
        if(getIntent() != null) {
            i = getIntent();
            if (!i.hasExtra("id") && !i.hasExtra("title") && !i.hasExtra("preference")) {
                throw new IllegalArgumentException("Wrong Intent Parameters");
            }
        } else {
            throw new IllegalArgumentException("Needs Intent to use Preference Activity");
        }

        // Set the Preference Acitvity's Title and Preferences based on intent
        setTitle(getIntent().getStringExtra("title"));
        preferenceFragment = new PrefFragment();
        preferenceFragment.setArguments(getIntent().getExtras());

        // get an instance of FragmentTransaction from your Activity
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //add a fragment
        fragmentTransaction.add(R.id.activity_pref_linearlayout_nested, preferenceFragment);
        fragmentTransaction.commit();
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
    public void onBackPressed() {
        if (getIntent().getStringExtra("preference").equals(PREFERENCES.KEYS.MAIN.ANZ)) {
            if (preferenceFragment.hasValuesChanged()) {
                onFinishDialog_anzGoMoneyNZ();
                return;
            }
        }
        super.onBackPressed();
    }


    public void onFinishDialog_anzGoMoneyNZ() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setTitle(getResources().getString(R.string.ANZPrefActivity_quit_title))
            .setMessage(getString(R.string.ANZPrefActivity_quit_message))
            .setCancelable(false)
            .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                startActivity(intent);*/
                NavUtils.navigateUpFromSameTask(PrefActivity.this);
            }
        });
        builder.show();
    }
}
