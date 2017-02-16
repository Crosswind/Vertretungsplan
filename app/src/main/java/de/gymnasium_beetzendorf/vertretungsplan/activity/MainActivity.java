package de.gymnasium_beetzendorf.vertretungsplan.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import de.gymnasium_beetzendorf.vertretungsplan.data1.Subject;
import de.gymnasium_beetzendorf.vertretungsplan.data1.SubstitutionDay;
import de.gymnasium_beetzendorf.vertretungsplan.fragment.BaseTabFragment;

public class MainActivity extends BaseActivity
        implements Constants, BaseTabFragment.OnSwipeRefreshListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private SharedPreferences mSharedPreferences;
    private ViewPager mMainViewPager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "1 MainActivity");

        Log.i(TAG, "Mat: " + Subject.getSubjectIdBySubjectShort("Eng") + " - " + Subject.getSubjectShortById(Subject.getSubjectIdBySubjectShort("Eng")));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        mSharedPreferences = getSharedPreferences();
        mDatabaseHandler = getDatabaseHandler();

        mSchool = mSharedPreferences.getInt(PREFERENCE_SCHOOL, -1);
        String classYearLetter = mSharedPreferences.getString(PREFERENCE_CLASS_YEAR_LETTER, "");
        mClassYear = Integer.parseInt(classYearLetter.substring(0, 2));
        mClassLetter = classYearLetter.substring(3);

        // register alarm if it hasn't been done by the application after booting the device
        if (!mSharedPreferences.getBoolean(PREFERENCE_ALARM_REGISTERED, false)) {
            // assign RefreshService class
            Intent alarmIntent = new Intent(this, RefreshService.class);
            PendingIntent alarmPendingIntent = PendingIntent.getService(this, BootReceiver.alarmManagerRequestCode, alarmIntent, 0);

            // set the alarm
            // it starts at 6 am and repeats once an hour
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(ALARM_TYPE, System.currentTimeMillis(), ALARM_INTERVAL, alarmPendingIntent);
            mSharedPreferences.edit().putBoolean(PREFERENCE_ALARM_REGISTERED, true).apply();
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.mainSwipeContainer);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refresh();
                }
            });
        }

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter("refresh_message"));

        mSchool = mSharedPreferences.getInt(PREFERENCE_SCHOOL, -1);
        String classYearLetter = mSharedPreferences.getString(PREFERENCE_CLASS_YEAR_LETTER, "");
        mClassYear = Integer.parseInt(classYearLetter.substring(0, 2));
        mClassLetter = classYearLetter.substring(3);
        mSharedPreferences.edit().putBoolean(PREFERENCES_CHANGED, false).apply(); // reset prefs
        displayData();
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
        TabLayout mainTabLayout = (TabLayout) findViewById(R.id.mainTabLayout);
        mMainViewPager = (ViewPager) findViewById(R.id.mainViewPager);

        List<SubstitutionDay> databaseResults = mDatabaseHandler.getSubstitutionDayList(mSchool);
        Log.i(TAG, "results from the database: " + databaseResults.size());
        // draw the tabs depending on the days from the file
        mainTabLayout.removeAllTabs();
        String tabTitle;
        for (int n = 0; n < databaseResults.size(); n++) {
            // only create a tab if there's any information to show within that tab
            Log.i(TAG, String.format("Tag %1$d: Menge der Vertretungen: %2$d", n, databaseResults.get(n).getSubstitutionList().size()));
            if (databaseResults.get(n).getSubstitutionList().size() > 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(databaseResults.get(n).getDate());
                String tempDate = dateFormatter.format(calendar.getTime());
                String tempWeekday = weekdayFormatter.format(calendar.getTime());

                tabTitle = tempWeekday + " " + tempDate.substring(0, 6);
                Log.i(TAG, n + " tabtitle: " + tabTitle);

                mainTabLayout.addTab(mainTabLayout.newTab().setText(tabTitle));
            }
        }

        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), mainTabLayout.getTabCount(), "substitution", databaseResults);
        mMainViewPager.setAdapter(pagerAdapter);
        mainTabLayout.setupWithViewPager(mMainViewPager);

        mMainViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mainTabLayout) {
            @Override
            public void onPageScrollStateChanged(int state) {
                toggleRefreshing(state == ViewPager.SCROLL_STATE_IDLE);
            }
        });

        mainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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

        // add an empty tab if there are (for some reason) no results to display
        if (databaseResults.size() == 0) {
            mainTabLayout.addTab(mainTabLayout.newTab().setText("Keine Vertretung gefunden!"));
        }

        mSwipeRefreshLayout.setRefreshing(false);
    }
}