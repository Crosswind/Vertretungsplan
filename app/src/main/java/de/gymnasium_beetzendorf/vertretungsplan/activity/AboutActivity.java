package de.gymnasium_beetzendorf.vertretungsplan.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;

public class AboutActivity extends BaseActivity implements Constants {

    TextView mVersionTextView;
    //Button mLinkButton;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected Toolbar getToolbar() {
        return findViewById(R.id.mainToolbar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVersionTextView = findViewById(R.id.versionTextView);

        PackageInfo packageInfo = null;

        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(TAG, "NameNotFoundException in AboutActivity", e);
        }

        if (packageInfo != null) {
            mVersionTextView.setText(String.format(getString(R.string.version_number), packageInfo.versionName, packageInfo.getLongVersionCode()));
        } else {
            mVersionTextView.setText(R.string.version_number_not_found);
        }

        Button changelogButton = findViewById(R.id.changelogButton);
        changelogButton.setOnClickListener(v -> showWhatsNewDialog());
    }
}
