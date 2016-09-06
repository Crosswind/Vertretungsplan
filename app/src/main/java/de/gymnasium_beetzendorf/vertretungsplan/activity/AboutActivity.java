package de.gymnasium_beetzendorf.vertretungsplan.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;

public class AboutActivity extends AppCompatActivity {

    TextView mVersionTextView;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mToolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mVersionTextView = (TextView) findViewById(R.id.versionTextView);

        PackageInfo packageInfo = null;

        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(Constants.TAG, "NameNotFoundException in AboutActivity", e);
        }

        if (packageInfo != null) {
            mVersionTextView.setText(String.format(getString(R.string.version_number), packageInfo.versionName, packageInfo.versionCode));
        } else {
            mVersionTextView.setText(R.string.version_number_not_found);
        }
    }
}
