package com.vva.androidopencbt;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DateBaseHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "opencbd_beta9.db"; // название бд
    private static final int SCHEMA = 1; // версия базы данных
    static final String TABLE = "diary"; // название таблицы в бд
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SITUATION = "_situation";
    public static final String COLUMN_THOUGHTS = "_thoughts";
    public static final String COLUMN_RATIONAL = "_rational";
    public static final String COLUMN_EMOTIONS = "_emotions";
    public static final String COLUMN_DISTORTIONS = "_distortions";
    public static final String COLUMN_FEELINGS = "_feelings";
    public static final String COLUMN_ACTIONS = "_actions";
    public static final String COLUMN_INTENSITY = "_intensity";
    public static final String COLUMN_DATETIME = "_datetime";

    public static final int ORDER_ASC = 0;
    public static final int ORDER_DESC = 1;
    public static final int ORDER_NONE = 2;

    public DateBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_SITUATION
                + " TEXT, "     + COLUMN_THOUGHTS
                + " TEXT, "     + COLUMN_RATIONAL
                + " TEXT, "     + COLUMN_EMOTIONS
                + " TEXT, "     + COLUMN_FEELINGS
                + " TEXT, "     + COLUMN_ACTIONS
                + " TEXT, "     + COLUMN_INTENSITY
                + " INTEGER, "  + COLUMN_DISTORTIONS
                + " INTEGER, "   + COLUMN_DATETIME + " INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE);
        onCreate(db);
    }
}

