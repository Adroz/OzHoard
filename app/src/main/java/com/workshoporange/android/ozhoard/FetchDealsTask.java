package com.workshoporange.android.ozhoard;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.workshoporange.android.ozhoard.utils.Utility;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import static com.workshoporange.android.ozhoard.data.DealsContract.CategoryEntry;
import static com.workshoporange.android.ozhoard.data.DealsContract.DealEntry;

public class FetchDealsTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchDealsTask.class.getSimpleName();

    private final Context mContext;

    public FetchDealsTask(Context context) {
        mContext = context;
    }

    private boolean DEBUG = true;

    /**
     * Helper method to handle insertion of a new category in the deal database.
     *
     * @param categoryPath  The category path string used to request updates from the server.
     * @param categoryTitle A human-readable category title, e.g "Gaming"
     * @return the row ID of the added category.
     */
    long addCategory(String categoryPath, String categoryTitle) {
        Long categoryId;
        // Check if category exists (if it does, then return the current ID).
        Cursor categoryCursor = mContext.getContentResolver().query(
                CategoryEntry.CONTENT_URI,
                new String[]{CategoryEntry._ID},
                CategoryEntry.COLUMN_CATEGORY_PATH + " = ?",
                new String[]{categoryPath},
                null);

        if (categoryCursor.moveToFirst()) {
            int categoryIdIndex = categoryCursor.getColumnIndex(CategoryEntry._ID);
            categoryId = categoryCursor.getLong(categoryIdIndex);
        } else { // Else insert into database
            ContentValues values = new ContentValues();
            values.put(CategoryEntry.COLUMN_CATEGORY_TITLE, categoryTitle);
            values.put(CategoryEntry.COLUMN_CATEGORY_PATH, categoryPath);

            Uri insertedUri = mContext.getContentResolver().insert(CategoryEntry.CONTENT_URI, values);

            categoryId = ContentUris.parseId(insertedUri);
        }
        categoryCursor.close();
        return categoryId;
    }

    /**
     * Take the String representing the complete deal list in XML Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     */
    private void getDealsDataFromRss(String feedXmlStr, String categoryPath)
            throws XmlPullParserException, IOException {

        /** We're looking for the <code>"<title>"</code> and <code>"<link>"</code> tags (among
         * others), which appear inside the <code>"<item>"</code> tag. The main tags wanted also
         * appear within the RSS feed's <code>"<channel>"</code> tag as well, so use
         * @link{insideItem} to ensure we're in the right tag.
         */
        boolean insideItem = false;

        // These are the names of the XML tags that need to be extracted.
        final String RSS_ITEM = "item";
        final String RSS_TITLE = "title";
        final String RSS_DESCRIPTION = "description";
        final String OB_DATE = "pubDate";
        final String OB_AUTHOR = "creator";
        final String RSS_THUMBNAIL = "thumbnail";

        final String OB_META = "meta";
        final String META_COMMENT_COUNT = "comment-count";
        final String META_URL = "url";

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(feedXmlStr));

        ArrayList<String> headlines = new ArrayList<>();
        ArrayList<String> links = new ArrayList<>();
        ArrayList<String> descriptions = new ArrayList<>();
        ArrayList<Long> dates = new ArrayList<>();
        ArrayList<String> authors = new ArrayList<>();
        ArrayList<Integer> scores = new ArrayList<>();
        ArrayList<Integer> comments = new ArrayList<>();
        ArrayList<Long> expiries = new ArrayList<>();
        ArrayList<String> imageUrls = new ArrayList<>();

        String categoryTitle = "";

        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (xpp.getName().equalsIgnoreCase(RSS_ITEM)) {
                    insideItem = true;
                } else if (xpp.getName().equalsIgnoreCase(RSS_TITLE)) {
                    if (insideItem) {
                        headlines.add(xpp.nextText());      // Extract the headline
                    } else {
                        categoryTitle = xpp.nextText();     // Extract page title
                    }
                } else if (xpp.getName().equalsIgnoreCase(RSS_DESCRIPTION)) {
                    if (insideItem)
                        descriptions.add(xpp.nextText());   // Extract the deal's description
                } else if (xpp.getName().equalsIgnoreCase(OB_DATE)) {
                    if (insideItem) {
                        long date = Utility.formatDateToMillis(xpp.nextText(), Utility.OB_DATE_FORMAT);
                        dates.add(date);                    // Extract the posting date
                    }
                } else if (xpp.getName().equalsIgnoreCase(OB_AUTHOR)) {
                    if (insideItem) authors.add(xpp.nextText()); // Extract the author
                } else if (xpp.getName().equalsIgnoreCase(OB_META)) {
                    if (insideItem) {
                        links.add(xpp.getAttributeValue(null, META_URL));// Extract the deal's link
                        scores.add(getScore(xpp));
                        comments.add(Integer.parseInt(xpp.getAttributeValue(null, META_COMMENT_COUNT)));
                        expiries.add(getExpiry(xpp));
                    }
                } else if (xpp.getName().equalsIgnoreCase(RSS_THUMBNAIL)) {
                    if (insideItem)
                        imageUrls.add(xpp.getAttributeValue(null, META_URL)); // Extract the image URL
                }
            }
            eventType = xpp.next();
        }
        long categoryId = addCategory(categoryPath, categoryTitle);
        Vector<ContentValues> cVVector = new Vector<>(headlines.size());

        for (int i = 0; i < headlines.size(); i++) {
            ContentValues dealValues = new ContentValues();

            dealValues.put(DealEntry.COLUMN_CAT_KEY, categoryId);
            dealValues.put(DealEntry.COLUMN_DATE, dates.get(i));
            dealValues.put(DealEntry.COLUMN_TITLE, headlines.get(i));
            dealValues.put(DealEntry.COLUMN_LINK, links.get(i));
            dealValues.put(DealEntry.COLUMN_DESC, descriptions.get(i));
            dealValues.put(DealEntry.COLUMN_AUTHOR, authors.get(i));
            dealValues.put(DealEntry.COLUMN_SCORE, scores.get(i));
            dealValues.put(DealEntry.COLUMN_COMMENT_COUNT, comments.get(i));
            dealValues.put(DealEntry.COLUMN_EXPIRY, expiries.get(i));
            dealValues.put(DealEntry.COLUMN_IMAGE, imageUrls.get(i));

            cVVector.add(dealValues);
        }

        int inserted = 0;
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(DealEntry.CONTENT_URI, cvArray);
        }
        Log.d(LOG_TAG, "FetchDealsTask Complete. " + inserted + " Inserted");
    }

    private int getScore(XmlPullParser xmlPullParser) {
        final String META_POSITIVE = "votes-pos";
        final String META_NEGATIVE = "votes-neg";

        int pos = Integer.parseInt(xmlPullParser.getAttributeValue(null, META_POSITIVE));
        int neg = Integer.parseInt(xmlPullParser.getAttributeValue(null, META_NEGATIVE));
        return pos - neg;
    }

    private long getExpiry(XmlPullParser xmlPullParser) {
        final String META_EXPIRY = "expiry";

        String expiryString = xmlPullParser.getAttributeValue(null, META_EXPIRY);
        return (expiryString == null) ? 0L : Utility.formatDateToMillis(expiryString, Utility.OB_EXPIRY_DATE_FORMAT);
    }

    private int getCommentCount(String meta) {

        return 0;
    }

    private int getUrl(String meta) {

        return 0;
    }

    @Override
    protected Void doInBackground(String... params) {
        // If there's no category, then set to default.  Verify size of params.
        if (params.length == 0) {
            Log.v(LOG_TAG, "No category input, using default.");
            params = new String[]{"deals"};
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw XML response as a string.
        String feedXmlStr = null;

        try {
            // Construct the URL for OzBargain's feed.
            final String OZBARGAIN_BASE_URL = "https://www.ozbargain.com.au/";
//            final String OZBARGAIN_CATEGORY_URL = "cat";

            Uri builtUri = Uri.parse(OZBARGAIN_BASE_URL).buildUpon()
                    .appendPath(params[0])
                    .appendPath("feed")
                    .build();

            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, "Constructed URI " + builtUri.toString());

            // Create the request to OzBargain, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's XML, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            feedXmlStr = buffer.toString();
            getDealsDataFromRss(feedXmlStr, params[0]);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the deal data, there's no point in attempting
            // to parse it.
            return null;
        } catch (XmlPullParserException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return null;
    }
}