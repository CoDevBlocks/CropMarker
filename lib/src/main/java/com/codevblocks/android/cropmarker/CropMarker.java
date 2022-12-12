package com.codevblocks.android.cropmarker;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.Arrays;
import java.util.List;

public class CropMarker extends View {

    public static final int MASK_RECTANGLE = 0;
    public static final int MASK_OVAL = 1;

    public static final int GRID_LINES_BEHAVIOR_NONE = 0;
    public static final int GRID_LINES_BEHAVIOR_TOUCH = 1;
    public static final int GRID_LINES_BEHAVIOR_ALWAYS = 2;

    private static final boolean DEFAULT_ENABLED = false;
    private static final float DEFAULT_ASPECT_RATIO = Float.NaN;
    private static final float DEFAULT_MIN_SIZE_DP = 50;
    private static final int DEFAULT_OVERLAY_COLOR = 0x80000000;
    private static final int DEFAULT_MASK = MASK_RECTANGLE;
    private static final int DEFAULT_MARKER_STROKE_COLOR = 0x7FFFFFFF;
    private static final int DEFAULT_MARKER_STROKE_WIDTH_DP = 1;
    private static final int DEFAULT_MARKER_STROKE_DASH_WIDTH_DP = 0;
    private static final int DEFAULT_MARKER_STROKE_DASH_GAP_DP = 0;
    private static final float DEFAULT_TOUCH_HANDLE_DRAWABLE_ANCHOR_X = 0.5F;
    private static final float DEFAULT_TOUCH_HANDLE_DRAWABLE_ANCHOR_Y = 0.5F;
    private static final int DEFAULT_TOUCH_HANDLE_STROKE_COLOR = DEFAULT_MARKER_STROKE_COLOR;
    private static final int DEFAULT_TOUCH_HANDLE_STROKE_WIDTH_DP = DEFAULT_MARKER_STROKE_WIDTH_DP;
    private static final int DEFAULT_TOUCH_HANDLE_STROKE_LENGTH_DP = 20;
    private static final int DEFAULT_TOUCH_HANDLE_STROKE_INSET_DP = 0;
    private static final int DEFAULT_TOUCH_THRESHOLD_DP = 10;
    private static final int DEFAULT_GRID_LINES_BEHAVIOR = GRID_LINES_BEHAVIOR_TOUCH;
    private static final int DEFAULT_GRID_LINES = 1;
    private static final int DEFAULT_GRID_LINES_COLOR = DEFAULT_MARKER_STROKE_COLOR;
    private static final int DEFAULT_GRID_LINES_WIDTH_DP = 1;
    private static final int DEFAULT_GRID_LINES_DASH_WIDTH_DP = 0;
    private static final int DEFAULT_GRID_LINES_DASH_GAP_DP = 0;

    private float mAspectRatio;
    private int mMinSize;
    private int mOverlayColor;
    private int mMask;
    private int mMarkerStrokeColor;
    private int mMarkerStrokeWidth;
    private int mMarkerStrokeDashWidth;
    private int mMarkerStrokeDashGap;
    private Drawable mTouchHandleDrawable;
    private float mTouchHandleDrawableAnchorX;
    private float mTouchHandleDrawableAnchorY;
    private int mTouchHandleStrokeColor;
    private int mTouchHandleStrokeWidth;
    private int mTouchHandleStrokeLength;
    private int mTouchHandleStrokeInset;
    private int mTouchThreshold;
    private int mGridLinesBehavior;
    private int mGridLines;
    private int mGridLinesColor;
    private int mGridLinesWidth;
    private int mGridLinesDashWidth;
    private int mGridLinesDashGap;

    private final MInt mMarkerLeft = new MInt(0);
    private final MInt mMarkerTop = new MInt(0);
    private final MInt mMarkerRight = new MInt(0);
    private final MInt mMarkerBottom = new MInt(0);

    private final Point mMarkerTopLeft = new Point(mMarkerLeft, mMarkerTop);
    private final Point mMarkerTopRight = new Point(mMarkerRight, mMarkerTop);
    private final Point mMarkerBottomLeft = new Point(mMarkerLeft, mMarkerBottom);
    private final Point mMarkerBottomRight = new Point(mMarkerRight, mMarkerBottom);

    private final MFloat mAspectRatioRef = new MFloat(0F);
    private final MInt mMinSizeRef = new MInt(0);
    private final MInt mTouchThresholdRef = new MInt(0);

    private final PointTouchHandle mMarkerHandleTopLeft = new PointTouchHandle(mMarkerTopLeft, mTouchThresholdRef,
            new Bounds() {
                @Override final int left() { return 0; }
                @Override final int top() { return 0; }
                @Override final int right() { return Math.max(mMarkerRight.value - mMinSizeRef.value, left()); }
                @Override final int bottom() { return Math.max(mMarkerBottom.value - mMinSizeRef.value, top()); }
            },
            mAspectRatioRef, mMarkerBottomRight);

    private final PointTouchHandle mMarkerHandleTopRight = new PointTouchHandle(mMarkerTopRight, mTouchThresholdRef,
            new Bounds() {
                @Override final int left() { return Math.min(mMarkerLeft.value + mMinSizeRef.value, right()); }
                @Override final int top() { return 0; }
                @Override final int right() { return getWidth(); }
                @Override final int bottom() { return Math.max(mMarkerBottom.value - mMinSizeRef.value, top()); }
            },
            mAspectRatioRef, mMarkerBottomLeft);

    private final PointTouchHandle mMarkerHandleBottomLeft = new PointTouchHandle(mMarkerBottomLeft, mTouchThresholdRef,
            new Bounds() {
                @Override final int left() { return 0; }
                @Override final int top() { return Math.min(mMarkerTop.value + mMinSizeRef.value, bottom()); }
                @Override final int right() { return Math.max(mMarkerRight.value - mMinSizeRef.value, left()); }
                @Override final int bottom() { return getHeight(); }
            },
            mAspectRatioRef, mMarkerTopRight);

