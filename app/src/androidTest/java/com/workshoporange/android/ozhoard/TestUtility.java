package com.workshoporange.android.ozhoard;

import android.test.AndroidTestCase;

import com.workshoporange.android.ozhoard.utils.Utility;

/**
 * Created by Nik on 22/01/2016.
 */
public class TestUtility extends AndroidTestCase {

    final String TEST_TIME = "Wed, 20 Jan 2016 07:56:17 +1100";
    final String TEST_EXPIRY = "2016-01-31T00:00:00+11:00";

    public void testTimeConversions() {
        long expectedTime = 1453236977000L;
        long convertedTime = Utility.formatDateToLong(TEST_TIME, Utility.OB_DATE_FORMAT);

        assertEquals("Error: Time not converted correctly to milliseconds",
                expectedTime, convertedTime);

        expectedTime = 1454158800000L;
        convertedTime = Utility.formatDateToLong(TEST_EXPIRY, Utility.OB_EXPIRY_DATE_FORMAT);

        assertEquals("Error: Expiry time not converted correctly to milliseconds",
                expectedTime, convertedTime);
    }

}
