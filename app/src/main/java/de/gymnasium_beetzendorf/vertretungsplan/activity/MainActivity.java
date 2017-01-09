package de.gymnasium_beetzendorf.vertretungsplan.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;
import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.BootReceiver;
import de.gymnasium_beetzendorf.vertretungsplan.DatabaseHandler;
import de.gymnasium_beetzendorf.vertretungsplan.R;
import de.gymnasium_beetzendorf.vertretungsplan.RefreshService;
import de.gymnasium_beetzendorf.vertretungsplan.adapter.PagerAdapter;
import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;
import de.gymnasium_beetzendorf.vertretungsplan.data1.SubstitutionDay;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.BaseTabFragment;

public class MainActivity extends BaseActivity
        implements Constants, BaseTabFragment.OnSwipeRefreshListener {

    private SharedPreferences mSharedPreferences;
    private TabLayout mMainTabLayout;
    private ViewPager mMainViewPager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Context mContext;
    private DatabaseHandler mDatabaseHandler;
    private int mSchool;
    private int mClassYear;
    private String mClassLetter;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean new_update = intent.getBooleanExtra("new_update", false);

            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }

            if (new_update) {
                displayData();
                makeSnackbar("Aktualisiert.");
            }
        }
    };


    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected Toolbar getToolbar() {
        return (Toolbar) findViewById(R.id.mainToolbar);
    }

    // activity override methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // finish if an instance of this app is already running
        if (mContext != null) {
            ((Activity) mContext).finish();
            mContext = this;
        }

        // instantiate preference
        mSharedPreferences = getSharedPreferences();

        // check if all settings are set
        mSchool = mSharedPreferences.getInt(PREFERENCE_SCHOOL, 0);
        mClassLetter = "A";
        mClassYear = 12;
        //mClassYear = Integer.parseInt(mSharedPreferences.getString(PREFERENCE_CLASS_YEAR_LETTER, "").substring(0, 2));
        //mClassLetter = mSharedPreferences.getString(PREFERENCE_CLASS_YEAR_LETTER, "").substring(3);

        // activate boot receiver
        // this will start the alarm that is responsible for refreshing data
        ComponentName receiver = new ComponentName(this, BootReceiver.class);
        PackageManager packageManager = this.getPackageManager();
        packageManager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);



        if (mSharedPreferences.getLong("last_class_list_refresh", 0) == 0) {
            Intent intent = new Intent(this, RefreshService.class);
            intent.putExtra(RefreshService.INSTRUCTION, RefreshService.CLASSLIST_REFRESH);
            startService(intent);
        }

        if (mSharedPreferences.getLong("last_substitution_plan_refresh", 0) == 0) {
            mDatabaseHandler = getDatabaseHandler();
            List<SubstitutionDay> list = mDatabaseHandler.getSubstitutionDayList(mSchool, mClassYear, mClassLetter);
            if (list.size() > 0) {
                mSharedPreferences.edit().putLong("last_substitution_plan_refresh", list.get(0).getUpdated()).apply();
            }
        }

        // register alarm if it hasn't been done by the application after booting the device
        if (!mSharedPreferences.getBoolean(PREFERENCE_ALARM_REGISTERED, false)) {
            // assign RefreshService class
            Intent alarmIntent = new Intent(this, RefreshService.class);
            PendingIntent alarmPendingIntent = PendingIntent.getService(this, BootReceiver.alarmManagerRequestCode, alarmIntent, 0);

            // set the alarm
            // it starts at 6 am and repeats once an hour
            // elapsed_realtime is used to save ressources
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(
                    ALARM_TYPE,
                    System.currentTimeMillis(),
                    ALARM_INTERVAL,
                    alarmPendingIntent);

            // change prefs so it won't set alarm everytime the app opens
            // unless the app is uninstalled/cache wiped the alarm will be handled by BootReceiver after the devices boots
            mSharedPreferences.edit().putBoolean(PREFERENCE_ALARM_REGISTERED, true).apply();
        }

        // instantiate SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.mainSwipeContainer);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });


        mDatabaseHandler = getDatabaseHandler();
        if (mDatabaseHandler.getSubstitutionDayList(mSchool, mClassYear, mClassLetter).size() == 0) {
            refresh();
        } else {
            // definitely show the data that's already been loaded at some point
            displayData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter("refresh_message"));

        if (mSharedPreferences.getBoolean(PREFERENCES_CHANGED, false)) { // refresh if prefs have changed
            displayData();
            mSharedPreferences.edit().putBoolean(PREFERENCES_CHANGED, false).apply(); // reset prefs

            mSchool = mSharedPreferences.getInt(PREFERENCE_SCHOOL, 0);
            mClassYear = Integer.parseInt(mSharedPreferences.getString(PREFERENCE_CLASS_YEAR_LETTER, "").substring(0, 2));
            mClassLetter = mSharedPreferences.getString(PREFERENCE_CLASS_YEAR_LETTER, "").substring(3);
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
        switch (id) {
            case R.id.menu_refresh:
                refresh();
                break;
            case R.id.menu_settings:
                startActivity(new Intent(this, PreferenceActivity.class));
                break;
            case R.id.menu_donate:
                startActivity(new Intent(this, DonateActivity.class));
                break;
            case R.id.menu_uber:
                startActivity(new Intent(this, AboutActivity.class));
                break;
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

    // takes care of data refreshing logic
    // if internet connection is available it tries a full refresh
    // else it just displays the current data
    public void refresh() {

        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }

        if (hasInternetAccess()) {
            Intent refreshServiceIntent = new Intent(this, RefreshService.class);
            // TODO: Remove next line after implementing RefreshService correctly
            refreshServiceIntent.putExtra(RefreshService.INSTRUCTION, RefreshService.SUBSTITUTION_REFRESH);
            refreshServiceIntent.putExtra("manual_refresh", true);
            startService(refreshServiceIntent);
        } else {
            makeSnackbar(String.valueOf(getResources().getText(R.string.no_connection)));
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }


    public void displayData() {
        List<SubstitutionDay> databaseResults;

        mMainTabLayout = (TabLayout) findViewById(R.id.mainTabLayout);
        mMainViewPager = (ViewPager) findViewById(R.id.mainViewPager);

        String currentTabText = "";
        int currentTabCount = -1;

        // get needed results
        databaseResults = mDatabaseHandler.getSubstitutionDayList(mSchool, mClassYear, mClassLetter);

        // check if tabs are already there and store the current tab to reopen it
        if (mMainTabLayout.getTabCount() > 0) {
            currentTabCount = mMainTabLayout.getSelectedTabPosition();

            try {
                //noinspection ConstantConditions
                currentTabText = (String) mMainTabLayout.getTabAt(currentTabCount).getText();
            } catch (NullPointerException e) {
                Log.i(TAG, "NullPointerException on getting currentTabText", e);
            }
        }

        // draw the tabs depending on the days from the file
        mMainTabLayout.removeAllTabs();
        for (int n = 0; n < databaseResults.size(); n++) {

            // only create a tab if there's any information to show within that tab
            if (databaseResults.get(n).getSubstitutionList().size() > 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(databaseResults.get(n).getDate());
                String tempDate = dateFormatter.format(calendar.getTime());
                String tempWeekday = weekdayFormatter.format(calendar.getTime());

                mMainTabLayout.addTab(mMainTabLayout.newTab().setText(tempWeekday + " " + tempDate.substring(0, 6)));
            }
        }

        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), mMainTabLayout.getTabCount(), "substitution", databaseResults);
        mMainViewPager.setAdapter(pagerAdapter);
        mMainTabLayout.setupWithViewPager(mMainViewPager);

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


        // if possible sets the tab before refreshing as the current one again
        // moving the ViewPager is already taken care of (OnTabSelectedListener)
        if (mMainTabLayout.getTabCount() > 0) {
            String newTabText;

            try {
                if (currentTabCount > -1 && currentTabCount < mMainTabLayout.getTabCount()) {
                    for (int i = 0; i < mMainTabLayout.getTabCount(); i++) {
                        try {
                            newTabText = (String) mMainTabLayout.getTabAt(i).getText();
                            if (newTabText.equals(currentTabText)) {
                                mMainTabLayout.getTabAt(i).select();
                            }
                        } catch (NullPointerException e) {
                            Log.i(TAG, "NullPointer", e);
                        }
                    }
                }
            } catch (NullPointerException e) {
                Log.i(TAG, "NullPointerException on setting currentTabText", e);
            }
        }

        // add an empty tab if there are (for some reason) no results to display
        if (databaseResults.size() == 0) {
            mMainTabLayout.addTab(mMainTabLayout.newTab().setText("Keine Vertretung gefunden!"));
        }

        mSwipeRefreshLayout.setRefreshing(false);

    }
}