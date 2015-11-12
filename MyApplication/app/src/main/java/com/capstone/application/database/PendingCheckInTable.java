package com.capstone.application.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PendingCheckInTable {

    // Database table
    public static final String TABLE_PENDING_CHECK_IN = "PendingCheckIn";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DATE = "date";

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_PENDING_CHECK_IN
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_DATE + " text not null"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(PendingCheckInTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion + ", which will destroy all old data");

        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PENDING_CHECK_IN);
        onCreate(database);
    }
}
