package de.gymnasium_beetzendorf.vertretungsplan;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class XMLParser {

    static final String TAG = ".vertretungsplan";
    // header tags
    static final String headerTag = "kopf"; // header for timetable
    static final String dateTag = "titel"; // date needs to be extracted
    static final String affectedClassesTag = "aenderungk"; // shows affected classes
    static final String lastUpdatedTag = "datum";

    // actual information tags
    static final String timetableTag = "haupt";
    static final String changeTag = "aktion"; // contains the timetable
    static final String courseTag = "klasse"; // contains a change
    static final String periodTag = "stunde";
    static final String subjectTag = "fach";
    static final String teacherTag = "lehrer"; // new/changed room
    static final String roomTag = "raum"; // additional information such as assignments etc
    static final String infoTag = "info";

    public static String extractDateFromTitle(String title) {
        // needed to extract the date (format: dd:MM:yyyy)
        String dateString, dateStringHolder;
        String[] dateStringHolderSplit;
        String[] dateNames = {"Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"};
        String[] dateNumbers = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};

        dateStringHolderSplit = title.split(",");
        dateStringHolder = dateStringHolderSplit[1].trim();
        dateStringHolderSplit = dateStringHolder.split("\\(");
        dateStringHolder = dateStringHolderSplit[0].trim();

        for (int n = 0; n < 12; n++) {
            if (dateStringHolder.contains(dateNames[n])) {
                dateStringHolder = dateStringHolder.replace(dateNames[n], dateNumbers[n] + ".");
            }
        }
        dateStringHolder = dateStringHolder.replaceAll("\\s", "");
        if (dateStringHolder.length() == 9) {
            dateStringHolder = "0" + dateStringHolder;
        }
        dateString = dateStringHolder;
        return dateString;
    }

    public static List<Schoolday> parseXMLInput(Context context) {
        List<Schoolday> result;
        result = new ArrayList<>();

        Schoolday currentDay = new Schoolday();
        Subject currentSubject = new Subject();
        List<Subject> currentSubjectList = new ArrayList<>();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            // open fis to the file to get the information
            FileInputStream fis = context.openFileInput("temp.xml");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis, "ISO-8859-1")); // set enconding!!

            // set input for the parser
            xpp.setInput(bufferedReader);

            // set starting event type
            int eventType = xpp.getEventType();

            // go
            String tag, text = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                tag = xpp.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tag.equalsIgnoreCase(headerTag)) {
                            currentDay = new Schoolday();
                            currentSubjectList = new ArrayList<>();
                        } else if (tag.equalsIgnoreCase(changeTag)) {
                            currentSubject = new Subject();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        text = xpp.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        switch (tag) {
                            case changeTag:
                                currentSubjectList.add(currentSubject);
                                break;
                            case timetableTag:
                                currentDay.setSubjects(currentSubjectList);
                                result.add(currentDay);
                                break;
                            case dateTag:
                                String dateString = extractDateFromTitle(text);
                                long dateInMillis = 0;
                                try {
                                    dateInMillis = MainActivity.dateFormatter.parse(dateString).getTime();
                                } catch (ParseException e) {
                                    Log.i(MainActivity.TAG, "ParseException", e);
                                }
                                currentDay.setDate(dateInMillis);
                                break;
                            case affectedClassesTag:
                                String[] affectedClasses = text.split(", ");
                                currentDay.setChanges(Arrays.asList(affectedClasses));
                                break;
                            case lastUpdatedTag:
                                long last_updated;
                                Date date;
                                last_updated = 0;
                                try {
                                    date = MainActivity.dateFormatter.parse(text);
                                    last_updated = date.getTime();

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                } catch (NullPointerException e) {
                                    Log.i(MainActivity.TAG, "NullPointer on .getTime(): ", e);
                                }
                                currentDay.setLastUpdated(last_updated);
                                break;
                            case courseTag:
                                String temp;
                                if (text.length() > 4) {
                                    temp = text.substring(0, 3);
                                    temp += text.substring(text.length() - 4);
                                    currentSubject.setCourse(temp);
                                } else {
                                    currentSubject.setCourse(text);
                                }
                                break;
                            case periodTag:
                                int period = 0;
                                try {
                                    period = Integer.parseInt(text);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                currentSubject.setPeriod(period);
                                break;
                            case subjectTag:
                                currentSubject.setSubject(text);
                                break;
                            case teacherTag:
                                currentSubject.setTeacher(text);
                                break;
                            case roomTag:
                                currentSubject.setRoom(text);
                                break;
                            case infoTag:
                                currentSubject.setInfo(text);
                                break;
                        }
                        break;
                }
                // move to the next iteration -- finally reach END_DOCUMENT
                eventType = xpp.next();
            }
        } catch (
                XmlPullParserException e
                )

        {
            Log.i(TAG, "PullParserException", e);
        } catch (
                FileNotFoundException e
                )

        {
            Log.i(TAG, "File not found", e);
        } catch (
                IOException e
                )

        {
            Log.i(TAG, "IOException", e);
        } catch (
                NullPointerException e
                )

        {
            Log.i(TAG, "Some data is missing (NullPointer)", e);
        }

        //Log.i(TAG, "1. tag: " + String.valueOf(result.get(0).getDate()) + "\n 2. tag: " + String.valueOf(result.get(1).getDate()));
        return result;
    }
}