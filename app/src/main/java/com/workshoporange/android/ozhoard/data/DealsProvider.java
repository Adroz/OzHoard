package com.workshoporange.android.ozhoard.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.workshoporange.android.ozhoard.data.DealsContract.CategoryEntry;

import static com.workshoporange.android.ozhoard.data.DealsContract.CONTENT_AUTHORITY;
import static com.workshoporange.android.ozhoard.data.DealsContract.DealEntry;
import static com.workshoporange.android.ozhoard.data.DealsContract.PATH_CATEGORY;
import static com.workshoporange.android.ozhoard.data.DealsContract.PATH_DEALS;

public class DealsProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DealsDbHelper mOpenHelper;

    static final int DEALS = 100;
    static final int DEALS_WITH_CATEGORY = 101;
    static final int DEALS_WITH_CATEGORY_AND_DATE = 102;
    static final int CATEGORY = 300;

    private static final SQLiteQueryBuilder sDealsByCategoryIdQueryBuilder;

    static {
        sDealsByCategoryIdQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //deal INNER JOIN category ON deal.category_id = category._id
        sDealsByCategoryIdQueryBuilder.setTables(
                DealEntry.TABLE_NAME + " INNER JOIN " +
                        CategoryEntry.TABLE_NAME +
                        " ON " + DealEntry.TABLE_NAME +
                        "." + DealEntry.COLUMN_CAT_KEY +
                        " = " + CategoryEntry.TABLE_NAME +
                        "." + CategoryEntry._ID);
    }

    //category.category_path = ?
    private static final String sCategoryPathSelection =
            CategoryEntry.TABLE_NAME +
                    "." + CategoryEntry.COLUMN_CATEGORY_PATH + " = ? ";

    //category.category_path = ? AND date >= ?
    private static final String sCategoryPathWithStartDateSelection =
            CategoryEntry.TABLE_NAME +
                    "." + CategoryEntry.COLUMN_CATEGORY_PATH + " = ? AND " +
                    DealEntry.COLUMN_DATE + " >= ? ";

    //category.category_path = ? AND date = ?
    private static final String sCategoryPathAndDaySelection =
            CategoryEntry.TABLE_NAME +
                    "." + CategoryEntry.COLUMN_CATEGORY_PATH + " = ? AND " +
                    DealEntry.COLUMN_DATE + " = ? ";

    private Cursor getDealsByCategoryPath(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = DealEntry.getCategoryPathFromUri(uri);
        long startDate = DealEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0) {
            selection = sCategoryPathSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
            selection = sCategoryPathWithStartDateSelection;
        }

        return sDealsByCategoryIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getWeatherByLocationSettingAndDate(
            Uri uri, String[] projection, String sortOrder) {
        String locationSetting = DealEntry.getCategoryPathFromUri(uri);
        long date = DealEntry.getDateFromUri(uri);

        return sDealsByCategoryIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sCategoryPathAndDaySelection,
                new String[]{locationSetting, Long.toString(date)},
                null,
                null,
                sortOrder
        );
    }


    static UriMatcher buildUriMatcher() {
        final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CONTENT_AUTHORITY;

        sURIMatcher.addURI(authority, PATH_DEALS, DEALS);
        sURIMatcher.addURI(authority, PATH_DEALS + "/*", DEALS_WITH_CATEGORY);
        sURIMatcher.addURI(authority, PATH_DEALS + "/*/#", DEALS_WITH_CATEGORY_AND_DATE);
        sURIMatcher.addURI(authority, PATH_CATEGORY, CATEGORY);

        return sURIMatcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new DealsDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case DEALS_WITH_CATEGORY_AND_DATE:
                return DealEntry.CONTENT_ITEM_TYPE;
            case DEALS_WITH_CATEGORY:
                return DealEntry.CONTENT_TYPE;
            case DEALS:
                return DealEntry.CONTENT_TYPE;
            case CATEGORY:
                return CategoryEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "weather/*/*"
            case DEALS_WITH_CATEGORY_AND_DATE:
                retCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
                break;
            // "weather/*"
            case DEALS_WITH_CATEGORY:
                retCursor = getDealsByCategoryPath(uri, projection, sortOrder);
                break;
            // "weather"
            case DEALS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DealEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            // "location"
            case CATEGORY:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CategoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case DEALS: {
                normalizeDate(values);
                long _id = db.insert(DealEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DealEntry.buildDealUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CATEGORY: {
                long _id = db.insert(CategoryEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = CategoryEntry.buildCategoryUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        if (null == selection) selection = "1";
        switch (match) {
            case DEALS: {
                rowsDeleted = db.delete(DealEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case CATEGORY: {
                rowsDeleted = db.delete(CategoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(DealEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(DealEntry.COLUMN_DATE);
            values.put(DealEntry.COLUMN_DATE, DealsContract.normalizeDate(dateValue));
        }
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case DEALS: {
                normalizeDate(values);
                rowsUpdated = db.update(DealEntry.TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            }
            case CATEGORY: {
                rowsUpdated = db.update(CategoryEntry.TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DEALS:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        Log.d("TAG", value.toString());
                        long _id = db.insert(DealEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // This is a method specifically to assist the testing framework in running smoothly. More info
    // at: http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}