package com.vinaysshenoy.okulusdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.vinaysshenoy.okulus.OkulusImageView;
import com.vinaysshenoy.okulusdemo.utils.RawBitmapManager;

/**
 * Adapter for raw resource bitmaps Created by vinay.shenoy on 24/07/14.
 */
public class RawBitmapAdapter extends BaseAdapter {

    private int[] mItems;

    private Context mContext;

    private int mTargetWidth, mTargetHeight;

    public RawBitmapAdapter(Context context, int[] items) {

        mContext = context;
        mItems = items;
        mTargetWidth = context.getResources().getDimensionPixelSize(R.dimen.image_size);
        mTargetHeight = context.getResources().getDimensionPixelSize(R.dimen.image_size);
    }

    @Override
    public int getCount() {
        return mItems == null ? 0 : mItems.length;
    }

    @Override
    public Object getItem(final int position) {
        return mItems[position];
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {

        View view = convertView;

        if (convertView == null) {

            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_image_round_rect, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.okulusImageView = (OkulusImageView) view.findViewById(R.id.image_okulus);
            view.setTag(viewHolder);
        }

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.okulusImageView.setImageBitmap(
                RawBitmapManager.INSTANCE.getBitmap(
                        mContext,
                        mItems[position],
                        mTargetWidth,
                        mTargetHeight)
        );
        return view;
    }


    private static class ViewHolder {

        OkulusImageView okulusImageView;
    }
}
