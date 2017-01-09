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
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;
import de.gymnasium_beetzendorf.vertretungsplan.data1.Lesson;
import de.gymnasium_beetzendorf.vertretungsplan.data1.School;
import de.gymnasium_beetzendorf.vertretungsplan.data1.Subject;
import de.gymnasium_beetzendorf.vertretungsplan.data1.Substitution;
import de.gymnasium_beetzendorf.vertretungsplan.data1.SubstitutionDay;
import de.gymnasium_beetzendorf.vertretungsplan.data1.Teacher;

class XmlParser implements Constants {

    private final String TAG = XmlParser.class.getSimpleName();

    private String type;
    private Context context;
    private String filename;
    private String classToShow;
    private int school;
    private FileInputStream fileInputStream = null;
    private BufferedReader bufferedReader = null;
    private BufferedReader tempBufferedReader = null;

    // meta tags
    static final String xmlStartTag = "?xml";

    static final String SUBSTITUTION = "substitution";
    static final String SCHEDULE = "schedule";

    XmlParser(Context context, String type) {
        this.context = context;
        this.type = type;
        init();
    }


    private void init() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.school = sharedPreferences.getInt("school", 1);

        switch (type) {
            case SCHEDULE:
                this.classToShow = sharedPreferences.getString("class_to_show", "");
                this.filename = "aktuell" + this.classToShow + ".xml";
                break;
            case SUBSTITUTION:
                Log.i(TAG, "Schule: " + String.valueOf(school));
                this.filename = "substitution_" + String.valueOf(school) + ".xml";
                break;
        }

        try {
            fileInputStream = context.openFileInput(filename);
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            tempBufferedReader = bufferedReader;
        } catch (IOException e) {
            Log.e(TAG, "missing file here: ", e);
        }

        //setFileEncoding();
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

    // extract the encoding from the first line of the xml doc
    // if no encoding is set, utf-8 will be used by standard
    private void setFileEncoding() {
        try {
            bufferedReader = tempBufferedReader;
            String firstLine = bufferedReader.readLine();
            int start = firstLine.indexOf("encoding=") + 10; // +10 to actually start after the encoding string
            String encoding = firstLine.substring(start, firstLine.indexOf("\"", start));

            this.bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            this.tempBufferedReader = bufferedReader;
        } catch (IOException e) {
            Log.e(TAG, "problem with file encoding ", e);
        }
    }

