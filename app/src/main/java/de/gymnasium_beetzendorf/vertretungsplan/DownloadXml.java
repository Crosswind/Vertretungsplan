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
    String fileType;
    String url;
    String schedClass;
    int school;

    public DownloadXml(RefreshService refreshService, String fileType, String url, String schedClass, int school) {
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

            //String encoding = getFileEncoding(urlConnection);
            //Log.i(TAG, "encoding: " + encoding);

            InputStream inputStream = urlConnection.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            String filename;
            switch (fileType) {
                case "substitution":
                    filename = "substitution_" + String.valueOf(school) + ".xml";
                    break;
                case "schedule":
                    filename = "schedule_" + String.valueOf(school) + "_" + schedClass + ".xml";
                    break;
                default:
                    filename = "temp.xml";
                    break;
            }


            FileOutputStream fileOutputStream = refreshService.openFileOutput(filename, Context.MODE_PRIVATE);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);


            byte data[] = new byte[1024];
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
                refreshService.callBackSchedule(schedClass);
                break;

        }

    }

    private String getFileEncoding(URLConnection urlConnection) {
        String header = urlConnection.getHeaderField("Content-Type");
        Log.i(Constants.TAG, "tag: " + TAG + " header: " + header);
        int startingIndex = header.indexOf("charset=") + "charset".length();
        int endingIndex = header.indexOf("\"", startingIndex);
        return header.substring(startingIndex, endingIndex);
    }
}
