package com.workshoporange.android.ozhoard.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;

import com.workshoporange.android.ozhoard.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
        StringBuilder stringBuilder = new StringBuilder();

        // Time since posted
        String postTimeString = getFriendlyTime(postTime);
        stringBuilder.append(postTimeString);
        if (!postTimeString.contains("/")) stringBuilder.append(" ago");

        // Number of comments
        stringBuilder.append(" \u2022 ");
        String commentString = (comments == 1) ? "1 comment" : comments + " comments";
        stringBuilder.append(commentString);

        // Time until expiry
        if (expiry != 0L) {
            stringBuilder.append(" \u2022 ");
            String expiryString = getFriendlyTime(expiry);
            stringBuilder.append("expires ");
            if (!postTimeString.contains("/")) stringBuilder.append("in ");
            stringBuilder.append(expiryString);
        }

        return stringBuilder.toString();
    }

    // Format used for storing dates in the database.  Also used for converting those strings
    // back into date objects for comparison/processing.
    public static final String OB_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss ZZZZZ";
    public static final String OB_EXPIRY_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";

    /**
     * Converts a date string into its equivalent time (in milliseconds) since Jan 1st, 1970 UTC.
     *
     * @param time       The time string to convert
     * @param dateFormat The format guideline used to convert the string
     * @return The converted time, in milliseconds. Or returns 0 if there was a parsing exception.
     */
    public static long formatDateToMillis(String time, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.US);
        try {
            Date date = formatter.parse(time);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.
     *
     * @param dateInMillis The date in milliseconds
     * @return A user-friendly representation of the date.
     */
    public static String getFriendlyTime(long dateInMillis) {
        // Return string is in the format:
        // Less than 1hr: "12min ago" or "in 12min"
        // Same day: "6hrs ago" or "in 6hrs"
        // Less than a week: "5 days ago" or "in 5 days"
        // Less than 2 months: "6 weeks ago" or "in 6 weeks"
        // More than 2 months: "23/01/2016"
        long currentTime = System.currentTimeMillis();
        String differenceString;

        long timeDifference[] = getDateDifference(currentTime, dateInMillis);
        if (timeDifference[0] < 60) {
            // Less than 1 hour
            String minString = (timeDifference[0] == 1) ? "min" : "mins";
            differenceString = timeDifference[0] + minString;
        } else if (timeDifference[1] < 24) {
            // Less than 1 day
            String hrString = (timeDifference[1] == 1) ? "hr" : "hrs";
            differenceString = timeDifference[1] + hrString;
        } else if (timeDifference[2] < 7) {
            // Less than 1 week
            String dayString = (timeDifference[2] == 1) ? " day" : " days";
            differenceString = timeDifference[2] + dayString;
        } else if (timeDifference[3] < 4) {
            // Less than 4 weeks
            String weekString = (timeDifference[3] == 1) ? " week" : " weeks";
            differenceString = timeDifference[3] + weekString;
        } else if (timeDifference[3] < 12) {
            // Less than 3 months
            int monthDifference = getMonthDifference(currentTime, dateInMillis);
            String monthString = (monthDifference == 1) ? " month" : " months";
            differenceString = monthString + monthString;
        } else {
            Date date = new Date(dateInMillis);
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy", Locale.US);
            differenceString = df.format(date);
        }
        return differenceString;
    }

    /**
     * Gets the minute, hour, day and week difference between two dates.
     *
     * @param dateInMillis1 The first date to compare, in milliseconds
     * @param dateInMillis2 The second date to compare, in milliseconds
     * @return An array of all four time differences
     */
    public static long[] getDateDifference(long dateInMillis1, long dateInMillis2) {
        long diff = Math.abs(dateInMillis1 - dateInMillis2);
        long difference[] = new long[4];
        difference[0] = diff / (60 * 1000);
        difference[1] = diff / (60 * 60 * 1000);
        difference[2] = diff / (24 * 60 * 60 * 1000);
        difference[3] = diff / (7 * 24 * 60 * 60 * 1000);

        return difference;
    }

    /**
     * Gets the number of months difference between two dates. Ignores the day of month in the
     * calculations.
     *
     * @param dateInMillis1 The first date to compare, in milliseconds
     * @param dateInMillis2 The second date to compare, in milliseconds
     * @return The month difference, as an absolute (positive) value
     */
    public static int getMonthDifference(long dateInMillis1, long dateInMillis2) {
        Calendar calendar1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar1.setTimeInMillis(dateInMillis1);

        Calendar calendar2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar2.setTimeInMillis(dateInMillis2);

        int months = (calendar1.get(Calendar.YEAR) - calendar2.get(Calendar.YEAR)) * 12 +
                ((calendar1.get(Calendar.MONTH) - calendar2.get(Calendar.MONTH)));
        return Math.abs(months);
    }

    /**
     * Checks if two dates are on the same day.
     *
     * @param dateInMillis1 The first date to compare
     * @param dateInMillis2 The second date to compare
     * @return True if both dates are the same day
     */
    public static boolean isSameDay(long dateInMillis1, long dateInMillis2) {
        Calendar calendar1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar1.setTimeInMillis(dateInMillis1);

        Calendar calendar2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar2.setTimeInMillis(dateInMillis2);

        return ((calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)) &&
                (calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)));
    }
}
