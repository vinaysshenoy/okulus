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

package com.vinaysshenoy.okulus;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;


/**
 * Custom ImageView to draw the content in the desired way.
 *
 * @author Vinay S Shenoy
 */
public class OkulusImageView extends ImageView {

    private static final String TAG = "OkulusImageView";

    private static final float DEFAULT_CORNER_RADIUS = 5f;               //dips
    private static final float DEFAULT_BORDER_WIDTH = 0f;                //dips
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;
    private static final float DEFAULT_SHADOW_WIDTH = 0f; //dips
    private static final int DEFAULT_SHADOW_COLOR = 0xB3444444; //70% dark gray
    private static final int DEFAULT_TOUCH_SELECTOR_COLOR = 0x66444444; //40% dark gray
    private static final boolean DEFAULT_FULL_CIRCLE = false;
    private static final boolean DEFAULT_TOUCH_SELECTOR_ENABLED = false;

    private float mCornerRadius;
    private float mBorderWidth;
    private int mBorderColor;
    private float mShadowSize;
    private int mShadowColor;
    private int mTouchSelectorColor;
    private boolean mTouchSelectorEnabled;
    private boolean mFullCircle;

    /**
     * Used to store the view coordinates for holding touch events
     */
    private Rect mViewRect;

    /**
     * Used to keep track of whether the user was already inside the view when moving finger so that
     * we don't need to re-invalidate the view on every motion event
     */
    private boolean mAlreadyInside;


    /**
     * @param context
     */
    public OkulusImageView(Context context) {
        super(context);
        init(context, null);
    }

    /**
     * @param context
     * @param attrs
     */
    public OkulusImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public OkulusImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    /**
     * @param context
     * @param attrs
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void init(Context context, AttributeSet attrs) {

        mViewRect = new Rect();
        mCornerRadius = dpToPx(DEFAULT_CORNER_RADIUS);
        float borderWidthInDips = DEFAULT_BORDER_WIDTH;
        mBorderColor = DEFAULT_BORDER_COLOR;
        mFullCircle = DEFAULT_FULL_CIRCLE;
        float shadowWidthInDips = DEFAULT_SHADOW_WIDTH;
        mShadowColor = DEFAULT_SHADOW_COLOR;
        mTouchSelectorColor = DEFAULT_TOUCH_SELECTOR_COLOR;
        mTouchSelectorEnabled = DEFAULT_TOUCH_SELECTOR_ENABLED;

        if (attrs != null) {

            TypedArray styledAttrs = context
                    .obtainStyledAttributes(attrs, R.styleable.OkulusImageView);
            mCornerRadius = styledAttrs
                    .getDimension(R.styleable.OkulusImageView_okulus_cornerRadius, mCornerRadius);
            mBorderColor = styledAttrs
                    .getColor(R.styleable.OkulusImageView_okulus_borderColor, mBorderColor);
            mFullCircle = styledAttrs
                    .getBoolean(R.styleable.OkulusImageView_okulus_fullCircle, mFullCircle);
            mShadowColor = styledAttrs
                    .getColor(R.styleable.OkulusImageView_okulus_shadowColor, mShadowColor);
            mTouchSelectorColor = styledAttrs
                    .getColor(R.styleable.OkulusImageView_okulus_touchSelectorColor, mTouchSelectorColor);
            mTouchSelectorEnabled = styledAttrs
                    .getBoolean(R.styleable.OkulusImageView_okulus_touchSelectorEnabled, mTouchSelectorEnabled);

            float dimension = styledAttrs
                    .getDimension(R.styleable.OkulusImageView_okulus_borderWidth, borderWidthInDips);
            borderWidthInDips = pxToDp(dimension);
            dimension = styledAttrs
                    .getDimension(R.styleable.OkulusImageView_okulus_shadowWidth, shadowWidthInDips);
            shadowWidthInDips = pxToDp(dimension);

            styledAttrs.recycle();
        }

        mBorderWidth = dpToPx(borderWidthInDips);
        mShadowSize = dpToPx(shadowWidthInDips);
        setImageDrawable(null);

    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewRect.set(getLeft(), getTop(), getRight(), getBottom());
    }

    /**
     * Converts a raw pixel value to a dp value, based on the device density
     */
    private static float pxToDp(float px) {
        return px / Resources.getSystem().getDisplayMetrics().density;
    }

