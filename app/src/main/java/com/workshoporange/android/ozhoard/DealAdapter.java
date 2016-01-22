package com.workshoporange.android.ozhoard;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.workshoporange.android.ozhoard.data.DealsContract;

/**
 * Created by Nik on 21/01/2016.
 */
public class DealAdapter extends RecyclerViewCursorAdapter<DealAdapter.ViewHolder> {

    private FragmentManager mSupportFragmentManager;
    private boolean mTwoPane;

    public DealAdapter(FragmentManager supportFragmentManager, Cursor c, boolean twoPane) {
        super(c);
        mSupportFragmentManager = supportFragmentManager;
        mTwoPane = twoPane;
    }

    private String convertCursorRowToUXFormat(Cursor cursor) {
        // Get row indices for cursor
        int idx_title = cursor.getColumnIndex(DealsContract.DealEntry.COLUMN_TITLE);
        int idx_desc = cursor.getColumnIndex(DealsContract.DealEntry.COLUMN_DESC);
        int idx_date = cursor.getColumnIndex(DealsContract.DealEntry.COLUMN_DATE);
        int idx_author = cursor.getColumnIndex(DealsContract.DealEntry.COLUMN_AUTHOR);
        int idx_link = cursor.getColumnIndex(DealsContract.DealEntry.COLUMN_LINK);

//        String highAndLow = formatHighLows(
//                cursor.getDouble(idx_max_temp),
//                cursor.getDouble(idx_min_temp));

        return Utility.formatDate(cursor.getLong(idx_date)) + " " +
                cursor.getString(idx_title) + " - " +
                cursor.getString(idx_desc) + "-" +
                cursor.getString(idx_link) + ", by " +
                cursor.getString(idx_author);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.deal_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final Cursor cursor) {
        holder.contentView.setText(convertCursorRowToUXFormat(cursor));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(DealDetailFragment.ARG_ITEM_ID,
                            cursor.getString(DealListActivity.COL_DEAL_TITLE));
                    DealDetailFragment fragment = new DealDetailFragment();
                    fragment.setArguments(arguments);
                    mSupportFragmentManager.beginTransaction()
                            .replace(R.id.deal_detail_container, fragment)
                            .commit();
                } else {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, DealDetailActivity.class);
                    intent.putExtra(DealDetailFragment.ARG_ITEM_ID,
                            cursor.getString(DealListActivity.COL_DEAL_TITLE));

                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public Cursor swapCursor(Cursor c) {
        return super.swapCursor(c);
    }

    /**
     * Cache of children views for deal item list.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView idView;
        public final TextView contentView;
//    public final ImageView iconView;
//    public final TextView dateView;
//    public final TextView descriptionView;
//    public final TextView highTempView;
//    public final TextView lowTempView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            idView = (TextView) view.findViewById(R.id.id);
            contentView = (TextView) view.findViewById(R.id.content);
//        iconView = (ImageView) view.findViewById(R.id.list_item_icon);
//        dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
//        descriptionView = (TextView) view.findViewById(R.id.list_item_textview);
//        highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
//        lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}
