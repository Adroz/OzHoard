package com.workshoporange.android.ozhoard.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import static com.workshoporange.android.ozhoard.data.DealsContract.CONTENT_AUTHORITY;
import static com.workshoporange.android.ozhoard.data.DealsContract.CategoryEntry;
import static com.workshoporange.android.ozhoard.data.DealsContract.DealEntry;

/*
    Note: Not a complete set of tests of the ContentProvider, but does test that basic functionality
    has been implemented correctly.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /**
     * This helper method deletes all records from both database tables using the ContentProvider.
     * It also queries the ContentProvider to make sure that the database has been successfully
     * deleted, so it cannot be used until the Query and Delete functions have been written
     * in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                DealEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                CategoryEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                DealEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Deals table during delete",
                0,
                cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                CategoryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Category table during delete",
                0,
                cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Run deleteAllRecords in setUp for clean slate (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // Define the component name based on the package name from the context and the
        // DealsProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                DealsProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: DealsProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + CONTENT_AUTHORITY,
                    providerInfo.authority, CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // The provider isn't registered correctly.
            assertTrue("Error: DealsProvider not registered at " + mContext.getPackageName(), false);
        }
    }

    public void testGetType() {
        // content://com.workshoporange.android.ozhoard/deals/
        String type = mContext.getContentResolver().getType(DealEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.workshoporange.android.ozhoard/deals
        assertEquals("Error: the DealEntry CONTENT_URI should return DealEntry.CONTENT_TYPE",
                DealEntry.CONTENT_TYPE, type);

        String testCategory = "internet";
        // content://com.workshoporange.android.ozhoard/category/internet
        type = mContext.getContentResolver().getType(
                DealEntry.buildDealCategory(testCategory));
        // vnd.android.cursor.dir/
        assertEquals("Error: the DealEntry CONTENT_URI with category should return " +
                "DealEntry.CONTENT_TYPE", DealEntry.CONTENT_TYPE, type);

        long testDate = 1419120000L; // December 21st, 2014
        // content://com.workshoporange.android.ozhoard/deals/internet/20140612
        type = mContext.getContentResolver().getType(
                DealEntry.buildDealCategoryWithDate(testCategory, testDate));
        // vnd.android.cursor.item/com.workshoporange.android.ozhoard/deals/1419120000
        assertEquals("Error: the DealEntry CONTENT_URI with category and date should return " +
                "DealEntry.CONTENT_ITEM_TYPE", DealEntry.CONTENT_ITEM_TYPE, type);

        String testLink = "https://www.ozbargain.com.au/node/231028";
        // content://com.workshoporange.android.ozhoard/deals/internet/https://www.ozbargain.com.au/node/231028
        type = mContext.getContentResolver().getType(
                DealEntry.buildDealCategoryWithLink(testCategory, testLink));
        // vnd.android.cursor.item/com.workshoporange.android.ozhoard/deals/https://www.ozbargain.com.au/node/231028
        assertEquals("Error: the DealEntry CONTENT_URI with category and link should return " +
                "DealEntry.CONTENT_ITEM_TYPE", DealEntry.CONTENT_ITEM_TYPE, type);

        // content://com.workshoporange.android.ozhoard/category/
        type = mContext.getContentResolver().getType(CategoryEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.workshoporange.android.ozhoard/category
        assertEquals("Error: the CategoryEntry CONTENT_URI should return CategoryEntry.CONTENT_TYPE",
                CategoryEntry.CONTENT_TYPE, type);
    }

    public void testBasicDealQuery() {
        // Insert test records into the database
        DealsDbHelper dbHelper = new DealsDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        TestUtilities.createGamingCategoryValues();
        long categoryRowId = TestUtilities.insertGamingCategoryValues(mContext);

        // Next add a deal
        ContentValues dealValues = TestUtilities.createDealValues(categoryRowId);

        long dealRowId = db.insert(DealEntry.TABLE_NAME, null, dealValues);
        assertTrue("Unable to Insert DealEntry into the Database", dealRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor dealCursor = mContext.getContentResolver().query(
                DealEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicDealQuery", dealCursor, dealValues);
    }

    public void testBasicCategoryQueries() {
        // Insert test records into the database
        ContentValues testValues = TestUtilities.createGamingCategoryValues();
        TestUtilities.insertGamingCategoryValues(mContext);

        // Test the basic content provider query
        Cursor categoryCursor = mContext.getContentResolver().query(
                CategoryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicCategoryQueries, category query", categoryCursor, testValues);

        // Has the NotificationUri been set correctly? --- can only test this easily against API
        // level 19 or greater because getNotificationUri was added in API level 19.
        if (Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: Category Query did not properly set NotificationUri",
                    categoryCursor.getNotificationUri(), CategoryEntry.CONTENT_URI);
        }
    }

    public void testUpdateCategory() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createGamingCategoryValues();

        Uri categoryUri = mContext.getContentResolver().insert(CategoryEntry.CONTENT_URI, values);
        long categoryRowId = ContentUris.parseId(categoryUri);

        // Verify we got a row back.
        assertTrue(categoryRowId != -1);
        Log.d(LOG_TAG, "New row id: " + categoryRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(CategoryEntry._ID, categoryRowId);
        updatedValues.put(CategoryEntry.COLUMN_CATEGORY_TITLE, "Toys Kids");

        // Create a cursor with observer to ensure that the content provider is notifying the
        // observers as expected
        Cursor categoryCursor = mContext.getContentResolver()
                .query(CategoryEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        categoryCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                CategoryEntry.CONTENT_URI, updatedValues, CategoryEntry._ID + "= ?",
                new String[]{Long.toString(categoryRowId)});
        assertEquals(count, 1);

        // Test to make sure the observer is called.  If not, throw an assertion.
        tco.waitForNotificationOrFail();

        categoryCursor.unregisterContentObserver(tco);
        categoryCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                CategoryEntry.CONTENT_URI,
                null,   // projection
                CategoryEntry._ID + " = " + categoryRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateCategory.  Error validating category entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createGamingCategoryValues();

        // Register a content observer for the insert. This time, directly with the content resolver.
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(CategoryEntry.CONTENT_URI, true, tco);
        Uri categoryUri = mContext.getContentResolver().insert(CategoryEntry.CONTENT_URI, testValues);

        // Did the ContentObserver get called?
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // Parse row and verify valid.
        long categoryRowId = ContentUris.parseId(categoryUri);
        assertTrue(categoryRowId != -1);

        // Data's inserted in theory. Now check that it made it and verify it made the round trip.

        Cursor cursor = mContext.getContentResolver().query(
                CategoryEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.validateCursor("testInsertReadProvider. Error validating CategoryEntry.",
                cursor, testValues);

        ContentValues dealValues = TestUtilities.createDealValues(categoryRowId);
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(DealEntry.CONTENT_URI, true, tco);

        Uri dealInsertUri = mContext.getContentResolver()
                .insert(DealEntry.CONTENT_URI, dealValues);
        assertTrue(dealInsertUri != null);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        Cursor dealsCursor = mContext.getContentResolver().query(
                DealEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.validateCursor("testInsertReadProvider. Error validating DealEntry insert.",
                dealsCursor, dealValues);

        // Add the category values in with the deal data so that it can be ensured that the join
        // worked and all the values are returned.
        dealValues.putAll(testValues);

        // Get the joined Deals and Category data
        dealsCursor = mContext.getContentResolver().query(
                DealEntry.buildDealCategory(TestUtilities.TEST_CATEGORY), null, null, null, null);
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Deals and " +
                "Category Data.", dealsCursor, dealValues);

        // Get the joined Deals and Category data with a start date
        dealsCursor = mContext.getContentResolver().query(
                DealEntry.buildDealCategoryWithStartDate(
                        TestUtilities.TEST_CATEGORY, TestUtilities.TEST_DATE), null, null, null, null);
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Deals and " +
                "Category Data with start date.", dealsCursor, dealValues);

        // Get the joined Deals data for a specific date
        dealsCursor = mContext.getContentResolver().query(
                DealEntry.buildDealCategoryWithDate(TestUtilities.TEST_CATEGORY, TestUtilities.TEST_DATE),
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("testInsertReadProvider. Error validating joined Deals and " +
                "Category data for a specific date.", dealsCursor, dealValues);

        // Get the joined Deals data for a specific link
        dealsCursor = mContext.getContentResolver().query(
                DealEntry.buildDealCategoryWithLink(TestUtilities.TEST_CATEGORY, TestUtilities.TEST_LINK),
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("testInsertReadProvider. Error validating joined Deals and " +
                "Category data for a specific link.", dealsCursor, dealValues);
    }

    // Make sure records can still be deleted after adding/updating stuff.
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for category delete.
        TestUtilities.TestContentObserver categoryObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(CategoryEntry.CONTENT_URI, true, categoryObserver);

        // Register a content observer for deals delete.
        TestUtilities.TestContentObserver dealsObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(DealEntry.CONTENT_URI, true, dealsObserver);

        deleteAllRecordsFromProvider();

        categoryObserver.waitForNotificationOrFail();
        dealsObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(categoryObserver);
        mContext.getContentResolver().unregisterContentObserver(dealsObserver);
    }

    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;

    static ContentValues[] createBulkInsertDealValues(long categoryRowId) {
        long currentTestDate = TestUtilities.TEST_DATE;
        long millisecondsInADay = 1000 * 60 * 60 * 24;
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, currentTestDate += millisecondsInADay) {
            ContentValues dealValues = new ContentValues();
            dealValues.put(DealEntry.COLUMN_CAT_KEY, categoryRowId);
            dealValues.put(DealEntry.COLUMN_DATE, currentTestDate);
            dealValues.put(DealEntry.COLUMN_TITLE, "Title " + i);
            dealValues.put(DealEntry.COLUMN_LINK, "blah-blah-blah.com/" + (10 * i));
            dealValues.put(DealEntry.COLUMN_DESC, "Default TEST TEXT");
            dealValues.put(DealEntry.COLUMN_AUTHOR, "Author #" + i);
            dealValues.put(DealEntry.COLUMN_SCORE, (10 * i + i));
            dealValues.put(DealEntry.COLUMN_COMMENT_COUNT, (BULK_INSERT_RECORDS_TO_INSERT - i));
            dealValues.put(DealEntry.COLUMN_EXPIRY, currentTestDate + 500);
            dealValues.put(DealEntry.COLUMN_IMAGE, TestUtilities.TEST_IMAGE);
            returnContentValues[i] = dealValues;
        }
        return returnContentValues;
    }

    public void testBulkInsert() {
        // Create a category value, verify that it's returned
        ContentValues testValues = TestUtilities.createGamingCategoryValues();
        Uri categoryUri = mContext.getContentResolver().insert(CategoryEntry.CONTENT_URI, testValues);
        long categoryRowId = ContentUris.parseId(categoryUri);
        assertTrue(categoryRowId != -1);

        // Data's inserted in theory. Now check that it made it and verify it made the round trip.

        Cursor cursor = mContext.getContentResolver().query(
                CategoryEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.validateCursor("testBulkInsert. Error validating CategoryEntry.",
                cursor, testValues);

        // Now bulkInsert some deals.
        ContentValues[] bulkInsertContentValues = createBulkInsertDealValues(categoryRowId);

        // Register a content observer for bulk insert.
        TestUtilities.TestContentObserver dealObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(DealEntry.CONTENT_URI, true, dealObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(DealEntry.CONTENT_URI, bulkInsertContentValues);

        dealObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(dealObserver);

        assertEquals(BULK_INSERT_RECORDS_TO_INSERT, insertCount);

        cursor = mContext.getContentResolver().query(
                DealEntry.CONTENT_URI,
                null,
                null,
                null,
                DealEntry.COLUMN_DATE + " ASC"  // sort order == by DATE ASC
        );

        // Should have as many records as we inserted
        assertEquals(BULK_INSERT_RECORDS_TO_INSERT, cursor.getCount());

        // Check that they're the same as what was created
        cursor.moveToFirst();
        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext()) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating DealEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}