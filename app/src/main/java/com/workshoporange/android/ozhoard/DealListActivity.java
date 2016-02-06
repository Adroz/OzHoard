package com.workshoporange.android.ozhoard;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.workshoporange.android.ozhoard.data.DealsContract;

/**
 * An activity representing a list of Deals. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link DealDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class DealListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, DealAdapter.Callback {

    private final String LOG_TAG = DealListActivity.class.getSimpleName();

    private final String DETAIL_FRAGMENT_TAG = "DFTAG";
    private static final String SELECTED_KEY = "selected_position";
    private static final String PAGE_KEY = "page_count";
    private boolean mTwoPane = false;
    private DealAdapter mDealsAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private static int mPosition = ListView.INVALID_POSITION;
    private static int mPageCount = 0;

    private static final int DEALS_LOADER = 0;
    // Only create a projection of the columns we need.
    private static final String[] DEAL_COLUMNS = {
            // The id needs to be fully qualified with a table name, since the content provider
            // joins the category & deal tables in the background (both have an _id column).
            DealsContract.DealEntry.TABLE_NAME + "." + DealsContract.DealEntry._ID,
            DealsContract.DealEntry.COLUMN_DATE,
            DealsContract.DealEntry.COLUMN_TITLE,
            DealsContract.DealEntry.COLUMN_LINK,
            DealsContract.DealEntry.COLUMN_DESC,
            DealsContract.DealEntry.COLUMN_AUTHOR,
            DealsContract.DealEntry.COLUMN_SCORE,
            DealsContract.DealEntry.COLUMN_COMMENT_COUNT,
            DealsContract.DealEntry.COLUMN_EXPIRY,
            DealsContract.DealEntry.COLUMN_IMAGE,
            DealsContract.CategoryEntry.COLUMN_CATEGORY_PATH,
            DealsContract.CategoryEntry.COLUMN_CATEGORY_TITLE
    };
    // These indices are tied to DEAL_COLUMNS.  If DEAL_COLUMNS changes, these
    // must change.
    static final int COL_DEAL_ID = 0;
    static final int COL_DEAL_DATE = 1;
    static final int COL_DEAL_TITLE = 2;
    static final int COL_DEAL_LINK = 3;
    static final int COL_DEAL_DESC = 4;
    static final int COL_DEAL_AUTHOR = 5;
    static final int COL_DEAL_SCORE = 6;
    static final int COL_DEAL_COMMENTS = 7;
    static final int COL_DEAL_EXPIRY = 8;
    static final int COL_DEAL_IMAGE = 9;
    static final int COL_CATEGORY_PATH = 10;
    static final int COL_CATEGORY_TITLE = 11;


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
                onChangeCategory();
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        assert mSwipeRefreshLayout != null;
        setupSwipeRefreshLayout(mSwipeRefreshLayout);

        // The CursorAdapter will take data from the cursor and populate the RecyclerView.
        mRecyclerView = (RecyclerView) findViewById(R.id.deal_list);
        mDealsAdapter = new DealAdapter(this, null, mRecyclerView);
        assert mRecyclerView != null;
        setupRecyclerView(mRecyclerView);

        if (findViewById(R.id.deal_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.deal_detail_container,
                                new DealDetailFragment(),
                                DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        }
        // If there's instance state, mine it for useful information.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The ListView probably hasn't even been populated yet.  Actually perform the
            // swap-out in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
            mPageCount = savedInstanceState.getInt(PAGE_KEY);
        }
        getSupportLoaderManager().initLoader(DEALS_LOADER, null, this);
        refreshDeals();                                                                             // ALWAYS refresh on startup
    }

    private void onChangeCategory() {
        refreshDeals();
        getSupportLoaderManager().restartLoader(DEALS_LOADER, null, this);
    }

    /**
     * Clears the adapter's scroll values (so that the adapter's scroll page count is 0) and the
     * current database table, then requests new data via {@link FetchDealsTask}.
     */
    private void refreshDeals() {
        mPageCount = 0;
        mPosition = -1;
        mDealsAdapter.resetItems();
        // String categoryPath = Utility.getPreferredCategory(getActivity());
        String categoryPath = "deals";  // TODO: Get real path
        this.getContentResolver().delete(DealsContract.DealEntry.CONTENT_URI, null, null);
        FetchDealsTask dealsTask = new FetchDealsTask(getApplicationContext());
        dealsTask.execute(categoryPath);
    }

    private void setupSwipeRefreshLayout(@NonNull SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshDeals();
                    }
                }
        );
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(mDealsAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "onSaveInstanceState called, mPosition = " + mPosition + ", mPageCount = " + mPageCount);
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to ListView.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        outState.putInt(PAGE_KEY, mPageCount);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            // Signal SwipeRefreshLayout to start the progress indicator
            mSwipeRefreshLayout.setRefreshing(true);
            refreshDeals();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri contentUri, int position) {
        mPosition = position;
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(DealDetailFragment.DETAIL_URI, contentUri);
            DealDetailFragment fragment = new DealDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.deal_detail_container, fragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DealDetailActivity.class);
            intent.setData(contentUri);

            startActivity(intent);
        }
    }

    @Override
    public void onLoadMore(int pageCount) {
        // Don't load more if trying to refresh
        if (mSwipeRefreshLayout.isRefreshing()) return;

        mPosition = -1;
        mPageCount = pageCount;

        // Fetch next page
        // String categoryPath = Utility.getPreferredCategory(getActivity());
        String categoryPath = "deals";  // TODO: Get real path
        FetchDealsTask dealsTask = new FetchDealsTask(getApplicationContext());
        dealsTask.execute(categoryPath, String.valueOf(pageCount));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        String categoryPath = Utility.getPreferredCategory(getActivity());
        String categoryPath = "deals";  // TODO: Get real path

        // Sort order:  Descending, by date.
        String sortOrder = DealsContract.DealEntry.COLUMN_DATE + " DESC";
        Uri dealForCategoryUri = DealsContract.DealEntry.buildDealCategory(categoryPath);

        return new CursorLoader(this,
                dealForCategoryUri,
                DEAL_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Set swipe refresh to refreshing if there is no data (as a visual cue).
        mDealsAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mRecyclerView.scrollToPosition(mPosition);
        }
        // Call this to stop the refreshing indicator as the refresh is complete.
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDealsAdapter.swapCursor(null);
    }
}
