package com.example.cs491_capstone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class AwardDataBaseHelper extends SQLiteOpenHelper {

    /**
     * THE NAME OF THE DRAWABLE AS IT APPEARS IN THE  RES/DRAWABLES FOLDER e.g R.DRAWABLES.##
     */
    public static final String DRAWABLE_NAME = "DRAWABLE_NAME";
    public static final String ACHIEVEMENT_NAME = "ACHIEVEMENT_NAME";
    /**
     * AN INT VALUE REPRESENTING THE STATUS OF THE AWARD, 0=LOCKED 1=UNLOCKED
     */
    public static final String STATUS = "STATUS";
    /**
     * The description of the achievement
     */
    public static final String DESCRIPTION = "DESCRIPTION";
    /**
     * THE NAME OF THE TABLE
     */
    private static final String TABLE_NAME = "AWARDS_TABLE";
    /**
     * AN AUTO INCREMENT VARIABLE THAT IS THE PK
     */
    private static final String ENTRY_ID = "ENTRY_ID";
    /**
     * THE NAME OF THIS DATABASE
     */
    private static final String DATABASE_NAME = "Award.db";


    public AwardDataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
//        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (" + ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + DRAWABLE_NAME + " TEXT," + STATUS + " INTEGER, " + DESCRIPTION + " TEXT," + ACHIEVEMENT_NAME + " TEXT )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //MAY NEED TO BE REMOVED
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insert(String drawable, String name, String description, int status) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(DRAWABLE_NAME, drawable);
        contentValues.put(ACHIEVEMENT_NAME, name);
        contentValues.put(DESCRIPTION, description);
        contentValues.put(STATUS, status);

        db.insertOrThrow(TABLE_NAME, null, contentValues);
    }

    public void set(String name, int value) {
        SQLiteDatabase db = this.getWritableDatabase();

        //UPDATE TABLE_NAME SET COL = VALUE WHERE DATE = CURRENT_DATE AND HOUR_OF_DAY = CURRENT_HOUR
        //UPDATE THE CURRENT COL TO A NEW VALUE
        db.execSQL("UPDATE " + TABLE_NAME +
                " SET " + STATUS + "=" + value +
                " WHERE " + name + "= \"" + ACHIEVEMENT_NAME + "\"");
    }

    public boolean getStatus(String name) {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor res = db.rawQuery("SELECT " + STATUS + " FROM " + TABLE_NAME +
                " WHERE " + DRAWABLE_NAME + " = \"" + name + "\"", null);

        //RES WILL ONLY EVERY CONTAIN A SINGULAR VALUE BECAUSE ALL NAMES ARE UNIQUE BECAUSE THEY ARE DRAWABLE NAMES
        //

        StringBuilder buffer = new StringBuilder();
        //RES WILL ONLY EVERY CONTAIN A SINGULAR VALUE BECAUSE ALL NAMES ARE UNIQUE BECAUSE THEY ARE DRAWABLE NAMES
        while (res.moveToNext()) {
            buffer.append(res.getString(0));
        }
        res.close();

        int result = Integer.parseInt(buffer.toString());
        return result == 0;
    }

    public String get(String name, String col) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor res = db.rawQuery("SELECT " + col + " FROM " + TABLE_NAME +
                " WHERE " + DRAWABLE_NAME + " = \"" + name + "\"", null);
        StringBuilder buffer = new StringBuilder();
        //RES WILL ONLY EVERY CONTAIN A SINGULAR VALUE BECAUSE ALL NAMES ARE UNIQUE BECAUSE THEY ARE DRAWABLE NAMES
        while (res.moveToNext()) {
            buffer.append(res.getString(0));
        }
        res.close();

        return buffer.toString();
    }


    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }
}
