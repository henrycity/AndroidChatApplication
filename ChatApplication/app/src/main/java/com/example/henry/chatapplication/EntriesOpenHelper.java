package com.example.henry.chatapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by hieu on 5/3/16.
 */
public class EntriesOpenHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Entry.db";
    private static final String SQL_CREATE_TABLE =
            "create table entries (" +
                    "_id integer primary key autoincrement, " +
                    "type text not null, " +
                    "timeStamp integer, " +
                    "time text, " +
                    "date text, " +
                    "message text, " +
                    "roomId integer, " +
                    "username text" +
                    ")";
    private static final String SQL_DELETE_TABLE =
            "drop table if exists entries";

    public EntriesOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from entries");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TABLE);
        onCreate(db);
    }
}
