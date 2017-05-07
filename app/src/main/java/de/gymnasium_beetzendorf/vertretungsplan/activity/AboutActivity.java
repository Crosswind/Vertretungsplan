package de.gymnasium_beetzendorf.vertretungsplan.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
        return (Toolbar) findViewById(R.id.mainToolbar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVersionTextView = (TextView) findViewById(R.id.versionTextView);
        //mLinkButton = (Button) findViewById(R.id.linkButton);

        PackageInfo packageInfo = null;

        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(TAG, "NameNotFoundException in AboutActivity", e);
        }

        if (packageInfo != null) {
            mVersionTextView.setText(String.format(getString(R.string.version_number), packageInfo.versionName, packageInfo.versionCode));
        } else {
            mVersionTextView.setText(R.string.version_number_not_found);
        }

        /*mLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://vplankl.gymnasium-beetzendorf.de/Vertretungsplan_Klassen.xml");

                CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();

                intentBuilder.setToolbarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));

                intentBuilder.setStartAnimations(getApplicationContext(), android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                intentBuilder.setExitAnimations(getApplicationContext(), android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                CustomTabsIntent customTabsIntent = intentBuilder.build();
                customTabsIntent.launchUrl(getApplicationContext(), uri);
            }
        });*/

        Button changelogButton = (Button) findViewById(R.id.changelogButton);
        changelogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWhatsNewDialog();
            }
        });
    }
}
