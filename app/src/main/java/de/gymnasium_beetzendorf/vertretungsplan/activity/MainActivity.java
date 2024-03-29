package de.gymnasium_beetzendorf.vertretungsplan.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.BootReceiver;
import de.gymnasium_beetzendorf.vertretungsplan.DatabaseHandler;
import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.RefreshService;
import de.gymnasium_beetzendorf.vertretungsplan.adapter.PagerAdapter;
import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;
import de.gymnasium_beetzendorf.vertretungsplan.data.SubstitutionDay;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.BaseTabFragment;

public class MainActivity extends BaseActivity
        implements Constants, BaseTabFragment.OnSwipeRefreshListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private SharedPreferences mSharedPreferences;
    private ViewPager mMainViewPager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TabLayout mMainTabLayout;
    private DatabaseHandler mDatabaseHandler;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean new_update = intent.getBooleanExtra("new_update", false);
            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
            if (new_update) {
                displayData();
                makeSnackbar("Aktualisiert");
            } else {
                makeSnackbar("Keine neuen Vertretungen vorhanden");
            }
        }
    };


    @Override
    protected int getLayoutId() {
        return R.layout.activity_main_new;
    }

    @Override
    protected Toolbar getToolbar() {
        return (Toolbar) findViewById(R.id.mainToolbar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // if BaseActivity calls finish it will still run through the whole onCreate here but I don't want that
        if (isFinishing()) {
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        // register alarm if it hasn't been done by the application after booting the device
        mSharedPreferences = getSharedPreferences();
        if (!mSharedPreferences.getBoolean(PREFERENCE_ALARM_REGISTERED, false)) {
            // assign RefreshService class
            Intent alarmIntent = new Intent(this, RefreshService.class);
            PendingIntent alarmPendingIntent = PendingIntent.getService(this, BootReceiver.alarmManagerRequestCode, alarmIntent, PendingIntent.FLAG_IMMUTABLE);

            // set the alarm
            // it starts at 6 am and repeats once an hour
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(ALARM_TYPE, System.currentTimeMillis(), ALARM_INTERVAL, alarmPendingIntent);
            mSharedPreferences.edit().putBoolean(PREFERENCE_ALARM_REGISTERED, true).apply();
        }

        mDatabaseHandler = getDatabaseHandler();

        mMainTabLayout = findViewById(R.id.mainTabLayout);
        mMainViewPager = findViewById(R.id.mainViewPager);

        mSwipeRefreshLayout = findViewById(R.id.mainSwipeContainer);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
            mSwipeRefreshLayout.setOnRefreshListener(this::refresh);
        }

        long currentVersion = 0;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersion = packageInfo.getLongVersionCode();
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(TAG, "Name not found: ", e);
        }
        if (currentVersion > mSharedPreferences.getInt(PREFERENCE_CURRENT_VERSION, 0)) {
            refresh();
            showWhatsNewDialog();
            mSharedPreferences.edit().putLong(PREFERENCE_CURRENT_VERSION, currentVersion).apply();
        } else {
            displayData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter("refresh_message"));
        if (mSharedPreferences.getBoolean(PREFERENCES_CHANGED, false) || (mMainTabLayout != null && mMainTabLayout.getTabCount() == 0)) {
            displayData();
            mSharedPreferences.edit().putBoolean(PREFERENCES_CHANGED, false).apply(); // reset prefs
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister receiver to no longer receive broadcast but subsequently push a notification
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    // overflow menu override methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_refresh) {
            refresh();
        } else if (id == R.id.menu_settings) {
            startActivity(new Intent(this, PreferenceActivity.class));
        } else if (id == R.id.menu_donate) {
            startActivity(new Intent(this, DonateActivity.class));
        } else if (id == R.id.menu_uber) {
            startActivity(new Intent(this, AboutActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    // workaround for bug with SwipeRefreshLayout
    // it would refresh even if RecyclerView is not at the top
    // interface call is from SubstitutionTabFragment
    @Override
    public void toggleRefreshing(boolean enabled) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setEnabled(enabled);
        }
    }

    public void refresh() {
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }

        if (hasInternetAccess()) {
            Intent refreshServiceIntent = new Intent(this, RefreshService.class);
            refreshServiceIntent.putExtra(RefreshService.INSTRUCTION, RefreshService.SUBSTITUTION_REFRESH);
            startService(refreshServiceIntent);
        } else {
            makeSnackbar(String.valueOf(getResources().getText(R.string.no_connection)));
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }


    public void displayData() {
        int school = mSharedPreferences.getInt(PREFERENCE_SCHOOL, -1);
        String classYearLetter = mSharedPreferences.getString(PREFERENCE_CLASS_YEAR_LETTER, "");
        int classYear = Integer.parseInt(classYearLetter.substring(0, 2));
        String classLetter = classYearLetter.substring(3);

        Calendar calendar = Calendar.getInstance();
        List<String> tabTitles = new ArrayList<>();

        List<SubstitutionDay> databaseResults;
        if (mSharedPreferences.getBoolean(PREFERENCE_SHOW_WHOLE_PLAN, false)) {
            databaseResults = mDatabaseHandler.getSubstitutionDayList(school, 0, "");
        } else {
            databaseResults = mDatabaseHandler.getSubstitutionDayList(school, classYear, classLetter);
        }


        mMainTabLayout.removeAllTabs();
        for (int n = 0; n < databaseResults.size(); n++) {
            if (databaseResults.get(n).getSubstitutionList().size() > 0) {
                calendar.setTimeInMillis(databaseResults.get(n).getDate());
                String tempDate = dateFormatter.format(calendar.getTime());
                String tempWeekday = weekdayFormatter.format(calendar.getTime());
                tabTitles.add(tempWeekday + " " + tempDate.substring(0, 6));
            }
        }

        if (tabTitles.size() == 0) {
            mMainTabLayout.addTab(mMainTabLayout.newTab().setText("Keine Vertretungen gefunden"));
            mMainViewPager.removeAllViews();
        } else {
            PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabTitles, "substitution", databaseResults);
            mMainViewPager.setAdapter(pagerAdapter);
            mMainTabLayout.setupWithViewPager(mMainViewPager);
        }

        mMainViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mMainTabLayout) {
            @Override
            public void onPageScrollStateChanged(int state) {
                toggleRefreshing(state == ViewPager.SCROLL_STATE_IDLE);
            }
        });

        mMainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mMainViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mSwipeRefreshLayout.setRefreshing(false);
    }
}