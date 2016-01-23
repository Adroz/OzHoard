package com.workshoporange.android.ozhoard.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;

import com.workshoporange.android.ozhoard.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Nik on 21/01/2016.
 */
public class Utility {

    public static Spanned formatAuthorAndCategory(Context context, String author, String category) {
        String themeColor = String.format("#%06X",
                (0xFFFFFF & ContextCompat.getColor(context, R.color.colorPrimaryDark)));
        return Html.fromHtml("<font color=\"" + themeColor + "\">" + author + "</font>"
                + " in " +
                "<font color=\"" + themeColor + "\">" + category + "</font>");
    }

    public static String formatTimeCommentsExpiry(long postTime, int comments, long expiry) {
        String postTimeString = getFriendlyTime(postTime);
        String commentString = (comments == 1) ? "1 comment" : comments + " comments";
        String expiryString = getFriendlyTime(expiry);
        return postTimeString + " ago - " + commentString + " - expires in " + expiryString;
    }

    public static String getFriendlyTime(long time) {
        return "dummyTime";
    }

    // Format used for storing dates in the database.  Also used for converting those strings
    // back into date objects for comparison/processing.
    public static final String OB_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss ZZZZZ";

    public static long formatDateToLong(String time, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.US);
        try {
            Date date = formatter.parse(time);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;


//        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
//        calendar.setTimeInMillis(date);
    }

    public static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }

//    // Format used for storing dates in the database.  ALso used for converting those strings
//    // back into date objects for comparison/processing.
//    public static final String DATE_FORMAT = "yyyyMMdd";
//
//    /**
//     * Helper method to convert the database representation of the date into something to display
//     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
//     *
//     * @param context Context to use for resource localization
//     * @param dateInMillis The date in milliseconds
//     * @return a user-friendly representation of the date.
//     */
//    public static String getFriendlyDayString(Context context, long dateInMillis) {
//        // The day string for forecast uses the following logic:
//        // For today: "Today, June 8"
//        // For tomorrow:  "Tomorrow"
//        // For the next 5 days: "Wednesday" (just the day name)
//        // For all days after that: "Mon Jun 8"
//
//        Time time = new Time();
//        time.setToNow();
//        long currentTime = System.currentTimeMillis();
//        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
//        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);
//
//        // If the date we're building the String for is today's date, the format
//        // is "Today, June 24"
//        if (julianDay == currentJulianDay) {
//            String today = context.getString(R.string.today);
//            int formatId = R.string.format_full_friendly_date;
//            return String.format(context.getString(
//                    formatId,
//                    today,
//                    getFormattedMonthDay(context, dateInMillis)));
//        } else if ( julianDay < currentJulianDay + 7 ) {
//            // If the input date is less than a week in the future, just return the day name.
//            return getDayName(context, dateInMillis);
//        } else {
//            // Otherwise, use the form "Mon Jun 3"
//            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
//            return shortenedDateFormat.format(dateInMillis);
//        }
//    }
//
//    /**
//     * Given a day, returns just the name to use for that day.
//     * E.g "today", "tomorrow", "wednesday".
//     *
//     * @param context Context to use for resource localization
//     * @param dateInMillis The date in milliseconds
//     * @return
//     */
//    public static String getDayName(Context context, long dateInMillis) {
//        // If the date is today, return the localized version of "Today" instead of the actual
//        // day name.
//
//        Time t = new Time();
//        t.setToNow();
//        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
//        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
//        if (julianDay == currentJulianDay) {
//            return context.getString(R.string.today);
//        } else if ( julianDay == currentJulianDay +1 ) {
//            return context.getString(R.string.tomorrow);
//        } else {
//            Time time = new Time();
//            time.setToNow();
//            // Otherwise, the format is just the day of the week (e.g "Wednesday".
//            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
//            return dayFormat.format(dateInMillis);
//        }
//    }
//
//    /**
//     * Converts db date format to the format "Month day", e.g "June 24".
//     * @param context Context to use for resource localization
//     * @param dateInMillis The db formatted date string, expected to be of the form specified
//     *                in Utility.DATE_FORMAT
//     * @return The day in the form of a string formatted "December 6"
//     */
//    public static String getFormattedMonthDay(Context context, long dateInMillis ) {
//        Time time = new Time();
//        time.setToNow();
//        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
//        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
//        String monthDayString = monthDayFormat.format(dateInMillis);
//        return monthDayString;
//    }
}
