package com.example.cs491_capstone;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.cs491_capstone.ui.goal.Goal;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class GoalDataBaseHelper extends SQLiteOpenHelper {

    /**
     * if goals are for a specific app the package name of the app is recorded else 0
     */
    public static final String GOAL_APP = "GOAL_APP";
    /**
     *
     */
    public static final String GOAL_PHONE = "GOAL_PHONE";
    /**
     * if the goal is usage based then the time is recorded in milli
     */
    public static final String GOAL_USAGE = "GOAL_USAGE";
    /**
     * if the goal is unlocks based then the number of unlocks is recorded
     */
    public static final String GOAL_UNLOCKS = "GOAL_UNLOCKS";
    /**
     * THE NAME OF THE DRAWABLE AS IT APPEARS IN THE  RES/DRAWABLES FOLDER e.g R.DRAWABLES.##
     */
    private static final String GOAL_DATE = "GOAL_DATE";
    /**
     * goals can be for a specific app or for overall phone usage or a specific category
     */
    private static final String GOAL_TYPE = "GOAL_TYPE";
    /**
     * THE NAME OF THE TABLE
     */
    private static final String TABLE_NAME = "GOAL_TABLE";
    /**
     * AN AUTO INCREMENT VARIABLE THAT IS THE PK
     */
    private static final String ENTRY_ID = "ENTRY_ID";
    /**
     * THE NAME OF THIS DATABASE
     */
    private static final String DATABASE_NAME = "Goal.db";
    private static final String PACKAGE_NAME = "PACKAGE_NAME";

    /**
     * @param activity this is a reference to the Application. We use to to get the context since Context can't be static
     */
    GoalDataBaseHelper(App activity) {
        super(activity, DATABASE_NAME, null, 1);
        new WeakReference<>(activity);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(" CREATE TABLE " + TABLE_NAME + " (" + ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + GOAL_DATE + " TEXT,"
                + GOAL_TYPE + " TEXT,"
                + GOAL_USAGE + " REAL,"
                + GOAL_UNLOCKS + " INTEGER,"
                + PACKAGE_NAME + " TEXT DEFAULT \"0\""
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //MAY NEED TO BE REMOVED
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * @param date
     * @param type
     * @param usage
     * @param unlocks
     * @throws SQLException
     */
    public void insert(String date, String type, long usage, int unlocks) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();


        contentValues.put(GOAL_DATE, date);
        contentValues.put(GOAL_TYPE, type);

        contentValues.put(GOAL_USAGE, usage);
        contentValues.put(GOAL_UNLOCKS, unlocks);
        db.insertOrThrow(TABLE_NAME, null, contentValues);
    }

    public void insert(String date, String type, long usage, int unlocks, String packageName) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();


        contentValues.put(GOAL_DATE, date);
        contentValues.put(GOAL_TYPE, type);

        contentValues.put(GOAL_USAGE, usage);
        contentValues.put(GOAL_UNLOCKS, unlocks);
        contentValues.put(PACKAGE_NAME, packageName);
        db.insertOrThrow(TABLE_NAME, null, contentValues);
    }


    /**
     * @param date
     * @param type
     * @param value
     */
    public void set(String date, String type, int value) {
        SQLiteDatabase db = this.getWritableDatabase();

        //UPDATE TABLE_NAME SET COL = VALUE WHERE DATE = CURRENT_DATE AND HOUR_OF_DAY = CURRENT_HOUR
        //UPDATE THE CURRENT COL TO A NEW VALUE

        db.execSQL("UPDATE " + TABLE_NAME +
                " SET " + GOAL_UNLOCKS + "=" + value +
                " WHERE " + GOAL_DATE + "= \"" + date + "\"" + " AND " + GOAL_TYPE + " = " + type);
    }

    /**
     * GOAL_USAGE can not be a String or else it will be converted to exponential notation so we overload the set method
     *
     * @param date
     * @param type
     * @param value
     */
    public void set(String date, String type, long value) {
        SQLiteDatabase db = this.getWritableDatabase();

        //UPDATE TABLE_NAME SET COL = VALUE WHERE DATE = CURRENT_DATE AND HOUR_OF_DAY = CURRENT_HOUR
        //UPDATE THE CURRENT COL TO A NEW VALUE

        db.execSQL("UPDATE " + TABLE_NAME +
                " SET " + GOAL_USAGE + "=" + value +
                " WHERE " + GOAL_DATE + "= \"" + date + "\"" + " AND " + GOAL_TYPE + " = " + type);
    }

    /**
     * @param date
     * @param type
     * @param col
     * @return
     */
    public String get(String date, String type, String col) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT " + col + " FROM " + TABLE_NAME +
                " WHERE " + GOAL_DATE + " = \"" + date + "\"" + " AND " + GOAL_TYPE + "=\"" + type + "\"", null);


        StringBuilder buffer = new StringBuilder();
        if (col.equals(GOAL_USAGE)) {
            while (res.moveToNext()) {
                buffer.append(res.getLong(0));
            }
        } else {
            while (res.moveToNext()) {
                buffer.append(res.getString(0));

            }
        }

        res.close();

        String result = buffer.toString();
        if (result.equals("")) {
            return "0";
        } else {
            return result;
        }
    }

    public String get(String date, String type, String packageName, String col) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT " + col + " FROM " + TABLE_NAME +
                " WHERE " + GOAL_DATE + " = \"" + date + "\"" + " AND " + GOAL_TYPE + "=\"" + type + "\"" + " AND " + PACKAGE_NAME + "=\"" + packageName + "\"", null);


        StringBuilder buffer = new StringBuilder();
        if (col.equals(GOAL_USAGE)) {
            while (res.moveToNext()) {
                buffer.append(res.getLong(0));
            }
        } else {
            while (res.moveToNext()) {
                buffer.append(res.getString(0));

            }
        }

        res.close();

        String result = buffer.toString();
        if (result.equals("")) {
            return "0";
        } else {
            return result;
        }
    }

    public void remove(String id) {
        SQLiteDatabase db = this.getWritableDatabase();

        //UPDATE TABLE_NAME SET COL = VALUE WHERE DATE = CURRENT_DATE AND HOUR_OF_DAY = CURRENT_HOUR
        //UPDATE THE CURRENT COL TO A NEW VALUE

        db.execSQL("DELETE FROM " + TABLE_NAME +
                " WHERE " + ENTRY_ID + "= " + id + "");
    }

    //public String get

    public List<Goal> getAllActiveGoals(String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Goal> goals = new ArrayList<>();
        //
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_NAME +
                " WHERE " + GOAL_DATE + ">= date(\"" + startDate + "\")" +
                " AND " + GOAL_DATE + "<= date(\"" + endDate + "\")"
                + " AND GOAL_TYPE = \"GOAL_PHONE\" ", null);

        //READ LINES FROM CURSOR INTO BUFFER
        while (res.moveToNext()) {
            String id = res.getString(0);
            String goalDate = res.getString(1);
            String goalType = res.getString(2);
            long usage = res.getLong(3);
            int unlocks = res.getInt(4);
            String packageName = res.getString(5);
            goals.add(new Goal(id, goalDate, goalType, usage, unlocks, packageName));
        }


        res.close();
        return goals;
    }

    public List<Goal> getUniquePackageGoals(String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Goal> goals = new ArrayList<>();
        //
        Cursor res = db.rawQuery("SELECT DISTINCT * FROM " + TABLE_NAME +
                " WHERE " + GOAL_DATE + ">= date(\"" + startDate + "\")" +
                " AND " + GOAL_DATE + "<= date(\"" + endDate + "\")"
                + " AND GOAL_TYPE = \"GOAL_APP\" "
                + " GROUP BY PACKAGE_NAME"
                + " ORDER BY date(\"GOAL_DATE\") DESC", null);

        //READ LINES FROM CURSOR INTO BUFFER
        while (res.moveToNext()) {
            String id = res.getString(0);
            String goalDate = res.getString(1);
            String goalType = res.getString(2);
            long usage = res.getLong(3);
            int unlocks = res.getInt(4);
            String packageName = res.getString(5);
            Log.i("GOALS", "DB: " + packageName);
            goals.add(new Goal(id, goalDate, goalType, usage, unlocks, packageName));
        }


        res.close();
        return goals;
    }

    public boolean canInsert(String date, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME +
                " WHERE " + GOAL_DATE + " = \"" + date + "\"" + " AND " + GOAL_TYPE + "='" + type + "'", null);


        StringBuilder buffer = new StringBuilder();
        while (res.moveToNext()) {
            buffer.append(res.getString(0));
        }
        String result = buffer.toString();

        return result.equals("0");
    }

    public boolean canInsertApp(String date, String type, String packageName) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME +
                " WHERE " + GOAL_DATE + " = \"" + date + "\""
                + " AND " + GOAL_TYPE + "='" + type + "'"
                + " AND " + PACKAGE_NAME + "=\"" + packageName + "\"", null);


        StringBuilder buffer = new StringBuilder();
        while (res.moveToNext()) {
            buffer.append(res.getString(0));
        }
        String result = buffer.toString();

        return result.equals("0");
    }

}
