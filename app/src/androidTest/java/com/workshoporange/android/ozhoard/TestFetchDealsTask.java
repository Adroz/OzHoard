package com.workshoporange.android.ozhoard;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.workshoporange.android.ozhoard.data.DealsContract;

public class TestFetchDealsTask extends AndroidTestCase {
    static final String ADD_CATEGORY_PATH = "gaming";
    static final String ADD_CATEGORY_TITLE = "Gaming";

    @TargetApi(11)
    public void testAddCategory() {
        // start from a clean state
        getContext().getContentResolver().delete(DealsContract.CategoryEntry.CONTENT_URI,
                DealsContract.CategoryEntry.COLUMN_CATEGORY_PATH + " = ?",
                new String[]{ADD_CATEGORY_PATH});

        FetchDealsTask fdt = new FetchDealsTask(getContext());
        long categoryId = fdt.addCategory(ADD_CATEGORY_PATH, ADD_CATEGORY_TITLE);

        // Does addCategory return a valid record ID?
        assertFalse("Error: addCategory returned an invalid ID on insert", categoryId == -1);

        // Test all this twice
        for (int i = 0; i < 2; i++) {
            // Does the ID point to the right category?
            Cursor categoryCursor = getContext().getContentResolver().query(
                    DealsContract.CategoryEntry.CONTENT_URI,
                    new String[]{
                            DealsContract.CategoryEntry._ID,
                            DealsContract.CategoryEntry.COLUMN_CATEGORY_PATH,
                            DealsContract.CategoryEntry.COLUMN_CATEGORY_TITLE,
                    },
                    DealsContract.CategoryEntry.COLUMN_CATEGORY_PATH + " = ?",
                    new String[]{ADD_CATEGORY_PATH},
                    null);

            // these match the indices of the projection
            if (categoryCursor.moveToFirst()) {
                assertEquals("Error: the queried value of categoryId does not match the returned value" +
                        "from addCategory", categoryCursor.getLong(0), categoryId);
                assertEquals("Error: the queried value of category setting is incorrect",
                        categoryCursor.getString(1), ADD_CATEGORY_PATH);
                assertEquals("Error: the queried value of category city is incorrect",
                        categoryCursor.getString(2), ADD_CATEGORY_TITLE);
            } else {
                fail("Error: The id you used to query returned an empty cursor");
            }

            // There should be no more records
            assertFalse("Error: there should be only one record returned from a category query",
                    categoryCursor.moveToNext());

            // Add the category again
            long newCategoryId = fdt.addCategory(ADD_CATEGORY_PATH, ADD_CATEGORY_TITLE);

            assertEquals("Error: Inserting a category again should return the same ID",
                    categoryId, newCategoryId);
        }
        // Reset state back to normal
        getContext().getContentResolver().delete(DealsContract.CategoryEntry.CONTENT_URI,
                DealsContract.CategoryEntry.COLUMN_CATEGORY_PATH + " = ?",
                new String[]{ADD_CATEGORY_PATH});

        // Clean up the test so that other tests can use the content provider
        getContext().getContentResolver().
                acquireContentProviderClient(DealsContract.CategoryEntry.CONTENT_URI).
                getLocalContentProvider().shutdown();
    }
}