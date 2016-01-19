/*
 * Copyright (C) 2015 Nicholas Moores
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.workshoporange.android.ozhoard.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    /**
     * Deletes the database
     */
    void deleteTheDatabase() {
        mContext.deleteDatabase(DealsDbHelper.DATABASE_NAME);
    }

    /**
     * This method gets called before each test is executed to delete the database. This is to
     * ensure that it's always a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    /**
     * Tests that the database can be created and that the tables have the correct columns. The
     * tests check that both tables have been created.
     *
     * @throws Throwable
     */
    public void testCreateDb() throws Throwable {
        // Build a HashSet of all of the table names to look for. Note that there will be another
        // table in the DB that stores the Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(DealsContract.CategoryEntry.TABLE_NAME);
        tableNameHashSet.add(DealsContract.DealEntry.TABLE_NAME);

        mContext.deleteDatabase(DealsDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new DealsDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // Have the correct tables been created?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // Verify that the tables have been created.
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        // If this fails, it means that your database doesn't contain both the category entry
        // and deal entry tables.
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // Do the tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + DealsContract.CategoryEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for.
        final HashSet<String> categoryColumnHashSet = new HashSet<>();
        categoryColumnHashSet.add(DealsContract.CategoryEntry._ID);
        categoryColumnHashSet.add(DealsContract.CategoryEntry.COLUMN_LABEL);
        categoryColumnHashSet.add(DealsContract.CategoryEntry.COLUMN_PATH);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            categoryColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        c.close();

        // if this fails, it means that the database doesn't contain all of the required category
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required category entry columns",
                categoryColumnHashSet.isEmpty());
        db.close();
    }

    /**
     * Test category table basics: Can be written to and read from. Data in matches data out.
     */
    public void testCategoryTable() {
        // First step: Get reference to writable database
        SQLiteDatabase db = new DealsDbHelper(this.mContext).getWritableDatabase();

        // Insert category into database
        insertCategory();

        // Query the database and receive a Cursor back
        Cursor c = db.query(DealsContract.CategoryEntry.TABLE_NAME,
                null, null, null, null, null, null);

        // Move the cursor to a valid database row, and check that we got records back
        assertTrue("Error: No Records returned from category query", c.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        TestUtilities.validateCurrentRecord(
                "Error: Category query validation failed",
                c,
                TestUtilities.createGamingCategoryValues());

        // Finally, close the cursor and database
        c.close();
        db.close();
    }

    /**
     * Test deal table basics: Can be written to and read from. Data in matches data out.
     */
    public void testDealsTable() {
        // First step: Get reference to writable database
        SQLiteDatabase db = new DealsDbHelper(this.mContext).getWritableDatabase();

        // Create ContentValues of dummy deal, and insert into database
        long categoryRowId = insertCategory();
        ContentValues testValues = TestUtilities.createDealValues(categoryRowId);
        db.insert(DealsContract.DealEntry.TABLE_NAME, null, testValues);

        // Query the database and receive a Cursor back, assert that it's not empty
        Cursor c = db.query(DealsContract.DealEntry.TABLE_NAME, null, null, null, null, null, null);
        assertTrue("Error: No Records returned from deal query", c.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        // Also check there's not more than one entry at this stage
        TestUtilities.validateCurrentRecord("Error: Deal query validation failed", c, testValues);
        assertFalse("ErrorL More than one record returned from deal query", c.moveToNext());

        // Finally, close the cursor and database
        c.close();
        db.close();
    }

    /**
     * Helper method. Inserts dummy category data into database and returns the row ID of the new
     * row.
     *
     * @return The ID of the new row, or -1 if an error occurred.
     */
    public long insertCategory() {
        // Create and insert dummy ContentValues. Check that a row ID is returned
        long categoryRowId = TestUtilities.insertGamingCategoryValues(this.mContext);
        assertTrue(categoryRowId != -1);
        return categoryRowId;
    }
}
