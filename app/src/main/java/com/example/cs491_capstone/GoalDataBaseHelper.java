package com.example.cs491_capstone;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class GoalDataBaseHelper extends SQLiteOpenHelper {

    /**
     * THE NAME OF THE DRAWABLE AS IT APPEARS IN THE  RES/DRAWABLES FOLDER e.g R.DRAWABLES.##
     */
    public static final String GOAL_DATE = "GOAL_DATE";
    public static final String GOAL_TYPE = "GOAL_TYPE";
    public static final String GOAL_APP = "GOAL_APP";
    public static final String GOAL_USAGE = "GOAL_USAGE";
    public static final String GOAL_NOTIFICATION = "GOAL_NOTIFICATION";
    public static final String GOAL_UNLOCKS = "GOAL_UNLOCKS";

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

    public GoalDataBaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
