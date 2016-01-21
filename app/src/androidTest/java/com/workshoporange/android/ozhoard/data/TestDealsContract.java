package com.workshoporange.android.ozhoard.data;

import android.net.Uri;
import android.test.AndroidTestCase;

public class TestDealsContract extends AndroidTestCase {

    // Intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final String TEST_DEAL_CATEGORY = "/Toys";
    private static final long TEST_DEAL_DATE = 1419033600L;  // December 20th, 2014

    public void testBuildWeatherLocation() {
        Uri locationUri = DealsContract.DealEntry.buildDealCategory(TEST_DEAL_CATEGORY);
        assertNotNull("Error: Null Uri returned. You must fill-in buildDealCategory in " +
                        "DealsContract.",
                locationUri);
        assertEquals("Error: Weather location not properly appended to the end of the Uri",
                TEST_DEAL_CATEGORY, locationUri.getLastPathSegment());
        assertEquals("Error: Weather location Uri doesn't match our expected result",
                locationUri.toString(),
                "content://com.workshoporange.android.ozhoard/deals/%2FToys");
    }
}