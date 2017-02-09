package de.gymnasium_beetzendorf.vertretungsplan.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import de.gymnasium_beetzendorf.vertretungsplan.BootReceiver;
import de.gymnasium_beetzendorf.vertretungsplan.DatabaseHandler;
import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;

public abstract class BaseActivity extends AppCompatActivity implements Constants {

    private final String TAG = BaseActivity.class.getSimpleName();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (WelcomeActivity.shouldDisplay(this)) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        PackageManager packageManager = this.getPackageManager();
        ComponentName receiver = new ComponentName(this, BootReceiver.class);
        if (packageManager.getComponentEnabledSetting(receiver) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            packageManager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }

        if (getLayoutId() != 0) {
            setContentView(getLayoutId());
        }

        Toolbar toolbar;
        if ((toolbar = getToolbar()) != null) {
            setSupportActionBar(toolbar);
        }

        ActionBar actionBar;
        if ((actionBar = getSupportActionBar()) != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    protected DatabaseHandler getDatabaseHandler() {
        return new DatabaseHandler(getApplicationContext(), DatabaseHandler.DATABASE_NAME, null, DatabaseHandler.DATABASE_VERSION);
    }

    protected SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    protected void makeSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show();
    }

    protected boolean hasInternetAccess() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    protected abstract int getLayoutId();

    protected abstract Toolbar getToolbar();


}
