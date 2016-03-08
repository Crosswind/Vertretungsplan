package de.gymnasium_beetzendorf.vertretungsplan;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements TabFragment.OnSwipeRefreshListener {

    public static final String TAG = "MainActivity";

    public static final String FIRST_TIME_OPENED = "first_time_opened";
    public static final String SHOW_WHOLE_PLAN = "show_whole_plan";
    public static final String CLASS_TO_SHOW = "class_to_show";
    public static final String PREFERENCES_CHANGED = "preferences_changed";
    public static final String ALARM_REGISTERED = "alarm_registered";

    // server related
    public static final String SERVER_URL = "http://vplankl.gymnasium-beetzendorf.de";
    public static final String SUBSTITUTION_QUERY_FILE = "/Vertretungsplan_Klassen.xml";
    public static final String SERVER_SCHEDULE_DIRECTORY = "/stundenkl";
    public static final String SCHEDULE_QUERY_FILE = "/aktuell";

    // date formatting variables - used all over the project
    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
    public static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY);
    public static final SimpleDateFormat weekdayFormatter = new SimpleDateFormat("EE", Locale.GERMANY);

    // alarm constants
    public static final long ALARM_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    public static final int ALARM_TYPE = AlarmManager.RTC;

    public String mDate = "";

    private SharedPreferences mSharedPreferences;
    private TabLayout mMainTabLayout;
    private ViewPager mMainViewPager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Toolbar mToolbar;

    // method to return the classes the school is
    // right now just returns a static result because classes are not dynamically receivable from the website
    // will be added in a future release
    public static String[] getClasses() {
        List<String> classList = new ArrayList<>();
        classList.add("05 A");
        classList.add("05 B");
        classList.add("05 C");
        classList.add("06 A");
        classList.add("06 B");
        classList.add("06 C");
        classList.add("06 D");
        classList.add("07 A");
        classList.add("07 B");
        classList.add("07 C");
        classList.add("08 A");
        classList.add("08 B");
        classList.add("08 C");
        classList.add("08 D");
        classList.add("09 A");
        classList.add("09 B");
        classList.add("09 C");
        classList.add("10 A");
        classList.add("10 B");
        classList.add("10 C");
        classList.add("11 A");
        classList.add("11 B");
        classList.add("11 C");
        classList.add("12 A");
        classList.add("12 B");
        classList.add("12 C");

        String[] result = new String[classList.size()];
        result = classList.toArray(result);
        return result;
    }

    // activity override methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // activate boot receiver
        // this will start the alarm that is responsible for refreshing data
        ComponentName receiver = new ComponentName(this, BootReceiver.class);
        PackageManager packageManager = this.getPackageManager();
        packageManager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        // instantiate preference
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // register alarm if it hasn't been done by the application after booting the device
        if (!mSharedPreferences.getBoolean(ALARM_REGISTERED, false)) {
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
            mSharedPreferences.edit().putBoolean(ALARM_REGISTERED, true).apply();
        }

        // set the main layout to be used by the activity
        setContentView(R.layout.activity_main);

        // todays mDate
        Calendar c = Calendar.getInstance();
        mDate = dateFormatter.format(c.getTime());

        // define SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.mainSwipeContainer);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.Inf);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                mSwipeRefreshLayout.setRefreshing(false);

            }
        });

        if (savedInstanceState == null) {
            refresh();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();


        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Log.i(TAG, "pref - changed: " + String.valueOf(mSharedPreferences.getBoolean(PREFERENCES_CHANGED, false)));
        if (mSharedPreferences.getBoolean(PREFERENCES_CHANGED, false)) { // refresh if prefs have changed
            mSwipeRefreshLayout.setRefreshing(true);
            refresh();
            mSharedPreferences.edit().putBoolean(PREFERENCES_CHANGED, false).apply(); // reset prefs
        } else {
            displayData();
        }

        if (isMyServiceRunning()) {
            Toast.makeText(this, "Service running", Toast.LENGTH_LONG).show();
        }

        mSwipeRefreshLayout.setRefreshing(false);


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
        Intent i;
        switch (id) {
            case R.id.menu_refresh:
                refresh();
                mSwipeRefreshLayout.setRefreshing(false);
                break;
            case R.id.menu_settings:
                i = new Intent(this, PreferenceActivity.class);
                startActivity(i);
                break;
            case R.id.menu_donate:
                i = new Intent(this, DonateActivity.class);
                startActivity(i);
                break;
            case R.id.menu_uber:
                i = new Intent(this, AboutActivity.class);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // workaround for bug with SwipeRefreshLayout
    // it would refresh even if RecyclerView is not at the top
    // interface call is from TabFragment
    @Override
    public void toggleRefreshing(boolean enabled) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setEnabled(enabled);
        }
    }

    // checks whether there's an active internet connection (WiFi/Data)
    public boolean checkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public boolean isMyServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if ("de.gymnasium_beetzendorf.vertretungsplan".equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // takes care of data refreshing logic
    // if internet connection is available it tries a full refresh
    // else it just displays the current data
    public void refresh() {

        mSwipeRefreshLayout.setRefreshing(true);

        if (checkConnection()) {
            DownloadFileFromURL downloadFileFromURL = new DownloadFileFromURL(SERVER_URL + SUBSTITUTION_QUERY_FILE);
            downloadFileFromURL.execute(); // the rest will be executed in onPostExecute


            // notify to cancel refreshing
        } else {
            Toast.makeText(this, "Keine Verbindung. ", Toast.LENGTH_LONG).show();
            //displayData();
        }

        mSwipeRefreshLayout.setEnabled(false);
    }

    public void displayData() {
        List<Schoolday> databaseResults;
        mMainTabLayout = (TabLayout) findViewById(R.id.mainTabLayout);
        mMainViewPager = (ViewPager) findViewById(R.id.mainViewPager);

        String currentTabText = "";
        int currentTabCount = -1;

        // get needed results
        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext(), DatabaseHandler.DATABASE_NAME, null, DatabaseHandler.DATABASE_VERSION);
        databaseResults = databaseHandler.getAllSubstitutions();

        // check if tabs are already there and store the current tab to reopen it
        if (mMainTabLayout.getTabCount() > 0) {
            currentTabCount = mMainTabLayout.getSelectedTabPosition();
            try {
                currentTabText = (String) mMainTabLayout.getTabAt(currentTabCount).getText();
            } catch (NullPointerException e) {
                Log.i(TAG, "NullPointerException on getting currentTabText", e);
            }
        }

        // draw the tabs depending on the days from the file
        mMainTabLayout.removeAllTabs();
        for (int n = 0; n < databaseResults.size(); n++) {

            // only create a tab if there's any information to show within that tab
            if (databaseResults.get(n).getSubjects().size() > 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(databaseResults.get(n).getDate());
                String tempDate = dateFormatter.format(calendar.getTime());
                String tempWeekday = weekdayFormatter.format(calendar.getTime());

                mMainTabLayout.addTab(mMainTabLayout.newTab().setText(tempWeekday + " " + tempDate.substring(0, 6)));
            }
        }

        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), mMainTabLayout.getTabCount(), databaseResults);
        mMainViewPager.setAdapter(pagerAdapter);
        mMainViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mMainTabLayout) {
            @Override
            public void onPageScrollStateChanged(int state) {
                toggleRefreshing(state == ViewPager.SCROLL_STATE_IDLE);
            }
        });
        mMainTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
                        newTabText = (String) mMainTabLayout.getTabAt(i).getText();
                        if (newTabText.equals(currentTabText)) {
                            mMainTabLayout.getTabAt(i).select();
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
    }

    // inner class to download the timetable -> might be extended so it also downloads the whole timetable if made available on the website
    private class DownloadFileFromURL extends AsyncTask<Void, Void, Void> {
        private String QUERY_URL;

        public DownloadFileFromURL(String Url) {
            QUERY_URL = Url;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // open connection and streams for writing the file
                URL url = new URL(QUERY_URL);
                URLConnection urlConnection = url.openConnection();

                InputStream is = urlConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                FileOutputStream fos = openFileOutput("substitution.xml", MODE_PRIVATE);
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                // writing to file
                byte data[] = new byte[1024];
                int count;
                while ((count = bis.read(data)) != -1) {
                    bos.write(data, 0, count);
                }

                // close streams so the file does not get corrupted
                bos.flush();
                bos.close();

            } catch (FileNotFoundException e) {
                Log.i(Helper.TAG, "Datei konnte nicht gefunden werden", e);
            } catch (MalformedURLException e) {
                Log.i(Helper.TAG, "URL inkorrekt", e);
            } catch (IOException e) {
                Log.i(Helper.TAG, "IOException", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // kick off the xml parsing
            List<Schoolday> xmlResults = XMLParser.parseSubstitutionXml(getApplicationContext());

            // get database
            DatabaseHandler db_handler = new DatabaseHandler(getApplicationContext(), DatabaseHandler.DATABASE_NAME, null, DatabaseHandler.DATABASE_VERSION);
            // get the current results from db
            List<Schoolday> databaseResults = db_handler.getAllSubstitutions();

            // if the database is empty or if there's newer content available insert into the database
            if ((databaseResults.size() == 0 || databaseResults.get(0).getLastUpdated() > xmlResults.get(0).getLastUpdated())
                    && xmlResults.size() > 0) {
                db_handler.insertSubstitutionXmlResults(xmlResults);
                Toast.makeText(getApplicationContext(), "Daten aktualisiert.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Keine neuen Daten vorhanden.", Toast.LENGTH_SHORT).show();
            }

            // displaying is independent of new data so we can call it either way
            displayData();

        }
    }
}