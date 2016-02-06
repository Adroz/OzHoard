package com.workshoporange.android.ozhoard.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the deals database.
 */
public class DealsContract {

    public static final String CONTENT_AUTHORITY = "com.workshoporange.android.ozhoard";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_DEALS = "deals";
    public static final String PATH_CATEGORY = "category";

    /**
     * Inner class that defines the table contents of the category table
     */
    public static final class CategoryEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORY).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;

        public static Uri buildCategoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String TABLE_NAME = "category";

        // Category label. Human readable for displaying nice label.
        public static final String COLUMN_CATEGORY_TITLE = "category_title";
        // In order to navigate to the correct category, a short path is needed.
        public static final String COLUMN_CATEGORY_PATH = "category_path";
    }

    /**
     * Inner class that defines the table contents of the deal table
     */
    public static final class DealEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DEALS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DEALS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DEALS;


        public static Uri buildDealUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildDealCategory(String categoryPath) {
            return CONTENT_URI.buildUpon().appendPath(categoryPath).build();
        }

        public static Uri buildDealCategoryWithStartDate(String categoryPath, long startDate) {
            return CONTENT_URI.buildUpon().appendPath(categoryPath)
                    .appendQueryParameter(COLUMN_DATE, Long.toString(startDate)).build();
        }

        public static Uri buildDealCategoryWithDate(String categoryPath, long date) {
            return CONTENT_URI.buildUpon().appendPath(categoryPath)
                    .appendPath(Long.toString(date)).build();
        }

        public static Uri buildDealCategoryWithLink(String categoryPath, String link) {
            return CONTENT_URI.buildUpon().appendPath(categoryPath)
                    .appendPath(link).build();
        }

        public static String getCategoryPathFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static long getDateFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }

        public static String getLinkFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static long getStartDateFromUri(Uri uri) {
            String dateString = uri.getQueryParameter(COLUMN_DATE);
            if (null != dateString && dateString.length() > 0)
                return Long.parseLong(dateString);
            else
                return 0;
        }

        public static final String TABLE_NAME = "deals";

        // Column with the foreign key into the category table.
        public static final String COLUMN_CAT_KEY = "category_id";

        // Date and time, in milliseconds (long)
        public static final String COLUMN_DATE = "date";
        // Title (String)
        public static final String COLUMN_TITLE = "title";
        // Link (URL as String)
        public static final String COLUMN_LINK = "link";
        // Description
        public static final String COLUMN_DESC = "description";
        // Author (String)
        public static final String COLUMN_AUTHOR = "author";
        // Net score: positive-negative scores (int)
        public static final String COLUMN_SCORE = "score";
        // Comment count (int)
        public static final String COLUMN_COMMENT_COUNT = "comment_count";
        // Expiry date and time, in milliseconds (long)
        public static final String COLUMN_EXPIRY = "expiry";
        // Image URL (String)
        public static final String COLUMN_IMAGE = "image";
    }
}
