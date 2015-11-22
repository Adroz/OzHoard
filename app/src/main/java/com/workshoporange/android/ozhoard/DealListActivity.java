package com.workshoporange.android.ozhoard;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.workshoporange.android.ozhoard.Deal.DealContent;

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
import java.util.List;

/**
 * An activity representing a list of Deals. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link DealDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class DealListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    public SimpleItemRecyclerViewAdapter mDealsAdapter = new SimpleItemRecyclerViewAdapter(DealContent.ITEMS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Running FetchFeedTask", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                FetchFeedTask feedTask = new FetchFeedTask();
                feedTask.execute("deals");
            }
        });

        View recyclerView = findViewById(R.id.deal_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.deal_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(mDealsAdapter);
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<DealContent.Deal> mValues;

        public SimpleItemRecyclerViewAdapter(List<DealContent.Deal> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.deal_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).details);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(DealDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        DealDetailFragment fragment = new DealDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.deal_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, DealDetailActivity.class);
                        intent.putExtra(DealDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public void add(DealContent.Deal item) {
            add(mValues.size(), item);
        }

        public void add(int position, DealContent.Deal item) {
            mValues.add(position, item);
            notifyItemInserted(position);
        }

        public void remove(DealContent.Deal item) {
            int position = mValues.indexOf(item);
            mValues.remove(position);
            notifyItemRemoved(position);
        }

        public void clear() {
            mValues.clear();
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public DealContent.Deal mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }

    /**
     * Created by Nik on 22/11/2015.
     */
    public class FetchFeedTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchFeedTask.class.getSimpleName();

        /**
         * Take the String representing the complete deal list in XML Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         */
        private String[] getDealsDataFromRss(String feedXmlStr, int numDeals)
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

            String[] resultStrs = new String[numDeals];
            ArrayList<String> headlines = new ArrayList<>();
            ArrayList<String> links = new ArrayList<>();
            ArrayList<String> dates = new ArrayList<>();
            ArrayList<String> authors = new ArrayList<>();
            int count = 0;


            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equalsIgnoreCase(RSS_ITEM)) {
                        insideItem = true;
                    } else if (xpp.getName().equalsIgnoreCase(RSS_TITLE)) {
                        if (insideItem) headlines.add(xpp.nextText());      // Extract the headline
                    } else if (xpp.getName().equalsIgnoreCase(RSS_LINK)) {
                        if (insideItem)
                            links.add(xpp.nextText());          // Extract the deal's link
                    } else if (xpp.getName().equalsIgnoreCase(OB_DATE)) {
                        if (insideItem)
                            dates.add(xpp.nextText());          // Extract the posting date
                    } else if (xpp.getName().equalsIgnoreCase(OB_AUTHOR)) {
                        if (insideItem) authors.add(xpp.nextText());        // Extract the author
                    }
//                Log.d(LOG_TAG, xpp.getName());
                } else if (eventType == XmlPullParser.END_TAG
                        && xpp.getName().equalsIgnoreCase(RSS_ITEM)) {
                    insideItem = false;
                    resultStrs[count] = headlines.get(count) + " - " + links.get(count)
                            + " (" + dates.get(count) + ", by: " + authors.get(count) + ")";               // Construct string.
                    count++;
                }
                eventType = xpp.next();
            }
            return resultStrs;
        }

        @Override
        protected String[] doInBackground(String... params) {
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

            int numDeals = 10;

            try {
                // Construct the URL for OzBargain's feed.
                final String OZBARGAIN_BASE_URL = "https://www.ozbargain.com.au/";
                final String OZBARGAIN_CATEGORY_URL = "cat";

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
                return getDealsDataFromRss(feedXmlStr, numDeals);
            } catch (XmlPullParserException | IOException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mDealsAdapter.clear();
                int id = 0;
                for (String dealStr : result) {
                    mDealsAdapter.add(new DealContent.Deal(Integer.toString(id), dealStr, "Blah", dealStr));
                    id++;
                }
            }
        }
    }
}
