package com.workshoporange.android.ozhoard;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
     */
    private boolean mTwoPane;

    private DealAdapter mDealsAdapter;
    private RecyclerView mRecyclerView;
    private int mPosition = ListView.INVALID_POSITION;

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
    static final int COL_CATEGORY_PATH = 6;
    static final int COL_CATEGORY_TITLE = 7;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The CursorAdapter will take data from our cursor and populate the RecyclerView.
        mDealsAdapter = new DealAdapter(getSupportFragmentManager(), null, mTwoPane);

        setContentView(R.layout.activity_deal_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDeals(view);
                onChangeCategory();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.deal_list);
        assert mRecyclerView != null;
        setupRecyclerView(mRecyclerView);

        if (findViewById(R.id.deal_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
        getSupportLoaderManager().initLoader(DEALS_LOADER, null, this);
    }

    private void onChangeCategory() {
        getSupportLoaderManager().restartLoader(DEALS_LOADER, null, this);
    }

    private void updateDeals(View view) {
        Snackbar.make(view, "Running FetchFeedTask", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        FetchDealsTask dealsTask = new FetchDealsTask(getApplicationContext());
        dealsTask.execute("deals"); // Default category
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(mDealsAdapter);
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
        mDealsAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mRecyclerView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDealsAdapter.swapCursor(null);
    }
}
