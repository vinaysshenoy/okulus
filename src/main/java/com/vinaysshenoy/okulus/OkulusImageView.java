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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * Custom ImageView to draw the content in the desired way.
 *
 * @author Vinay S Shenoy
 */
public class OkulusImageView extends ImageView {

    private static final int     DEFAULT_CORNER_RADIUS = 5;               //dips
    private static final int     DEFAULT_BORDER_WIDTH  = 0;                //dips
    private static final int     DEFAULT_BORDER_COLOR  = Color.BLACK;
    private static final int     DEFAULT_SHADOW_WIDTH  = 0; //dips
    private static final int     DEFAULT_SHADOW_COLOR  = 0xB3444444; //70% dark gray
    private static final boolean DEFAULT_FULL_CIRCLE   = false;
    private static final float   DEFAULT_SHADOW_RADIUS = 0.5f;

    private int          mCornerRadius;
    private int          mBorderWidth;
    private int          mBorderColor;
    private int          mShadowWidth;
    private float        mShadowRadius;
    private int          mShadowColor;
    private boolean      mFullCircle;

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

        mCornerRadius = dpToPx(DEFAULT_CORNER_RADIUS);
        int borderWidthInDips = DEFAULT_BORDER_WIDTH;
        mBorderColor = DEFAULT_BORDER_COLOR;
        mFullCircle = DEFAULT_FULL_CIRCLE;
        int shadowWidthInDips = DEFAULT_SHADOW_WIDTH;
        mShadowColor = DEFAULT_SHADOW_COLOR;
        mShadowRadius = DEFAULT_SHADOW_RADIUS;


        if (attrs != null) {

            TypedArray styledAttrs = context
                    .obtainStyledAttributes(attrs, R.styleable.OkulusImageView);
            mCornerRadius = (int) styledAttrs
                    .getDimension(R.styleable.OkulusImageView_cornerRadius, mCornerRadius);
            mBorderColor = styledAttrs
                    .getColor(R.styleable.OkulusImageView_borderColor, mBorderColor);
            mFullCircle = styledAttrs
                    .getBoolean(R.styleable.OkulusImageView_fullCircle, mFullCircle);
            mShadowColor = styledAttrs
                    .getColor(R.styleable.OkulusImageView_shadowColor, mShadowColor);
            mShadowRadius = styledAttrs
                    .getFloat(R.styleable.OkulusImageView_shadowRadius, mShadowRadius);

            int dimension = (int) styledAttrs
                    .getDimension(R.styleable.OkulusImageView_borderWidth, borderWidthInDips);
            borderWidthInDips = pxToDp(dimension);
            dimension = (int) styledAttrs
                    .getDimension(R.styleable.OkulusImageView_shadowWidth, shadowWidthInDips);
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

    }

    /**
     * Converts a raw pixel value to a dp value, based on the device density
     */
    private static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * Converts a raw dp value to a pixel value, based on the device density
     */
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
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
    private void clampBorderAndShadowWidths(int borderWidthInDips, int shadowWidthInDips) {

        if (borderWidthInDips > 5) {
            borderWidthInDips = 5;
        } else if (borderWidthInDips < 0) {
            borderWidthInDips = 0;
        }

        if (shadowWidthInDips > 3) {
            shadowWidthInDips = 3;
        } else if (shadowWidthInDips < 0) {
            shadowWidthInDips = 0;
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

        final Drawable content = getDrawable();

        if (content instanceof OkulusDrawable) {
            ((OkulusDrawable) content).updateBitmap(bm);
            invalidate();
        } else {
            setImageDrawable(null);
            setImageDrawable(new OkulusDrawable(bm, mCornerRadius, mFullCircle, mBorderWidth, mBorderColor, mShadowWidth, mShadowColor, mShadowRadius));
        }
    }

}
