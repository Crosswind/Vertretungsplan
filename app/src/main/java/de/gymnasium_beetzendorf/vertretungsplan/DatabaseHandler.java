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
import de.gymnasium_beetzendorf.vertretungsplan.data.Subject;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHandler";
    // databse
    public static String DATABASE_NAME = "database.db";
    public static int DATABASE_VERSION = 3;

    // table substitution_days
    private static String TABLE_SUBSTITUTION_DAYS = "substitution_days";
    private static String SD_ID = "id";
    private static String SD_DATE = "Date";
    private static String SD_LAST_UPDATED = "last_updated";
    private static String SD_PAST = "past";

    // table substitutions_rows
    private static String TABLE_SUBSTITUTION_ROWS = "substitution_rows";
    private static String SR_ID = "id";
    private static String SR_DAY = "day";
    private static String SR_COURSE = "course";
    private static String SR_PERIOD = "period";
    private static String SR_SUBJECT = "subject";
    private static String SR_TEACHER = "teacher";
    private static String SR_ROOM = "room";
    private static String SR_INFO = "info";

    // classlist
    private static String TABLE_CLASSLIST = "classlist";
    private static String CL_ID = "id";
    private static String CL_NAME = "name";
    private static String CL_URL = "url";

    // stuff
    private String query, date;
    private SharedPreferences sharedPreferences;


    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        Calendar c = Calendar.getInstance();
        date = Constants.dateFormatter.format(c.getTime());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creation of substitution days
        query = "CREATE TABLE " + TABLE_SUBSTITUTION_DAYS + " (" +
                SD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SD_DATE + " FLOAT, " +
                SD_LAST_UPDATED + " FLOAT, " +
                SD_PAST + " BOOLEAN" +
                ");";
        db.execSQL(query);

        // creation of substitution rows
        query = "CREATE TABLE " + TABLE_SUBSTITUTION_ROWS + " (" +
                SR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SR_DAY + " INTEGER, " +
                SR_COURSE + " TEXT, " +
                SR_PERIOD + " INTEGER, " +
                SR_SUBJECT + " TEXT, " +
                SR_TEACHER + " TEXT, " +
                SR_ROOM + " TEXT, " +
                SR_INFO + " TEXT" +
                ");";
        db.execSQL(query);

        // create classlist table
        query = "CREATE TABLE " + TABLE_CLASSLIST + " (" +
                CL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CL_NAME + " TEXT, " +
                CL_URL + " TEXT" +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        query = "DROP TABLE IF EXISTS " + TABLE_SUBSTITUTION_DAYS;
        db.execSQL(query);
        query = "DROP TABLE IF EXISTS " + TABLE_SUBSTITUTION_ROWS;
        db.execSQL(query);
        query = "DROP TABLE IF EXISTS " + TABLE_CLASSLIST;
        db.execSQL(query);
        onCreate(db);
    }

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
            time = Constants.dateFormatter.parse(date).getTime();
        } catch (ParseException e) {
            Log.i(Constants.TAG, "ParseException", e);
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
        Log.i(Constants.TAG, "Größe der Liste: " + schooldayList.size());
        return schooldayList;
    }

    public List<Subject> getSubstitutions(int id) { // return substitutions for a certain day
        SQLiteDatabase db = getReadableDatabase();
        List<Subject> subjectList = new ArrayList<>();
        Cursor cursor;
        String classToShow, classToShowPrimary, classToShowSecondary;

        // specify the results needed depending on the settings
        if (sharedPreferences.getBoolean(Constants.SHOW_WHOLE_PLAN, true)) {
            query = "SELECT * FROM " + TABLE_SUBSTITUTION_ROWS + " WHERE " + SR_DAY + " = '" + id + "'";
        } else {
            classToShow = sharedPreferences.getString(Constants.CLASS_TO_SHOW, null);
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

        Log.i(Constants.TAG, "wieviel ist jetzt in der db - " + getAllSubstitutions().size());
        db.close();
    }

    public boolean after3Pm() {
        // check if current day is needed or if it's passt 3 pm that day
        long currentTime = System.currentTimeMillis();
        long afterSchoolTime = 0;

        try {
            afterSchoolTime = Constants.dateTimeFormatter.parse(date + " 15:00").getTime();
        } catch (ParseException e) {
            Log.i(TAG, "ParseException for afterSchoolTime", e);
        }

        // today no longer needs to be displayed
        return afterSchoolTime != 0 && currentTime > afterSchoolTime;
    }

    public void emptyClassListTable() {
        SQLiteDatabase db = getWritableDatabase();

        query = "DELETE FROM " + TABLE_CLASSLIST;
        db.execSQL(query);
    }

    public void updateClassList(List<Class> classList) {
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
        for(int i = 0; i < cursor.getCount(); i++) {
            classList.add(cursor.getString(cursor.getColumnIndex(CL_NAME)).substring(7));
            cursor.moveToNext();
        }

        cursor.close();
        return classList;
    }

    public boolean cleanDatabase() {
        // TODO: implement method to delete all rows older than 30 days
        return true;
    }

    public boolean markAsPast() {
        // TODO: implement method to mark all past days to avoid using them again
        return true;
    }
}
