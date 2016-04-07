package nz.pbomb.xposed.anzmods;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HelpASyncTask extends AsyncTask<String, Void, String> {
    private static final String ERROR_MSG = "Cannot show Help and FAQ\'s at the moment. Try again later.";

    private final Activity activity;
    private final Context context;

    public HelpASyncTask(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            URL url = new URL(params[0]);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            if(httpCon.getResponseCode() != 200) {
                throw new IOException();
            }
            InputStream in = httpCon.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }

            reader.close();
            in.close();
            httpCon.disconnect();

            return out.toString();
        } catch (IOException e) {
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        if(s == null) {
            displayErrorDialog();
            return;
        }
    }

    public void displayErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(ERROR_MSG);
        builder.setCancelable(true);
        builder.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                activity.finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