    List<SubstitutionDay> parseReturnSubstitution() {
        SubstitutionDay currentSubstitutionDay = new SubstitutionDay();
        List<SubstitutionDay> results = new ArrayList<>();
        Substitution currentSubstitution = new Substitution();
        List<Substitution> currentSubstitutionList = new ArrayList<>();
        int multipleClasses = 0, multiplePeriods = 0;

        try {
            XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
            xmlPullParser.setInput(bufferedReader);

            int eventType = xmlPullParser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String text = "";
                String tag;
                String attributeName = "";
                String attributeValue = "";
                String[] tempStringArray;

                tag = xmlPullParser.getName();
                switch (eventType) {
                    case XmlPullParser.TEXT:
                        text = xmlPullParser.getText();
                        break;

                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getAttributeCount() > 0) {
                            attributeName = xmlPullParser.getAttributeName(0);
                            attributeValue = xmlPullParser.getAttributeValue(0);
                        }
                        switch (tag) {
                            case "kopf":
                                currentSubstitutionDay = new SubstitutionDay();
                                break;
                            case "aktion":
                                currentSubstitution = new Substitution();
                                multipleClasses = 0;
                                multiplePeriods = 0;
                                break;
                            case "fach":
                                if (attributeName.equalsIgnoreCase("fageandert") && attributeValue.equalsIgnoreCase("ae")) {
                                    currentSubstitution.setChanges(currentSubstitution.getChanges() + "subject|");
                                }
                                break;
                            case "lehrer":
                                if (attributeName.equalsIgnoreCase("legeandert") && attributeValue.equalsIgnoreCase("ae")) {
                                    currentSubstitution.setChanges(currentSubstitution.getChanges() + "teacher|");
                                }
                                break;
                            case "raum":
                                if (attributeName.equalsIgnoreCase("rageandert") && attributeValue.equalsIgnoreCase("ae")) {
                                    currentSubstitution.setChanges(currentSubstitution.getChanges() + "room|");
                                }
                                break;
                        }
                        break;


                    case XmlPullParser.END_TAG:
                        switch (tag) {
                            // all data stored as header data
                            case "titel":
                                tempStringArray = text.split("[,(]");
                                Log.i(TAG, "text: " + text + "\narray[1]: " + tempStringArray[0]);
                                String weekString = tempStringArray[2].substring(0, 1);

                                String dateString = tempStringArray[1].trim();
                                long date = dateFormatter.parse(dateString).getTime();

                                currentSubstitutionDay.setDate(date);
                                currentSubstitutionDay.setWeek(weekString);
                                break;
                            case "schulname":
                                int id = text.isEmpty() ? 0 : School.findSchoolIdByName(text);
                                currentSubstitutionDay.setSchool(id);
                                break;
                            case "datum":
                                long updated = dateTimeFormatter.parse(text).getTime();
                                currentSubstitutionDay.setUpdated(updated);
                                break;
                            // all data that is actual substitution information
                            case "klasse":
                                if (text.length() == 4) {
                                    currentSubstitution.setClassYearLetter(text);
                                    currentSubstitution.setClassCourse("");
                                } else {
                                    if (text.contains("/")) {
                                        String course = text.substring(text.indexOf('/'));
                                        currentSubstitution.setClassCourse(course.trim());
                                    }
                                    String classes = text.substring(0, text.indexOf('/'));
                                    if (text.length() > 10) {
                                        String[] rangeClasses = {classes.substring(0, 4), classes.substring(5, 9)};
                                        currentSubstitution.setClassYearLetter(rangeClasses[0]);
                                        String[] rangeClassesLetter = {rangeClasses[0].substring(3, 4), rangeClasses[1].substring(3, 4)};
                                        multipleClasses = (int) rangeClassesLetter[1].charAt(0) - (int) rangeClassesLetter[0].charAt(0);
                                    } else {
                                        classes = classes.substring(0, 4);
                                        currentSubstitution.setClassYearLetter(classes);
                                    }
                                }
                                break;
                            case "stunde":
                                int periods = Integer.parseInt(text.replaceAll("-", ""));
                                currentSubstitution.setPeriod(periods);
                                if (String.valueOf(periods).length() > 1) {
                                    multiplePeriods = (int) String.valueOf(periods).charAt(0) - (int) String.valueOf(periods).charAt(2);
                                }
                                break;
                            case "fach":
                                if (!text.equalsIgnoreCase("")) {
                                    currentSubstitution.setSubject(Subject.getSubjectIdBySubjectShort(text));
                                }
                                break;
                            case "lehrer":
                                if (!text.equalsIgnoreCase("")) {
                                    String teacherString = text.replaceAll("[()]", "");
                                    currentSubstitution.setTeacher(Teacher.getTeacherIdByTeacherShort(teacherString));
                                } else {
                                    currentSubstitution.setTeacher(-1);
                                }
                                break;
                            case "raum":
                                currentSubstitution.setRoom(text);
                                break;
                            case "info":
                                currentSubstitution.setInfo(text);
                                break;

                            case "haupt":
                                Substitution temp = currentSubstitution;
                                for (int i = 0; i <= multiplePeriods; i++) {
                                    currentSubstitution.setPeriod(temp.getPeriod() + i);
                                    currentSubstitutionList.add(currentSubstitution);
                                }
                                for (int i = 0; i <= multipleClasses; i++) {
                                    int letter = temp.getClassYearLetter().charAt(3);
                                    currentSubstitution.setClassYearLetter(temp.getClassYearLetter().replace(Character.toChars(letter)[0], Character.toChars(letter + i)[0]));
                                    currentSubstitutionList.add(currentSubstitution);
                                }
                                currentSubstitutionDay.setSubstitutionList(currentSubstitutionList);
                                break;
                        }
                        break;

                }
                eventType = xmlPullParser.next();
            }
        } catch (IOException | XmlPullParserException e) {
            Log.e(TAG, "Fehler beim Parsen/IO: ", e);
        } catch (ParseException e) {
            Log.e(TAG, "ParseException: ", e);
        } catch (IllegalFormatException e) {
            Log.e(TAG, "No school with this name found!", e);
        }

        Log.i(TAG, "finished parsing, result size: " + String.valueOf(results.size()));
        //finish();
        return results;
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
}