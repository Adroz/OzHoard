package com.workshoporange.android.ozhoard;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.workshoporange.android.ozhoard.customtabs.CustomTabActivityHelper;
import com.workshoporange.android.ozhoard.customtabs.WebViewFallback;

import static com.workshoporange.android.ozhoard.data.DealsContract.DealEntry;

/**
 * A fragment representing a single Deal detail screen.
 * This fragment is either contained in a {@link DealListActivity}
 * in two-pane mode (on tablets) or a {@link DealDetailActivity}
 * on handsets.
 */
public class DealDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mUri;
    private String mDestinationLink;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            DealEntry.TABLE_NAME + "." + DealEntry._ID,
            DealEntry.COLUMN_DATE,
            DealEntry.COLUMN_TITLE,
            DealEntry.COLUMN_LINK,
            DealEntry.COLUMN_DESC,
            DealEntry.COLUMN_AUTHOR
    };

    // These constants correspond to DETAIL_COLUMNS, and must change if it does
    private static final int COL_DEAL_ID = 0;
    private static final int COL_DEAL_DATE = 1;
    private static final int COL_DEAL_TITLE = 2;
    private static final int COL_DEAL_LINK = 3;
    private static final int COL_DEAL_DESC = 4;
    private static final int COL_DEAL_AUTHOR = 5;

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String DETAIL_URI = "item_id";

    TextView mDealDetail;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DealDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            if (getArguments().containsKey(DETAIL_URI)) {
                mUri = getArguments().getParcelable(DETAIL_URI);

                Activity activity = this.getActivity();
                CollapsingToolbarLayout appBarLayout =
                        (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
                if (appBarLayout != null) {
                    appBarLayout.setTitle(mUri.getPathSegments().get(2));
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.deal_detail, container, false);

        mDealDetail = (TextView) rootView.findViewById(R.id.deal_detail);
        mDealDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDestinationLink != null) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
                        CustomTabActivityHelper.openCustomTab(getActivity(),
                                customTabsIntent, Uri.parse(mDestinationLink), new WebViewFallback());
                    } else {
                        WebViewFallback fallback = new WebViewFallback();
                        fallback.openUri(getActivity(), Uri.parse(mDestinationLink));
                    }
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );

            // TODO: Add share action provider
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            mDealDetail.setText(data.getString(COL_DEAL_DESC));
            mDestinationLink = data.getString(COL_DEAL_LINK);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
