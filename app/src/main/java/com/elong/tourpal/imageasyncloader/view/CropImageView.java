package com.elong.tourpal.imageasyncloader.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;

import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.TourPalApplication;

/**
 * Created by zhitao.xu on 2015/4/27.
 */
public class CropImageView extends CustomImageView implements ScaleGestureDetector.OnScaleGestureListener,
        View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener {
    private static final boolean DEBUG = Env.DEBUG;
    private static final String TAG = CropImageView.class.getSimpleName();
    private static final float STEP = 2.0f;
    public float SCALE_MAX = 4.0f;
    private float SCALE_MID = 2.0f;
    private float SCALE_MINIMUM = 1.0f;

    /**
     * 初始化时的缩放比例，如果图片宽或高大于屏幕，此值将小于0
     */
    private float initScale = 1.0f;
    private boolean once = true;

    /**
     * 用于存放矩阵的9个值
     */
    private final float[] matrixValues = new float[9];

    /**
     * 缩放的手势检测
     */
    private ScaleGestureDetector mScaleGestureDetector = null;
    private Matrix mScaleMatrix = new Matrix();

    /**
     * 用于双击检测
     */
    private GestureDetector mGestureDetector;
    private boolean isAutoScale;

    private int mTouchSlop;

    private float mLastX;
    private float mLastY;

    private boolean isCanDrag;
    private int lastPointerCount;

    /**
     * 剪切头像相关
     */
    private boolean isCheckTopAndBottom = true;//移动缩放时是否达到上下边界
    private boolean isCheckLeftAndRight = true;//移动缩放时是否达到左右边界
    protected FloatDrawable mFloatDrawable;// 浮层框图片对象
    private int mCropWidth; //浮层框宽度，px
    private int mCropHeight;// 浮层框高度，px
    protected Rect mDrawableFloat = new Rect();// 浮层的Rect

    public CropImageView(Context context) {
        super(context);
        mFloatDrawable = new FloatDrawable(context);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setScaleType(ScaleType.MATRIX);
        mGestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        if (isAutoScale == true)
                            return true;

                        float x = e.getX();
                        float y = e.getY();
                        if (DEBUG) {
                            Log.e(TAG, "DoubleTap" + getScale() + " , " + initScale);
                        }
                        if (getScale() < SCALE_MID) {
                            CropImageView.this.postDelayed(
                                    new AutoScaleRunnable(SCALE_MID, x, y), 16);
                            isAutoScale = true;
                        } else if (getScale() >= SCALE_MID
                                && getScale() < SCALE_MAX) {
                            CropImageView.this.postDelayed(
                                    new AutoScaleRunnable(SCALE_MAX, x, y), 16);
                            isAutoScale = true;
                        } else {
                            CropImageView.this.postDelayed(
                                    new AutoScaleRunnable(initScale, x, y), 16);
                            isAutoScale = true;
                        }

                        return true;
                    }
                });
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        this.setOnTouchListener(this);
        mFloatDrawable = new FloatDrawable(context);
    }

    /**
     * 自动缩放的任务
     */
    private class AutoScaleRunnable implements Runnable {
        static final float BIGGER = 1.07f;
        static final float SMALLER = 0.93f;
        private float mTargetScale;
        private float tmpScale;

        /**
         * 缩放的中心
         */
        private float x;
        private float y;

        /**
         * 传入目标缩放值，根据目标值与当前值，判断应该放大还是缩小
         *
         * @param targetScale
         */
        public AutoScaleRunnable(float targetScale, float x, float y) {
            this.mTargetScale = targetScale;
            this.x = x;
            this.y = y;
            if (getScale() < mTargetScale) {
                tmpScale = BIGGER;
            } else {
                tmpScale = SMALLER;
            }

        }

        @Override
        public void run() {
            // 进行缩放
            mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
            setImageMatrix(mScaleMatrix);

            final float currentScale = getScale();
            // 如果值在合法范围内，继续缩放
            if (((tmpScale > 1f) && (currentScale < mTargetScale))
                    || ((tmpScale < 1f) && (mTargetScale < currentScale))) {
                CropImageView.this.postDelayed(this, 16);
            } else
            // 设置为目标的缩放比例
            {
                final float deltaScale = mTargetScale / currentScale;
                mScaleMatrix.postScale(deltaScale, deltaScale, x, y);
                setImageMatrix(mScaleMatrix);
                isAutoScale = false;
            }

        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scale = getScale();
        float scaleFactor = detector.getScaleFactor();

        if (getDrawable() == null)
            return true;

        /**
         * 缩放的范围控制
         */
        if ((scale < SCALE_MAX && scaleFactor > 1.0f)
                || (scale > SCALE_MINIMUM && scaleFactor < 1.0f)) {
            /**
             * 最大值最小值判断
             */
            if (scaleFactor * scale < SCALE_MINIMUM) {
                scaleFactor = SCALE_MINIMUM / scale;
            }
            if (scaleFactor * scale > SCALE_MAX) {
                scaleFactor = SCALE_MAX / scale;
            }
            /**
             * 设置缩放比例
             */
            mScaleMatrix.postScale(scaleFactor, scaleFactor,
                    detector.getFocusX(), detector.getFocusY());
            setImageMatrix(mScaleMatrix);
        }
        return true;
    }

//    /**
//     * 在缩放时，进行图片显示范围的控制，显示必须盖住剪切框
//     */
//    private void checkBorderAndCenterWhenScale() {
//
//        RectF rect = getMatrixRectF();
//        float deltaX = 0;
//        float deltaY = 0;
//
//        int width = getWidth();
//        int height = getHeight();
//
//        // 如果宽或高大于屏幕，则控制范围
//        if (rect.width() >= width) {
//            if (rect.left > 0) {
//                deltaX = -rect.left;
//            }
//            if (rect.right < width) {
//                deltaX = width - rect.right;
//            }
//        }
//        if (rect.height() >= height) {
//            if (rect.top > 0) {
//                deltaY = -rect.top;
//            }
//            if (rect.bottom < height) {
//                deltaY = height - rect.bottom;
//            }
//        }
//        // 如果宽或高小于屏幕，则让其居中
//        if (rect.width() < width) {
//            deltaX = width * 0.5f - rect.right + 0.5f * rect.width();
//        }
//        if (rect.height() < height) {
//            deltaY = height * 0.5f - rect.bottom + 0.5f * rect.height();
//        }
//
//        // 添加图片缩放时对于剪切框的位置
//        if (rect.width() < )
//        if (DEBUG) {
//            Log.e(TAG, "deltaX = " + deltaX + " , deltaY = " + deltaY);
//        }
//
//        mScaleMatrix.postTranslate(deltaX, deltaY);
//    }

    /**
     * 根据当前图片的Matrix获得图片的范围
     *
     * @return
     */
    private RectF getMatrixRectF() {
        Matrix matrix = mScaleMatrix;
        RectF rect = new RectF();
        Drawable d = getDrawable();
        if (null != d) {
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rect);
        }
        return rect;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (mGestureDetector.onTouchEvent(event))
            return true;
        mScaleGestureDetector.onTouchEvent(event);

        float x = 0, y = 0;
        // 拿到触摸点的个数
        final int pointerCount = event.getPointerCount();
        // 得到多个触摸点的x与y均值
        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }
        x = x / pointerCount;
        y = y / pointerCount;

        /**
         * 每当触摸点发生变化时，重置mLasX , mLastY
         */
        if (pointerCount != lastPointerCount) {
            isCanDrag = false;
            mLastX = x;
            mLastY = y;
        }

        lastPointerCount = pointerCount;
        RectF rectF = getMatrixRectF();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (rectF.width() > getWidth() || rectF.height() > getHeight()) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (rectF.width() > getWidth() || rectF.height() > getHeight()) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (DEBUG) {
                    Log.e(TAG, "ACTION_MOVE");
                }
                float dx = x - mLastX;
                float dy = y - mLastY;

                if (!isCanDrag) {
                    isCanDrag = isCanDrag(dx, dy);
                }
                if (isCanDrag) {

                    if (getDrawable() != null) {
//                        if (getMatrixRectF().left >= (getWidth() - mCropWidth ) / 2 && dx > 0)
//                        {
//                            getParent().requestDisallowInterceptTouchEvent(false);
//                        }
//
//                        if (getMatrixRectF().right <= (getWidth() + mCropWidth) / 2 && dx < 0)
//                        {
//                            getParent().requestDisallowInterceptTouchEvent(false);
//                        }
//
//                        if (getMatrixRectF().top >= (getHeight() - mCropHeight) / 2 && dy > 0)
//                        {
//                            getParent().requestDisallowInterceptTouchEvent(false);
//                        }
//
//                        if (getMatrixRectF().bottom <= (getHeight() + mCropHeight) / 2 && dx < 0)
//                        {
//                            getParent().requestDisallowInterceptTouchEvent(false);
//                        }
                        isCheckLeftAndRight = isCheckTopAndBottom = true;
                        // 如果宽度小于剪切框宽度，则禁止左右移动
                        if (rectF.width() < mCropWidth) {
                            dx = 0;
                            isCheckLeftAndRight = false;
                        }
                        // 如果高度小于剪切框高度，则禁止上下移动
                        if (rectF.height() < mCropHeight) {
                            dy = 0;
                            isCheckTopAndBottom = false;
                        }


                        mScaleMatrix.postTranslate(dx, dy);
                        checkMatrixBounds();
                        setImageMatrix(mScaleMatrix);
                    }
                }
                mLastX = x;
                mLastY = y;
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (DEBUG) {
                    Log.e(TAG, "ACTION_UP");
                }
                lastPointerCount = 0;
                break;
        }

        return true;
    }

    /**
     * 获得当前的缩放比例
     *
     * @return
     */
    public final float getScale() {
        mScaleMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeGlobalOnLayoutListener(this);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        once = true;
        mScaleMatrix.reset();
        super.setImageDrawable(drawable);
    }

    @Override
    public void onGlobalLayout() {
        if (once) {
            Drawable d = getDrawable();
            if (d == null)
                return;
            if (DEBUG) {
                Log.e(TAG, d.getIntrinsicWidth() + " , " + d.getIntrinsicHeight());
            }
            int width = getWidth();
            int height = getHeight();
            // 拿到图片的宽和高
            int dw = d.getIntrinsicWidth();
            int dh = d.getIntrinsicHeight();

            if (DEBUG) {
                Log.d(TAG, String.format("photo width=%d, height=%d, screen width=%d, height=%d", dw, dh, width, height));
            }
            float scale = 1.0f;
            // 如果图片的宽或者高大于屏幕，则缩放至屏幕的宽或者高
            if (dw > width && dh <= height) {
                scale = width * 1.0f / dw;
            }
            if (dh > height && dw <= width) {
                scale = height * 1.0f / dh;
            }
            // 如果宽和高都大于屏幕，则让其按按比例适应屏幕大小
            if (dw > width && dh > height) {
                scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
            }

            if (dw < width && dh < height) {
                scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
            }

            if (dw * scale < mCropWidth) {
                scale = mCropWidth * 1.0f / dw;
            }

            if (dh * scale < mCropHeight) {
                scale = mCropHeight * 1.0f / dh;
            }
            initScale = scale;
            SCALE_MAX = STEP * STEP * initScale;
            SCALE_MID = STEP * initScale;
            SCALE_MINIMUM = (float)(Math.max((double)mCropHeight / dh, (double)mCropWidth / dw));

            if (DEBUG) {
                Log.e(TAG, "initScale = " + initScale);
            }
            mScaleMatrix.postTranslate((width - dw) / 2, (height - dh) / 2);
            mScaleMatrix.postScale(scale, scale, getWidth() / 2,
                    getHeight() / 2);
            // 图片移动至屏幕中心
            setImageMatrix(mScaleMatrix);
            once = false;
        }

    }

    /**
     * 移动时，进行边界判断，主要判断宽或高大于剪切框的
     */
    private void checkMatrixBounds() {
        RectF rect = getMatrixRectF();

        float deltaX = 0, deltaY = 0;
        final float viewWidth = getWidth();
        final float viewHeight = getHeight();
        // 判断移动或缩放后，图片显示是否超出屏幕边界
        if (rect.top > topBound() && isCheckTopAndBottom) {
            deltaY = topBound() - rect.top;
        }
        if (rect.bottom < bottomBound() && isCheckTopAndBottom) {
            deltaY = bottomBound() - rect.bottom;
        }
        if (rect.left > leftBound() && isCheckLeftAndRight) {
            deltaX = leftBound() - rect.left;
        }
        if (rect.right < rightBound() && isCheckLeftAndRight) {
            deltaX = rightBound() - rect.right;
        }
        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    public int topBound() {
        return (getHeight() - mCropHeight) / 2;
    }

    private int bottomBound() {
        return (getHeight() + mCropHeight) / 2;
    }

    public int leftBound() {
        return (getWidth() - mCropWidth) / 2;
    }

    private int rightBound() {
        return (getWidth() + mCropWidth) / 2;
    }

    /**
     * 是否是推动行为
     *
     * @param dx
     * @param dy
     * @return
     */
    private boolean isCanDrag(float dx, float dy) {
        return Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        configureFloatRect(mCropWidth, mCropHeight);

        // 在画布上画浮层FloatDrawable,Region.Op.DIFFERENCE是表示Rect交集的补集
        canvas.clipRect(mDrawableFloat, Region.Op.DIFFERENCE);
        // 在交集的补集上画上灰色用来区分
        canvas.drawColor(Color.parseColor("#a0000000"));
        canvas.restore();
        // 画浮层
        mFloatDrawable.draw(canvas);
    }

    public void setDrawable(String mDrawable, int cropWidth, int cropHeight) {
        this.mCropWidth = dipTopx(TourPalApplication.getAppContext(), cropWidth);
        this.mCropHeight = dipTopx(TourPalApplication.getAppContext(), cropHeight);
        if (com.elong.tourpal.utils.Utils.isUrl(mDrawable)) {
            setImageUrl(mDrawable);
        } else {
            setImagePath(mDrawable);
        }
        loadImage();
    }

    private void configureFloatRect(int cropWidth, int cropHeight) {
        int floatWidth = cropWidth;
        int floatHeight = cropHeight;
        int floatLeft = (getWidth() - floatWidth) / 2;
        int floatTop = (getHeight() - floatHeight) / 2;
        mDrawableFloat.set(floatLeft, floatTop, floatLeft + floatWidth,
                floatTop + floatHeight);
        mFloatDrawable.setBounds(mDrawableFloat);
    }

    public int dipTopx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public int getCropWidth() {
        return mCropWidth;
    }

    public int getCropHeight() {
        return mCropHeight;
    }

}
