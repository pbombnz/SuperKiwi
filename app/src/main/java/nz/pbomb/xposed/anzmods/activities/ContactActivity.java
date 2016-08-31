package nz.pbomb.xposed.anzmods.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import butterknife.ButterKnife;
import butterknife.OnClick;
import nz.pbomb.xposed.anzmods.R;

public class ContactActivity extends AppCompatActivity {
    @Override
    @SuppressWarnings("all")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.nothing, R.anim.fade_out);
    }

    @OnClick(R.id.contact_twitter_button)
    public void onTwitterButtonClicked() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=528317895")));
        }catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/PBombNZ")));
        }
    }

    @OnClick(R.id.contact_email_button)
    public void onEmailButtonClicked() {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:pbomb.nz@gmail.com"));
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
        startActivity(intent);
    }

    @OnClick(R.id.contact_xda_button)
    public void onXDAButtonClicked() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/xposed/modules/xposed-anz-gomoney-zealand-mods-bypass-t3270623")));
    }
}
