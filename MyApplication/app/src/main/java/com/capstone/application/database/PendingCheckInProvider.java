package com.capstone.application.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;

public class PendingCheckInProvider extends ContentProvider {

    // database
    private PendingCheckInDatabaseHelper database;

    // used for the UriMatcher
    private static final int PENDING_CHECK_IN = 10;
    private static final int PENDING_CHECK_IN_ID = 20;

    private static final String AUTHORITY = "com.capstone.application.diabetes";

    private static final String BASE_PATH = "pendingCheckIn";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    private static final UriMatcher sURIMatcher;

    static {
        sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        sURIMatcher.addURI(AUTHORITY, BASE_PATH, PENDING_CHECK_IN);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", PENDING_CHECK_IN_ID);
    }

    @Override
    public boolean onCreate() {
        database = new PendingCheckInDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(PendingCheckInTable.TABLE_PENDING_CHECK_IN);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case PENDING_CHECK_IN:
                break;
            case PENDING_CHECK_IN_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(PendingCheckInTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id;
        switch (uriType) {
            case PENDING_CHECK_IN:
                id = sqlDB.insert(PendingCheckInTable.TABLE_PENDING_CHECK_IN, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted;
        switch (uriType) {
            case PENDING_CHECK_IN:
                rowsDeleted = sqlDB.delete(PendingCheckInTable.TABLE_PENDING_CHECK_IN, selection,
                        selectionArgs);
                break;
            case PENDING_CHECK_IN_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(PendingCheckInTable.TABLE_PENDING_CHECK_IN,
                            PendingCheckInTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(PendingCheckInTable.TABLE_PENDING_CHECK_IN,
                            PendingCheckInTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated;

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case PENDING_CHECK_IN:
                rowsUpdated = sqlDB.update(PendingCheckInTable.TABLE_PENDING_CHECK_IN,
                        values, selection, selectionArgs);
                break;
            case PENDING_CHECK_IN_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(PendingCheckInTable.TABLE_PENDING_CHECK_IN,
                            values, PendingCheckInTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = sqlDB.update(PendingCheckInTable.TABLE_PENDING_CHECK_IN,
                            values, PendingCheckInTable.COLUMN_ID + "=" + id
                                    + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = {PendingCheckInTable.COLUMN_DATE, PendingCheckInTable.COLUMN_ID};

        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<>(Arrays.asList(available));

            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

    private class PendingCheckInDatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "diabetes.db";
        private static final int DATABASE_VERSION = 1;

        public PendingCheckInDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            PendingCheckInTable.onCreate(database);
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            PendingCheckInTable.onUpgrade(database, oldVersion, newVersion);
        }
    }
}