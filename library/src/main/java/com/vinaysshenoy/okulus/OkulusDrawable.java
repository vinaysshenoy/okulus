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
import android.graphics.BlurMaskFilter;
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
     * Rect used for drawing the shadow
     */
    private RectF mShadowRect;

    /**
     * Rect used for drawing the actual image
     */
    private RectF mImageRect;

    private BitmapShader mBitmapShader;
    private final Paint mPaint;
    private float mBorderSize;
    private int mBorderColor;
    private boolean mFullCircle;
    private float mCornerRadius;
    private float mShadowSize;
    private int mShadowColor;
    private int mBitmapWidth;
    private int mBitmapHeight;
    private int mTouchSelectorColor;

    private Matrix mShaderMatrix;
    private ImageView.ScaleType mScaleType;

    private BlurMaskFilter mShadowMaskFilter;


    public OkulusDrawable(Bitmap bitmap, float cornerRadius, boolean fullCircle, float borderWidth, int borderColor, float shadowSize, int shadowColor, int touchSelectorColor, ImageView.ScaleType scaleType) {

        mCornerRadius = cornerRadius;
        mBorderSize = borderWidth;
        mBorderColor = borderColor;
        mFullCircle = fullCircle;
        mShadowColor = shadowColor;
        mShadowSize = shadowSize;
        mTouchSelectorColor = touchSelectorColor;

        if (ImageView.ScaleType.FIT_XY != scaleType && ImageView.ScaleType.CENTER_CROP != scaleType) {
            android.util.Log.w(TAG, "Only FIT_XY and CENTER_CROP scale types are supported");
        } else {
            mScaleType = scaleType;
        }

        mBorderRect = new RectF();
        mImageRect = new RectF();
        mShadowRect = new RectF();
        mShaderMatrix = new Matrix();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

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
        mShadowRect.set(mRect);
        mShadowRect.inset(mShadowSize, mShadowSize);

        if (mFullCircle) {
            mCornerRadius = Math.abs(mRect.left - mRect.right) / 2;
        }

        if (mBorderSize > 0) {
            initRectsWithBorders();
        } else {
            initRectsWithoutBorders();
        }
        if(mShadowSize > 0) {
            mShadowMaskFilter = new BlurMaskFilter(mShadowSize * 0.95F, BlurMaskFilter.Blur.SOLID);
        }
        updateShaderMatrix();
    }

    /**
     * Initializes the rects without borders, taking shadows into account
     */
    private void initRectsWithoutBorders() {

        mImageRect.set(mRect);
        mImageRect.bottom -= (mShadowSize * 0.5F);

    }

    /**
     * Initialize the rects with borders, taking shadows into account
     */
    private void initRectsWithBorders() {

        mBorderRect.set(mRect);
        mBorderRect.inset(mBorderSize, mBorderSize);
        mBorderRect.left += Math.min(mBorderSize, mShadowSize) * 0.5F;
        mBorderRect.right -= Math.min(mBorderSize, mShadowSize) * 0.5F;
        mBorderRect.top += Math.min(mBorderSize, mShadowSize) * 0.5F;
        mBorderRect.bottom -= (mShadowSize + mBorderSize * 0.5F);
//                * 1.25F;
        mImageRect.set(mBorderRect);
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

    @Override
    public void draw(Canvas canvas) {

        if (mBitmapShader != null) {

            if (mShadowSize > 0) {
                drawShadows(canvas);
            }
            drawImage(canvas);
            drawBorders(canvas);
            if (mTouchSelectorColor != Color.TRANSPARENT) {
                drawTouchSelector(canvas);
            }
        }

    }

    /**
     * Draws drop shadows
     */
    private void drawShadows(Canvas canvas) {

        mPaint.setShader(null);
        mPaint.setStrokeWidth(mShadowSize);
        mPaint.setColor(mShadowColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setMaskFilter(mShadowMaskFilter);
        if (mFullCircle) {
            canvas.drawCircle(mShadowRect.centerX(), mShadowRect.centerY(), mShadowRect.width() / 2.0F, mPaint);
        } else {
            canvas.drawRoundRect(mShadowRect, mCornerRadius, mCornerRadius, mPaint);
        }
        mPaint.setMaskFilter(null);
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

        if (mBorderSize > 0) {
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

        mPaint.setColor(Color.WHITE);
        mPaint.setShader(mBitmapShader);
        mPaint.setStyle(Paint.Style.FILL);
        if (mFullCircle) {
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
    private void drawBorders(final Canvas canvas) {

        if (mBorderSize > 0) {
            mPaint.setShader(null);
            mPaint.setColor(mBorderColor);
            mPaint.setStrokeWidth(mBorderSize);
            mPaint.setStyle(Paint.Style.STROKE);

            if (mFullCircle) {
                canvas.drawCircle(mBorderRect.centerX(), mBorderRect.centerY(), mBorderRect.width() / 2.0F, mPaint);
            } else {
                canvas.drawRoundRect(mBorderRect, mCornerRadius, mCornerRadius, mPaint);
            }
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
