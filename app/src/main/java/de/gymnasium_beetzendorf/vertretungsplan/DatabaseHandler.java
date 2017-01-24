package de.gymnasium_beetzendorf.vertretungsplan;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.gymnasium_beetzendorf.vertretungsplan.data.Class;
import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;
import de.gymnasium_beetzendorf.vertretungsplan.data.Schoolday;
import de.gymnasium_beetzendorf.vertretungsplan.data1.Lesson;
import de.gymnasium_beetzendorf.vertretungsplan.data1.Subject;
import de.gymnasium_beetzendorf.vertretungsplan.data1.Substitution;
import de.gymnasium_beetzendorf.vertretungsplan.data1.SubstitutionDay;
import de.gymnasium_beetzendorf.vertretungsplan.data1.Teacher;


public class DatabaseHandler extends SQLiteOpenHelper implements Constants {
    private static final String TAG = DatabaseHandler.class.getSimpleName();

    // databse
    public static String DATABASE_NAME = "database.db";
    public static int DATABASE_VERSION = 5;

    // substitution table / coloumn names
    private static String TABLE_SUBSTITUTION = "substitution";

    private static String S_ID = "s_id";
    private static String S_ID_DAY = "s_id_day";
    private static String S_CLASS_YEAR = "s_class_year";
    private static String S_CLASS_LETTER = "s_class_letter";
    private static String S_CLASS_TYPE = "s_class_type";
    private static String S_PERIOD = "s_period";
    private static String S_SUBJECT = "s_subject";
    private static String S_TEACHER = "s_teacher";
    private static String S_ROOM = "s_room";
    private static String S_INFO = "s_info";
    private static String S_CHANGE = "s_change";

    // substitution days table / coloumn names
    private static String TABLE_SUBSTITUTION_DAYS = "substitution_days";

    private static String SD_ID = "sd_id";
    private static String SD_DATE = "sd_date";
    private static String SD_UPDATED = "sd_updated";
    private static String SD_SCHOOL = "sd_school";

    // lesson table / coloum names
    private static String TABLE_LESSON = "lesson";

    private static String L_ID = "l_id";
    private static String L_ID_DAY = "l_id_day";
    private static String L_CLASS_TYPE = "l_class_type";
    private static String L_PERIOD = "l_period";
    private static String L_SUBJECT = "l_subject";
    private static String L_TEACHER = "l_teacher";
    private static String L_ROOM = "l_room";

    // lesson days table / coloum names
    private static String TABLE_LESSON_DAYS = "lesson_days";

    private static String LD_ID = "ld_id";
    private static String LD_CLASS_YEAR_LETTER = "ld_class_year_letter";
    private static String LD_VALID = "ld_valid";

    // classlist table / coloum names
    private static String TABLE_CLASSLIST = "table_classlist";

    private static String CL_ID = "cl_id";
    private static String CL_NAME = "cl_name";
    private static String CL_URL = "cl_url";


    // stuff
    private Context context;
    private String query, date;
    private SharedPreferences sharedPreferences;
    private long dateInMillis, todayInMillis;

    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;

        init();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // substitution table
        query = "CREATE TABLE " + TABLE_SUBSTITUTION + " (" +
                S_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                S_ID_DAY + " INTEGER, " +
                S_CLASS_YEAR + " INTEGER, " +
                S_CLASS_LETTER + " TEXT, " +
                S_CLASS_TYPE + " TEXT, " +
                S_PERIOD + " INTEGER, " +
                S_SUBJECT + " INTEGER, " +
                S_TEACHER + " INTEGER, " +
                S_ROOM + " TEXT, " +
                S_INFO + " TEXT " +
                S_CHANGE + " TEXT " +
                ");";
        db.execSQL(query);

        // substitution days table
        query = "CREATE TABLE " + TABLE_SUBSTITUTION_DAYS + " (" +
                SD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SD_DATE + " FLOAT, " +
                SD_UPDATED + " FLOAT, " +
                SD_SCHOOL + " INTEGER " +
                ");";
        db.execSQL(query);

