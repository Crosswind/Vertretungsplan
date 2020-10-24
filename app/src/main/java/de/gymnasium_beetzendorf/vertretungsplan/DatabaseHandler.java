package de.gymnasium_beetzendorf.vertretungsplan;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import java.util.Objects;

import de.gymnasium_beetzendorf.vertretungsplan.data.Class;
import de.gymnasium_beetzendorf.vertretungsplan.data.Constants;
import de.gymnasium_beetzendorf.vertretungsplan.data.Substitution;
import de.gymnasium_beetzendorf.vertretungsplan.data.SubstitutionDay;


public class DatabaseHandler extends SQLiteOpenHelper implements Constants {
    private static final String TAG = DatabaseHandler.class.getSimpleName();

    // databse
    public static String DATABASE_NAME = "database.db";
    public static int DATABASE_VERSION = 8;

    // substitution table / column names
    private static final String TABLE_SUBSTITUTION = "substitution";

    private static final String S_ID = "s_id";
    private static final String S_ID_DAY = "s_id_day";
    private static final String S_CLASS_YEAR = "s_class_year";
    private static final String S_CLASS_LETTER = "s_class_letter";
    private static final String S_CLASS_TYPE = "s_class_type";
    private static final String S_PERIOD = "s_period";
    private static final String S_SUBJECT = "s_subject";
    private static final String S_TEACHER = "s_teacher";
    private static final String S_ROOM = "s_room";
    private static final String S_INFO = "s_info";
    private static final String S_CHANGE = "s_change";

    // substitution days table / column names
    private static final String TABLE_SUBSTITUTION_DAYS = "substitution_days";

    private static final String SD_ID = "sd_id";
    private static final String SD_DATE = "sd_date";
    private static final String SD_UPDATED = "sd_updated";
    private static final String SD_SCHOOL = "sd_school";

    // lesson table / column names
    private static final String TABLE_LESSON = "lesson";

    private static final String L_ID = "l_id";
    private static final String L_ID_DAY = "l_id_day";
    private static final String L_CLASS_TYPE = "l_class_type";
    private static final String L_PERIOD = "l_period";
    private static final String L_SUBJECT = "l_subject";
    private static final String L_TEACHER = "l_teacher";
    private static final String L_ROOM = "l_room";

    // lesson days table / column names
    private static final String TABLE_LESSON_DAYS = "lesson_days";

    private static final String LD_ID = "ld_id";
    private static final String LD_CLASS_YEAR_LETTER = "ld_class_year_letter";
    private static final String LD_VALID = "ld_valid";

    // classlist table / column names
    private static final String TABLE_CLASSLIST = "table_classlist";

    private static final String CL_ID = "cl_id";
    private static final String CL_NAME = "cl_name";
    private static final String CL_URL = "cl_url";

    // courselist table / column names
    private static final String TABLE_COURSELIST = "table_courselist";

    private static final String CO_ID = "co_id";
    private static final String CO_NAME = "co_name";
    private static final String CO_SCHOOL = "co_school";
    private static final String CO_CLASSYEARLETTER = "co_classyearletter";


    // stuff
    private final Context context;
    private String query, date;
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
                S_SUBJECT + " TEXT, " +
                S_TEACHER + " TEXT, " +
                S_ROOM + " TEXT, " +
                S_INFO + " TEXT, " +
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

