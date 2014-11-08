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
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/** Custom drawable class that takes care of the actual drawing */
class OkulusDrawable extends Drawable {

    private final RectF mRect = new RectF();

    /** Rect used for drawing the border */
    private RectF mBorderRect;

    /** Rect used for drawing the actual image */
    private RectF mImageRect;

    private       BitmapShader mBitmapShader;
    private final Paint        mPaint;
    private       float        mBorderWidth;
    private       int          mBorderColor;
    private       boolean      mFullCircle;
    private       float        mCornerRadius;
    private       float        mShadowWidth;
    private       int          mShadowColor;
    private       float        mShadowRadius;
    private       int          mBitmapWidth;
    private       int          mBitmapHeight;
    private       int          mTouchSelectorColor;

    private Matrix              mShaderMatrix;
    private ImageView.ScaleType mScaleType;


    public OkulusDrawable(Bitmap bitmap, float cornerRadius, boolean fullCircle, float borderWidth, int borderColor, float shadowWidth, int shadowColor, float shadowRadius, int touchSelectorColor, ImageView.ScaleType scaleType) {

        mCornerRadius = cornerRadius;
        mBorderWidth = borderWidth;
        mBorderColor = borderColor;
        mFullCircle = fullCircle;
        mShadowColor = shadowColor;
        mShadowRadius = shadowRadius;
        mShadowWidth = shadowWidth;
        mTouchSelectorColor = touchSelectorColor;
        mScaleType = scaleType;

        mBorderRect = new RectF();
        mImageRect = new RectF();
        mShaderMatrix = new Matrix();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        updateBitmap(bitmap);

    }

    /**
     * Updates the touch selector color
     *
     * @param touchSelectorColor The color to use as the touch selector
     */
    public void setTouchSelectorColor(final int touchSelectorColor) {
        mTouchSelectorColor = touchSelectorColor;
    }

    /**
     * Creates a bitmap shader with a bitmap
     */
    private BitmapShader getShaderForBitmap(Bitmap bitmap) {
        return new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    }

    /**
     * Updates the drawable with a Bitmap. {@link OkulusImageView#invalidate()} must be called by
     * the caller after this method returns
     *
     * @param bitmap The Bitmap to set, or <code>null</code> to clear the bitmap being drawn
     */
    public void updateBitmap(Bitmap bitmap) {

        if (bitmap == null) {
            mBitmapShader = null;
            mBitmapWidth = 0;
            mBitmapHeight = 0;
        } else {
            mBitmapWidth = bitmap.getWidth();
            mBitmapHeight = bitmap.getHeight();
            mBitmapShader = getShaderForBitmap(bitmap);
            mBitmapShader.setLocalMatrix(mShaderMatrix);
        }

    }

    @Override
    protected void onBoundsChange(Rect bounds) {

        super.onBoundsChange(bounds);
        mRect.set(bounds);

        if (mFullCircle) {
            mCornerRadius = Math.abs(mRect.left - mRect.right) / 2;
        }

        updateRects();
        /*if (mBorderWidth > 0) {
            initRectsWithBorders();
        } else {
            initRectsWithoutBorders();
        }*/


    }

