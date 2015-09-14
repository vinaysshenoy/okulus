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
import android.widget.ImageView;

import com.vinaysshenoy.okulus.OkulusImageView;
import com.vinaysshenoy.okulusdemo.R;
import com.vinaysshenoy.okulusdemo.utils.RawBitmapManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by vinay.shenoy on 24/07/14.
 */
public class ComparisonFragment extends Fragment {

    private OkulusImageView mOkulusImageView;
    private ImageView mNormalImageView;

    private ExecutorService mPool;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean mPaused;

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {

        mPool = Executors.newSingleThreadExecutor();
        final View content = inflater.inflate(R.layout.fragment_comparison, container, false);

        mOkulusImageView = (OkulusImageView) content.findViewById(R.id.image_okulus);
        mNormalImageView = (ImageView) content.findViewById(R.id.image_normal);

        return content;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadBitmaps();
    }

    private void loadBitmaps() {

        mPool.submit(new LoadBitmapRunnable(mOkulusImageView, R.raw.img_6, 128, 128));
        mPool.submit(new LoadBitmapRunnable(mNormalImageView, R.raw.img_6, 128, 128));

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
        private ImageView mImageView;

        public LoadBitmapRunnable(ImageView imageView, int resourceId, int targetWidth, int targetheight) {

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
