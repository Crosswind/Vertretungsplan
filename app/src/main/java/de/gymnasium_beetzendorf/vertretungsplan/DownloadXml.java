package de.gymnasium_beetzendorf.vertretungsplan;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;

class DownloadXml extends AsyncTask<String, Void, String> implements Constants {

    private RefreshService refreshService;
    private String fileType;
    private String url;
    private String schedClass;
    private int school;

    public static int SUBSTITUTION = 1;
    public static int SCHEDULE = 2;
    public static int SCHEDULE_ALL = 3;

    DownloadXml(RefreshService refreshService, String fileType, String url, String schedClass, int school) {
        this.refreshService = refreshService;
        this.fileType = fileType;
        this.url = url;
        this.schedClass = schedClass;
        this.school = school;
    }

    @Override
    protected String doInBackground(String... params) {

        try {
            URL url = new URL(this.url);
            URLConnection urlConnection = url.openConnection();

            InputStream inputStream = urlConnection.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            String filename;
            switch (fileType) {
                case "substitution":
                    Log.i(TAG, "Schule(DownloadXML): " + school);

                    filename = "substitution_" + school + ".xml";
                    break;
                case "schedule":
                    filename = "schedule_" + school + "_" + schedClass + ".xml";
                    break;
                default:
                    filename = "temp.xml";
                    break;
            }

            FileOutputStream fileOutputStream = refreshService.openFileOutput(filename, Context.MODE_PRIVATE);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);


            byte[] data = new byte[1024];
            int count;
            while ((count = bufferedInputStream.read(data)) != -1) {
                bufferedOutputStream.write(data, 0, count);
            }

            bufferedOutputStream.flush();
            bufferedOutputStream.close();

        } catch (MalformedURLException e) {
            Log.e(TAG, "URL is not valid", e);
        } catch (IOException e) {
            Log.e(TAG, "IO Exception urlConnection", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        switch (fileType) {
            case "substitution":
                refreshService.callBackSubstitution();
                break;
            case "schedule":
                refreshService.callBackSchedule(school, schedClass);
                break;

        }

    }


}
