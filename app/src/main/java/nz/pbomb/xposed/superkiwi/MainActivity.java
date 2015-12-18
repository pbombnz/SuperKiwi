package nz.pbomb.xposed.superkiwi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;
        switch (id) {
            case R.id.action_donate:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=QNQDESEMGWDPY"));
                break;
            case R.id.action_contact:
                intent = new Intent(getApplicationContext(), ContactActivity.class);
                break;
            case R.id.action_sourceCode:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/pbombnz/ANZGoMoneyNZMods/"));
                break;
            case R.id.action_xda:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/xposed/modules/xposed-anz-gomoney-zealand-mods-bypass-t3270623"));
                break;
        }
        startActivity(intent);
        return true;
    }

}
