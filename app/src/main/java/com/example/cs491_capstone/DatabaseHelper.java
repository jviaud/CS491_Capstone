package com.example.cs491_capstone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * Database helper to create firebaseDatabase and store usage statistics
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    /**
     * The date of the usage statistics
     */
    public static final String DATE = "USAGE_DATE";
    /**
     * The hour of the day that the usage stat occurs, from 0-23
     */
    public static final String HOUR_OF_DAY = "HOUR_OF_DAY";
    /**
     * The package that the usage stat belongs to. This will be shared between the two tables and will act like a foreign key
     */
    public static final String PACKAGE_NAME = "PACKAGE_NAME";
    /**
     * This represents to number of times a specific app was the first to be opened after an unlock event and not the total number oof time the phone was unlocked
     * this information will be used to get the total number of phone unlocks by just taking the sum of unlocks for that day.
     */
    public static final String UNLOCKS_COUNT = "UNLOCKS_COUNT";
    /**
     * The number of notifications an app has sent
     */
    public static final String NOTIFICATIONS_COUNT = "NOTIFICATIONS_COUNT";
    /**
     * the total amount of time an app was used for
     */
    public static final String USAGE_TIME = "USAGE_TIME";
    /**
     * The UsageTable will give detailed usage statistics for a specific app
     */
    private static final String TABLE_NAME = "USAGE_STAT_TABLE";
    /**
     * The Auto-generated Primary key present in both tables. There is no relation between the keys between the two tables other than sharing the name "Entry_ID"
     */
    private static final String ENTRY_ID = "ENTRY_ID";
    /**
     * Name of the firebaseDatabase
     */
    private static final String DATABASE_NAME = "Usage.db";


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
//        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (" + ENTRY_ID + " TEXT PRIMARY KEY," + DATE + " TEXT," + HOUR_OF_DAY + " INTEGER," + PACKAGE_NAME + " TEXT, " + UNLOCKS_COUNT + " INTEGER," + NOTIFICATIONS_COUNT + " INTEGER, " + USAGE_TIME + " REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //MAY NEED TO BE REMOVED
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * @param packageName   The package name of the app
     * @param unlocks       the number of time the app was used first after an unlock event on a specific date during 1 hour intervals
     * @param notifications the number of notifications the app has sent on a specific date during 1 hour intervals
     * @param usage         the total time the app has been used on a specific date during 1 hour intervals
     * @throws SQLException Not a valid Query
     */
    public void insert(String packageName, int unlocks, int notifications, float usage) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();


        //THESE TWO VALUES WILL BE AUTOMATICALLY ASSIGNED
        //AS THEY WILL CHANGE AUTOMATICALLY DEPENDING ON THE DATE AND TIME OF DAY
        contentValues.put(DATE, App.DATE);
        contentValues.put(HOUR_OF_DAY, App.HOUR);

        ///THERE SHOULD ONLY BE ONE PACKAGE NAME PER DAY/HOUR
        //WE CANT USE AN AUTO INCREMENT PRIMARY KEY BECAUSE THERE WILL BE REPEATS WITHIN THE HOUR
        //WE ALSO CAN'T USE THE COMBINATION OF DATE AND HOUR BECAUSE WE ARE TACKING MULTIPLE PACKAGES
        contentValues.put(ENTRY_ID, packageName + "-" + App.DATE + "-" + App.HOUR);

        //THESE VALUES WILL BE MANUALLY PASSED
        contentValues.put(PACKAGE_NAME, packageName);
        contentValues.put(UNLOCKS_COUNT, unlocks);
        contentValues.put(NOTIFICATIONS_COUNT, notifications);
        contentValues.put(USAGE_TIME, usage);

        db.insertOrThrow(TABLE_NAME, null, contentValues);
    }

    /**
     * @param id
     * @param date
     * @param hour
     * @param packageName   The package name of the app
     * @param unlocks       the number of time the app was used first after an unlock event on a specific date during 1 hour intervals
     * @param notifications the number of notifications the app has sent on a specific date during 1 hour intervals
     * @param usage         the total time the app has been used on a specific date during 1 hour intervals
     * @throws SQLException Not a valid Query
     */
    public void insert(String id, String date, String hour, String packageName, String unlocks, String notifications, String usage) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        //THESE VALUES WILL BE MANUALLY PASSED

        contentValues.put(DATE, date);
        contentValues.put(HOUR_OF_DAY, hour);

        contentValues.put(ENTRY_ID, packageName + "-" + date + "-" + hour);

        contentValues.put(PACKAGE_NAME, packageName);
        contentValues.put(UNLOCKS_COUNT, unlocks);
        contentValues.put(NOTIFICATIONS_COUNT, notifications);
        contentValues.put(USAGE_TIME, usage);

        db.insertOrThrow(TABLE_NAME, null, contentValues);
    }

    /**
     * this method allows me to update one column inside the table according to the date and hour of the day
     *
     * @param packageName the package name of the app
     * @param col_one     the first column to be updated
     * @param value_one   the value for the first column
     */
    public void set(String packageName, String col_one, int value_one) {
        SQLiteDatabase db = this.getWritableDatabase();

        //UPDATE TABLE_NAME SET COL = VALUE WHERE DATE = CURRENT_DATE AND HOUR_OF_DAY = CURRENT_HOUR
        //UPDATE THE CURRENT COL TO A NEW VALUE
        db.execSQL("UPDATE " + TABLE_NAME +
                " SET " + col_one + "=" + value_one +
                " WHERE " + DATE + "= \"" + App.DATE + "\"" + " AND " + HOUR_OF_DAY + "= " + App.HOUR + " AND " + PACKAGE_NAME + "= \"" + packageName + "\"");
    }

    /**
     * This method allows me to update one column inside the table according to the date and hour of the day
     * Because one of the values in the database is a float I have to account for it here
     *
     * @param packageName the package name of the app
     * @param col_one     the first column to be updated
     * @param value_one   the value for the first column as a float`
     */
    public void set(String packageName, String col_one, float value_one) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.i("RESULT", "VALUE" + value_one);
        //UPDATE TABLE_NAME SET COL = VALUE WHERE DATE = CURRENT_DATE AND HOUR_OF_DAY = CURRENT_HOUR
        //UPDATE THE CURRENT COL TO A NEW VALUE
        db.execSQL("UPDATE " + TABLE_NAME +
                " SET " + col_one + "=" + value_one +
                " WHERE " + DATE + " = \"" + App.DATE + "\"" + " AND " + HOUR_OF_DAY + " = " + App.HOUR + " AND " + PACKAGE_NAME + " = \"" + packageName + "\"");
    }

    /**
     * this method allows me to update two columns inside the table according to the date and hour of the day
     *
     * @param packageName the package name of the app
     * @param col_one     the first column to be updated
     * @param value_one   the value for the first column
     * @param col_two     the second column to be updated
     * @param value_two   the value for the second column
     */
    public void set(String packageName, String col_one, int value_one, String col_two, int value_two) {
        SQLiteDatabase db = this.getWritableDatabase();


        db.execSQL("UPDATE " + TABLE_NAME +
                " SET " + col_one + "=" + value_one + "," + col_two + "=" + value_two +
                " WHERE " + DATE + "= \"" + App.DATE + "\"" + " AND " + HOUR_OF_DAY + "= " + App.HOUR + " AND " + PACKAGE_NAME + "= \"" + packageName + "\"");
    }

    /**
     * This method allows me to update two columns inside the table according to the date and hour of the day
     * Because one of the values in the database is a float I have to account for it here
     *
     * @param packageName the package name of the app
     * @param col_one     the first column to be updated
     * @param value_one   the value for the first column
     * @param col_two     the second column to be updated
     * @param value_two   the value for the second column
     */
    public void set(String packageName, String col_one, int value_one, String col_two, float value_two) {
        SQLiteDatabase db = this.getWritableDatabase();


        db.execSQL("UPDATE " + TABLE_NAME +
                " SET " + col_one + "=" + value_one + "," + col_two + "=" + value_two +
                " WHERE " + DATE + "= \"" + App.DATE + "\"" + " AND " + HOUR_OF_DAY + "= " + App.HOUR + " AND " + PACKAGE_NAME + "= \"" + packageName + "\"");
    }

    /**
     * this method allows me to update three columns inside the table according to the date and hour of the day
     *
     * @param packageName the package name of the app
     * @param col_one     the first column to be updated
     * @param value_one   the value for the first column
     * @param col_two     the second column to be updated
     * @param value_two   the value for the second column
     * @param col_three   the third column to be updated
     * @param value_three the value for the third column (third is assumed to be a float)
     */
    public void set(String packageName, String col_one, int value_one, String col_two, int value_two, String col_three, float value_three) {
        SQLiteDatabase db = this.getWritableDatabase();


        db.execSQL("UPDATE " + TABLE_NAME +
                " SET " + col_one + "=" + value_one + "," + col_two + "=" + value_two + "," + col_three + "=" + value_three +
                " WHERE " + DATE + "= \"" + App.DATE + "\"" + " AND " + HOUR_OF_DAY + "= " + App.HOUR + " AND " + PACKAGE_NAME + "= \"" + packageName + "\"");
    }

    /**
     * This method is set to always return info for the current hour
     *
     * @param col         The column to be selected
     * @param packageName The condition to attach to where clause, in this case it is a package name
     * @return A string with the value of the result, it will either be null or an int so we use String to catch both conditions
     */
    public String get(String col, String packageName) {
        SQLiteDatabase db = this.getWritableDatabase();
        //
        Cursor res = db.rawQuery("SELECT " + col + " FROM " + TABLE_NAME +
                " WHERE " + PACKAGE_NAME + " = \"" + packageName + "\"" + " AND " + DATE + "=\"" + App.DATE + "\" AND " + HOUR_OF_DAY + "= \"" + App.HOUR + "\"", null);
        StringBuilder buffer = new StringBuilder();

        if (col.equals(USAGE_TIME)) {
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
            Log.i("RESULT", "NULL");
            return "0";
        } else {
            Log.i("RESULT", "..." + result);
            return result;
        }
    }

    /**
     * This method will take a date and time
     *
     * @param col         The column to be selected
     * @param packageName The condition to attach to where clause, in this case it is a package name
     * @param date        The date to be selected
     * @param hour        The hour to be selected
     * @return A string with the value of the result, it will either be null or an int so we use String to catch both conditions
     */
    public String get(String col, String packageName, String date, String hour) {
        SQLiteDatabase db = this.getWritableDatabase();
        //
        Cursor res = db.rawQuery("SELECT " + col + " FROM " + TABLE_NAME +
                " WHERE " + PACKAGE_NAME + " = \"" + packageName + "\"" + " AND " + DATE + " =\"" + date + "\" AND " + HOUR_OF_DAY + "= \"" + hour + "\"", null);
        StringBuilder buffer = new StringBuilder();

        //READ LINES FROM CURSOR INTO BUFFER
        if (col.equals(USAGE_TIME)) {
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
            Log.i("RESULT", "NULL");
            return "0";
        } else {
            Log.i("RESULT", "..." + result);
            return result;
        }
    }

    /**
     * @param packageName the package name of the app
     * @return A String representing the total time this app has been used today
     */
    public String getSumAppUsageTime(String packageName) {

        SQLiteDatabase db = this.getWritableDatabase();
        //

        Cursor res = db.rawQuery("SELECT SUM(" + USAGE_TIME + ") " +
                "FROM " + TABLE_NAME + " " +
                "WHERE " + PACKAGE_NAME + " = \"" + packageName + "\"" + " " +
                "AND " + DATE + "= \"" + App.DATE + "\"", null);

        StringBuilder buffer = new StringBuilder();

        //READ LINES FROM CURSOR INTO BUFFER
        while (res.moveToNext()) {
            buffer.append(res.getLong(0));
        }

        res.close();

        String result = buffer.toString();
        if (result.equals("")) {
            Log.i("RESULT", "NULL");
            return "0";
        } else {
            Log.i("RESULT", "" + result);
            return result;
        }
    }

    /**
     * @param date the date of the usage stats
     * @return A string representing to total time the phone has been used for a specific date
     */
    public String getSumTotalStat(String date, String col) {
        SQLiteDatabase db = this.getWritableDatabase();
        //

        Cursor res = db.rawQuery("SELECT ROUND(SUM(" + col + "),0) " +
                "FROM " + TABLE_NAME + " " +
                "WHERE " + DATE + "= \"" + date + "\"", null);

        StringBuilder buffer = new StringBuilder();

        //READ LINES FROM CURSOR INTO BUFFER
        if (col.equals(USAGE_TIME)) {
            while (res.moveToNext()) {
                buffer.append(res.getLong(0));

            }
        } else {
            while (res.moveToNext()) {
                buffer.append(res.getString(0));

            }
        }

        res.close();

        if (buffer.toString().equals(""))
            return "0";
        else return buffer.toString();
    }

    /**
     * @param date the date of the usage stats
     * @return A string representing to total time the phone has been used for a specific date
     */
    public String getSumTotalStat(String date, String hour, String col) {
        SQLiteDatabase db = this.getWritableDatabase();
        //

        Cursor res = db.rawQuery("SELECT ROUND(SUM(" + col + "),0) " +
                "FROM " + TABLE_NAME + " " +
                "WHERE " + DATE + "= \"" + date + "\" AND " + HOUR_OF_DAY + "= \"" + hour + "\"", null);

        StringBuilder buffer = new StringBuilder();

        //READ LINES FROM CURSOR INTO BUFFER
        if (col.equals(USAGE_TIME)) {
            while (res.moveToNext()) {
                buffer.append(res.getLong(0));

            }
        } else {
            while (res.moveToNext()) {
                buffer.append(res.getString(0));

            }
        }

        res.close();

        Log.i("RETURN", buffer.toString());
        if (buffer.toString().equals("null")) {
            return "0";
        } else {
            return buffer.toString();
        }
    }

    public String getSumTotalStatByPackage(String date, String col, String packageName) {
        SQLiteDatabase db = this.getWritableDatabase();
        //

        Cursor res = db.rawQuery("SELECT ROUND(SUM(" + col + "),0) " +
                "FROM " + TABLE_NAME + " " +
                "WHERE " + DATE + "= \"" + date + "\" AND " + PACKAGE_NAME + "= \"" + packageName + "\"", null);

        StringBuilder buffer = new StringBuilder();

        //READ LINES FROM CURSOR INTO BUFFER
        if (col.equals(USAGE_TIME)) {
            while (res.moveToNext()) {
                buffer.append(res.getLong(0));

            }
        } else {
            while (res.moveToNext()) {
                buffer.append(res.getString(0));

            }
        }

        res.close();

        if (buffer.toString().equals("null"))
            return "0";
        else return buffer.toString();
    }

    public String getSumTotalStatByPackage(String date, String hour, String col, String packageName) {
        SQLiteDatabase db = this.getWritableDatabase();
        //

        Cursor res = db.rawQuery("SELECT ROUND(SUM(" + col + "),0) " +
                "FROM " + TABLE_NAME + " " +
                "WHERE " + DATE + "= \"" + date + "\" AND " + HOUR_OF_DAY + "= \"" + hour + "\" AND " + PACKAGE_NAME + "= \"" + packageName + "\"", null);

        StringBuilder buffer = new StringBuilder();

        //READ LINES FROM CURSOR INTO BUFFER
        if (col.equals(USAGE_TIME)) {
            while (res.moveToNext()) {
                buffer.append(res.getLong(0));

            }
        } else {
            while (res.moveToNext()) {
                buffer.append(res.getString(0));

            }
        }

        res.close();

        Log.i("RETURN", buffer.toString());
        if (buffer.toString().equals("null")) {
            return "0";
        } else {
            return buffer.toString();
        }
    }

    public ArrayList<String> getTopThreeRankings(String date, String col) {
        ArrayList<String> top = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor res = db.rawQuery("SELECT " + PACKAGE_NAME + " FROM " + TABLE_NAME +
                " GROUP BY " + PACKAGE_NAME +
                " HAVING " + DATE + " =\"" + date + "\"" +
                " ORDER BY SUM(" + col + ") DESC LIMIT 3", null);

        //READ LINES FROM CURSOR INTO BUFFER

        while (res.moveToNext()) {
            top.add(res.getString(0));
        }

        Log.i("TOP", "" + top);
        res.close();
        return top;
    }

    /**
     * returns all data from table
     *
     * @return Cursor of all data in table
     */
    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    void emptyWeek(String date) {
        //DELETE FROM USAGE_STAT_TABLE WHERE USAGE_DATE <= date("2020-02-28")

        SQLiteDatabase db = this.getWritableDatabase();
        //
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + DATE + "< date(\"" + date + "\")");
    }
}
