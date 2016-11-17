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
import de.gymnasium_beetzendorf.vertretungsplan.data.Lesson;
import de.gymnasium_beetzendorf.vertretungsplan.data.School;
import de.gymnasium_beetzendorf.vertretungsplan.data.Schoolday;
import de.gymnasium_beetzendorf.vertretungsplan.data.Subject;

class XmlParser implements Constants {

    final String TAG = XmlParser.class.getSimpleName();

    private String type;
    private Context context;
    private String filename;
    private String classToShow;
    private int school;
    private FileInputStream fileInputStream = null;
    private BufferedReader bufferedReader = null;
    private BufferedReader tempBufferedReader = null;

    // schedule xml tags
    static final String headerTag = "kopf"; // header for timetable
    static final String dateTag = "titel"; // mDate needs to be extracted
    static final String schoolTag = "schulname";
    static final String affectedClassesTag = "aenderungk"; // shows affected classes
    static final String lastUpdatedTag = "datum";

    static final String timetableTag = "haupt";
    static final String changeTag = "aktion"; // contains the timetable
    static final String yearClassletterTag = "klasse"; // contains a change
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

    static final String XMLPARSER_TYPE_SUBSTITUTION = "substitution";
    static final String XMLPARSER_TYPE_SCHEDULE = "schedule";

    XmlParser(Context context, String type) {
        this.context = context;
        this.type = type;

        init();
    }


    private void init() {

        switch (type) {
            case "schedule":
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                this.classToShow = sharedPreferences.getString("class_to_show", "");
                this.school = sharedPreferences.getInt("school", 0);
                this.filename = "aktuell" + this.classToShow + ".xml";
                break;
            case "substitution":
                this.filename = "substitution_" + String.valueOf(school) + ".xml";
                break;
        }

        try {
            fileInputStream = context.openFileInput(filename);
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            tempBufferedReader = bufferedReader;
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: add method to try to redownload the missing file
        }

        setFileEncoding();
    }


    public void parse() {
        switch (type) {
            case "schedule":

                break;
            case "substitution":

                break;
        }

        finish();
    }


