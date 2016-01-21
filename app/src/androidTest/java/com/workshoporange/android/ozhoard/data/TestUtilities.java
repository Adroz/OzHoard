package com.workshoporange.android.ozhoard.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.workshoporange.android.ozhoard.utils.PollingCheck;

import java.util.Map;
import java.util.Set;


public class TestUtilities extends AndroidTestCase {
    static final String TEST_CATEGORY = "gaming";
    static final long TEST_DATE = 1419033600L;  // December 20th, 2014

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /**
     * Create some default weather values for database tests.
     */
    static ContentValues createDealValues(long locationRowId) {
        ContentValues dealValues = new ContentValues();
        dealValues.put(DealsContract.DealEntry.COLUMN_CAT_KEY, locationRowId);
        dealValues.put(DealsContract.DealEntry.COLUMN_DATE, TEST_DATE);
        dealValues.put(DealsContract.DealEntry.COLUMN_TITLE, "Deals, deals, deals!");
        dealValues.put(DealsContract.DealEntry.COLUMN_LINK, "www.deals.com");
        dealValues.put(DealsContract.DealEntry.COLUMN_DESC, "Boy have I got some deals for you!");
        dealValues.put(DealsContract.DealEntry.COLUMN_AUTHOR, "Nik Moores");

        return dealValues;
    }

    static ContentValues createGamingCategoryValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(DealsContract.CategoryEntry.COLUMN_CATEGORY_PATH, TEST_CATEGORY);
        testValues.put(DealsContract.CategoryEntry.COLUMN_CATEGORY_TITLE, "Gaming");

        return testValues;
    }

    static long insertGamingCategoryValues(Context context) {
        // insert test records into the database
        DealsDbHelper dbHelper = new DealsDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createGamingCategoryValues();

        long locationRowId;
        locationRowId = db.insert(DealsContract.CategoryEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Gaming Category Values", locationRowId != -1);

        return locationRowId;
    }

    /**
     * This utility class is used to test the ContentObserver callbacks using the PollingCheck
     * class. Note that this only tests that onChange is called, and not that the correct URI is
     * returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
