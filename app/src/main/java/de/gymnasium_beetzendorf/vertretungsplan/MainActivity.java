package de.gymnasium_beetzendorf.vertretungsplan;

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
import java.text.ParseException;
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

    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
    public static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY);
    public static final SimpleDateFormat weekdayFormatter = new SimpleDateFormat("EE", Locale.GERMANY);

    public String date = "";

    private SharedPreferences myPreferences;
    private TabLayout mainTabLayout;
    private ViewPager mainViewPager;
    private SwipeRefreshLayout swipeRefreshLayout;

    // method to return the classes the school is
    // right now just returns a static result because classes are not dynamically receivable from the website
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
        ComponentName receiver = new ComponentName(this, BootReceiver.class);
        PackageManager packageManager = this.getPackageManager();
        packageManager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        // instantiate preference
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // register alarm if it hasn't been done by the application after booting the device
        if (!myPreferences.getBoolean(ALARM_REGISTERED, false)) {
            // assign RefreshService class
            Intent alarmIntent = new Intent(this, RefreshService.class);
            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, BootReceiver.alarmManagerRequestCode, alarmIntent, 0);
            // set the alarm
            // it starts at 6 am and repeats once an hour
            // elapsed_realtime is used to save ressources
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    System.currentTimeMillis(),
                    AlarmManager.INTERVAL_HOUR,
                    alarmPendingIntent);
            myPreferences.edit().putBoolean(ALARM_REGISTERED, true).apply();
        }

        // set the main layout to be used by the activity
        setContentView(R.layout.activity_main);

        // toolbar
        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        // todays date
        Calendar c = Calendar.getInstance();
        date = dateFormatter.format(c.getTime());

        // define SwipeRefreshLayout
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.mainSwipeContainer);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.Inf);

        // display the data that's in the database for far
        displayData();

        // try to refresh the data whenever the application is opened
        if (checkConnection()) {
            refresh();
        } else {
            Toast.makeText(this, "Keine Verbindung.", Toast.LENGTH_LONG).show();
        }

        //String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwq6nvTxbdANQu4J1ru2fEx3DGB3xbEuHP6PcWl6zcLNwhPwjhZeu6Dvgpj/f1NxvehaT0c4US5BEu9XBC16k9hTf/FFHw/9OHr+hC9UtAsMlq07705pdreNVj/J9SYISPFWWMcoMAaRUyFj2ujLdTvs//bI5TO5lgxHqOcK4FeTGTLw4d4LyX10sz+CtDhFukbAqQG7PwkSON+wRJm/9NzXutXkWyFtMFmpsj+dHoQfbLwF82VYej135aZMPRmpd4f2+aScU2BKolJKq3uxYT2RCohmcqj1ZWYGf0mnl3yKi5o9Jnj9uDkeO6u+H7YUKGZMWHw54KlNIZX/OLGSe+QIDAQAB";
    }

    @Override
    protected void onResume() {
        super.onResume();

        myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (myPreferences.getBoolean(PREFERENCES_CHANGED, false)) { // refresh if prefs have changed
            if (checkConnection()) {
                swipeRefreshLayout.setRefreshing(true);
                displayData();
            } else {
                displayData();
                Toast.makeText(this, "Keine Verbindung. Offlinedaten geladen.", Toast.LENGTH_LONG).show();
            }
        }
        myPreferences.edit().putBoolean(PREFERENCES_CHANGED, false).apply(); // reset prefs

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
                swipeRefreshLayout.setRefreshing(false);
                break;
            case R.id.menu_settings:
                i = new Intent(this, PreferenceActivity.class);
                startActivity(i);
                break;
            case R.id.menu_donate:
                i = new Intent(this, DonateActivity.class);
                startActivity(i);
                break;
            /*case R.id.menu_uber:
                Toast.makeText(this, "Nocht nicht implementiert.", Toast.LENGTH_LONG).show();
                break;*/
        }
        return super.onOptionsItemSelected(item);
    }

    // workaround for bug with SwipeRefreshLayout
    // it would refresh even if RecyclerView is not at the top
    @Override
    public void toggleRefreshing(boolean enabled) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(enabled);
        }
    }

    // checks whether there's an active internet connection (WiFi/Data)
    public boolean checkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void onCancelDownload() {
        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(this, "Fehler!", Toast.LENGTH_LONG).show();

    }

    // main handler for refreshing data from the server and distributing it to database and to recycler
    public void refresh() {
        if (checkConnection()) {
            swipeRefreshLayout.setRefreshing(true);
            DownloadFileFromURL downloadFileFromURL = new DownloadFileFromURL(SERVER_URL + SUBSTITUTION_QUERY_FILE);
            downloadFileFromURL.execute(); // the rest will be executed in onPostExecute

            // notify to cancel refreshing
        } else {
            Toast.makeText(this, "Keine Verbindung.", Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void displayData() {
        List<Schoolday> results;
        mainTabLayout = (TabLayout) findViewById(R.id.mainTabLayout);
        mainViewPager = (ViewPager) findViewById(R.id.mainViewPager);

        String currentTabText = "";
        int currentTabCount = -1;

        //mainViewPager.canScrollVertically(-1);

        // get needed results
        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext(), DatabaseHandler.DATABASE_NAME, null, DatabaseHandler.DATABASE_VERSION);
        results = databaseHandler.getAllSubstitutions();

        // remove unnecessary items from results
        long today = 0, temp;
        try {
            today = dateFormatter.parse(date).getTime(); // put todays date in a milliseconds value
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (int i = results.size() - 1; i >= 0; i--) {
            temp = results.get(i).getDate(); // time of the results in milliseconds

            if (temp < today) {
                results.remove(i);
            }
        }

        // check if tabs are already there and store the current tab to reopen it
        if (mainTabLayout.getTabCount() > 0) {
            currentTabCount = mainTabLayout.getSelectedTabPosition();
            try {
                currentTabText = (String) mainTabLayout.getTabAt(currentTabCount).getText();
            } catch (NullPointerException e) {
                Log.i(TAG, "NullPointerException on setting currentTabText", e);
            }
        }

        // draw the tabs depending on the days from the file
        mainTabLayout.removeAllTabs();
        for (int n = 0; n < results.size(); n++) {

            // only create a tab if there's any information to show within that tab
            if (results.get(n).getSubjects().size() > 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(results.get(n).getDate());
                String tempDate = dateFormatter.format(calendar.getTime());
                String tempWeekday = weekdayFormatter.format(calendar.getTime());
                mainTabLayout.addTab(mainTabLayout.newTab().setText(tempWeekday + " " + tempDate.substring(0, 6)));
            }
        }

        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), mainTabLayout.getTabCount(), results);
        mainViewPager.setAdapter(pagerAdapter);
        mainViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mainTabLayout));
        mainTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mainViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        if (mainTabLayout.getTabCount() > 0) {
            try {
                if (currentTabCount > -1 && currentTabCount < mainTabLayout.getTabCount()) {
                    if (mainTabLayout.getTabAt(currentTabCount).getText() == currentTabText &&
                            mainTabLayout.getSelectedTabPosition() == currentTabCount) {
                        mainTabLayout.getTabAt(currentTabCount).select();
                    }
                }
            } catch (NullPointerException e) {
                Log.i(TAG, "NullPointerException on setting currentTabText", e);
            }
        }

        if (results.size() == 0) {
            mainTabLayout.addTab(mainTabLayout.newTab().setText("Keine Vertretung gefunden!"));
        }

        swipeRefreshLayout.setRefreshing(false);
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
                onCancelDownload();
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
            List<Schoolday> databaseResults = db_handler.getAllSubstitutions();


            if (databaseResults.size() > 0 && xmlResults.size() > 0) {

                if (databaseResults.get(0).getLastUpdated() >= xmlResults.get(0).getLastUpdated()) {
                    Toast.makeText(getApplicationContext(), "Vertretungsplan aktuell.", Toast.LENGTH_LONG).show();
                } else {
                    // insert data into database
                    db_handler.insertSubstitutionXmlResults(xmlResults);
                    displayData();
                }
            }
        }
    }
}