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
        long convertedTime = Utility.formatDateToMillis(TEST_TIME, Utility.OB_DATE_FORMAT);

        assertEquals("Error: Time not converted correctly to milliseconds",
                expectedTime, convertedTime);

        expectedTime = 1454158800000L;
        convertedTime = Utility.formatDateToMillis(TEST_EXPIRY, Utility.OB_EXPIRY_DATE_FORMAT);

        assertEquals("Error: Expiry time not converted correctly to milliseconds",
                expectedTime, convertedTime);

        // 23/01/2016, 01/12/2015
        checkMonths(1453542323588L, 1448974800000L, 1);
        // 28/12/2016, 09/01/2015
        checkMonths(1482930000000L, 1420808400000L, 23);
        // 23/01/2016, 31/01/2000 - Unexpected, as app uses dd/mm/yyyy format for large differences in dates
        checkMonths(1453542323588L, 949323600000L, 192);
    }

    /**
     * Check given dates for month difference, compared with input expected difference.
     *
     * @param date1    Date one, in milliseconds
     * @param date2    Date two, in milliseconds
     * @param expected The expected date difference
     */
    private void checkMonths(long date1, long date2, int expected) {
        int monthDifference = Utility.getMonthDifference(date1, date2);
        assertEquals("Error: Month difference incorrect", expected, monthDifference);
        // Try the dates in reverse too, for good measure
        monthDifference = Utility.getMonthDifference(date2, date1);
        assertEquals("Error: Month difference incorrect", expected, monthDifference);


    }

}
