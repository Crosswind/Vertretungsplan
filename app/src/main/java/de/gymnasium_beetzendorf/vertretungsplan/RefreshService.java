package de.gymnasium_beetzendorf.vertretungsplan;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class RefreshService {

    Context context;
    SharedPreferences sharedPreferences;
    DatabaseHandler databaseHandler;
    private static RefreshService instance = new RefreshService();

    private RefreshService() {
    }

    public static RefreshService getInstance() {
        return instance;
    }

    public void init (Context context) {
        this.context = context;
    }

    public void refresh() {
        DownloadFileFromURL downloadFileFromURL = new DownloadFileFromURL(MainActivity.SERVER_URL + MainActivity.SUBSTITUTION_QUERY_FILE);
        downloadFileFromURL.execute();

        // more code to refresh needs to be added here
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
                URL url = new URL(QUERY_URL);

                // keep track of the start time
                long start = System.currentTimeMillis();

                URLConnection urlConnection = url.openConnection();
                // Log.i(TAG, "Connection geöffnet");

                InputStream is = urlConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                //Log.i(TAG, "Buffered-/InputStream aktiv");

                FileOutputStream fos = context.openFileOutput("temp.xml", Context.MODE_PRIVATE);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                OutputStreamWriter out = new OutputStreamWriter(bos, "UTF-8");
                //Log.i(TAG, "File-/BufferedOutput aktiv");

                // writing to file
                byte data[] = new byte[1024];
                int count, total = 0;
                while ((count = bis.read(data)) != -1) {
                    total += count; // total size
                    bos.write(data, 0, count);
                }

                // close streams so the file does not get corrupted
                bos.flush();
                bos.close();

                // how much time did it take
                //Log.i(TAG, "Dauerte - " + String.valueOf(System.currentTimeMillis() - start) + "ms \n Buffer: " + total);

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
            List<Schoolday> xmlResults = XMLParser.parseXMLInput(context);
            // saves result to db
            databaseHandler = new DatabaseHandler(context, DatabaseHandler.DATABASE_NAME, null, DatabaseHandler.DATABASE_VERSION);
            databaseHandler.insertXmlResults(xmlResults);
        }
    }
}
