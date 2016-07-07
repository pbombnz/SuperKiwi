package nz.pbomb.xposed.anzmods.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import java.io.File;

import common.PACKAGES;
import nz.pbomb.xposed.anzmods.preferences.PREFERENCES;
import nz.pbomb.xposed.anzmods.R;

public class ContactActivity extends AppCompatActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        findViewById(R.id.contact_twitter_button).setOnClickListener(this);
        findViewById(R.id.contact_email_button).setOnClickListener(this);
        findViewById(R.id.contact_xda_button).setOnClickListener(this);

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
    public void onClick(View v) {
        Intent intent = null;
        switch(v.getId()) {
            case R.id.contact_twitter_button:
                try {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=528317895"));
                }catch (ActivityNotFoundException e) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/PBombNZ"));
                }
                break;
            case R.id.contact_email_button:
                intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:pbomb.nz@gmail.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "[XPOSED][SUPERKIWI] General Feedback");
                intent.putExtra(Intent.EXTRA_TEXT,
                        "Build Fingerprint: "+ Build.FINGERPRINT + "\n" +
                        "Build Manufacturer: "+ Build.MANUFACTURER + "\n" +
                        "Build Brand: "+ Build.BRAND + "\n" +
                        "Build Model: "+ Build.MODEL + "\n" +
                        "Build Product: "+ Build.PRODUCT + "\n" +
                        "\n" +
                        "Android OS Information: " + Build.VERSION.RELEASE + "\n" +
                        "\n" +
                        "Additional Text (Insert Feedback/Bug Report/Feature Request Here):\n");

                intent = Intent.createChooser(intent, "Chooser Email Client");
                break;
            case R.id.contact_xda_button:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/xposed/modules/xposed-anz-gomoney-zealand-mods-bypass-t3270623"));
                break;
            default:
                return;
        }
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        new File("/data/data/"+ PACKAGES.MODULE + "/shared_prefs/" + PREFERENCES.SHARED_PREFS_FILE_NAME + ".xml").setReadable(true, false);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        new File("/data/data/"+ PACKAGES.MODULE + "/shared_prefs/" + PREFERENCES.SHARED_PREFS_FILE_NAME + ".xml").setReadable(true, false);
        super.onPause();
    }
}
