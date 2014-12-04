package com.vinaysshenoy.okulusdemo;

import android.app.Application;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

/**
 * Created by vinaysshenoy on 04/12/14.
 */
public class DemoApplication extends Application {

    private RequestQueue mRequestQueue;

    private ImageLoader mImageLoader;

    @Override
    public void onCreate() {
        super.onCreate();

        mRequestQueue = Volley.newRequestQueue(this);
        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap>
                    cache = new LruCache<>(20);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });

        final File cacheDirectory = StorageUtils.getCacheDirectory(this);
        final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .diskCache(new UnlimitedDiscCache(cacheDirectory))
                .build();
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().init(config);
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public com.nostra13.universalimageloader.core.ImageLoader getUniversalImageLoader() {
        return com.nostra13.universalimageloader.core.ImageLoader.getInstance();
    }
}
