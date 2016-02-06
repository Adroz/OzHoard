package com.workshoporange.android.ozhoard.utils;

import android.content.Context;
import android.content.res.Resources;
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

    private static final String LOG_TAG = Utility.class.getSimpleName();

    public static Spanned formatAuthorAndCategory(Context context, String author, String category) {
        String themeColor = String.format("#%06X",
                (0xFFFFFF & ContextCompat.getColor(context, R.color.colorPrimaryDark)));
        return Html.fromHtml("<font color=\"" + themeColor + "\">" + author + "</font>"
                + " in " +
                "<font color=\"" + themeColor + "\">" + category + "</font>");
    }

    public static String formatTimeCommentsExpiry(Context context, long postTime, int comments, long expiry) {
        Resources resources = context.getResources();

        // Time since posted string
        String postTimeString = getFriendlyTime(context, postTime);

        // Number of comments
        String commentString = resources.getQuantityString(R.plurals.number_of_comments, comments, comments);

        // If no expiry, use short format
        if (expiry == 0L)
            return resources.getString(R.string.format_time_comments, postTimeString, commentString);

        // Else format with Expiry
        int expiryTenseRes = (System.currentTimeMillis() > expiry) ? R.string.expired : R.string.expires;
        String expiryString = resources.getString(expiryTenseRes) + getFriendlyTime(context, expiry);

        return resources.getString(R.string.format_time_comments_expiry,
                postTimeString, commentString, expiryString);
    }

    private Spanned setStringColour(Context context, String string, int colourResource) {
        String stringColour = String.format("#%06X",
                (0xFFFFFF & ContextCompat.getColor(context, colourResource)));
        return Html.fromHtml("<font color=\"" + stringColour + "\">" + string + "</font>");
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
    public static String getFriendlyTime(Context context, long dateInMillis) {
        // Return string is in the format:
        // Less than 1hr: "12min ago" or "in 12min"
        // Same day: "6hrs ago" or "in 6hrs"
        // Less than a week: "5 days ago" or "in 5 days"
        // Less than 2 months: "6 weeks ago" or "in 6 weeks"
        // More than 2 months: "23/01/2016"
        long currentTime = System.currentTimeMillis();
        Resources resources = context.getResources();
        String differenceString;

        LengthOfTime timeDifference = new LengthOfTime(currentTime, dateInMillis);
        if (timeDifference.hours == 0) {
            // Less than 1 hour
            differenceString = resources.getQuantityString(R.plurals.time_in_minutes,
                    (int) timeDifference.minutes, (int) timeDifference.minutes);
        } else if (timeDifference.days == 0) {
            // Less than 1 day
            differenceString = resources.getQuantityString(R.plurals.time_in_hours,
                    (int) timeDifference.hours, (int) timeDifference.hours);
        } else if (timeDifference.weeks == 0) {
            // Less than 1 week
            differenceString = resources.getQuantityString(R.plurals.time_in_days,
                    (int) timeDifference.days, (int) timeDifference.days);
        } else if (timeDifference.months == 0) {
            // Less than a month
            differenceString = resources.getQuantityString(R.plurals.time_in_weeks,
                    (int) timeDifference.weeks, (int) timeDifference.weeks);
        } else if (timeDifference.months < 4) {
            // Less than 4 months
            differenceString = resources.getQuantityString(R.plurals.time_in_months,
                    (int) timeDifference.months, (int) timeDifference.months);
        } else {
            Date date = new Date(dateInMillis);
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy", Locale.US);
            return df.format(date);
        }

        String returnString;
        if (currentTime > dateInMillis) {
            returnString = (differenceString.contains("/")) ? differenceString : differenceString + " ago";
        } else {
            returnString = (differenceString.contains("/")) ? differenceString : "in " + differenceString;
        }
        return returnString;
    }

    /**
     * Formats the minute, hour, day and week difference between two dates.
     */
    public static class LengthOfTime {
        public long minutes;
        public long hours;
        public long days;
        public long weeks;
        public long months;

        public LengthOfTime(long dateInMillis1, long dateInMillis2) {
            long diff = Math.abs(dateInMillis1 - dateInMillis2);
            minutes = diff / (60 * 1000);
            hours = diff / (60 * 60 * 1000);
            days = diff / (24 * 60 * 60 * 1000);
            weeks = diff / (7 * 24 * 60 * 60 * 1000);
            months = getMonthDifference(dateInMillis1, dateInMillis2);
        }
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
