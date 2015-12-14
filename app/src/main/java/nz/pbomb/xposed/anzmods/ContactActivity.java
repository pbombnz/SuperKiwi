package nz.pbomb.xposed.anzmods;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;

public class ContactActivity extends AppCompatActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        findViewById(R.id.contact_twitter_button).setOnClickListener(this);
        findViewById(R.id.contact_email_button).setOnClickListener(this);
        findViewById(R.id.contact_xda_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.contact_twitter_button:
                Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/PBombNZ"));
                startActivity(twitterIntent);
                break;
            case R.id.contact_email_button:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:pbomb.nz@gmail.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[XPOSED][ANZMODS] Feedback");
                emailIntent.putExtra(Intent.EXTRA_TEXT,
                        "Build Fingerprint: "+ Build.FINGERPRINT + "\n" +
                        "Build Manufacturer: "+ Build.MANUFACTURER + "\n" +
                        "Build Brand: "+ Build.BRAND + "\n" +
                        "Build Model: "+ Build.MODEL + "\n" +
                        "Build Product: "+ Build.PRODUCT + "\n" +
                        "\n" +
                        "Android OS Information: " + Build.VERSION.RELEASE + "\n" +
                        "\n" +
                        "Addtional Text (Insert Feedback Here):\n");
                startActivity(Intent.createChooser(emailIntent, "Chooser Email Client"));
                break;
            case R.id.contact_xda_button:
                Intent xdaIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/xposed/modules/xposed-anz-gomoney-zealand-mods-bypass-t3270623"));
                startActivity(xdaIntent);
                break;
            default:
                break;
        }
    }
}
