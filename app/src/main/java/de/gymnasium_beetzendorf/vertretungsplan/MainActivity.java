package de.gymnasium_beetzendorf.vertretungsplan;

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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
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

    // server related
    public static final String SERVER_URL = "http://vplankl.gymnasium-beetzendorf.de";
    public static final String SUBSTITUTION_QUERY_FILE = "/Vertretungsplan_Klassen.xml";
    public static final String SERVER_SCHEDULE_DIRECTORY = "/vertrertungsplaene";
    public static final String SCHEDULE_QUERY_FILE = "/";

    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
    public static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY);
    public static final SimpleDateFormat weekdayFormatter = new SimpleDateFormat("EE", Locale.GERMANY);

    public String date = "";

    private SharedPreferences myPreferences;
    private TabLayout mainTabLayout;
    private ViewPager mainViewPager;
    private ProgressBar progressBar;

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

        // set the main layout to be used by the activity
        setContentView(R.layout.activity_main);

        // todays date
        Calendar c = Calendar.getInstance();
        date = dateFormatter.format(c.getTime());

        // instantiate preference
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // define progress bar
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // based on current connection state launch activity
        if (checkConnection()) {
            refresh();
        } else {
            displayData();
            Toast.makeText(this, "Offlinedaten geladen. Möglicherweise nicht aktuell!", Toast.LENGTH_LONG).show();
        }
        progressBar.setVisibility(View.INVISIBLE);


        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwq6nvTxbdANQu4J1ru2fEx3DGB3xbEuHP6PcWl6zcLNwhPwjhZeu6Dvgpj/f1NxvehaT0c4US5BEu9XBC16k9hTf/FFHw/9OHr+hC9UtAsMlq07705pdreNVj/J9SYISPFWWMcoMAaRUyFj2ujLdTvs//bI5TO5lgxHqOcK4FeTGTLw4d4LyX10sz+CtDhFukbAqQG7PwkSON+wRJm/9NzXutXkWyFtMFmpsj+dHoQfbLwF82VYej135aZMPRmpd4f2+aScU2BKolJKq3uxYT2RCohmcqj1ZWYGf0mnl3yKi5o9Jnj9uDkeO6u+H7YUKGZMWHw54KlNIZX/OLGSe+QIDAQAB";

        byte[] bytes = base64EncodedPublicKey.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] >= 'A' && bytes[i] <= 'Z') {
                bytes[i] = (byte) ('a' + (bytes[i] - 'A'));
            } else if (bytes[i] >= 'A' && bytes[i] <= 'Z') {
                bytes[i] = (byte) ('A' + (bytes[i] - 'a'));
            }
        }

        String base64DecodedPublicKey = new String (bytes);
        Log.i(MainActivity.TAG, "new decoded string: " + base64DecodedPublicKey);

        bytes = base64DecodedPublicKey.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] >= 'A' && bytes[i] <= 'Z') {
                bytes[i] = (byte) ('a' + (bytes[i] - 'A'));
            } else if (bytes[i] >= 'a' && bytes[i] <= 'z') {
                bytes[i] = (byte) ('A' + (bytes[i] - 'a'));
            }
        }

        base64EncodedPublicKey = new String (bytes);
        Log.i(MainActivity.TAG, "new encoded string: " + base64EncodedPublicKey);
    }

    @Override
    protected void onResume() {
        super.onResume();

        progressBar.setVisibility(View.VISIBLE);

        myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (myPreferences.getBoolean(PREFERENCES_CHANGED, false)) { // refresh if prefs have changed
            if (checkConnection()) {
                refresh();
            } else {
                displayData();
                Toast.makeText(this, "Offlinedaten geladen. Möglicherweise nicht aktuell!", Toast.LENGTH_LONG).show();
            }
            progressBar.setVisibility(View.INVISIBLE);
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

    // checks whether there's an active internet connection (WiFi/Data)
    public boolean checkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // main handler for refreshing data from the server and distributing it to database and to recycler
    public void refresh() {
        if (checkConnection()) {
            DownloadFileFromURL downloadFileFromURL = new DownloadFileFromURL(SERVER_URL + SUBSTITUTION_QUERY_FILE);
            downloadFileFromURL.execute(); // the rest will be executed in onPostExecute

            // notify to cancel refreshing
        } else {
            //displayData();
            Toast.makeText(this, "Keine Internetverbindung - Daten möglicherweise nicht aktuell!", Toast.LENGTH_LONG).show();
            // notifiy swipeRefreshLayout to cancel refreshing
        }
    }

    public void displayData() {
        List<Schoolday> results;
        mainTabLayout = (TabLayout) findViewById(R.id.mainTabLayout);
        mainViewPager = (ViewPager) findViewById(R.id.mainViewPager);

        // check if current day is needed or if it's passt 3 pm that day
        long currentTime = System.currentTimeMillis();
        long afterSchoolTime = 0;

        try {
            afterSchoolTime = dateTimeFormatter.parse(date + " 15:00").getTime();
        } catch (ParseException e) {
            Log.i(TAG, "ParseException for afterSchoolTime", e);
        }

        boolean after3Pm; // true if it's later than 3 pm
        // today no longer needs to be displayed
        after3Pm = afterSchoolTime != 0 && currentTime > afterSchoolTime;

        // get needed results
        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext(), DatabaseHandler.DATABASE_NAME, null, DatabaseHandler.DATABASE_VERSION);
        results = databaseHandler.getAllSubstitutions(date, after3Pm);

        // remove unnecessary items from results
        long today = 0, temp;
        try {
            today = dateFormatter.parse(date).getTime(); // put todays date in a milliseconds value
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "today: " + String.valueOf(today));
        for (int i = results.size() - 1; i >= 0; i--) {
            temp = results.get(i).getDate(); // time of the results in milliseconds

            if (temp < today) {
                results.remove(i);
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
                Log.i(TAG, "tempDate: " + tempWeekday + " " + tempDate);
                mainTabLayout.addTab(mainTabLayout.newTab().setText(tempWeekday + " " + tempDate.substring(0, 6)));
            }
        }

        // remove empty results
        // easier to do an extra for loop because this doesn't reverse the order since we're going backwards through the list
        for (int k = results.size() - 1; k >= 0; k--) {
            if (results.get(k).getSubjects().size() == 0) {
                results.remove(k);
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

        if (results.size() == 0) {
            mainTabLayout.addTab(mainTabLayout.newTab().setText("Keine Vertretung gefunden!"));
        }

        progressBar.setVisibility(View.INVISIBLE);
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

                FileOutputStream fos = openFileOutput("temp.xml", MODE_PRIVATE);
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
            List<Schoolday> xmlResults = XMLParser.parseXMLInput(getApplicationContext());

            // saves result to db
            DatabaseHandler db_handler = new DatabaseHandler(getApplicationContext(), DatabaseHandler.DATABASE_NAME, null, DatabaseHandler.DATABASE_VERSION);
            db_handler.insertXmlResults(xmlResults);
            //Log.i(TAG, "Menge der gespeicherten Ergebnisse: " + String.valueOf(xmlResults.size()));

            // display data
            displayData();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setIndeterminate(true);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            progressBar.setIndeterminate(false);
        }
    }
}