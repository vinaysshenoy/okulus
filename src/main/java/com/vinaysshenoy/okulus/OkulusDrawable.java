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

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

/** Custom drawable class that takes care of the actual drawing */
class OkulusDrawable extends Drawable {

    private final RectF mRect = new RectF();

    /** Rect used for drawing the border */
    private RectF mBorderRect;

    /** Rect used for drawing the actual image */
    private RectF mImageRect;

    private       BitmapShader mBitmapShader;
    private final Paint        mPaint;
    private       int          mBorderWidth;
    private       int          mBorderColor;
    private       boolean      mFullCircle;
    private       float        mCornerRadius;
    private       int          mShadowWidth;
    private       int          mShadowColor;
    private       float        mShadowRadius;


    public OkulusDrawable(Bitmap bitmap, float cornerRadius, boolean fullCircle, int borderWidth, int borderColor, int shadowWidth, int shadowColor, float shadowRadius) {
        mCornerRadius = cornerRadius;
        mBitmapShader = getShaderForBitmap(bitmap);
        mBorderWidth = borderWidth;
        mBorderColor = borderColor;
        mFullCircle = fullCircle;
        mShadowColor = shadowColor;
        mShadowRadius = shadowRadius;
        mShadowWidth = shadowWidth;

        mBorderRect = new RectF();
        mImageRect = new RectF();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

    }

    /**
     * Creates a bitmap shader with a bitmap
     */
    private BitmapShader getShaderForBitmap(Bitmap bitmap) {
        return new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    }

    public void updateBitmap(Bitmap bitmap) {

        mBitmapShader = getShaderForBitmap(bitmap);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {

        super.onBoundsChange(bounds);
        mRect.set(0, 0, bounds.width(), bounds
                .height());

        if (mFullCircle) {
            mCornerRadius = Math.abs(mRect.left - mRect.right) / 2;
        }

        if (mBorderWidth > 0) {
            initRectsWithBorders();
        } else {
            initRectsWithoutBorders();
        }

    }

    /**
     * Initializes the rects without borders, taking shadows into account
     */
    private void initRectsWithoutBorders() {

        mImageRect.set(mRect);
        if (mShadowWidth > 0) {

            /* Shadows will be drawn to the right & bottom,
             * so adjust the image rect on the right & bottom
             */
            mImageRect.right -= mShadowWidth;
            mImageRect.bottom -= mShadowWidth;
        }
    }

    /**
     * Initialize the rects with borders, taking shadows into account
     */
    private void initRectsWithBorders() {

        mBorderRect.set(mRect);
        mBorderRect.left += mBorderWidth;
        mBorderRect.top += mBorderWidth;
        mBorderRect.right -= mBorderWidth;
        mBorderRect.bottom -= mBorderWidth;

        if (mShadowWidth > 0) {


            /* Shadows will be drawn to the right & bottom,
             * so adjust the border rect on the right & bottom.
             *
             * Since the image rect is calculated from the
             * border rect, the dimens will be accounted for.
             */
            mBorderRect.right -= mShadowWidth;
            mBorderRect.bottom -= mShadowWidth;
        }

        mImageRect.set(
                mBorderRect.left + mBorderWidth,
                mBorderRect.top + mBorderWidth,
                mBorderRect.right - mBorderWidth,
                mBorderRect.bottom - mBorderWidth);
    }

    @Override
    public void draw(Canvas canvas) {

        mPaint.setShader(null);
        drawBordersAndShadow(canvas);
        drawImage(canvas);

    }

    /**
     * Draw the image on the canvas based on the View attributes
     *
     * @param canvas The canvas to draw the image on
     */
    private void drawImage(final Canvas canvas) {

        mPaint.setShader(mBitmapShader);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRoundRect(mImageRect, mCornerRadius, mCornerRadius, mPaint);
    }

    /**
     * Draw the borders & shadows on the canvas based on the view attributes
     *
     * @param canvas The canvas to draw the borders on
     */
    private void drawBordersAndShadow(final Canvas canvas) {

        if (mBorderWidth > 0) {
            mPaint.setShader(null);
            mPaint.setColor(mBorderColor);
            mPaint.setStrokeWidth(mBorderWidth);
            mPaint.setStyle(Paint.Style.STROKE);

            if (mShadowWidth > 0) {
                mPaint.setShadowLayer(mShadowRadius, mShadowWidth, mShadowWidth, mShadowColor);
            }
            canvas.drawRoundRect(mBorderRect, mCornerRadius, mCornerRadius, mPaint);
            mPaint.setShadowLayer(0f, 0f, 0f, mShadowColor);
        }

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }
}
