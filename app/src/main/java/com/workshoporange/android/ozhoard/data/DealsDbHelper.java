package com.workshoporange.android.ozhoard.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.workshoporange.android.ozhoard.data.DealsContract.CategoryEntry;
import com.workshoporange.android.ozhoard.data.DealsContract.DealEntry;

/**
 * Manages a local database for deals data.
 */
public class DealsDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "deals.db";

    public DealsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " + CategoryEntry.TABLE_NAME +
                " (" + CategoryEntry._ID + " INTEGER PRIMARY KEY ," +
                CategoryEntry.COLUMN_CATEGORY_TITLE + " TEXT NOT NULL, " +
                CategoryEntry.COLUMN_CATEGORY_PATH + " TEXT UNIQUE NOT NULL" +
                ");";

        final String SQL_CREATE_DEALS_TABLE = "CREATE TABLE " + DealEntry.TABLE_NAME + " (" +

                DealEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                // the ID of the category entry associated with this deal data
                DealEntry.COLUMN_CAT_KEY + " INTEGER NOT NULL, " +
                DealEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                DealEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                DealEntry.COLUMN_LINK + " TEXT NOT NULL, " +
                DealEntry.COLUMN_DESC + " TEXT NOT NULL, " +
                DealEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +

                // Set up the category column as a foreign key to category table.
                " FOREIGN KEY (" + DealEntry.COLUMN_CAT_KEY + ") REFERENCES " +
                CategoryEntry.TABLE_NAME + " (" + CategoryEntry._ID + ")" +

                // On same time stamp and category, replace.
                " UNIQUE (" + DealEntry.COLUMN_DATE + ", " +
                DealEntry.COLUMN_CAT_KEY + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_CATEGORY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_DEALS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is to simply to
        // discard the data and start over.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DealEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
