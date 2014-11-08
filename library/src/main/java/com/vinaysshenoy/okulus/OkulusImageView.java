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
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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

    private static final float   DEFAULT_CORNER_RADIUS          = 5f;               //dips
    private static final float   DEFAULT_BORDER_WIDTH           = 0f;                //dips
    private static final int     DEFAULT_BORDER_COLOR           = Color.BLACK;
    private static final float   DEFAULT_SHADOW_WIDTH           = 0f; //dips
    private static final int     DEFAULT_SHADOW_COLOR           = 0xB3444444; //70% dark gray
    private static final int     DEFAULT_TOUCH_SELECTOR_COLOR   = 0x66444444; //40% dark gray
    private static final boolean DEFAULT_FULL_CIRCLE            = false;
    private static final float   DEFAULT_SHADOW_RADIUS          = 0.5f;
    private static final boolean DEFAULT_TOUCH_SELECTOR_ENABLED = false;

    private float   mCornerRadius;
    private float   mBorderWidth;
    private int     mBorderColor;
    private float   mShadowWidth;
    private float   mShadowRadius;
    private int     mShadowColor;
    private int     mTouchSelectorColor;
    private boolean mTouchSelectorEnabled;
    private boolean mFullCircle;

    /** Used to store the view coordinates for holding touch events */
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
        mShadowRadius = DEFAULT_SHADOW_RADIUS;
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
            mShadowRadius = styledAttrs
                    .getFloat(R.styleable.OkulusImageView_okulus_shadowRadius, mShadowRadius);
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

        clampBorderAndShadowWidths(borderWidthInDips, shadowWidthInDips);
        mBorderWidth = dpToPx(borderWidthInDips);
        mShadowWidth = dpToPx(shadowWidthInDips);

        if (mShadowWidth > 0f) {

            if (Build.VERSION.SDK_INT >= 14) {
                /* We need to set layer type for shadows to work
                 * on ICS and above
                 */
                setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
        }
        setOkulusDrawable(null);

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


    /**
     * Clamps the widths of the border & shadow(if set) to a sane max. This modifies the passed in
     * paramters if needed
     * <p/>
     * Currently, border is allowed to be [1,5] dp and shadow is allowed to be [1,3] dp
     * <p/>
     * If they exceed their maximums, they are set to the max value. If they go below the minimums,
     * they are set to 0
     *
     * @param borderWidthInDips The set border width in dips
     * @param shadowWidthInDips The set shadow width in dips
     */
    private void clampBorderAndShadowWidths(float borderWidthInDips, float shadowWidthInDips) {

        if (borderWidthInDips > 5f) {
            borderWidthInDips = 5f;
        } else if (borderWidthInDips < 0f) {
            borderWidthInDips = 0f;
        }

        if (shadowWidthInDips > 3f) {
            shadowWidthInDips = 3f;
        } else if (shadowWidthInDips < 0f) {
            shadowWidthInDips = 0f;
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        int requiredWidth = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int requiredHeight = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();

        requiredWidth = ViewCompat.resolveSizeAndState(
                requiredWidth,
                widthMeasureSpec,
                1
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
                requiredHeight = requiredWidth;
            } else {
                requiredWidth = requiredHeight;
            }

        }

        setMeasuredDimension(requiredWidth, requiredHeight);

    }

    @Override
    public void setImageBitmap(Bitmap bm) {

        if(bm == null) {
            super.setImageBitmap(null);
        } else {
            final Drawable content = getDrawable();

            if (content instanceof OkulusDrawable) {
                ((OkulusDrawable) content).updateBitmap(bm);
                invalidate();
            } else {

                setImageDrawable(null);
                setOkulusDrawable(bm);
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

        setImageDrawable(new OkulusDrawable(
                                 bitmap,
                                 mCornerRadius,
                                 mFullCircle,
                                 mBorderWidth,
                                 mBorderColor,
                                 mShadowWidth,
                                 mShadowColor,
                                 mShadowRadius,
                                 Color.TRANSPARENT)
        );

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
                        if(mAlreadyInside) {
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
