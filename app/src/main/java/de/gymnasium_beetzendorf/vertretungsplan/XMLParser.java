package de.gymnasium_beetzendorf.vertretungsplan;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;
import de.gymnasium_beetzendorf.vertretungsplan.data.Schoolday;
import de.gymnasium_beetzendorf.vertretungsplan.data.Subject;

public class XMLParser {

    static final String TAG = ".vertretungsplan";

    // schedule xml tags
    static final String headerTag = "kopf"; // header for timetable
    static final String dateTag = "titel"; // mDate needs to be extracted
    static final String affectedClassesTag = "aenderungk"; // shows affected classes
    static final String lastUpdatedTag = "datum";

    static final String timetableTag = "haupt";
    static final String changeTag = "aktion"; // contains the timetable
    static final String courseTag = "klasse"; // contains a change
    static final String periodTag = "stunde";
    static final String subjectTag = "fach";
    static final String teacherTag = "lehrer"; // new/changed room
    static final String roomTag = "raum"; // additional information such as assignments etc
    static final String infoTag = "info";

    // schedule xml tags
    static final String rowScheduleTag = "zeile";
    static final String periodScheduleTag = "stunde";
    static final String dayScheduleTag = "tag";

    // meta tags
    static final String xmlStartTag = "?xml";

    public static String extractDateFromTitle(String title) {
        // needed to extract the mDate (format: dd:MM:yyyy)
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


    // extract the encoding from the first line of the xml doc
    // if no encoding is set, utf-8 will be used by standard
    private static String getFileEncoding(Context context, String filename) {
        String encoding;

        try {
            FileInputStream fileInputStream = context.openFileInput(filename);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

            String firstLine = bufferedReader.readLine();

            int start = firstLine.indexOf("encoding=") + 10; // +10 to actually start after the encoding string

            encoding = firstLine.substring(start, firstLine.indexOf("\"", start));

            bufferedReader.close();
        } catch (FileNotFoundException e) {
            Log.i(Constants.TAG, "Datei existiert nicht.");
            encoding = "UTF-8";
        } catch (IOException e) {
            Log.i(Constants.TAG, "IOExcpetion. Leere Datei?");
            encoding = "UTF-8";
        }

        return encoding;
    }

    public static List<Schoolday> parseSubstitutionXml(Context context) {
        List<Schoolday> result;
        result = new ArrayList<>();

        Schoolday currentDay = new Schoolday();
        Subject currentSubject = new Subject();
        List<Subject> currentSubjectList = new ArrayList<>();

        String filename = "substitution.xml";


        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            // open fis to the file to get the information
            FileInputStream fis = context.openFileInput(filename);

            // after extracting the encoding start the actual parsing
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis, getFileEncoding(context, filename)));

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
                        } else if (tag.equalsIgnoreCase(xmlStartTag)) {
                            Log.i(Constants.TAG, xpp.getAttributeValue(null, "encoding"));
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
                                    dateInMillis = Constants.dateFormatter.parse(dateString).getTime();
                                } catch (ParseException e) {
                                    Log.i(Constants.TAG, "ParseException", e);
                                }
                                currentDay.setDate(dateInMillis);
                                break;
                            case affectedClassesTag:
                                String[] affectedClasses = text.split(", ");
                                currentDay.setChanges(Arrays.asList(affectedClasses));
                                break;
                            case lastUpdatedTag:
                                long last_updated;
                                last_updated = 0;
                                // remove komma so parsing doesn't fail
                                text = text.replace(",", "");
                                try {
                                    last_updated = Constants.dateTimeFormatter.parse(text).getTime();

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                } catch (NullPointerException e) {
                                    Log.i(Constants.TAG, "NullPointer on .getTime(): ", e);
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
        } catch (XmlPullParserException e) {
            Log.i(TAG, "PullParserException in ", e);
        } catch (FileNotFoundException e) {
            Log.i(TAG, "File not found", e);
        } catch (IOException e) {
            Log.i(TAG, "IOException", e);
        } catch (NullPointerException e) {
            Log.i(TAG, "Some data is missing (NullPointer)", e);
        }

        return result;
    }


    // not needed right now
    public static List<Schoolday> parseScheduleXml(Context context) {

        Schoolday currentDay = new Schoolday();
        List<Schoolday> currentDayList = new ArrayList<>();
        Subject currentSubject = new Subject();
        List<Subject> currentSubjectList = new ArrayList<>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String classToShow = sharedPreferences.getString(Constants.CLASS_TO_SHOW, "");

        String filename = "aktuell" + classToShow + ".xml";

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            // open fis to the file to get the information
            FileInputStream fis = context.openFileInput(filename);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis, getFileEncoding(context, filename))); // set enconding!!

            // set input for the parser
            xpp.setInput(bufferedReader);

            // set starting event type
            int eventType = xpp.getEventType();

            String tag, period, text = "";
            int day = 0;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                tag = xpp.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        switch (tag) {
                            case dayScheduleTag + "{1,5}":
                                try {
                                    day = Integer.parseInt(tag.substring(3));
                                } catch (NumberFormatException e) {
                                    Log.e(Constants.TAG, "NumberFormatException when parsing day: ", e);
                                }
                                if (currentDayList.get(day - 1) == null) {
                                    currentDay = new Schoolday();
                                    currentSubjectList = new ArrayList<>();

                                    currentDay.setSubjects(currentSubjectList);
                                    currentDayList.add(day - 1, currentDay);

                                    currentSubject = new Subject();

                                }
                                break;

                        }

                        break;

                    case XmlPullParser.TEXT:
                        text = xpp.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        switch (tag) {
                            case periodScheduleTag:
                                period = text;
                                break;
                            case dayScheduleTag + "{1,5}":
                                currentSubjectList.add(currentSubject);
                        }
                        break;
                }
                eventType = xpp.next();
            }

        } catch (XmlPullParserException e) {
            Log.i(Constants.TAG, "XmlPullParserException in ScheduleParser", e);
        } catch (FileNotFoundException e) {
            Log.i(Constants.TAG, "FileNotFoudException in ScheduleParser", e);
        } catch (UnsupportedEncodingException e) {
            Log.i(Constants.TAG, "UnsupportedEncodingException in ScheduleParser", e);
        } catch (IOException e) {
            Log.i(Constants.TAG, "IOException in ScheduleParser", e);
        }

        return currentDayList;
    }
}