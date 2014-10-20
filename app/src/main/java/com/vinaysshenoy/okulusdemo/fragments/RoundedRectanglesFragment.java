package com.vinaysshenoy.okulusdemo.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vinaysshenoy.okulus.OkulusImageView;
import com.vinaysshenoy.okulusdemo.R;
import com.vinaysshenoy.okulusdemo.utils.RawBitmapManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by vinay.shenoy on 24/07/14.
 */
public class RoundedRectanglesFragment extends Fragment {

    private static final int[] ITEMS = new int[]{
            R.raw.img_1,
            R.raw.img_2,
            R.raw.img_3,
            R.raw.img_4,
            R.raw.img_5,
            R.raw.img_6
    };

    private OkulusImageView mImageView1;
    private OkulusImageView mImageView2;
    private OkulusImageView mImageView3;
    private OkulusImageView mImageView4;
    private OkulusImageView mImageView5;
    private OkulusImageView mImageView6;


    private ExecutorService mPool;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean mPaused;

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {

        mPool = Executors.newSingleThreadExecutor();
        final View content = inflater.inflate(R.layout.fragment_rounded_rectangles, container, false);

        mImageView1 = (OkulusImageView) content.findViewById(R.id.image_1);
        mImageView2 = (OkulusImageView) content.findViewById(R.id.image_2);
        mImageView3 = (OkulusImageView) content.findViewById(R.id.image_3);
        mImageView4 = (OkulusImageView) content.findViewById(R.id.image_4);
        mImageView5 = (OkulusImageView) content.findViewById(R.id.image_5);
        mImageView6 = (OkulusImageView) content.findViewById(R.id.image_6);


        loadBitmaps();

        return content;
    }

    private void loadBitmaps() {

        mPool.submit(new LoadBitmapRunnable(mImageView1, R.raw.img_1, 256, 128));
        mPool.submit(new LoadBitmapRunnable(mImageView2, R.raw.img_2, 256, 128));
        mPool.submit(new LoadBitmapRunnable(mImageView3, R.raw.img_3, 256, 128));
        mPool.submit(new LoadBitmapRunnable(mImageView4, R.raw.img_4, 256, 128));
        mPool.submit(new LoadBitmapRunnable(mImageView5, R.raw.img_5, 128, 256));
        mPool.submit(new LoadBitmapRunnable(mImageView6, R.raw.img_6, 256, 128));

    }

    @Override
    public void onPause() {
        super.onPause();
        mPool.shutdownNow();
        mPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPaused = false;
    }

    private class LoadBitmapRunnable implements Runnable {

        private int mResourceId;
        private int mTargetWidth, mTargetHeight;
        private OkulusImageView mImageView;

        public LoadBitmapRunnable(OkulusImageView imageView, int resourceId, int targetWidth, int targetheight) {

            mImageView = imageView;
            mResourceId = resourceId;
            mTargetWidth = targetWidth;
            mTargetHeight = targetheight;
        }

        @Override
        public void run() {

            final Bitmap bitmap = RawBitmapManager.INSTANCE.getBitmap(
                    mImageView.getContext(),
                    mResourceId,
                    mTargetWidth,
                    mTargetHeight);

            if (!mPaused && bitmap != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(bitmap);
                    }
                });
            }
        }
    }
}
