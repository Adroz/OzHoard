package com.workshoporange.android.ozhoard;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.workshoporange.android.ozhoard.data.DealsContract;
import com.workshoporange.android.ozhoard.utils.RecyclerViewCursorAdapter;
import com.workshoporange.android.ozhoard.utils.Utility;

/**
 * Created by Nik on 21/01/2016.
 */
public class DealAdapter extends RecyclerViewCursorAdapter<DealAdapter.ViewHolder> {

    private Context mContext;

    public DealAdapter(Context context, Cursor cursor) {
        super(cursor);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.deal_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final Cursor cursor) {
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

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // String categoryPath = Utility.getPreferredLocation(getActivity());
                final String categoryPath = "deals";          // TODO: Add support for different categories
                ((Callback) mContext).onItemSelected(
                        DealsContract.DealEntry.buildDealCategoryWithLink(
                                categoryPath,
                                cursor.getString(DealListActivity.COL_DEAL_LINK))
                );
            }
        });
    }

    @Override
    public Cursor swapCursor(Cursor c) {
        return super.swapCursor(c);
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
}
