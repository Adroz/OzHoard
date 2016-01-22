package com.workshoporange.android.ozhoard.data;

import android.net.Uri;
import android.test.AndroidTestCase;

public class TestDealsContract extends AndroidTestCase {

    // Intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final String TEST_DEAL_CATEGORY = "/Toys";
    private static final long TEST_DEAL_DATE = 1419033600L;  // December 20th, 2014

    public void testBuildDealCategory() {
        Uri categoryUri = DealsContract.DealEntry.buildDealCategory(TEST_DEAL_CATEGORY);
        assertNotNull("Error: Null Uri returned. You must fill-in buildDealCategory in " +
                        "DealsContract.",
                categoryUri);
        assertEquals("Error: Deal category not properly appended to the end of the Uri",
                TEST_DEAL_CATEGORY, categoryUri.getLastPathSegment());
        assertEquals("Error: Deal category Uri doesn't match our expected result",
                categoryUri.toString(),
                "content://com.workshoporange.android.ozhoard/deals/%2FToys");
    }
}