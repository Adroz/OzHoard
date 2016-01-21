package com.workshoporange.android.ozhoard;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.workshoporange.android.ozhoard.data.DealsContract;

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
     * Helper method to handle insertion of a new category in the weather database.
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
            int locationIdIndex = categoryCursor.getColumnIndex(CategoryEntry._ID);
            categoryId = categoryCursor.getLong(locationIdIndex);
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
        final String RSS_LINK = "link";

        final String OB_DATE = "pubDate";
        final String OB_AUTHOR = "creator";

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(feedXmlStr));

//        String[] resultStrs = new String[numDeals];
//        int count = 0;
        ArrayList<String> headlines = new ArrayList<>();
        ArrayList<String> links = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();
        ArrayList<String> authors = new ArrayList<>();

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
                } else if (xpp.getName().equalsIgnoreCase(RSS_LINK)) {
                    if (insideItem)
                        links.add(xpp.nextText());          // Extract the deal's link
                } else if (xpp.getName().equalsIgnoreCase(OB_DATE)) {
                    if (insideItem)
                        dates.add(xpp.nextText());          // Extract the posting date
                } else if (xpp.getName().equalsIgnoreCase(OB_AUTHOR)) {
                    if (insideItem) authors.add(xpp.nextText()); // Extract the author
                }
//            } else if (eventType == XmlPullParser.END_TAG
//                    && xpp.getName().equalsIgnoreCase(RSS_ITEM)) {
//                insideItem = false;
//                resultStrs[count] = headlines.get(count) + " - " + links.get(count)
//                        + " (" + dates.get(count) + ", by: " + authors.get(count) + ")"; // Construct string.
//                count++;
            }
            eventType = xpp.next();
        }
        long categoryId = addCategory(categoryPath, categoryTitle);
        Vector<ContentValues> cVVector = new Vector<>(headlines.size());

        for (int i = 0; i < headlines.size(); i++) {
            ContentValues dealValues = new ContentValues();

            dealValues.put(DealsContract.DealEntry.COLUMN_CAT_KEY, categoryId);
            dealValues.put(DealsContract.DealEntry.COLUMN_DATE, dates.get(i));
            dealValues.put(DealsContract.DealEntry.COLUMN_TITLE, headlines.get(i));
            dealValues.put(DealsContract.DealEntry.COLUMN_LINK, links.get(i));
            dealValues.put(DealsContract.DealEntry.COLUMN_DESC, "Boy have I got some deals for you!"); // TODO: Implement description extraction and formatting
            dealValues.put(DealsContract.DealEntry.COLUMN_AUTHOR, authors.get(i));

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

        // Will contain the raw JSON response as a string.
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
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the deal data, there's no point in attempting
            // to parse it.
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

        try {
            getDealsDataFromRss(feedXmlStr, params[0]);
        } catch (XmlPullParserException | IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }
}