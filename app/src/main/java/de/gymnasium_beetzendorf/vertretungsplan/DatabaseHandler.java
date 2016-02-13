package de.gymnasium_beetzendorf.vertretungsplan;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHandler";
    // databse
    public static String DATABASE_NAME = "database.db";
    public static int DATABASE_VERSION = 1;

    // table substitution_days
    private static String TABLE_SUBSTITUTION_DAYS = "substitution_days";
    private static String SD_ID = "id";
    private static String SD_DATE = "date";
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

    // stuff
    private String query;
    private Cursor cursor;

    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        query = "DROP TABLE IF EXISTS " + TABLE_SUBSTITUTION_DAYS;
        db.execSQL(query);
        query = "DROP TABLE IF EXISTS " + TABLE_SUBSTITUTION_ROWS;
        db.execSQL(query);
        onCreate(db);
    }

    public boolean isUpToDate(String date, Long last_updated) {
        SQLiteDatabase db = getReadableDatabase();
        Boolean is_up_to_date;
        query = "SELECT * FROM " + TABLE_SUBSTITUTION_DAYS + " WHERE " + SD_DATE + " = '" + date + "'";
        cursor = db.rawQuery(query, null);

        if (last_updated > cursor.getLong(cursor.getColumnIndex(SD_LAST_UPDATED))) {
            is_up_to_date = false;
        } else {
            is_up_to_date = true;
        }
        return is_up_to_date;
    }

    public List<Schoolday> getAllSubstitutions(String date, boolean after3Pm) {
        SQLiteDatabase db = getReadableDatabase();

        long time = 0;
        try {
            time = MainActivity.formatter.parse(date).getTime();
        } catch (ParseException e) {
            Log.i(MainActivity.TAG, "ParseException", e);
        }

        if (after3Pm) {
            query = "SELECT * FROM " + TABLE_SUBSTITUTION_DAYS + " WHERE " + SD_DATE + " > '" + time + "'";

        } else {
            query = "SELECT * FROM " + TABLE_SUBSTITUTION_DAYS + " WHERE " + SD_DATE + " >= '" + time + "'";
        }
        cursor = db.rawQuery(query, null);

        /*if (cursor != null) {
            cursor.moveToFirst();
        }
        Integer id = cursor.getInt(cursor.getColumnIndex(SD_ID));
        String db_date = cursor.getString(cursor.getColumnIndex(SD_DATE));
        Log.i(MainActivity.TAG, "id: " + String.valueOf(id) + " date: " + db_date);

        query = "SELECT * FROM " + TABLE_SUBSTITUTION_ROWS + " WHERE " + SR_DAY + " = '" + id + "'";
        cursor = db.rawQuery(query, null);
        */
        Log.i(MainActivity.TAG, "query: " + query + "\nmenge der elemente: " + String.valueOf(cursor.getCount()));


        return null;
    }

    public List<Subject> getSubstitutions(int id) {
        // TODO: implement method to fetch substituions for a single day
        SQLiteDatabase db = getReadableDatabase();
        query = "SELECT * FROM " + TABLE_SUBSTITUTION_ROWS + " WHERE "  + SR_DAY + " = '" + id + "'";
        cursor = db.rawQuery(query, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        while (!cursor.isLast()) {

            cursor.moveToNext();
        }
        return new ArrayList<>();
    }

    public void insertXmlResults(List<Schoolday> results) {
        SQLiteDatabase db = getReadableDatabase();
        Schoolday currentDay;
        for (int n = 0; n < results.size(); n++) {
            currentDay = results.get(n);
            Log.i(MainActivity.TAG, "menge der ergebnisse: " + String.valueOf(results.size()));
            query = "SELECT * FROM " + TABLE_SUBSTITUTION_DAYS + " WHERE " + SD_DATE + " = '" + currentDay.getDate() + "'";
            cursor = db.rawQuery(query, null);
            cursor.moveToFirst();
            Log.i(MainActivity.TAG, "cursor number when checking for current date: " + String.valueOf(cursor.getCount()) + " datum: " + currentDay.getDate());
            //int id = cursor.getInt(0);
            ContentValues values = new ContentValues();
            if (cursor.getCount() == 0) {
                query = "INSERT INTO " + TABLE_SUBSTITUTION_DAYS + " (" + SD_DATE + "," + SD_LAST_UPDATED + "," + SD_PAST + ") VALUES ('" +
                        currentDay.getDate() + "','" + currentDay.getLastUpdated() + "','false')";
                Cursor newCursor = db.rawQuery(query, null);
                Log.i(MainActivity.TAG, "newcursor count: " + String.valueOf(newCursor.getCount()));
                newCursor.close();
            } else if (cursor.getCount() == 1) {
                int id = cursor.getInt(cursor.getColumnIndex(SD_ID));
                // updating table days
                values.put(SD_LAST_UPDATED, currentDay.getLastUpdated());
                db.update(TABLE_SUBSTITUTION_DAYS, values, SD_ID + " = ?", new String[]{String.valueOf(id)});

                // clear corresponding table rows and then readd it
                db.delete(TABLE_SUBSTITUTION_ROWS, SR_DAY + " = ?", new String[]{String.valueOf(id)});
                Subject currentSubject;
                for (int i = 0; i < currentDay.getSubjects().size(); i++) {
                    currentSubject = currentDay.getSubjects().get(i);
                    values.clear();
                    values.put(SR_DAY, id);
                    values.put(SR_COURSE, currentSubject.getCourse());
                    values.put(SR_PERIOD, currentSubject.getPeriod());
                    values.put(SR_ROOM, currentSubject.getRoom());
                    values.put(SR_SUBJECT, currentSubject.getSubject());
                    values.put(SR_TEACHER, currentSubject.getTeacher());
                    values.put(SR_INFO, currentSubject.getInfo());
                    db.insert(TABLE_SUBSTITUTION_ROWS, null, values);
                }
            }
        }
    }

    public boolean cleanDatabase() {
        // TODO: implement method to delete all rows older than 30 days
        SQLiteDatabase db = getReadableDatabase();
        return true;
    }

    public boolean markAsPast() {
        // TODO: implement method to mark all past days to avoid using them again
        return true;
    }
}
