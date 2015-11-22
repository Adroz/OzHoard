package com.workshoporange.android.ozhoard;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Nik on 22/11/2015.
 */
public class FetchFeedTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchFeedTask.class.getSimpleName();

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
            // TODO: 22/11/2015 at present only pull deals feed.
//            URL url = new URL("https://www.ozbargain.com.au/deals/feed");
            final String OZBARGAIN_BASE_URL = "https://www.ozbargain.com.au/";

            Uri builtUri = Uri.parse(OZBARGAIN_BASE_URL).buildUpon()
                    .appendPath(params[0])
                    .appendPath("feed")
                    .build();

            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, "Built URI " + builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
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
            // If the code didn't successfully get the weather data, there's no point in attemping
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


        return null;
    }
}