    private void finish() {
        try {
            fileInputStream.close();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static int extractWeekdayCountFromTitle(String title) {

        String[] weekdays = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag"};

        String[] holder = title.split(",");

        int weekday = 0;

        for (int n = 0; n < weekdays.length; n++) {
            if (holder[0].equalsIgnoreCase(weekdays[n])) {
                weekday = n;
                break;
            }
        }

        return weekday + 1;
    }


    private long extractDateFromTitle(String title) {
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

        long dateInMillis = 0;
        try {
            dateFormatter.parse(dateString).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateInMillis;
    }


    // extract the encoding from the first line of the xml doc
    // if no encoding is set, utf-8 will be used by standard
    private void setFileEncoding() {
        try {
            bufferedReader = tempBufferedReader;
            String firstLine = bufferedReader.readLine();
            int start = firstLine.indexOf("encoding=") + 10; // +10 to actually start after the encoding string
            String encoding = firstLine.substring(start, firstLine.indexOf("\"", start));

            this.bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, encoding));
            this.tempBufferedReader = bufferedReader;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    List<Lesson> parseReturnSubstitution() {
        Lesson headerData = null, current = new Lesson();
        List<Lesson> result = new ArrayList<>();

        try {
            XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
            //bufferedReader = new BufferedReader(new InputStreamReader(context.openFileInput(filename)));
            xmlPullParser.setInput(bufferedReader);

            int eventType = xmlPullParser.getEventType();

            String text = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tag = xmlPullParser.getName();
                Log.i(TAG, "start parsing substitution - tag: " + tag);

                switch (eventType) {

                    case XmlPullParser.START_TAG:
                        switch (tag) {
                            case headerTag:
                                Log.i(Constants.TAG, "parsing header");
                                headerData = parseSubstitutionHeader();
                                Log.i(Constants.TAG, "finished header parsing");
                                break;
                            case changeTag:
                                current = headerData;
                                break;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = xmlPullParser.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        switch (tag) {
                            case changeTag:
                                result.add(current);
                                break;
                            case yearClassletterTag:
                                current.setYear(text.substring(0, 2));
                                current.setClassletter(text.substring(3));
                                break;
                            case periodTag:
                                current.setPeriod(Integer.valueOf(text));
                                break;
                            case subjectTag:
                                current.setSubject(text);
                                break;
                            case teacherTag:
                                current.setTeacher(text);
                                break;
                            case roomTag:
                                current.setRoom(text);
                                break;
                            case infoTag:
                                current.setInfo(text);
                                break;
                        }
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (IOException | XmlPullParserException e) {
            Log.i(TAG, "Fehler beim Parsen/IO: ", e);
        }

        Log.i(TAG, "finished parsing, result size: " + String.valueOf(result.size()));
        return result;
    }


    private Lesson parseSubstitutionHeader() {
        Lesson result = new Lesson();

        try {
            XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
            xmlPullParserFactory.setNamespaceAware(true);
            XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();

            bufferedReader = tempBufferedReader;
            xmlPullParser.setInput(bufferedReader);


            int eventType = xmlPullParser.getEventType();
            // now actually loop through the data

            String text = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tag = xmlPullParser.getName();

                switch (eventType) {

                    case XmlPullParser.TEXT:
                        text = xmlPullParser.getText();
                        Log.i(Constants.TAG, "tag: " + tag + "\ntext: " + text);
                        break;
                    case XmlPullParser.END_TAG:
                        switch (tag) {
                            case dateTag:
                                result.setType("substitution");
                                result.setDay(extractWeekdayCountFromTitle(text));
                                result.setValid_on(extractDateFromTitle(text));
                                break;
                            case schoolTag:
                                try {
                                    result.setSchool(School.findSchoolByName(text).getId());
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                    // TODO: add method to inform that the school hasn't been added
                                }
                                break;
                        }
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            Log.i(Constants.TAG, "exception in header parsing", e);
            e.printStackTrace();
        }

        Log.i(Constants.TAG, "end of header parsing, result type: " + result.getType());

        return result;
    }


    public List<Lesson> parseReturnSchedule() {
        Lesson headerData = parseScheduleHeader();

        return null;
    }


    private Lesson parseScheduleHeader() {
        Lesson result = new Lesson();

        try {
            XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
            bufferedReader = tempBufferedReader;
            xmlPullParser.setInput(bufferedReader);

            int eventType = xmlPullParser.getEventType();
            // now actually loop through the data


        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        return result;
    }


    public List<Schoolday> parseSubstitutionXml(Context context) {
        List<Schoolday> result;
        result = new ArrayList<>();

        Schoolday currentDay = new Schoolday();
        Subject currentSubject = new Subject();
        List<Subject> currentSubjectList = new ArrayList<>();


        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            // reset the buffered reader because it might have been used before
            bufferedReader = tempBufferedReader;

            // set input for the parser
            xpp.setInput(bufferedReader);

            // set starting event type
            int eventType = xpp.getEventType();

            // go
            String text = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tag = xpp.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tag.equalsIgnoreCase(headerTag)) {
                            currentDay = new Schoolday();
                            currentSubjectList = new ArrayList<>();
                        } else if (tag.equalsIgnoreCase(changeTag)) {
                            currentSubject = new Subject();
                        } else if (tag.equalsIgnoreCase(xmlStartTag)) {
                            Log.i(TAG, xpp.getAttributeValue(null, "encoding"));
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
                                    last_updated = dateTimeFormatter.parse(text).getTime();

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                } catch (NullPointerException e) {
                                    Log.i(TAG, "NullPointer on .getTime(): ", e);
                                }
                                currentDay.setLastUpdated(last_updated);
                                break;
                            case yearClassletterTag:
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
        } catch (IOException e) {
            Log.i(TAG, "IOException", e);
        } catch (NullPointerException e) {
            Log.i(TAG, "Some data is missing (NullPointer)", e);
        }

        return result;
    }


    // not needed right now
    public List<Lesson> parseScheduleXml(Context context) {

        List<Lesson> results = new ArrayList<>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String classToShow = sharedPreferences.getString(CLASS_TO_SHOW, "");

        String filename = "aktuell" + classToShow + ".xml";

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            // set input for the parser
            xpp.setInput(bufferedReader);

            // set starting event type
            int eventType = xpp.getEventType();

            String tag, period, text = "";

            // get

            final String schedTitleTag = "titel";
            final String schedSchoolTag = "schulname";
            final String schedValidFromTag = "gueltigab";

            final String schedRowTag = "zeile";
            final String schedPeriodTag = "stunde";

            final String schedDayTag = "tag"; // tag follows a number indicating which day we on
            final String schedSubjectTag = "fach";
            final String schedYearClassTag = "klasse";
            final String schedRoomTag = "raum";

            String schedYear = "", schedClassletter = "";
            long schedValidFrom = 0;
            int schedDay, schedSchool = 0, schedPeriod = 0;

            Lesson currentLesson = new Lesson();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                tag = xpp.getName();


                switch (eventType) {


                    case XmlPullParser.START_TAG:
                        switch (tag) {
                            case schedDayTag + "{1,5}":
                                schedDay = Integer.getInteger(tag.substring(3));
                                currentLesson = new Lesson("schedule", schedYear, schedClassletter, schedSchool, schedValidFrom, schedDay);
                                currentLesson.setPeriod(schedPeriod);
                                break;
                        }

                        break;


                    case XmlPullParser.TEXT:
                        text = xpp.getText();
                        break;


                    case XmlPullParser.END_TAG:
                        switch (tag) {
                            case schedPeriodTag:
                                schedPeriod = Integer.getInteger(text);
                                break;
                            case schedTitleTag:
                                schedYear = text.substring(0, 2);
                                schedClassletter = text.substring(3);
                                break;
                            case schedSchoolTag:
                                School school = School.findSchoolByName(text); // check if data from enum exists
                                if (school != null) {
                                    schedSchool = school.getId();
                                }
                                break;
                            case schedValidFromTag:
                                schedValidFrom = dateFormatter.parse(text).getTime();
                                break;
                            case schedSubjectTag:
                                currentLesson.setSubject(text);
                                break;
                            case schedRoomTag:
                                currentLesson.setRoom(text);
                                break;
                            case schedDayTag + "{1,5}":
                                results.add(currentLesson);
                                break;
                        }
                        break;
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            Log.i(TAG, "XmlPullParserException in ScheduleParser", e);
        } catch (FileNotFoundException e) {
            Log.i(TAG, "FileNotFoudException in ScheduleParser", e);
        } catch (UnsupportedEncodingException e) {
            Log.i(TAG, "UnsupportedEncodingException in ScheduleParser", e);
        } catch (IOException e) {
            Log.i(TAG, "IOException in ScheduleParser", e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            Log.e(TAG, "ParseException while parsing validFrom date from schedule", e);
        }


        return results;
    }
}