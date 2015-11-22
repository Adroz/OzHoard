package com.workshoporange.android.ozhoard.data;

import android.provider.BaseColumns;

/**
 * Defines table and column names for the weather database.
 */
public class DealsContract {
    /**
     * Inner class that defines the table contents of the category table
     */
    public static final class CategoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "category";

        // Category label. Human readable for displaying nice label.
        public static final String COLUMN_LABEL = "label";
        // In order to navigate to the correct category, a short path is needed.
        public static final String COLUMN_PATH = "path";
    }

    /**
     * Inner class that defines the table contents of the deal table
     */
    public static final class DealEntry implements BaseColumns {

        public static final String TABLE_NAME = "deal";

        // Column with the foreign key into the category table.
        public static final String COLUMN_CAT_KEY = "category_id";

        // Date and time, stored as a human readable String
        public static final String COLUMN_DATE = "date";
        // Title (String)
        public static final String COLUMN_TITLE = "title";
        // Link (URL as String)
        public static final String COLUMN_LINK = "link";
        // Description
        public static final String COLUMN_DESC = "description";
        // Author (String)
        public static final String COLUMN_AUTHOR = "author";
    }
}
