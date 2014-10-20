/*
 * Copyright 2014 Vinay S Shenoy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vinaysshenoy.okulusdemo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.util.LruCache;

/**
 * Class to hold the raw bitmaps in a cache
 * <p/>
 * Created by vinay.shenoy on 24/07/14.
 */
public class RawBitmapManager {

    public static final RawBitmapManager INSTANCE = new RawBitmapManager();

    public static final int CACHE_SIZE = 5 * 1024 * 1024; //2 MB cache

    private LruCache<Integer, Bitmap> mRawBitmapLruCache = new LruCache<Integer, Bitmap>(CACHE_SIZE) {

        @Override
        protected int sizeOf(final Integer key, final Bitmap value) {

            if (Build.VERSION.SDK_INT >= 19) {
                return value.getAllocationByteCount();
            } else if (Build.VERSION.SDK_INT >= 12) {
                return value.getByteCount();
            } else {
                return value.getRowBytes() * value.getHeight();
            }
        }
    };

    /**
     * Gets a Bitmap from the cache, reading from the resources if not available in the cache
     * <p/>
     * This method should be called from a separate thread, since it can read from the resources
     */
    public Bitmap getBitmap(Context context, int resourceId, int targetWidth, int targetHeight) {

        if (mRawBitmapLruCache.get(resourceId) != null) {
            return mRawBitmapLruCache.get(resourceId);

        } else {
            Bitmap readBitmap = readScaledBitmapFromResources(context, resourceId, targetWidth, targetHeight);

            if (readBitmap != null) {
                mRawBitmapLruCache.put(resourceId, readBitmap);
            }

            return readBitmap;

        }
    }

    private Bitmap readScaledBitmapFromResources(final Context context, final int resourceId, final int targetWidth, final int targetHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = 3;
        return BitmapFactory.decodeResource(context.getResources(), resourceId, options);
    }

    private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


}
