package com.example.henry.chatapplication;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by hieu on 5/3/16.
 */
public class EntriesProvider extends ContentProvider {
    SQLiteDatabase db;

    public static final String PROVIDER_NAME = "com.example.henry.chatapplication";
    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/entries");

    public static final String _ID = "_id";

    static final int ENTRIES = 1;
    static final int ENTRY_ID = 2;

    static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "entries", ENTRIES);
        uriMatcher.addURI(PROVIDER_NAME, "entries/#", ENTRY_ID);
    }

    @Override
    public boolean onCreate() {
        Context c = getContext();
        EntriesOpenHelper dbHelper = new EntriesOpenHelper(c);
        db = dbHelper.getWritableDatabase();
        if (db == null) {
            return false;
        } else {
            return true;
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables("entries");

        if (uriMatcher.match(uri) == ENTRY_ID) {
            qb.appendWhere(_ID + " = " + uri.getPathSegments().get(1));
        }

        Cursor cur = qb.query(db, projection, selection, selectionArgs, null, null ,sortOrder);

        cur.setNotificationUri(getContext().getContentResolver(), uri);
        return cur;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ENTRIES:
                return "vnd.android.cursor.dir/vnd.example.henry.chatapplication";
            case ENTRY_ID:
                return "vnd.android.cursor.item/vnd.example.henry.chatapplication";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = db.insert("entries", "", values);
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            Log.d("Provider", "Added successfully");
            return _uri;
        }
        Log.d("Provider", "Failed when adding new record");
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}