        // lesson table
        query = "CREATE TABLE " + TABLE_LESSON + " (" +
                L_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                L_ID_DAY + " INTEGER, " +
                L_CLASS_TYPE + " TEXT, " +
                L_PERIOD + " INTEGER, " +
                L_SUBJECT + " INTEGER, " +
                L_TEACHER + " INTEGER, " +
                L_ROOM + " TEXT" +
                ");";
        db.execSQL(query);

        // lesson day table
        query = "CREATE TABLE " + TABLE_LESSON_DAYS + " (" +
                LD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LD_CLASS_YEAR_LETTER + " TEXT, " +
                LD_VALID + " FLOAT " +
                ");";
        db.execSQL(query);

        // classlist table
        query = "CREATE TABLE IF NOT EXISTS " + TABLE_CLASSLIST + " (" +
                CL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CL_NAME + " TEXT, " +
                CL_URL + " TEXT" +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (newVersion) {
            case 5:
                query = "DROP TABLE IF EXISTS " + TABLE_SUBSTITUTION_DAYS;
                db.execSQL(query);
                query = "DROP TABLE IF EXISTS " + TABLE_SUBSTITUTION;
                db.execSQL(query);
                query = "DROP TABLE IF EXISTS " + TABLE_LESSON;
                db.execSQL(query);
                query = "DROP TABLE IF EXISTS " + TABLE_LESSON_DAYS;
                db.execSQL(query);
                query = "DROP TABLE IF EXISTS " + TABLE_CLASSLIST;
                db.execSQL(query);
                onCreate(db);
            case 6:
                query = "DROP TABLE IF EXISTS " + TABLE_SUBSTITUTION;
                db.execSQL(query);
            default:
                break;
        }
    }

    private void init() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar c = Calendar.getInstance();
        date = dateFormatter.format(c.getTime());
        try {
            dateInMillis = dateFormatter.parse(date).getTime();
        } catch (ParseException e) {
            Log.e(TAG, "ParseException in init():", e);
        }


    }

    public List<SubstitutionDay> getSubstitutionDayList(int school, int classYear, String classLetter) {
        if (classYear == 0 && classLetter.equalsIgnoreCase("")) {
            query = "SELECT * FROM " + TABLE_SUBSTITUTION_DAYS + " WHERE " + SD_SCHOOL + " = " + school;
        } else {
            query = "SELECT * FROM " + TABLE_SUBSTITUTION_DAYS + " WHERE " + SD_DATE + " >= " + dateInMillis + " AND " + SD_SCHOOL + " = " + school;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<SubstitutionDay> result = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                //Log.i(TAG, "sd_id: " + cursor.getInt(0) + "\nsd_date: " + cursor.getLong(1) + "\nsd_updated: " + cursor.getLong(2) + "\nsd_school: " + cursor.getInt(3));
                SubstitutionDay substitutionDay = new SubstitutionDay();
                substitutionDay.setDate(cursor.getLong(2));
                substitutionDay.setUpdated(cursor.getLong(3));
                substitutionDay.setSubstitutionList(getSubstitutionListByDayId(cursor.getInt(0), classYear, classLetter));
                substitutionDay.setSchool(school);
                result.add(substitutionDay);
            } while (cursor.moveToNext());
        }

        cursor.close();

        Log.i(TAG, "Menge der auszugebenden Vertretungen: " + result.get(0).getSubstitutionList().size());
        return result;

    }

    private List<Substitution> getSubstitutionListByDayId(int dayId, int classYear, String classLetter, String... classTypes) {
        query = "SELECT * FROM " + TABLE_SUBSTITUTION + " WHERE " + S_ID_DAY + " = " + dayId + " ";
        if (classYear != 0 || !classLetter.equalsIgnoreCase("")) {
            query += "AND " + S_CLASS_YEAR + " = " + classYear + " AND " + S_CLASS_LETTER + " = '" + classLetter + "'";
        }
        if (classTypes.length > 0) {
            query += " AND " + L_CLASS_TYPE + " IN(";
            for (String classType : classTypes) {
                query += "'" + classType + "', ";
            }
            query = query.substring(0, query.length() - 1); // delete last comma
            query += ")";
        }
        query += ";";
        Log.i(TAG, "query string for getSubstitutionList: " + query);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<Substitution> result = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                Substitution substitution = new Substitution();
                substitution.setClassYearLetter(cursor.getInt(3) + " " + cursor.getString(4));
                substitution.setClassCourse(cursor.getString(5));
                substitution.setPeriod(cursor.getInt(6));
                substitution.setSubject(Subject.getSubjectIdBySubjectShort(cursor.getString(7)));
                substitution.setTeacher(Teacher.getTeacherIdByTeacherShort(cursor.getString(8)));
                substitution.setRoom(cursor.getString(9));
                substitution.setInfo(cursor.getString(10));
                result.add(substitution);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return result;
    }

    public List<Schoolday> getFullSchedule() {
        return null;
    }

    private List<Lesson> getScheduleLessonsByDay() {
        return null;
    }

    void insertSubstitutionResults(int school, List<SubstitutionDay> results) {
        Log.i(TAG, "Menge der Ergebnisse: " + results.size());
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor;
        ContentValues cv = new ContentValues();
        for (int i = 0; i < results.size(); i++) {
            query = "SELECT " + SD_ID + " FROM " + TABLE_SUBSTITUTION_DAYS + " WHERE " + SD_DATE + " = " + results.get(i).getDate() + " AND " + SD_SCHOOL + " = " + school;
            cursor = db.rawQuery(query, null);
            cursor.moveToFirst();

            cv.clear();
            cv.put(SD_UPDATED, results.get(i).getUpdated());

            if (cursor.getCount() == 0) {
                cv.put(SD_DATE, results.get(i).getDate());
                cv.put(SD_SCHOOL, school);
                db.insert(TABLE_SUBSTITUTION_DAYS, null, cv);
            } else {
                db.update(TABLE_SUBSTITUTION_DAYS, cv, SD_ID + " = " + cursor.getInt(0), null);
            }

            cursor = db.rawQuery(query, null);
            cursor.moveToFirst();

            // old data is deleted and then the new data is reinserted. no check if the data is actually different

            db.delete(TABLE_SUBSTITUTION, S_ID_DAY + " = " + cursor.getInt(0), null);

            for (int j = 0; j < results.get(i).getSubstitutionList().size(); j++) {
                Substitution substitution = results.get(i).getSubstitutionList().get(j);
                cv.clear();
                cv.put(S_ID_DAY, cursor.getInt(0));
                cv.put(S_CLASS_YEAR, substitution.getClassYearLetter().substring(0, 2));
                cv.put(S_CLASS_LETTER, substitution.getClassYearLetter().substring(3));
                cv.put(S_CLASS_TYPE, substitution.getClassCourse());
                cv.put(S_PERIOD, substitution.getPeriod());
                cv.put(S_SUBJECT, substitution.getSubject());
                cv.put(S_TEACHER, substitution.getTeacher());
                cv.put(S_ROOM, substitution.getRoom());
                cv.put(S_INFO, substitution.getInfo());

                db.insert(TABLE_SUBSTITUTION, null, cv);
            }

            cursor.close();
        }


    }

    private void cleanSubstitutionDatabase() {

    }


    /*
    public boolean isUpToDate(String date, Long last_updated) {
        SQLiteDatabase db = getReadableDatabase();
        Boolean is_up_to_date;
        Cursor cursor;
        query = "SELECT * FROM " + TABLE_SUBSTITUTION_DAYS + " WHERE " + SD_DATE + " = '" + date + "'";
        cursor = db.rawQuery(query, null);

        is_up_to_date = last_updated <= cursor.getLong(cursor.getColumnIndex(SD_LAST_UPDATED));

        cursor.close();
        return is_up_to_date;
    }

    public List<Schoolday> getAllSubstitutions() { // return all substitutions from a certain mDate on
        SQLiteDatabase db = getReadableDatabase();
        List<Schoolday> schooldayList = new ArrayList<>();
        Cursor cursor;

        long time = 0;
        try {
            time = dateFormatter.parse(date).getTime();
        } catch (ParseException e) {
            Log.i(TAG, "ParseException", e);
        }

        boolean after3Pm = after3Pm();

        if (after3Pm) {
            query = "SELECT * FROM " + TABLE_SUBSTITUTION_DAYS + " WHERE " + SD_DATE + " > '" + time + "'";

        } else {
            // original query for working app
            query = "SELECT * FROM " + TABLE_SUBSTITUTION_DAYS + " WHERE " + SD_DATE + " >= '" + time + "'";
            // demo query for testing
            //query = "SELECT * FROM " + TABLE_SUBSTITUTION_DAYS + " WHERE " + SD_DATE + " <= '" + time + " LIMIT(0,5)'";

        }
        cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                //Log.i(MainActivity.TAG, "current position: " + String.valueOf(cursor.getPosition()));
                Schoolday currentSchoolday = new Schoolday();
                currentSchoolday.setDate(cursor.getLong(1));
                currentSchoolday.setLastUpdated(cursor.getLong(2));
                currentSchoolday.setSubjects(getSubstitutions(cursor.getInt(0)));

                // only add currentSchoolday if it contains any subjects
                if (currentSchoolday.getSubjects().size() > 0) {
                    schooldayList.add(currentSchoolday);
                }
            } while (cursor.moveToNext());
        }

        //Log.i(MainActivity.TAG, "größe des schooldaylist arrays: " + String.valueOf(schooldayList.size()));

        cursor.close();
        db.close();
        Log.i(TAG, "Größe der Liste: " + schooldayList.size());
        return schooldayList;
    }

    public List<Subject> getSubstitutions(int id) { // return substitutions for a certain day
        SQLiteDatabase db = getReadableDatabase();
        List<Subject> subjectList = new ArrayList<>();
        Cursor cursor;
        String classToShow, classToShowPrimary, classToShowSecondary;

        // specify the results needed depending on the settings
        if (sharedPreferences.getBoolean(PREFERENCE_SHOW_WHOLE_PLAN, true)) {
            query = "SELECT * FROM " + TABLE_SUBSTITUTION_ROWS + " WHERE " + SR_DAY + " = '" + id + "'";
        } else {
            classToShow = sharedPreferences.getString(CLASS_TO_SHOW, null);
            if (classToShow != null) {
                classToShowPrimary = classToShow.substring(0, 2);
                classToShowSecondary = classToShow.substring(3, 4);
                // how it works right now (might change once the whole plan will be available through the app + specific course selection will be available
                // search for entries that definitely include the general class (05, 11, 12, etc). it necessarily checks if A/B/C is also included
                // if not, second condition (OR) is that the entry needs to be longer than 4 (-> all entries that have a specific course like 12 inf1

                query = "SELECT * FROM " + TABLE_SUBSTITUTION_ROWS + " WHERE " + SR_DAY + " = '" + id + "' " +
                        "AND " + SR_COURSE + " LIKE '%" + classToShowPrimary + "%' " +
                        "AND (" + SR_COURSE + " LIKE '%" + classToShowSecondary + "' " +
                        "OR length(" + SR_COURSE + ") > 4)" +
                        "ORDER BY " + SR_PERIOD;
            } else {
                query = "SELECT * FROM " + TABLE_SUBSTITUTION_ROWS + " WHERE " + SR_DAY + " = '" + id + "'";
            }
        }

        // query all subjects for a certain mDate
        cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Subject currentSubject = new Subject();
                currentSubject.setCourse(cursor.getString(2));
                currentSubject.setPeriod(cursor.getInt(3));
                currentSubject.setSubject(cursor.getString(4));
                currentSubject.setTeacher(cursor.getString(5));
                currentSubject.setRoom(cursor.getString(6));
                currentSubject.setInfo(cursor.getString(7));
                subjectList.add(currentSubject);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return subjectList;
    }

    public void insertSubstitutionXmlResults(List<Schoolday> results) {
        SQLiteDatabase db = getWritableDatabase();
        Schoolday currentDay;
        Cursor cursor;
        for (int n = 0; n < results.size(); n++) {
            currentDay = results.get(n);
            query = "SELECT * FROM " + TABLE_SUBSTITUTION_DAYS + " WHERE " + SD_DATE + " = '" + currentDay.getDate() + "'";
            cursor = db.rawQuery(query, null);
            cursor.moveToFirst();

            ContentValues values;
            if (cursor.getCount() == 0) {
                values = new ContentValues();
                values.put(SD_DATE, currentDay.getDate());
                values.put(SD_LAST_UPDATED, currentDay.getLastUpdated());
                values.put(SD_PAST, false);
                db.insert(TABLE_SUBSTITUTION_DAYS, null, values);
            } else if (cursor.getCount() == 1) {
                values = new ContentValues();
                int id = cursor.getInt(cursor.getColumnIndex(SD_ID));
                // updating table days
                values.put(SD_LAST_UPDATED, currentDay.getLastUpdated());
                db.update(TABLE_SUBSTITUTION_DAYS, values, SD_ID + " = ?", new String[]{String.valueOf(id)});
            }

            query = "SELECT * FROM " + TABLE_SUBSTITUTION_DAYS + " WHERE " + SD_DATE + " = '" + currentDay.getDate() + "'";
            cursor = db.rawQuery(query, null);
            cursor.moveToFirst();
            int id = cursor.getInt(cursor.getColumnIndex(SD_ID));

            // clear corresponding table rows and then re-add them
            // subjects need to be deleted everytime because we don't want old results in the database that aren't valid anymore
            // --> the way how the xml file is set up -> there is no indicator if a substitution lost its validity
            db.delete(TABLE_SUBSTITUTION_ROWS, SR_DAY + " = ?", new String[]{String.valueOf(id)});
            Subject currentSubject;
            for (int i = 0; i < currentDay.getSubjects().size(); i++) {
                currentSubject = currentDay.getSubjects().get(i);
                values = new ContentValues();
                values.put(SR_DAY, id);
                values.put(SR_COURSE, currentSubject.getCourse());
                values.put(SR_PERIOD, currentSubject.getPeriod());
                values.put(SR_ROOM, currentSubject.getRoom());
                values.put(SR_SUBJECT, currentSubject.getSubject());
                values.put(SR_TEACHER, currentSubject.getTeacher());
                values.put(SR_INFO, currentSubject.getInfo());
                db.insert(TABLE_SUBSTITUTION_ROWS, null, values);
            }
            cursor.close();
        }

        Log.i(TAG, "wieviel ist jetzt in der db - " + getAllSubstitutions().size());
        db.close();
    }

    public boolean after3Pm() {
        // check if current day is needed or if it's passt 3 pm that day
        long currentTime = System.currentTimeMillis();
        long afterSchoolTime = 0;

        try {
            afterSchoolTime = dateTimeFormatter.parse(date + " 15:00").getTime();
        } catch (ParseException e) {
            Log.i(TAG, "ParseException for afterSchoolTime", e);
        }

        // today no longer needs to be displayed
        return afterSchoolTime != 0 && currentTime > afterSchoolTime;
    }

    */

    void emptyClassListTable() {
        SQLiteDatabase db = getWritableDatabase();

        query = "DELETE FROM " + TABLE_CLASSLIST;
        db.execSQL(query);
    }

    void updateClassList(List<Class> classList) {
        SQLiteDatabase db = getWritableDatabase();

        if (classList.size() > 0) {
            emptyClassListTable();

            for (int i = 0; i < classList.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(CL_ID, i);
                values.put(CL_NAME, classList.get(i).getName());
                values.put(CL_URL, classList.get(i).getUrl());
                db.insert(TABLE_CLASSLIST, null, values);
            }
        }

        db.close();
    }

    public List<String> getClassList() {
        SQLiteDatabase db = getWritableDatabase();

        String query = "SELECT " + CL_NAME + " FROM " + TABLE_CLASSLIST;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        List<String> classList = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            classList.add(cursor.getString(cursor.getColumnIndex(CL_NAME)).substring(7));
            cursor.moveToNext();
        }

        cursor.close();
        return classList;
    }
}
