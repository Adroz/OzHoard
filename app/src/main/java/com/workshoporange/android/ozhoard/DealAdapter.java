package com.workshoporange.android.ozhoard;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.workshoporange.android.ozhoard.data.DealsContract;
import com.workshoporange.android.ozhoard.utils.RecyclerViewCursorAdapter;
import com.workshoporange.android.ozhoard.utils.Utility;

/**
 * Created by Nik on 21/01/2016.
 */
public class DealAdapter extends RecyclerViewCursorAdapter<DealAdapter.ViewHolder> {
    private static final int TYPE_NORMAL = Integer.MIN_VALUE;
    private static final int TYPE_FOOTER = Integer.MIN_VALUE + 1;

    private Context mContext;
    private boolean mFooter = false;

    public DealAdapter(Context context, Cursor cursor) {
        super(cursor);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.deal_list_footer, parent, false);
            return new ViewHolderFooter(view);
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.deal_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        if (cursor.getPosition() >= getBasicItemCount() && holder.getItemViewType() == TYPE_FOOTER) {
            ((ViewHolderFooter) holder).progressBar.setIndeterminate(true);
        } else {
            // Net score
            holder.netScoreView.setText(String.valueOf(cursor.getInt(DealListActivity.COL_DEAL_SCORE)));

            // Author and category in the format "joe_blow in Pizza"
            Spanned authorCategory = Utility.formatAuthorAndCategory(
                    holder.mView.getContext(),
                    cursor.getString(DealListActivity.COL_DEAL_AUTHOR),
                    cursor.getString(DealListActivity.COL_CATEGORY_TITLE)
            );
            holder.authorCategoryView.setText(authorCategory);

            // Deal's title
            holder.titleView.setText(cursor.getString(DealListActivity.COL_DEAL_TITLE));

            // Time since posted, # comments, time until expired
            String timeCommentsExpiry = Utility.formatTimeCommentsExpiry(
                    cursor.getLong(DealListActivity.COL_DEAL_DATE),
                    cursor.getInt(DealListActivity.COL_DEAL_COMMENTS),
                    cursor.getLong(DealListActivity.COL_DEAL_EXPIRY)
            );
            holder.timeCommentsExpiryView.setText(timeCommentsExpiry);

            holder.imageView.setImageResource(R.mipmap.ic_launcher);            // TODO: Get real image

            // String categoryPath = Utility.getPreferredLocation(getActivity());
            final String categoryPath = "deals";          // TODO: Add support for different categories
            holder.mView.setTag(DealsContract.DealEntry.buildDealCategoryWithLink(categoryPath,
                    cursor.getString(DealListActivity.COL_DEAL_LINK)));
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Callback) mContext).onItemSelected((Uri) v.getTag());
                }
            });
        }
    }

    @Override
    public Cursor swapCursor(Cursor c) {
        if (mFooter) {
            return addFooterToCursor(super.swapCursor(c));
        }
        return super.swapCursor(c);
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= getBasicItemCount() && mFooter) {
            return TYPE_FOOTER;
        }
        return TYPE_NORMAL;
    }

    public void useFooter(boolean useFooter) {
        mFooter = useFooter;
    }

    public int getBasicItemCount() {
        int itemCount = getItemCount();
        if (mFooter) {
            itemCount -= 1;
        }
        return itemCount;
    }

    @NonNull
    private static Cursor addFooterToCursor(Cursor data) {
        Cursor[] cursorToMerge = new Cursor[2];
        cursorToMerge[0] = data;
        cursorToMerge[1] = DealAdapter.BLANK_CURSOR;
        return new MergeCursor(cursorToMerge);
    }

    // Blank dummy cursor for footer, if used. This will have to be changed whenever the database
    // columns are.
    // TODO: Think of a cleaner and neater solutions
    public static final MatrixCursor BLANK_CURSOR = new MatrixCursor(
            //These are the names of the columns in my other cursor
            new String[]{
                    DealsContract.DealEntry._ID,
                    DealsContract.DealEntry.COLUMN_CAT_KEY,
                    DealsContract.DealEntry.COLUMN_DATE,
                    DealsContract.DealEntry.COLUMN_TITLE,
                    DealsContract.DealEntry.COLUMN_LINK,
                    DealsContract.DealEntry.COLUMN_DESC,
                    DealsContract.DealEntry.COLUMN_AUTHOR,
                    DealsContract.DealEntry.COLUMN_SCORE,
                    DealsContract.DealEntry.COLUMN_COMMENT_COUNT,
                    DealsContract.DealEntry.COLUMN_EXPIRY,
                    DealsContract.DealEntry.COLUMN_IMAGE,
                    DealsContract.DealEntry.COLUMN_IMAGE
            });

    // Blank input data
    static {
        BLANK_CURSOR.addRow(new String[]{
                "0",
                "1",
                "1",
                "1",
                "1",
                "1",
                "1",
                "1",
                "1",
                "1",
                "1",
                "1"
        });
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DealDetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri dateUri);
    }

    /**
     * Cache of children views for deal item list.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView netScoreView;
        public final TextView authorCategoryView;
        public final TextView titleView;
        public final TextView timeCommentsExpiryView;
        public final ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            netScoreView = (TextView) view.findViewById(R.id.net_score);
            authorCategoryView = (TextView) view.findViewById(R.id.author_and_category);
            titleView = (TextView) view.findViewById(R.id.deal_title);
            timeCommentsExpiryView = (TextView) view.findViewById(R.id.time_comments_expiry);
            imageView = (ImageView) view.findViewById(R.id.deal_image);
        }
    }

    /**
     * Cache of child view for deal item footer.
     */
    public class ViewHolderFooter extends ViewHolder {
        public final View mView;
        public final ProgressBar progressBar;

        public ViewHolderFooter(View view) {
            super(view);
            mView = view;
            progressBar = (ProgressBar) view.findViewById(R.id.footer_progress);
        }
    }
}
