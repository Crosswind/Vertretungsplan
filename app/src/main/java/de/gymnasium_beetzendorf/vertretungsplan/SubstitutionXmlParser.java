package de.gymnasium_beetzendorf.vertretungsplan;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;

/*
    This class is responsible for parsing the substion xml. The current one for Gymnasium
    Beetzendorf can always be found here: http://vplankl.gymnasium-beetzendorf.de/Vertretungsplan_Klassen.xml
    The parser is not responsible for downloading the file (therefore see DownloadXml.java).
    The naming format of the file needs to be: substitution_IdOfSchool.xml (IdOfSchool needs to be
    replaced with the corresponding id found in School.
 */


public class SubstitutionXmlParser implements Constants {

    private final String TAG = this.getClass().getSimpleName();

    private Context context;
    private int schoolId;

    private String fileName;
    private String fileEncoding = "UTF-8"; // default right there

    private final String xml_title = "titel";
    private final String xml_schoolname = "schulname";
    private final String xml_updated = "datum";
    private final String xml_header = "kopf";


    /* constructor */
    SubstitutionXmlParser(Context context, int schoolId) {
        this.context = context;
        this.schoolId = schoolId;

        // make an init call here to set up everything and be ready to start parsing
        init();
    }

    /* public methods */


    /* private methods */
    private void init() {
        fileName = "substitution_" + String.valueOf(schoolId) + ".xml";
        fileEncoding = getFileEncoding();
    }

    private String getFileEncoding() {
        String encoding = "";
        try {
            FileInputStream fileInputStream = context.openFileInput(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String firstLine = bufferedReader.readLine();
            int start = firstLine.indexOf("encoding=") + 10; // +10 to actually start after the encoding string
            encoding = firstLine.substring(start, firstLine.indexOf("\"", start));
            fileInputStream.close();
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            Log.i(TAG, "substitution file could not be found!", e);
        } catch (IOException e) {
            Log.i(TAG, "IOException while getting file encoding", e);
        }
        // if encoding could not be found for some reason return UTF-8 by default
        return encoding.equalsIgnoreCase("") ? "UTF-8" : encoding;
    }

    private int getWeekdayCountFromTitle(String title) {
        String weekday = title.split(",", 1)[0];
        String[] weekdays = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"};

        for (int i = 0; i < weekdays.length; i++) {
            if (weekdays[i].equalsIgnoreCase(weekday)) {
                return i + 1;
            }
        }
        return 0;
    }

    private long getValidDateFromTitle(String title) {
        String temp = title.split(",", 2)[1]; // the part after the weekday
        temp = temp.split("\\(")[0];
        String rawDate = temp.trim();

        String[] dateNames = {"Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"};
        String[] dateNumbers = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};

        for (int i = 0; i < dateNames.length; i++) {
            if (rawDate.contains(dateNames[i])) {
                rawDate = rawDate.replace(dateNames[i], dateNumbers[i] + ".");
            }
        }

        rawDate = rawDate.replaceAll("\\s", ""); // remove all whitespace characters from the string

        if (rawDate.length() == 9) rawDate = "0" + rawDate;

        long date = 0;
        try {
            date = dateFormatter.parse(rawDate).getTime();
        } catch (ParseException e) {
            Log.i(TAG, "problem while parsing the date from the header", e);
        }
        return date;
    }

    private long getLastUpdatedFromHeader(String header) {
        long last_updated = 0;
        header = header.replace(",", ""); // remove kommas
        try {
            last_updated = dateTimeFormatter.parse(header).getTime();
        } catch (ParseException e) {
            Log.i(TAG, "problem while parsing last updated from the header", e);
        }
        return last_updated;
    }
}
