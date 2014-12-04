package com.vinaysshenoy.okulusdemo.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vinaysshenoy.okulusdemo.DemoApplication;
import com.vinaysshenoy.okulusdemo.R;

/**
 * Fragment to display images in a ListView
 * <p/>
 * Created by vinaysshenoy on 04/12/14.
 */
public class NetworkFragment extends ListFragment {


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final DemoApplication application = (DemoApplication) getActivity().getApplication();
        setListAdapter(new ImageAdapter(getActivity(), "https://scontent-b-ams.xx.fbcdn.net/hphotos-xfp1/t31.0-8/10700409_10205008935044693_3621835106117173363_o.jpg", application.getRequestQueue(), application.getImageLoader(), application.getUniversalImageLoader()));

    }

    private static final class ImageAdapter extends BaseAdapter {

        private String mImageUrl;

        private ImageLoader mImageLoader;

        private RequestQueue mRequestQueue;

        private Context mContext;

        private com.nostra13.universalimageloader.core.ImageLoader mUniversalImageLoader;

        public ImageAdapter(final Context context, final String imageUrl, final RequestQueue requestQueue, final ImageLoader imageLoader, final com.nostra13.universalimageloader.core.ImageLoader universalImageLoader) {
            mContext = context;
            mImageUrl = imageUrl;
            mRequestQueue = requestQueue;
            mImageLoader = imageLoader;
            mUniversalImageLoader = universalImageLoader;
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object getItem(int position) {
            return mImageUrl;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 5;
        }

        @Override
        public int getItemViewType(int position) {

            return position % 5;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final View view = createView(position, convertView, parent);
            loadImage((ImageView) view, position);
            return view;
        }

        private int dpToPx(int dp) {

            float density = mContext.getResources().getDisplayMetrics().density;
            return (int) (dp * density);
        }

        private void loadImage(final ImageView imageView, final int position) {

            final String imageurl = (String) getItem(position);
            switch (getItemViewType(position)) {

                //Volley - ImageRequest
                case 0: {
                    final ImageRequest request = new ImageRequest(
                            imageurl,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    imageView.setImageBitmap(response);
                                }
                            },
                            dpToPx(128),
                            dpToPx(96),
                            null,
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                }
                            }
                    );

                    mRequestQueue.add(request);
                    break;
                }

                //Volley - NetworkImageView - In this case, NetworkImageView has been modified to extend OkulusImageView
                case 1: {

                    final NetworkImageView networkImageView = (NetworkImageView) imageView;
                    networkImageView.setImageUrl(imageurl, mImageLoader);
                    break;
                }

                //Picasso
                case 2: {

                    Picasso.with(imageView.getContext())
                            .load(imageurl)
                            .resize(dpToPx(128), dpToPx(96))
                            .centerCrop()
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    imageView.setImageBitmap(bitmap);
                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {

                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });
                    break;
                }

                //Glide
                case 3: {

                    Glide.with(imageView.getContext())
                            .load(imageurl)
                            .asBitmap()
                            //.override(dpToPx(128), dpToPx(96))
                            .centerCrop()
                            .into(new ViewTarget<ImageView, Bitmap>(imageView) {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    imageView.setImageBitmap(resource);
                                }
                            });
                    break;
                }

                // Universal Image Loader
                case 4: {

                    ImageSize targetSize = new ImageSize(dpToPx(96), dpToPx(128));
                    mUniversalImageLoader.loadImage(imageurl, targetSize, new SimpleImageLoadingListener() {

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            imageView.setImageBitmap(loadedImage);
                        }
                    });
                    break;
                }

            }
        }

        private View createView(int position, View convertView, ViewGroup parent) {

            if (convertView != null) {
                return convertView;
            } else {

                final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                int resId;
                switch (getItemViewType(position)) {

                    case 0: {
                        resId = R.layout.layout_image_1;
                        break;
                    }

                    case 1: {
                        resId = R.layout.layout_image_2;
                        break;
                    }

                    case 2: {
                        resId = R.layout.layout_image_3;
                        break;
                    }

                    case 3: {
                        resId = R.layout.layout_image_4;
                        break;
                    }

                    case 4:
                    default: {
                        resId = R.layout.layout_image_5;
                        break;
                    }

                }
                return inflater.inflate(resId, parent, false);
            }
        }
    }
}