    /**
     * Updates the bitmap shader matrix to take the scale type into account
     */
    private void updateRects() {

        mImageRect.set(mRect);
        float scale;
        float dx;
        float dy;

        switch (mScaleType) {
            case CENTER: {
                mBorderRect.set(mRect);
                mBorderRect.inset(mBorderWidth / 1.3f, mBorderWidth / 1.3f);
                updateBorderRectForShadows();
                mShaderMatrix.set(null);
                mShaderMatrix.setTranslate((int) ((mBorderRect.width() - mBitmapWidth) * 0.5f + 0.5f),
                                           (int) ((mBorderRect.height() - mBitmapHeight) * 0.5f + 0.5f));
                break;
            }

            case CENTER_CROP: {
                mBorderRect.set(mRect);
                mBorderRect.inset(mBorderWidth / 1.3f, mBorderWidth / 1.3f);
                updateBorderRectForShadows();

                mShaderMatrix.set(null);

                dx = 0;
                dy = 0;

                if (mBitmapWidth * mBorderRect.height() > mBorderRect.width() * mBitmapHeight) {
                    scale = mBorderRect.height() / (float) mBitmapHeight;
                    dx = (mBorderRect.width() - mBitmapWidth * scale) * 0.5f;
                } else {
                    scale = mBorderRect.width() / (float) mBitmapWidth;
                    dy = (mBorderRect.height() - mBitmapHeight * scale) * 0.5f;
                }

                mShaderMatrix.setScale(scale, scale);
                mShaderMatrix.postTranslate((int) (dx + 0.5f) + mBorderWidth,
                                            (int) (dy + 0.5f) + mBorderWidth);
                break;
            }

            case CENTER_INSIDE: {
                mShaderMatrix.set(null);

                if (mBitmapWidth <= mRect.width() && mBitmapHeight <= mRect.height()) {
                    scale = 1.0f;
                } else {
                    scale = Math.min(mRect.width() / (float) mBitmapWidth,
                                     mRect.height() / (float) mBitmapHeight);
                }

                dx = (int) ((mRect.width() - mBitmapWidth * scale) * 0.5f + 0.5f);
                dy = (int) ((mRect.height() - mBitmapHeight * scale) * 0.5f + 0.5f);

                mShaderMatrix.setScale(scale, scale);
                mShaderMatrix.postTranslate(dx, dy);

                mBorderRect.set(mImageRect);
                mShaderMatrix.mapRect(mBorderRect);
                mBorderRect.inset(mBorderWidth / 1.3f, mBorderWidth / 1.3f);
                updateBorderRectForShadows();
                mShaderMatrix.setRectToRect(mImageRect, mBorderRect, Matrix.ScaleToFit.FILL);
                break;
            }

            case FIT_END: {
                mBorderRect.set(mImageRect);
                mShaderMatrix.setRectToRect(mImageRect, mRect, Matrix.ScaleToFit.END);
                mShaderMatrix.mapRect(mBorderRect);
                mBorderRect.inset(mBorderWidth / 1.3f, mBorderWidth / 1.3f);
                updateBorderRectForShadows();
                mShaderMatrix.setRectToRect(mImageRect, mBorderRect, Matrix.ScaleToFit.FILL);
                break;
            }

            case FIT_START: {
                mBorderRect.set(mImageRect);
                mShaderMatrix.setRectToRect(mImageRect, mRect, Matrix.ScaleToFit.START);
                mShaderMatrix.mapRect(mBorderRect);
                mBorderRect.inset(mBorderWidth / 1.3f, mBorderWidth / 1.3f);
                updateBorderRectForShadows();
                mShaderMatrix.setRectToRect(mImageRect, mBorderRect, Matrix.ScaleToFit.FILL);
                break;
            }

            case FIT_XY: {
                mBorderRect.set(mRect);
                mBorderRect.inset(mBorderWidth / 1.3f, mBorderWidth / 1.3f);
                updateBorderRectForShadows();
                mShaderMatrix.set(null);
                mShaderMatrix.setRectToRect(mImageRect, mBorderRect, Matrix.ScaleToFit.FILL);
                break;
            }

            default:
            case FIT_CENTER: {
                mBorderRect.set(mImageRect);
                mShaderMatrix.setRectToRect(mImageRect, mRect, Matrix.ScaleToFit.CENTER);
                mShaderMatrix.mapRect(mBorderRect);
                mBorderRect.inset(mBorderWidth / 1.3f, mBorderWidth / 1.3f);
                mShaderMatrix.setRectToRect(mImageRect, mBorderRect, Matrix.ScaleToFit.FILL);
                break;
            }
        }

        mImageRect.set(mBorderRect);
        if (mBitmapShader != null) {
            mBitmapShader.setLocalMatrix(mShaderMatrix);
        }

    }

    /**
     * Updates the border rect if shadows are to be drawn
     */
    private void updateBorderRectForShadows() {

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
        mBorderRect.inset(mBorderWidth / 1.3f, mBorderWidth / 1.3f);

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

        mImageRect.set(mBorderRect);
        mImageRect.inset(mBorderWidth / 1.3f, mBorderWidth / 1.3f);
    }

    @Override
    public void draw(Canvas canvas) {

        mPaint.setShader(null);
        drawBordersAndShadow(canvas);
        if (mBitmapShader != null) {
            drawImage(canvas);
        } else {
            //TODO: Draw some custom background color here
        }
        if (mTouchSelectorColor != Color.TRANSPARENT) {
            drawTouchSelector(canvas);
        }

    }

    /**
     * Draws the touch selector on the canvas based on the View attributes
     *
     * @param canvas The canvas to draw the touch selector on
     */
    private void drawTouchSelector(final Canvas canvas) {

        final int prevColor = mPaint.getColor();
        mPaint.setShader(null);
        mPaint.setColor(mTouchSelectorColor);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        if (mBorderWidth > 0) {
            canvas.drawRoundRect(mBorderRect, mCornerRadius, mCornerRadius, mPaint);
        } else {
            canvas.drawRoundRect(mImageRect, mCornerRadius, mCornerRadius, mPaint);
        }
        mPaint.setColor(prevColor);
    }

    /**
     * Draw the image on the canvas based on the View attributes
     *
     * @param canvas The canvas to draw the image on
     */
    private void drawImage(final Canvas canvas) {

        mPaint.setShader(mBitmapShader);
        mPaint.setStyle(Paint.Style.FILL);
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