    private final PointTouchHandle mMarkerHandleBottomRight = new PointTouchHandle(mMarkerBottomRight, mTouchThresholdRef,
            new Bounds() {
                @Override final int left() { return Math.min(mMarkerLeft.value + mMinSizeRef.value, right()); }
                @Override final int top() { return Math.min(mMarkerTop.value + mMinSizeRef.value, bottom()); }
                @Override final int right() { return getWidth(); }
                @Override final int bottom() { return getHeight(); }
            },
            mAspectRatioRef, mMarkerTopLeft);

    private final AreaTouchHandle mMarkerHandleArea = new AreaTouchHandle(mMarkerLeft, mMarkerTop, mMarkerRight, mMarkerBottom,
            new Bounds() {
                @Override final int left() { return 0; }
                @Override final int top() { return 0; }
                @Override final int right() { return left() + (getWidth() - left()) - (mMarkerRight.value - mMarkerLeft.value); }
                @Override final int bottom() { return top() + (getHeight() - top()) - (mMarkerBottom.value - mMarkerTop.value); }
            });

    private final List<TouchHandle> mMarkerHandles = Arrays.asList(
            mMarkerHandleTopLeft,
            mMarkerHandleTopRight,
            mMarkerHandleBottomLeft,
            mMarkerHandleBottomRight,
            mMarkerHandleArea
    );

    private int mActiveTouchPointerId = MotionEvent.INVALID_POINTER_ID;
    private TouchHandle mActiveTouchHandle = null;

    private final Path mMaskPath = new Path();
    private final RectF mCropDrawRect = new RectF();
    private final RectF mTouchHandlesRect = new RectF();
    private final Paint mPaint = new Paint();
    private DashPathEffect mMarkerStrokeDashPathEffect;
    private DashPathEffect mGridLinesDashPathEffect;

    public CropMarker(Context context) {
        this(context, null, 0);
    }

    public CropMarker(Context context, AttributeSet attrs) {
        this (context, attrs, 0);
    }

    public CropMarker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final Resources resources = getResources();
        final DisplayMetrics displayMetrics = resources.getDisplayMetrics();