    /**
     * Converts a raw dp value to a pixel value, based on the device density
     */
    public static float dpToPx(float dp) {
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }


    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        int requiredWidth = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int requiredHeight = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();

        requiredWidth = ViewCompat.resolveSizeAndState(
                requiredWidth,
                widthMeasureSpec,
                0
        );
        requiredHeight = ViewCompat.resolveSizeAndState(
                requiredHeight,
                heightMeasureSpec,
                0
        );

        /* If it's required to be a circle, set both height & width to be the
         * minimum of the two.
         * */
        if (mFullCircle) {

            if (requiredHeight > requiredWidth) {
                setMeasuredDimension(requiredWidth, requiredWidth );
            } else {
                setMeasuredDimension(requiredHeight, requiredHeight);
            }

        } else {
            setMeasuredDimension(requiredWidth, requiredHeight);
        }

    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        setImageBitmap(getBitmapFromDrawable(getDrawable()));
    }

    @Override
    public void setImageResource(int resId) {
        setImageBitmap(BitmapFactory.decodeResource(getResources(), resId));
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable == null) {
            super.setImageDrawable(null);
        } else if (drawable instanceof OkulusDrawable) {
            super.setImageDrawable(drawable);
        } else {
            setImageBitmap(getBitmapFromDrawable(drawable));
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {

        if (bm == null) {
            setImageDrawable(null);
        } else {
            final Drawable content = getDrawable();

            if (content instanceof OkulusDrawable) {
                ((OkulusDrawable) content).updateBitmap(bm);
                invalidate();
            } else {
                setImageDrawable(getOkulusDrawable(bm));
            }
        }
    }

    /**
     * Creates and sets an Okulus Drawable for a Bitmap
     *
     * @param bitmap The Bitmap to draw, or <code>null</code> to draw an empty bitmap with the rest
     *               of the settings
     */
    private void setOkulusDrawable(final Bitmap bitmap) {
        setImageDrawable(getOkulusDrawable(bitmap));
    }

    private OkulusDrawable getOkulusDrawable(final Bitmap bitmap) {
        return new OkulusDrawable(
                bitmap,
                mCornerRadius,
                mFullCircle,
                mBorderWidth,
                mBorderColor,
                mShadowSize,
                mShadowColor,
                Color.TRANSPARENT,
                getScaleType());
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap;

        if (drawable instanceof ColorDrawable) {
            bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
        } else {
            int width = drawable.getIntrinsicWidth();
            width = width > 0 ? width : 1;
            int height = drawable.getIntrinsicHeight();
            height = height > 0 ? height : 1;
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        boolean handled = super.onTouchEvent(event);
        if (mTouchSelectorEnabled) {

            final int action = event.getActionMasked();
            switch (action) {

                case MotionEvent.ACTION_DOWN: {
                    //User touched the view
                    mAlreadyInside = true;
                    updateTouchSelectorColor(mTouchSelectorColor);
                    handled = true;
                    break;
                }

                case MotionEvent.ACTION_MOVE: {

                    final int eventX = (int) event.getX();
                    final int eventY = (int) event.getY();

                    if (!mViewRect.contains(eventX + mViewRect.left, eventY + mViewRect.top)) {
                        //User moved outside
                        if (mAlreadyInside) {
                            mAlreadyInside = false;
                            updateTouchSelectorColor(Color.TRANSPARENT);
                        }
                    }
                    handled = true;
                    break;
                }

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    //User left the view
                    mAlreadyInside = false;
                    updateTouchSelectorColor(Color.TRANSPARENT);
                    handled = true;
                    break;
                }

            }
        }

        return handled;

    }

    /**
     * Updates the view's okulus drawable with the touch selector color
     *
     * @param touchSelectorColor The color to update with, or {@link android.graphics.Color#TRANSPARENT} to disable
     *                           the touch selctor
     */
    private void updateTouchSelectorColor(final int touchSelectorColor) {

        final Drawable content = getDrawable();

        if (content != null && content instanceof OkulusDrawable) {

            ((OkulusDrawable) content).setTouchSelectorColor(touchSelectorColor);
            invalidate();
        }
    }
}
