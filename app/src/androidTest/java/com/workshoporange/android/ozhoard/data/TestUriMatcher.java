package com.workshoporange.android.ozhoard.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

public class TestUriMatcher extends AndroidTestCase {
    private static final String CATEGORY_QUERY = "Home Garden";
    private static final long TEST_DATE = 1419033600L;  // December 20th, 2014
    private static final long TEST_CATEGORY_ID = 10L;

    // content://com.workshoporange.android.ozhoard.app/deals"
    private static final Uri TEST_DEALS_DIR = DealsContract.DealEntry.CONTENT_URI;
    private static final Uri TEST_DEALS_WITH_CATEGORY_DIR =
            DealsContract.DealEntry.buildDealCategory(CATEGORY_QUERY);
    private static final Uri TEST_DEALS_WITH_CATEGORY_AND_DATE_DIR =
            DealsContract.DealEntry.buildDealCategoryWithDate(CATEGORY_QUERY, TEST_DATE);
    // content://com.workshoporange.android.ozhoard.app/category"
    private static final Uri TEST_CATEGORY_DIR = DealsContract.CategoryEntry.CONTENT_URI;

    public void testUriMatcher() {
        UriMatcher testMatcher = DealsProvider.buildUriMatcher();

        assertEquals("Error: The DEALS URI was matched incorrectly.",
                testMatcher.match(TEST_DEALS_DIR), DealsProvider.DEALS);
        assertEquals("Error: The DEALS WITH CATEGORY URI was matched incorrectly.",
                testMatcher.match(TEST_DEALS_WITH_CATEGORY_DIR), DealsProvider.DEALS_WITH_CATEGORY);
        assertEquals("Error: The DEALS WITH CATEGORY AND DATE URI was matched incorrectly.",
                testMatcher.match(TEST_DEALS_WITH_CATEGORY_AND_DATE_DIR), DealsProvider.DEALS_WITH_CATEGORY_AND_DATE);
        assertEquals("Error: The CATEGORY URI was matched incorrectly.",
                testMatcher.match(TEST_CATEGORY_DIR), DealsProvider.CATEGORY);
    }
}