        this.mAspectRatio = DEFAULT_ASPECT_RATIO;
        this.mMinSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_MIN_SIZE_DP, displayMetrics);
        this.mOverlayColor = DEFAULT_OVERLAY_COLOR;
        this.mMask = DEFAULT_MASK;
        this.mMarkerStrokeColor = DEFAULT_MARKER_STROKE_COLOR;
        this.mMarkerStrokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_MARKER_STROKE_WIDTH_DP, displayMetrics);
        this.mMarkerStrokeDashWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_MARKER_STROKE_DASH_WIDTH_DP, displayMetrics);
        this.mMarkerStrokeDashGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_MARKER_STROKE_DASH_GAP_DP, displayMetrics);
        this.mTouchHandleDrawableAnchorX = DEFAULT_TOUCH_HANDLE_DRAWABLE_ANCHOR_X;
        this.mTouchHandleDrawableAnchorY = DEFAULT_TOUCH_HANDLE_DRAWABLE_ANCHOR_Y;
        this.mTouchHandleStrokeColor = DEFAULT_TOUCH_HANDLE_STROKE_COLOR;
        this.mTouchHandleStrokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TOUCH_HANDLE_STROKE_WIDTH_DP, displayMetrics);
        this.mTouchHandleStrokeLength = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TOUCH_HANDLE_STROKE_LENGTH_DP, displayMetrics);
        this.mTouchHandleStrokeInset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TOUCH_HANDLE_STROKE_INSET_DP, displayMetrics);
        this.mTouchThreshold = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TOUCH_THRESHOLD_DP, displayMetrics);
        this.mGridLinesBehavior = DEFAULT_GRID_LINES_BEHAVIOR;
        this.mGridLines = DEFAULT_GRID_LINES;
        this.mGridLinesColor = DEFAULT_GRID_LINES_COLOR;
        this.mGridLinesWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_GRID_LINES_WIDTH_DP, displayMetrics);
        this.mGridLinesDashWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_GRID_LINES_DASH_WIDTH_DP, displayMetrics);
        this.mGridLinesDashGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_GRID_LINES_DASH_GAP_DP, displayMetrics);

        final TypedArray typedArray = attrs != null ? context.getTheme().obtainStyledAttributes(attrs, R.styleable.CropMarker, 0, 0) : null;

        if (typedArray != null) {
            try {
                setEnabled(typedArray.getBoolean(R.styleable.CropMarker_enabled, DEFAULT_ENABLED));

                this.mMarkerLeft.value = Math.max(0, typedArray.getDimensionPixelSize(R.styleable.CropMarker_left, mMarkerLeft.value));
                this.mMarkerTop.value = Math.max(0, typedArray.getDimensionPixelSize(R.styleable.CropMarker_top, mMarkerTop.value));
                this.mMarkerRight.value = Math.max(0, typedArray.getDimensionPixelSize(R.styleable.CropMarker_right, mMarkerRight.value));
                this.mMarkerBottom.value = Math.max(0, typedArray.getDimensionPixelSize(R.styleable.CropMarker_bottom, mMarkerBottom.value));

                this.mAspectRatio = typedArray.getFloat(R.styleable.CropMarker_aspectRatio, mAspectRatio);
                this.mMinSize = Math.max(0 ,typedArray.getDimensionPixelSize(R.styleable.CropMarker_minSize, mMinSize));
                this.mOverlayColor = typedArray.getColor(R.styleable.CropMarker_overlayColor, mOverlayColor);
                this.mMask = typedArray.getInteger(R.styleable.CropMarker_mask, mMask);
                this.mMarkerStrokeColor = typedArray.getColor(R.styleable.CropMarker_markerStrokeColor, mMarkerStrokeColor);
                this.mMarkerStrokeWidth = Math.max(0, typedArray.getDimensionPixelSize(R.styleable.CropMarker_markerStrokeWidth, mMarkerStrokeWidth));
                this.mMarkerStrokeDashWidth = Math.max(0, typedArray.getDimensionPixelSize(R.styleable.CropMarker_markerStrokeDashWidth, mMarkerStrokeDashWidth));
                this.mMarkerStrokeDashGap = Math.max(0, typedArray.getDimensionPixelSize(R.styleable.CropMarker_markerStrokeDashGap, mMarkerStrokeDashGap));
                setTouchHandleDrawable(typedArray.getDrawable(R.styleable.CropMarker_touchHandleDrawable));
                setTouchHandleDrawableAnchorX(typedArray.getFraction(R.styleable.CropMarker_touchHandleDrawableAnchorX, 1, 1, mTouchHandleDrawableAnchorX));
                setTouchHandleDrawableAnchorY(typedArray.getFraction(R.styleable.CropMarker_touchHandleDrawableAnchorY, 1, 1, mTouchHandleDrawableAnchorY));

                this.mTouchHandleStrokeColor = typedArray.getColor(R.styleable.CropMarker_touchHandleStrokeColor, mTouchHandleStrokeColor);
                this.mTouchHandleStrokeWidth = Math.max(0, typedArray.getDimensionPixelSize(R.styleable.CropMarker_touchHandleStrokeWidth, mTouchHandleStrokeWidth));
                this.mTouchHandleStrokeLength = Math.max(0, typedArray.getDimensionPixelSize(R.styleable.CropMarker_touchHandleStrokeLength, mTouchHandleStrokeLength));
                this.mTouchHandleStrokeInset = typedArray.getDimensionPixelSize(R.styleable.CropMarker_touchHandleStrokeInset, mTouchHandleStrokeInset);
                this.mTouchThreshold = Math.max(0, typedArray.getDimensionPixelSize(R.styleable.CropMarker_touchThreshold, mTouchThreshold));
                this.mGridLinesBehavior = typedArray.getInteger(R.styleable.CropMarker_gridLinesBehavior, mGridLinesBehavior);
                this.mGridLines = Math.max(0, typedArray.getInteger(R.styleable.CropMarker_gridLines, mGridLines));
                this.mGridLinesColor = typedArray.getColor(R.styleable.CropMarker_gridLinesColor, mGridLinesColor);
                this.mGridLinesWidth = Math.max(0, typedArray.getDimensionPixelSize(R.styleable.CropMarker_gridLinesWidth, mGridLinesWidth));
                this.mGridLinesDashWidth = Math.max(0, typedArray.getDimensionPixelSize(R.styleable.CropMarker_gridLinesDashWidth, mGridLinesDashWidth));
                this.mGridLinesDashGap = Math.max(0, typedArray.getDimensionPixelSize(R.styleable.CropMarker_gridLinesDashGap, mGridLinesDashGap));
            } finally {
                typedArray.recycle();
            }
        }

        this.mMarkerLeft.value = Math.max(0, mMarkerLeft.value);
        this.mMarkerTop.value = Math.max(0, mMarkerTop.value);
        this.mMarkerRight.value = Math.max(mMarkerLeft.value + mMinSize, mMarkerRight.value);
        this.mMarkerBottom.value = Math.max(mMarkerTop.value + mMinSize, mMarkerBottom.value);

        this.mAspectRatioRef.value = mAspectRatio;
        this.mMinSizeRef.value = mMinSize;
        this.mTouchThresholdRef.value = mTouchThreshold;

        this.mMarkerStrokeDashPathEffect = (mMarkerStrokeDashWidth > 0 && mMarkerStrokeDashGap > 0) ?
                new DashPathEffect(new float[] { mMarkerStrokeDashWidth, mMarkerStrokeDashGap }, 0F): null;

        this.mGridLinesDashPathEffect = (mGridLinesDashWidth > 0 && mGridLinesDashGap > 0) ?
                new DashPathEffect(new float[] { mGridLinesDashWidth, mGridLinesDashGap }, 0F): null;

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mMarkerHandleBottomRight.grab(mMarkerHandleBottomRight.anchor.x.value, mMarkerHandleBottomRight.anchor.y.value);
                mMarkerHandleBottomRight.move(mMarkerRight.value, mMarkerBottom.value);
                mMarkerHandleBottomRight.release();
            }
        });
    }

    @Override
    public final boolean onTouchEvent(final MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        final int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            if (mActiveTouchPointerId == MotionEvent.INVALID_POINTER_ID) {
                final int pointerIndex = event.getActionIndex();
                final int pointerId = event.getPointerId(pointerIndex);

                final int eventX = Math.round(event.getX(pointerIndex));
                final int eventY = Math.round(event.getY(pointerIndex));

                TouchHandle grabbedTouchHandle = null;
                double grabbedTouchHandleMatch = Double.MAX_VALUE;

                double touchHandleMatch;
                for (TouchHandle touchHandle : mMarkerHandles) {
                    touchHandleMatch = touchHandle.grabMatch(eventX, eventY);
                    if (Double.compare(touchHandleMatch, grabbedTouchHandleMatch) < 0) {
                        grabbedTouchHandle = touchHandle;
                        grabbedTouchHandleMatch = touchHandleMatch;
                    }
                }

                if (grabbedTouchHandle != null) {
                    mActiveTouchPointerId = pointerId;
                    mActiveTouchHandle = grabbedTouchHandle;
                    mActiveTouchHandle.grab(eventX, eventY);
                }
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (mActiveTouchPointerId != MotionEvent.INVALID_POINTER_ID && mActiveTouchHandle != null) {
                final int pointerIndex = event.findPointerIndex(mActiveTouchPointerId);

                mActiveTouchHandle.move(
                        Math.round(event.getX(pointerIndex)),
                        Math.round(event.getY(pointerIndex))
                );

                invalidate();
            }
        } else if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_POINTER_UP ||
                action == MotionEvent.ACTION_OUTSIDE ||
                action == MotionEvent.ACTION_CANCEL) {
            if (mActiveTouchPointerId != MotionEvent.INVALID_POINTER_ID && mActiveTouchHandle != null) {
                if (mActiveTouchPointerId == event.getPointerId(event.getActionIndex())) {
                    mActiveTouchPointerId = MotionEvent.INVALID_POINTER_ID;
                    mActiveTouchHandle.release();
                    mActiveTouchHandle = null;

                    invalidate();
                }
            }
        }

        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isEnabled()) {
            final float strokeWidth_half = 0.5F * mMarkerStrokeWidth;

            mCropDrawRect.set(
                    mMarkerLeft.value + strokeWidth_half,
                    mMarkerTop.value + strokeWidth_half,
                    mMarkerRight.value - strokeWidth_half,
                    mMarkerBottom.value - strokeWidth_half);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mOverlayColor);

            canvas.save();

            mMaskPath.reset();
            if (mMask == MASK_RECTANGLE) {
                mMaskPath.addRect(mCropDrawRect, Path.Direction.CW);
            } else if (mMask == MASK_OVAL) {
                mMaskPath.addOval(mCropDrawRect, Path.Direction.CW);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                canvas.clipOutPath(mMaskPath);
            } else {
                canvas.clipPath(mMaskPath, Region.Op.DIFFERENCE);
            }

            canvas.drawPaint(mPaint);
            canvas.restore();

            if (mGridLines > 0 &&
                    mGridLinesBehavior != GRID_LINES_BEHAVIOR_NONE &&
                    (mGridLinesBehavior != GRID_LINES_BEHAVIOR_TOUCH || mActiveTouchHandle != null)) {
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(mGridLinesColor);
                mPaint.setStrokeWidth(mGridLinesWidth);
                mPaint.setPathEffect(mGridLinesDashPathEffect);

                final float gridCellWidth = mCropDrawRect.width() / (mGridLines + 1F);
                final float gridCellHeight = mCropDrawRect.height() / (mGridLines + 1F);

                float gridX = mCropDrawRect.left;
                float gridY = mCropDrawRect.top;

                canvas.save();
                canvas.clipPath(mMaskPath);

                for (int i = 0; i < mGridLines; ++i) {
                    gridX += gridCellWidth;
                    gridY += gridCellHeight;

                    canvas.drawLine(gridX, mCropDrawRect.top, gridX, mCropDrawRect.bottom, mPaint);
                    canvas.drawLine(mCropDrawRect.left, gridY, mCropDrawRect.right, gridY, mPaint);
                }

                canvas.restore();

                mPaint.setPathEffect(null);
            }

            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(mMarkerStrokeColor);
            mPaint.setStrokeWidth(mMarkerStrokeWidth);
            mPaint.setPathEffect(mMarkerStrokeDashPathEffect);

            canvas.drawRect(mCropDrawRect, mPaint);

            mPaint.setPathEffect(null);

            if (mTouchHandleDrawable != null) {
                for (TouchHandle touchHandle: mMarkerHandles) {
                    if (!(touchHandle instanceof PointTouchHandle)) {
                        continue;
                    }

                    canvas.save();
                    canvas.translate(
                            touchHandle.anchor.x.value - (mTouchHandleDrawable.getBounds().width() * mTouchHandleDrawableAnchorX),
                            touchHandle.anchor.y.value - (mTouchHandleDrawable.getBounds().height() * mTouchHandleDrawableAnchorY)
                    );

                    mTouchHandleDrawable.draw(canvas);

                    canvas.restore();
                }
            } else if (mTouchHandleStrokeWidth > 0 && mTouchHandleStrokeLength > 0 && mTouchHandleStrokeColor != 0) {
                mTouchHandlesRect.set(
                        mMarkerLeft.value + mTouchHandleStrokeInset,
                        mMarkerTop.value + mTouchHandleStrokeInset,
                        mMarkerRight.value - mTouchHandleStrokeInset,
                        mMarkerBottom.value - mTouchHandleStrokeInset
                );

                if (mTouchHandlesRect.left < mTouchHandlesRect.right && mTouchHandlesRect.top < mTouchHandlesRect.bottom) {
                    final float touchHandleStrokeWidth_half = 0.5F * mTouchHandleStrokeWidth;

                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setColor(mTouchHandleStrokeColor);
                    mPaint.setStrokeWidth(mTouchHandleStrokeWidth);

                    canvas.drawLine(mTouchHandlesRect.left, mTouchHandlesRect.top + touchHandleStrokeWidth_half,
                            mTouchHandlesRect.left + mTouchHandleStrokeLength, mTouchHandlesRect.top + touchHandleStrokeWidth_half, mPaint
                    );

                    canvas.drawLine(mTouchHandlesRect.left + touchHandleStrokeWidth_half, mTouchHandlesRect.top,
                            mTouchHandlesRect.left + touchHandleStrokeWidth_half, mTouchHandlesRect.top + mTouchHandleStrokeLength, mPaint
                    );

                    canvas.drawLine(mTouchHandlesRect.right, mTouchHandlesRect.top + touchHandleStrokeWidth_half,
                            mTouchHandlesRect.right - mTouchHandleStrokeLength, mTouchHandlesRect.top + touchHandleStrokeWidth_half, mPaint
                    );

                    canvas.drawLine(mTouchHandlesRect.right - touchHandleStrokeWidth_half, mTouchHandlesRect.top,
                            mTouchHandlesRect.right - touchHandleStrokeWidth_half, mTouchHandlesRect.top + mTouchHandleStrokeLength, mPaint
                    );

                    canvas.drawLine(mTouchHandlesRect.left, mTouchHandlesRect.bottom - touchHandleStrokeWidth_half,
                            mTouchHandlesRect.left + mTouchHandleStrokeLength, mTouchHandlesRect.bottom - touchHandleStrokeWidth_half, mPaint
                    );

                    canvas.drawLine(mTouchHandlesRect.left + touchHandleStrokeWidth_half, mTouchHandlesRect.bottom,
                            mTouchHandlesRect.left + touchHandleStrokeWidth_half, mTouchHandlesRect.bottom - mTouchHandleStrokeLength, mPaint
                    );

                    canvas.drawLine(mTouchHandlesRect.right, mTouchHandlesRect.bottom - touchHandleStrokeWidth_half,
                            mTouchHandlesRect.right - mTouchHandleStrokeLength, mTouchHandlesRect.bottom - touchHandleStrokeWidth_half, mPaint
                    );

                    canvas.drawLine(mTouchHandlesRect.right - touchHandleStrokeWidth_half, mTouchHandlesRect.bottom - mTouchHandleStrokeLength,
                            mTouchHandlesRect.right - touchHandleStrokeWidth_half, mTouchHandlesRect.bottom, mPaint
                    );
                }
            }
        }
    }

    public int getMarkerLeft() {
        return mMarkerLeft.value;
    }

    public void setMarkerLeft(final int left) {
        setMarker(
                left,
                mMarkerHandleTopLeft.anchor.y.value,
                mMarkerHandleBottomRight.anchor.x.value,
                mMarkerHandleBottomRight.anchor.y.value
        );
    }

    public int getMarkerTop() {
        return mMarkerTop.value;
    }

    public void setMarkerTop(final int top) {
        setMarker(
                mMarkerHandleTopLeft.anchor.x.value,
                top,
                mMarkerHandleBottomRight.anchor.x.value,
                mMarkerHandleBottomRight.anchor.y.value
        );
    }

    public int getMarkerRight() {
        return mMarkerRight.value;
    }

    public void setMarkerRight(final int right) {
        setMarker(
                mMarkerHandleTopLeft.anchor.x.value,
                mMarkerHandleTopLeft.anchor.y.value,
                right,
                mMarkerHandleBottomRight.anchor.y.value
        );
    }

    public int getMarkerBottom() {
        return mMarkerBottom.value;
    }

    public void setMarkerBottom(final int bottom) {
        setMarker(
                mMarkerHandleTopLeft.anchor.x.value,
                mMarkerHandleTopLeft.anchor.y.value,
                mMarkerHandleBottomRight.anchor.x.value,
                bottom
        );
    }

    public void setMarker(final int left, final int top, final int right, final int bottom) {
        mMarkerHandleTopLeft.grab(mMarkerHandleTopLeft.anchor.x.value, mMarkerHandleTopLeft.anchor.y.value);
        mMarkerHandleTopLeft.move(left, top);
        mMarkerHandleTopLeft.release();

        mMarkerHandleBottomRight.grab(mMarkerHandleBottomRight.anchor.x.value, mMarkerHandleBottomRight.anchor.y.value);
        mMarkerHandleBottomRight.move(right, bottom);
        mMarkerHandleBottomRight.release();

        invalidate();
    }

    public float getAspectRatio() {
        return mAspectRatio;
    }

    public void setAspectRatio(final float cropMarkerAspectRatio) {
        mAspectRatio = Float.isNaN(cropMarkerAspectRatio) || Float.compare(cropMarkerAspectRatio, 0F) < 0 ? 0 : cropMarkerAspectRatio;
        mAspectRatioRef.value = mAspectRatio;

        reset();
    }

    public int getMinSize() {
        return mMinSize;
    }

    public void setMinSize(final int minSize) {
        mMinSize = Math.max(0, minSize);
        mMinSizeRef.value = mMinSize;

        reset();
    }

    public int getOverlayColor() {
        return mOverlayColor;
    }

    public void setOverlayColor(final int overlayColor) {
        mOverlayColor = overlayColor;
        invalidate();
    }

    public int getMarkerStrokeColor() {
        return mMarkerStrokeColor;
    }

    public void setMarkerStrokeColor(final int markerStrokeColor) {
        mMarkerStrokeColor = markerStrokeColor;
        invalidate();
    }

    public int getMarkerStrokeWidth() {
        return mMarkerStrokeWidth;
    }

    public void setMarkerStrokeWidth(final int markerStrokeWidth) {
        mMarkerStrokeWidth = Math.max(0, markerStrokeWidth);
        invalidate();
    }

    public int getMarkerStrokeDashWidth() {
        return mMarkerStrokeDashWidth;
    }

    public int getMarkerStrokeDashGap() {
        return mMarkerStrokeDashGap;
    }

    public void setMarkerStrokeDash(final int width, final int gap) {
        this.mMarkerStrokeDashWidth = Math.max(0, width);
        this.mMarkerStrokeDashGap = Math.max(0, gap);

        this.mMarkerStrokeDashPathEffect = (mMarkerStrokeDashWidth > 0 && mMarkerStrokeDashGap > 0) ?
                new DashPathEffect(new float[] { mMarkerStrokeDashWidth, mMarkerStrokeDashGap }, 0F): null;

        invalidate();
    }

    public Drawable getTouchHandleDrawable() {
        return mTouchHandleDrawable;
    }

    public void setTouchHandleDrawable(final Drawable drawable) {
        this.mTouchHandleDrawable = drawable;

        if (mTouchHandleDrawable != null) {
            mTouchHandleDrawable.setBounds(0, 0, mTouchHandleDrawable.getIntrinsicWidth(), mTouchHandleDrawable.getIntrinsicHeight());
        }

        invalidate();
    }

    public float getTouchHandleDrawableAnchorX() {
        return mTouchHandleDrawableAnchorX;
    }

    public void setTouchHandleDrawableAnchorX(final float touchHandleDrawableAnchorX) {
        this.mTouchHandleDrawableAnchorX = (Float.isFinite(touchHandleDrawableAnchorX)) ?
                Math.min(1F, Math.max(0F, touchHandleDrawableAnchorX)) :
                DEFAULT_TOUCH_HANDLE_DRAWABLE_ANCHOR_X;
        invalidate();
    }

    public float getTouchHandleDrawableAnchorY() {
        return mTouchHandleDrawableAnchorY;
    }

    public void setTouchHandleDrawableAnchorY(float touchHandleDrawableAnchorY) {
        this.mTouchHandleDrawableAnchorY = (Float.isFinite(touchHandleDrawableAnchorY)) ?
                Math.min(1F, Math.max(0F, touchHandleDrawableAnchorY)) :
                DEFAULT_TOUCH_HANDLE_DRAWABLE_ANCHOR_Y;
        invalidate();
    }

    public int getTouchHandleStrokeColor() {
        return mTouchHandleStrokeColor;
    }

    public void setTouchHandleStrokeColor(final int touchHandleStrokeColor) {
        mTouchHandleStrokeColor = touchHandleStrokeColor;
        invalidate();
    }

    public int getTouchHandleStrokeWidth() {
        return mTouchHandleStrokeWidth;
    }

    public void setTouchHandleStrokeWidth(final int touchHandleStrokeWidth) {
        mTouchHandleStrokeWidth = Math.max(0, touchHandleStrokeWidth);
        invalidate();
    }

    public int getTouchHandleStrokeLength() {
        return mTouchHandleStrokeLength;
    }

    public void setTouchHandleStrokeLength(final int touchHandleStrokeLength) {
        mTouchHandleStrokeLength = Math.max(0, touchHandleStrokeLength);
        invalidate();
    }

    public int getTouchHandleStrokeInset() {
        return mTouchHandleStrokeInset;
    }

    public void setTouchHandleStrokeInset(final int inset) {
        mTouchHandleStrokeInset = inset;
        invalidate();
    }

    public int getTouchThreshold() {
        return mTouchThreshold;
    }

    public void setTouchThreshold(final int touchThreshold) {
        mTouchThreshold = Math.max(0, touchThreshold);
    }

    public int getGridLinesBehavior() {
        return mGridLinesBehavior;
    }

    public void setGridLinesBehavior(final int gridLinesBehavior) {
        mGridLinesBehavior = gridLinesBehavior;
        invalidate();
    }

    public int getGridLines() {
        return mGridLines;
    }

    public void setGridLines(final int gridLines) {
        mGridLines = Math.max(0, gridLines);
        invalidate();
    }

    public int getGridLinesColor() {
        return mGridLinesColor;
    }

    public void setGridLinesColor(final int gridLinesColor) {
        mGridLinesColor = gridLinesColor;
        invalidate();
    }

    public int getGridLinesWidth() {
        return mGridLinesWidth;
    }

    public void setGridLinesWidth(final int gridLinesWidth) {
        mGridLinesWidth = Math.max(0, gridLinesWidth);
        invalidate();
    }

    public int getGridLinesDashWidth() {
        return mGridLinesDashWidth;
    }

    public int getGridLinesDashGap() {
        return mGridLinesDashGap;
    }

    public void setGridLinesDash(final int width, final int gap) {
        this.mGridLinesDashWidth = Math.max(0, width);
        this.mGridLinesDashGap = Math.max(0, gap);

        this.mGridLinesDashPathEffect = (mGridLinesDashWidth > 0 && mGridLinesDashGap > 0) ?
                new DashPathEffect(new float[] { mGridLinesDashWidth, mGridLinesDashGap }, 0F): null;

        invalidate();
    }

    public int getMask() {
        return mMask;
    }

    public void setMask(final int mask) {
        mMask = mask;
        invalidate();
    }

    public final RectF getCropBounds() {
        RectF cropBounds = null;

        if (isEnabled()) {
            final float width = getWidth();
            final float height = getHeight();

            if (Float.compare(width, 0F) > 0 && Float.compare(height, 0F) > 0) {
                cropBounds = new RectF(
                        mMarkerLeft.value / width,
                        mMarkerTop.value / height,
                        mMarkerRight.value / width,
                        mMarkerBottom.value / height
                );
            }
        }

        return cropBounds;
    }

    private final void reset() {
        mMarkerHandleTopLeft.grab(mMarkerHandleTopLeft.anchor.x.value, mMarkerHandleTopLeft.anchor.y.value);
        mMarkerHandleTopLeft.move(0, 0);
        mMarkerHandleTopLeft.release();

        mMarkerHandleBottomRight.grab(mMarkerHandleBottomRight.anchor.x.value, mMarkerHandleBottomRight.anchor.y.value);
        mMarkerHandleBottomRight.move(getWidth(), getHeight());
        mMarkerHandleBottomRight.release();

        invalidate();
    }

    private static final class MInt {

        int value;

        MInt() { this.value = Integer.MIN_VALUE; }
        MInt(int value) { this.value = value; }

    }

    private static final class MFloat {

        float value;

        MFloat() { this.value = Float.NaN; }
        MFloat(final float value) { this.value = value; }

    }

    private static final class Point {

        final MInt x;
        final MInt y;

        Point() {
            this.x = new MInt();
            this.y = new MInt();
        }

        Point(final MInt x, final MInt y) {
            this.x = x;
            this.y = y;
        }

        void set(final int x, final int y) {
            this.x.value = x;
            this.y.value = y;
        }

    }

    private static abstract class Bounds {

        abstract int left();
        abstract int top();
        abstract int right();
        abstract int bottom();

        final boolean contains(final int x, final int y) {
            return left() <= x && x <= right() && top() <= y && y <= bottom();
        }

        final boolean contains(final float x, final float y) {
            return Float.compare(left(), x) <= 0 &&
                    Float.compare(x, right()) <= 0 &&
                    Float.compare(top(), y) <= 0 &&
                    Float.compare(y, bottom()) <= 0;
        }

    }

    private static final class RectBounds extends Bounds {

        private final MInt left;
        private final MInt top;
        private final MInt right;
        private final MInt bottom;

        RectBounds() {
            this.left = new MInt();
            this.top = new MInt();
            this.right = new MInt();
            this.bottom = new MInt();
        }

        RectBounds(final MInt left, final MInt top, final MInt right, final MInt bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        @Override public final int left() { return left.value; }
        @Override public final int top() { return top.value; }
        @Override public final int right() { return right.value; }
        @Override public final int bottom() { return bottom.value; }

        final void set(final int left, final int top, final int right, final int bottom) {
            this.left.value = left;
            this.top.value = top;
            this.right.value = right;
            this.bottom.value = bottom;
        }

        final void add(final int x, final int y) {
            this.left.value = Math.min(this.left.value, x);
            this.top.value = Math.min(this.top.value, y);
            this.right.value = Math.max(this.right.value, x);
            this.bottom.value = Math.max(this.bottom.value, y);
        }

    }

    private static final class AnchorBounds extends Bounds {

        private final Point anchor;
        private final MInt threshold;

        AnchorBounds(final Point anchor, final MInt threshold) {
            this.anchor = anchor;
            this.threshold = threshold;
        }

        @Override public final int left() { return anchor.x.value - threshold.value; }
        @Override public final int top() { return anchor.y.value - threshold.value; }
        @Override public final int right() { return anchor.x.value + threshold.value; }
        @Override public final int bottom() { return anchor.y.value + threshold.value; }

    }

    private static abstract class TouchHandle {

        final Point anchor;
        final Bounds touchArea;
        final Bounds dragBounds;
        final Point touchPoint;

        TouchHandle(final Point anchor, final Bounds touchArea, final Bounds dragBounds) {
            this.anchor = anchor;
            this.touchArea = touchArea;
            this.dragBounds = dragBounds;
            this.touchPoint = new Point(new MInt(0), new MInt(0));
        }

        double grabMatch(final int x, final int y) {
            if (touchArea.contains(x, y)) {
                return Math.sqrt(Math.pow(anchor.x.value - x, 2) + Math.pow(anchor.y.value - y, 2));
            }

            return Double.NaN;
        }

        boolean grab(final int x, final int y) {
            if (touchArea.contains(x, y)) {
                touchPoint.x.value = x - anchor.x.value;
                touchPoint.y.value = y - anchor.y.value;
                return true;
            }

            return false;
        }

        void move(final int x, final int y) {
            moveHandle(
                    Math.min(Math.max(dragBounds.left(), x - touchPoint.x.value), dragBounds.right()),
                    Math.min(Math.max(dragBounds.top(), y - touchPoint.y.value), dragBounds.bottom()));
        }

        abstract void moveHandle(int x, int y);

        void release() {
            touchPoint.x.value = 0;
            touchPoint.y.value = 0;
        }

    }

    private static final class PointTouchHandle extends TouchHandle {

        private final MFloat mAspectRatio;
        private final Point mAspectRatioAnchor;

        private final RectBounds mAspectRatioBounds;

        private float mDragLineSlope;
        private float mDragLineYIntercept;

        PointTouchHandle(final Point point, final MInt touchThreshold, final Bounds dragBounds, final MFloat aspectRatio, final Point aspectRatioAnchor) {
            super(point, new AnchorBounds(point, touchThreshold), dragBounds);

            this.mAspectRatio = aspectRatio;
            this.mAspectRatioAnchor = aspectRatioAnchor;
            this.mAspectRatioBounds = mAspectRatio != null ? new RectBounds() : null;

            this.mDragLineSlope = Float.NaN;
            this.mDragLineYIntercept = Float.NaN;
        }

        @Override
        final boolean grab(int x, int y) {
            final boolean grabbed = super.grab(x, y);

            mDragLineSlope = Float.NaN;
            mDragLineYIntercept = Float.NaN;

            if (grabbed && mAspectRatio != null && !Float.isNaN(mAspectRatio.value) && Float.compare(mAspectRatio.value, 0F) > 0) {
                // In order to keep an aspect ratio, we need to compute a drag line along which we
                // are allowed to move this touch handle. This line needs to start from the aspect
                // ratio anchor point and have the appropriate slope. In order to compute the slope,
                // we consider an imaginary point on that line. The X coordinate of this imaginary
                // line point is distanced from the aspect ratio anchor point by a POSITIVE factor,
                // in the direction of this handle's position. Using this X coordinate, we can
                // compute a matching Y value which respects the aspect ratio in relation to the
                // aspect ration anchor point. There will be 2 matching Y values, but we will consider
                // the one which is in the direction of the handle. Once we have the X,Y point, we
                // can compute the drag line slope & Y intercept (line equation). We then need to
                // define the limits of the drag line and we do this by intersecting it with the
                // drag bounds rectangle. The rectangle enclosing these intersection points represents
                // the bounds of this handle where the aspect ratio can be respected.

                // Compute the direction of this handle in relation to the aspect ratio anchor
                final int handleXDirection = anchor.x.value - mAspectRatioAnchor.x.value < 0 ? -1 : 1;
                final int handleYDirection = anchor.y.value - mAspectRatioAnchor.y.value < 0 ? -1 : 1;

                // Define a POSITIVE X axis distance from the aspect ratio anchor point
                final float factor = 100;

                // Compute the X value of the imaginary point heading towards this handle
                final PointF linePoint = new PointF();
                linePoint.x = mAspectRatioAnchor.x.value + (handleXDirection * factor);

                // Compute the two possible values for the imaginary point Y value
                final float pY1 = mAspectRatioAnchor.y.value - factor / mAspectRatio.value;
                final float pY2 = mAspectRatioAnchor.y.value + factor / mAspectRatio.value;

                // Choose the Y value which respects the direction of the handle
                if (handleYDirection == (pY1 - mAspectRatioAnchor.y.value < 0 ? -1 : 1)) {
                    linePoint.y = Math.round(pY1);
                } else if (handleYDirection == (pY2 - mAspectRatioAnchor.y.value < 0 ? -1 : 1)) {
                    linePoint.y = Math.round(pY2);
                }

                // Compute the drag line rise & run (needed to compute the slope)

                /* line_rise = y2 - y1 */
                /* line_run = x2 - x1 */
                /* line_slope = line_rise / lineRun */
                final float lineRise = mAspectRatioAnchor.y.value - linePoint.y;
                final float lineRun = mAspectRatioAnchor.x.value - linePoint.x;

                // Check if the drag line is vertical (which is not supported)
                if (Float.compare(lineRun, 0F) != 0) {
                    // Compute the slope & the Y intercept of the drag line equation

                    /* y = line_slope * x + yIntercept */

                    mDragLineSlope = lineRise / lineRun;
                    mDragLineYIntercept = linePoint.y - (mDragLineSlope * linePoint.x);

                    final int dragBoundsLeft = dragBounds.left();
                    final int dragBoundsTop = dragBounds.top();
                    final int dragBoundsRight = dragBounds.right();
                    final int dragBoundsBottom = dragBounds.bottom();

                    // Compute the intersection points of the drag line and the free drag bounds
                    final PointF[] intersectionPoints = new PointF[4];
                    intersectionPoints[0] = new PointF(dragBoundsLeft, mDragLineSlope * dragBoundsLeft + mDragLineYIntercept);
                    intersectionPoints[1] = new PointF((dragBoundsTop - mDragLineYIntercept) / mDragLineSlope, dragBoundsTop);
                    intersectionPoints[2] = new PointF(dragBoundsRight, mDragLineSlope * dragBoundsRight + mDragLineYIntercept);
                    intersectionPoints[3] = new PointF((dragBoundsBottom - mDragLineYIntercept) / mDragLineSlope, dragBoundsBottom);

                    mAspectRatioBounds.set(anchor.x.value, anchor.y.value, anchor.x.value, anchor.y.value);

                    // Define the more restricted bounds where the aspect ratio is respected
                    for (PointF intersectionPoint : intersectionPoints) {
                        if (dragBounds.contains(intersectionPoint.x, intersectionPoint.y)) {
                            mAspectRatioBounds.add(Math.round(intersectionPoint.x), Math.round(intersectionPoint.y));
                        }
                    }
                }
            }

            return grabbed;
        }

        @Override
        final void moveHandle(final int x, final int y) {
            float transformedX = x;
            float transformedY = y;

            if (!Float.isNaN(mDragLineSlope)) {
                // In order to compute a point on the drag line corresponding to the touch point,
                // we compute a perpendicular projection line from the touch point to the drag line.

                // perpendicular_slope = -1 / line_slope
                final float perpendicularLineSlope = -1 / mDragLineSlope;
                final float perpendicularLineYIntercept = transformedY - (perpendicularLineSlope * transformedX);

                // Compute the projected point
                transformedX = (perpendicularLineYIntercept - mDragLineYIntercept) / (mDragLineSlope - perpendicularLineSlope);
                transformedY = mDragLineSlope * transformedX + mDragLineYIntercept;

                // Limit the projection point to the scaling bounds
                transformedX = Math.min(Math.max(mAspectRatioBounds.left(), transformedX), mAspectRatioBounds.right());
                transformedY = Math.min(Math.max(mAspectRatioBounds.top(), transformedY), mAspectRatioBounds.bottom());
            }

            anchor.x.value = Math.round(transformedX);
            anchor.y.value = Math.round(transformedY);
        }

        @Override
        final void release() {
            super.release();

            mDragLineSlope = Float.NaN;
            mDragLineYIntercept = Float.NaN;
        }
    }

    private static final class AreaTouchHandle extends TouchHandle {

        final MInt left;
        final MInt top;
        final MInt right;
        final MInt bottom;

        AreaTouchHandle(final MInt left, final MInt top, final MInt right, final MInt bottom, final Bounds dragBounds) {
            super(new Point(left, top), new RectBounds(left, top, right, bottom), dragBounds);

            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        @Override
        final double grabMatch(final int x, final int y) {
            if (touchArea.contains(x, y)) {
                return Math.sqrt(Math.pow(((left.value + right.value) * 0.5F) - x, 2) + Math.pow(((top.value + bottom.value) * 0.5F) - y, 2));
            }

            return Double.NaN;
        }

        @Override
        final void moveHandle(final int x, final int y) {
            final int deltaX = x - left.value;
            final int deltaY = y - top.value;

            left.value = x;
            top.value = y;
            right.value += deltaX;
            bottom.value += deltaY;
        }

    }

}