        // courselist table
        query = "CREATE TABLE IF NOT EXISTS " + TABLE_COURSELIST + " (" +
                CO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CO_NAME + " TEXT, " +
                CO_SCHOOL + " INTEGER, " +
                CO_CLASSYEARLETTER + " TEXT" +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
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
        query = "DROP TABLE IF EXISTS " + TABLE_SUBSTITUTION;
        db.execSQL(query);
        query = "DROP TABLE IF EXISTS " + TABLE_COURSELIST;
        db.execSQL(query);
        onCreate(db);
        Intent intent = new Intent(context, RefreshService.class);
        intent.putExtra(RefreshService.INSTRUCTION, RefreshService.CLASSLIST_REFRESH);
        context.startService(intent);
        Intent intent2 = new Intent(context, RefreshService.class);
        intent2.putExtra(RefreshService.INSTRUCTION, RefreshService.SUBSTITUTION_REFRESH);
        context.startService(intent2);
    }

    private void init() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar c = Calendar.getInstance();
        date = dateFormatter.format(c.getTime());
        try {
            dateInMillis = Objects.requireNonNull(dateFormatter.parse(date)).getTime();
        } catch (ParseException e) {
            Log.e(TAG, "ParseException in init():", e);
        }


    }

    public List<SubstitutionDay> getSubstitutionDayList(int school, int classYear, String classLetter) {
        query = "SELECT * FROM " + TABLE_SUBSTITUTION_DAYS + " WHERE " + SD_DATE + " >= " + dateInMillis + " AND " + SD_SCHOOL + " = " + school;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<SubstitutionDay> result = new ArrayList<>();
        SubstitutionDay substitutionDay;

        if (cursor.moveToFirst()) {
            do {
                substitutionDay = new SubstitutionDay();
                substitutionDay.setDate(cursor.getLong(1));
                substitutionDay.setUpdated(cursor.getLong(2));
                substitutionDay.setSubstitutionList(getSubstitutionListByDayId(cursor.getInt(0), classYear, classLetter));
                substitutionDay.setSchool(school);
                if (substitutionDay.getSubstitutionList().size() > 0) {
                    result.add(substitutionDay);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    private List<Substitution> getSubstitutionListByDayId(int dayId, int classYear, String classLetter, String... classTypes) {
        query = "SELECT * FROM " + TABLE_SUBSTITUTION + " WHERE " + S_ID_DAY + " = " + dayId + " ";
        if (classYear != 0 && !classLetter.equalsIgnoreCase("")) {
            query += "AND (" + S_CLASS_YEAR + " = " + classYear + " OR " + S_CLASS_YEAR + " = 0)" +
                    " AND (" + S_CLASS_LETTER + " = '" + classLetter + "' OR " + S_CLASS_LETTER + " = 'X')";
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

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<Substitution> result = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                Substitution substitution = new Substitution();
                substitution.setClassYearLetter(cursor.getInt(2) + " " + cursor.getString(3));
                substitution.setClassCourse(cursor.getString(4));
                substitution.setPeriod(cursor.getInt(5));
                substitution.setSubject(cursor.getString(6));
                substitution.setTeacher(cursor.getString(7));
                substitution.setRoom(cursor.getString(8));
                substitution.setInfo(cursor.getString(9));
                result.add(substitution);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return result;
    }

    void insertSubstitutionResults(int school, List<SubstitutionDay> results) {
        Substitution substitution;

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor;
        ContentValues cv;

        db.delete(TABLE_SUBSTITUTION, null, null);
        db.delete(TABLE_SUBSTITUTION_DAYS, null, null);

        for (int i = 0; i < results.size(); i++) {
            query = "SELECT " + SD_ID + " FROM " + TABLE_SUBSTITUTION_DAYS + " WHERE " + SD_DATE + " = " + results.get(i).getDate() + " AND " + SD_SCHOOL + " = " + school;
            cursor = db.rawQuery(query, null);
            cursor.moveToFirst();

            cv = new ContentValues();
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
                substitution = results.get(i).getSubstitutionList().get(j);
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

    private void emptyClassListTable() {
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
        SQLiteDatabase db = getReadableDatabase();

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

    private void emptyCourseListTable(int school, String classYearLetter) {
        SQLiteDatabase db = getWritableDatabase();

        query = "DELETE FROM " + TABLE_COURSELIST + " WHERE " + CO_SCHOOL + " = " + school + " AND " + CO_CLASSYEARLETTER + " = '" + classYearLetter + "'";
        db.execSQL(query);
        db.close();
    }

    void updateCourseList(List<String> courseList, int school, String classYearLetter) {
        SQLiteDatabase db = getWritableDatabase();

        if (courseList.size() > 0) {
            emptyCourseListTable(school, classYearLetter);
            ContentValues cv;
            for (int i = 0; i < courseList.size(); i++) {
                cv = new ContentValues();
                cv.put(CO_NAME, courseList.get(i));
                cv.put(CO_SCHOOL, school);
                cv.put(CO_CLASSYEARLETTER, classYearLetter);
                db.insert(TABLE_COURSELIST, null, cv);
            }
        }
        db.close();
    }

}
