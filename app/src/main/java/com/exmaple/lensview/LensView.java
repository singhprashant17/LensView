package com.exmaple.lensview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.util.ArrayList;

public class LensView extends View {

    private static final String TAG = "LensView";
    private final AttributeSet attrs;
    private Paint mPaintIcons;
    //    private Paint mPaintCircles;
    private Paint mPaintTouchSelection;
    private Paint mPaintText;
    private float mTouchX = -Float.MAX_VALUE;
    private float mTouchY = -Float.MAX_VALUE;
    private boolean mInsideRect;
    private RectF mRectToSelect = new RectF(0, 0, 0, 0);
    private boolean mMustVibrate = true;
    private int mSelectIndex;
    private AdapterClass mApps;
    private float mAnimationMultiplier;
    private boolean mAnimationHiding;
    private int mNumberOfCircles;
    private float mTouchSlop;
    private boolean mMoving;
    private Rect mInsets = new Rect(0, 0, 0, 0);
    private int highlightColor;
    private boolean showTouchSelection;
    private float scaleFactor;
    private float distortionFactor;
    private int animationTime;
    private float iconSize;
    private float touchSelectionRadius;

    public LensView(Context context) {
        super(context);
        attrs = null;
        init();
    }

    public LensView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.attrs = attrs;
        init();
    }

    public LensView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.attrs = attrs;
        init();
    }

    public void setAdapter(AdapterClass apps) {
        mApps = apps;
        invalidate();
    }

    private void init() {
        mApps = new AdapterClass(getContext(), new ArrayList<App>());
        setupVariables();
        setupPaints();
    }

    private void setupVariables() {
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs,
                R.styleable.LensView, 0, 0);
        highlightColor = typedArray.getColor(R.styleable.LensView_highlightColor, 0);
        showTouchSelection = typedArray.getBoolean(R.styleable.LensView_showTouchSelection, false);

        scaleFactor = typedArray.getFloat(R.styleable.LensView_scaleFactor,
                ScaleFactor.DEFAULT_SCALE_FACTOR);

        distortionFactor = typedArray.getFloat(R.styleable.LensView_scaleFactor,
                DistortionFactor.DEFAULT_DISTORTION_FACTOR);

        animationTime = typedArray.getInt(R.styleable.LensView_animationTime,
                AnimationTime.DEFAULT_ANIMATION_TIME);

        iconSize = typedArray.getDimension(R.styleable.LensView_iconSize,
                IconSize.DEFAULT_ICON_SIZE);

        touchSelectionRadius = typedArray.getDimension(R.styleable.LensView_touchSelectionRadius,
                getResources().getDimension(R.dimen.radius_touch_selection));

        typedArray.recycle();
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        mInsets = insets;
        return true;
    }

    private void setupPaints() {
        mPaintIcons = new Paint();
        mPaintIcons.setAntiAlias(true);
        mPaintIcons.setStyle(Paint.Style.FILL);
        mPaintIcons.setFilterBitmap(true);
        mPaintIcons.setDither(true);

        mPaintTouchSelection = new Paint();
        mPaintTouchSelection.setAntiAlias(true);
        mPaintTouchSelection.setStyle(Paint.Style.STROKE);
        mPaintTouchSelection.setColor(getHighlightColor());
        mPaintTouchSelection.setStrokeWidth(getResources().getDimension(R.dimen
                .stroke_width_touch_selection));

        mPaintText = new Paint();
        mPaintText.setAntiAlias(true);
        mPaintText.setStyle(Paint.Style.FILL);
        mPaintText.setColor(ContextCompat.getColor(getContext(), android.R.color.white));
        mPaintText.setTextSize(getResources().getDimension(R.dimen.text_size_lens));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mApps != null) {
            drawGrid(canvas, mApps.getCount());
        }
        if (isShowTouchSelection()) {
            drawTouchSelection(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (event.getX() < 0.0f) {
                    mTouchX = 0.0f;
                } else {
                    mTouchX = event.getX();
                }
                if (event.getY() < 0.0f) {
                    mTouchY = 0.0f;
                } else {
                    mTouchY = event.getY();
                }
                mSelectIndex = -1;
                mMoving = false;
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!mMoving && Math.sqrt(Math.pow(event.getX() - mTouchX, 2) + Math.pow
                        (event.getY() - mTouchY, 2)) > mTouchSlop) {
                    mMoving = true;
                    LensAnimation lensShowAnimation = new LensAnimation(true);
                    startAnimation(lensShowAnimation);
                }

                if (!mMoving) {
                    return true;
                }

                if (event.getX() < 0.0f) {
                    mTouchX = 0.0f;
                } else {
                    mTouchX = event.getX();
                }
                if (event.getY() < 0.0f) {
                    mTouchY = 0.0f;
                } else {
                    mTouchY = event.getY();
                }
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_UP: {
                if (mMoving) {
                    LensAnimation lensHideAnimation = new LensAnimation(false);
                    startAnimation(lensHideAnimation);
                    mMoving = false;
                } else {
                    // FIXME: 19/1/18 onSelection();
                }
                return true;
            }
            default: {
                return super.onTouchEvent(event);
            }
        }
    }

    private void drawTouchSelection(Canvas canvas) {
        canvas.drawCircle(mTouchX, mTouchY, touchSelectionRadius, mPaintTouchSelection);
    }

    private void drawGrid(Canvas canvas, int itemCount) {
        Grid grid = LensCalculator.calculateGrid(
                getContext(),
                getWidth() - (mInsets.left + mInsets.right),
                getHeight() - (mInsets.top + mInsets.bottom),
                itemCount, iconSize);
        mInsideRect = false;
        int selectIndex = -1;
        mRectToSelect = null;

        for (float y = 0.0f; y < (float) grid.getItemCountVertical(); y += 1.0f) {
            for (float x = 0.0f; x < (float) grid.getItemCountHorizontal(); x += 1.0f) {

                int currentItem = (int) (y * (float) grid.getItemCountHorizontal() + (x + 1.0f));
                int currentIndex = currentItem - 1;

                if (currentItem <= grid.getItemCount()) {
                    RectF rect = new RectF();
                    rect.left = mInsets.left + (x + 1.0f) * grid.getSpacingHorizontal() + x *
                            grid.getItemSize();
                    rect.top = mInsets.top + (y + 1.0f) * grid.getSpacingVertical() + y * grid
                            .getItemSize();
                    rect.right = rect.left + grid.getItemSize();
                    rect.bottom = rect.top + grid.getItemSize();

                    float animationMultiplier;
                    animationMultiplier = mAnimationMultiplier;

                    if (mTouchX >= 0 && mTouchY >= 0) {
                        float shiftedCenterX = LensCalculator.shiftPoint(getContext(), mTouchX,
                                rect.centerX(), getWidth(), animationMultiplier,
                                getDistortionFactor());
                        float shiftedCenterY = LensCalculator.shiftPoint(getContext(), mTouchY,
                                rect.centerY(), getHeight(), animationMultiplier,
                                getDistortionFactor());
                        float scaledCenterX = LensCalculator.scalePoint(getContext(), mTouchX,
                                rect.centerX(), rect.width(), getWidth(), animationMultiplier,
                                getScaleFactor(), getDistortionFactor());
                        float scaledCenterY = LensCalculator.scalePoint(getContext(), mTouchY,
                                rect.centerY(), rect.height(), getHeight(), animationMultiplier,
                                getScaleFactor(), getDistortionFactor());
                        float newSize = LensCalculator.calculateSquareScaledSize(scaledCenterX,
                                shiftedCenterX, scaledCenterY, shiftedCenterY);

                        if (getDistortionFactor() > 0.0f && getScaleFactor() > 0.0f) {
                            rect = LensCalculator.calculateRect(shiftedCenterX, shiftedCenterY,
                                    newSize);
                        } else if (getDistortionFactor() > 0.0f && getScaleFactor() == 0.0f) {
                            rect = LensCalculator.calculateRect(shiftedCenterX, shiftedCenterY,
                                    rect.width());
                        }

                        if (LensCalculator.isInsideRect(mTouchX, mTouchY, rect)) {
                            mInsideRect = true;
                            selectIndex = currentIndex;
                            mRectToSelect = rect;
                        }
                    }

                    drawAppIcon(canvas, rect, currentIndex);
                }
            }
        }

        if (selectIndex >= 0) {
            mMustVibrate = selectIndex != mSelectIndex;
        } else {
            mMustVibrate = false;
        }

        if (!mAnimationHiding) {
            mSelectIndex = selectIndex;
        }

        performHoverVibration();

        if (mRectToSelect != null && mApps != null && mSelectIndex >= 0) {
            drawAppName(canvas, mRectToSelect);
        }
    }

    private void drawAppIcon(Canvas canvas, RectF rect, int index) {
        if (index < mApps.getCount()) {
            Bitmap appIcon = mApps.getBitmap(index);
            Rect src = new Rect(0, 0, appIcon.getWidth(), appIcon.getHeight());
            canvas.drawBitmap(appIcon, src, rect, mPaintIcons);
        }
    }

    private void drawAppName(Canvas canvas, RectF rect) {
        if (showNameAppHover() && mMoving) {
            canvas.drawText(mApps.getItem(mSelectIndex).toString(),
                    rect.centerX(),
                    rect.top - getResources().getDimension(R.dimen.margin_lens_text),
                    mPaintText);
        }
    }

    private boolean showNameAppHover() {
        return true;
    }

    private void performHoverVibration() {
        if (mInsideRect) {
            if (mMustVibrate) {
                if (vibrateAppHoover() && !mAnimationHiding) {
                    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                }
                mMustVibrate = false;
            }
        } else {
            mMustVibrate = true;
        }
    }

    private boolean vibrateAppHoover() {
        return false;
    }

    public int getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(int highlightColor) {
        this.highlightColor = highlightColor;
        invalidate();
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(@FloatRange(from = ScaleFactor.MIN_SCALE_FACTOR, to = ScaleFactor
            .MAX_SCALE_FACTOR) float scaleFactor) {
        this.scaleFactor = scaleFactor;
        invalidate();
    }

    public float getDistortionFactor() {
        return distortionFactor;
    }

    public void setDistortionFactor(@FloatRange(from = DistortionFactor.MIN_DISTORTION_FACTOR, to
            = DistortionFactor.MAX_DISTORTION_FACTOR) float distortionFactor) {
        this.distortionFactor = distortionFactor;
        invalidate();
    }

    public boolean isShowTouchSelection() {
        return showTouchSelection;
    }

    public void setShowTouchSelection(boolean showTouchSelection) {
        this.showTouchSelection = showTouchSelection;
        invalidate();
    }

    public int getAnimationTime() {
        return animationTime;
    }

    public void setAnimationTime(@IntRange(from = AnimationTime.MIN_ANIMATION_TIME, to =
            AnimationTime.MAX_ANIMATION_TIME) int animationTime) {
        this.animationTime = animationTime;
        invalidate();
    }

    public float getIconSize() {
        return iconSize;
    }


    public void setIconSize(@FloatRange(from = IconSize.MIN_ICON_SIZE, to = IconSize
            .MAX_ICON_SIZE) float iconSize) {
        this.iconSize = iconSize;
        invalidate();
    }

    static class IconSize {
        static final float DEFAULT_ICON_SIZE = 18;
        static final float MAX_ICON_SIZE = 45;
        static final float MIN_ICON_SIZE = 10;
    }

    static class DistortionFactor {
        static final int MAX_DISTORTION_FACTOR = 9;
        static final float MIN_DISTORTION_FACTOR = 0.5f;
        static final float DEFAULT_DISTORTION_FACTOR = 2.5f;
    }

    static class ScaleFactor {
        static final float MAX_SCALE_FACTOR = 5;
        static final float MIN_SCALE_FACTOR = 1.0f;
        static final float DEFAULT_SCALE_FACTOR = 1.0f;
    }

    static class AnimationTime {
        static final int MAX_ANIMATION_TIME = 600;
        static final int MIN_ANIMATION_TIME = 100;
        static final int DEFAULT_ANIMATION_TIME = 200;
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    private class LensAnimation extends Animation {

        private final boolean mShow;

        public LensAnimation(boolean show) {
            mShow = show;
            setInterpolator(new AccelerateDecelerateInterpolator());
            setDuration(getAnimationTime());
            setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if (!mShow) {
                        mAnimationHiding = true;
                        mPaintText.clearShadowLayer();
                    } else {
                        mAnimationHiding = false;
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (!mShow) {
                        // FIXME: 19/1/18 onSelection();
                        mTouchX = -Float.MAX_VALUE;
                        mTouchY = -Float.MAX_VALUE;
                        mAnimationHiding = false;
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            if (mShow) {
                mAnimationMultiplier = interpolatedTime;
                mPaintTouchSelection.setColor(getHighlightColor());
                mPaintTouchSelection.setAlpha((int) (255.0f * interpolatedTime));
                mPaintText.setAlpha((int) (255.0f * interpolatedTime));
            } else {
                mAnimationMultiplier = 1.0f - interpolatedTime;
                mPaintTouchSelection.setColor(getHighlightColor());
                mPaintTouchSelection.setAlpha((int) (255.0f * (1.0f - interpolatedTime)));
                mPaintText.setAlpha((int) (255.0f * (1.0f - interpolatedTime)));
            }
            postInvalidate();
        }
    }
}
