package de.gymnasium_beetzendorf.vertretungsplan;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    TextView mVersionTextView;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mVersionTextView = (TextView) findViewById(R.id.versionTextView);

        PackageInfo packageInfo = null;

        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(MainActivity.TAG, "NameNotFoundException in AboutActivity", e);
        }

        if (packageInfo != null) {
            mVersionTextView.setText("Version: " + packageInfo.versionName + " (" + packageInfo.versionCode + ")");
        } else {
            mVersionTextView.setText("Version number unavailable.");
        }
    }
}
