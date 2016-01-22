package com.workshoporange.android.ozhoard;

import android.test.AndroidTestCase;

/**
 * Created by Nik on 22/01/2016.
 */
public class TestUtility extends AndroidTestCase {

    final String TEST_TIME = "Wed, 20 Jan 2016 07:56:17 +1100";

    public void testTimeStringToLong() {
        long expectedTime = 1453236977000L;
        long convertedTime = Utility.formatDateToLong(TEST_TIME, Utility.OB_DATE_FORMAT);

        assertEquals("Error: Time not converted correctly to milliseconds",
                expectedTime, convertedTime);
    }

}
