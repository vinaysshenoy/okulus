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

/**
 * Custom drawable class that takes care of the actual drawing
 */
class OkulusDrawable extends Drawable {

    private static final String TAG = "OkulusDrawable";

    private final RectF mRect = new RectF();

    /**
     * Rect used for drawing the border
     */
    private RectF mBorderRect;

    /**
     * Rect used for drawing the actual image
     */
    private RectF mImageRect;

    private BitmapShader mBitmapShader;
    private final Paint mPaint;
    private float mBorderWidth;
    private int mBorderColor;
    private boolean mFullCircle;
    private float mCornerRadius;
    private float mShadowWidth;
    private int mShadowColor;
    private float mShadowRadius;
    private int mBitmapWidth;
    private int mBitmapHeight;
    private int mTouchSelectorColor;

    private Matrix mShaderMatrix;
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

        if(ImageView.ScaleType.FIT_XY != scaleType && ImageView.ScaleType.CENTER_CROP != scaleType) {
            android.util.Log.w(TAG, "Only FIT_XY and CENTER_CROP scale types are supported");
        } else {
            mScaleType = scaleType;
        }

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

        if (mBorderWidth > 0) {
            initRectsWithBorders();
        } else {
            initRectsWithoutBorders();
        }
        updateShaderMatrix();
    }

    /**
     * Updates the bitmap shader matrix to take the scale type into account
     */
    private void updateShaderMatrix() {

        final float viewWidth = Math.abs(mRect.left - mRect.right);
        final float viewHeight = Math.abs(mRect.top - mRect.bottom);

        mShaderMatrix.reset();

        if (mBitmapWidth == 0 && mBitmapHeight == 0) {
            return;
        }
        final float widthScale = viewWidth / (float) mBitmapWidth;
        final float heightScale = viewHeight / (float) mBitmapHeight;

        if (mScaleType == ImageView.ScaleType.CENTER_CROP) {
            float scale = Math.max(widthScale, heightScale);
            mShaderMatrix.postScale(scale, scale);
            mShaderMatrix.postTranslate((viewWidth - mBitmapWidth * scale) / 2F,
                    (viewHeight - mBitmapHeight * scale) / 2F);

        } else if (mScaleType == ImageView.ScaleType.FIT_XY) {

            final RectF tempSrc = new RectF(0, 0, mBitmapWidth, mBitmapHeight);
            mShaderMatrix.setRectToRect(tempSrc, mRect, Matrix.ScaleToFit.FILL);
            float scale = Math.min(1.0f, Math.min(widthScale, heightScale));
            mShaderMatrix.postScale(scale, scale);
            mShaderMatrix.postTranslate((viewWidth - mBitmapWidth * scale) / 2F,
                    (viewHeight - mBitmapHeight * scale) / 2F);

        }

        if (mBitmapShader != null) {
            mBitmapShader.setLocalMatrix(mShaderMatrix);
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
    }

    @Override
    public void draw(Canvas canvas) {

        if(mShadowWidth > 0) {
            drawShadows(canvas);
        }
        if (mBitmapShader != null) {
            drawImage(canvas);
        } else {
            //TODO: Draw some custom background color here
        }
        drawBordersAndShadow(canvas);
        if (mTouchSelectorColor != Color.TRANSPARENT) {
            drawTouchSelector(canvas);
        }

    }

    /**
     * Draws drop shadows
     * */
    private void drawShadows(Canvas canvas) {

        mPaint.setShader(null);
        mPaint.setStrokeWidth(0F);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setShadowLayer(mShadowRadius, mShadowWidth, mShadowWidth, mShadowColor);
        canvas.drawRoundRect(mBorderRect, mCornerRadius, mCornerRadius, mPaint);
        mPaint.setShadowLayer(0f, 0f, 0f, mShadowColor);
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
        if(mFullCircle) {
            canvas.drawCircle(mImageRect.centerX(), mImageRect.centerY(), mImageRect.width() / 2F, mPaint);
        } else {
            canvas.drawRoundRect(mImageRect, mCornerRadius, mCornerRadius, mPaint);
        }
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
            canvas.drawRoundRect(mBorderRect, mCornerRadius, mCornerRadius, mPaint);
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
