package nz.pbomb.xposed.anzmods.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.io.File;

import common.PACKAGES;
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
        preferenceFragment = new PrefFragment();//(PrefFragment) getFragmentManager().findFragmentById(R.id.prefFragment);
        preferenceFragment.setArguments(getIntent().getExtras());

        // get an instance of FragmentTransaction from your Activity
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //add a fragment
        fragmentTransaction.add(R.id.myfragment, preferenceFragment);
        fragmentTransaction.commit();


        // Display the back button the action bar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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
        if(getIntent().getStringExtra("preference").equals(PREFERENCES.KEYS.MAIN.ANZ)) {
            if(preferenceFragment.hasValuesChanged()) {
                onFinishDialog_anzGoMoneyNZ();
            } else {
                finish();
            }
        } else {
            finish();
        }
    }


    public void onFinishDialog_anzGoMoneyNZ() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.ANZPrefActivity_quit_title));
        builder.setMessage(getResources().getString(R.string.ANZPrefActivity_quit_message));
        builder.setCancelable(false);
        builder.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                startActivity(intent);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @SuppressLint({"setWorldReadable", "SdCardPath"})
    @Override
    protected void onDestroy() {
        new File("/data/data/"+ PACKAGES.MODULE + "/shared_prefs/" + PREFERENCES.SHARED_PREFS_FILE_NAME + ".xml").setReadable(true,false);
        super.onDestroy();
    }

    @SuppressLint({"setWorldReadable", "SdCardPath"})
    @Override
    protected void onPause() {
        new File("/data/data/"+ PACKAGES.MODULE + "/shared_prefs/" + PREFERENCES.SHARED_PREFS_FILE_NAME + ".xml").setReadable(true, false);
        super.onPause();
    }
}
