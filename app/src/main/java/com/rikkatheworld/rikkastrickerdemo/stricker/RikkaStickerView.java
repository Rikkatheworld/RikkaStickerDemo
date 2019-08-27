package com.rikkatheworld.rikkastrickerdemo.stricker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.rikkatheworld.rikkastrickerdemo.R;

/**
 * 贴纸类，不用继承自View，自己来定义缩放、移动等操作
 */
public class RikkaStickerView {
    private static final String TAG = "RikkaStickerView";

    //没有任何接触、与操作的模式
    public static final int MODE_NONE = 0;
    //单指按下的时的状态，并且可以移动
    public static final int MODE_SINGLE = 1;
    //双指按下的状态，可以缩放大小
    public static final int MODE_POINT = 2;

    //设置一个内边距值
    private static final int PADDING = 20;

    //贴纸图像
    private Bitmap mBitmap;
    //删除图标图像
    private Bitmap mDelBitmap;
    //贴纸边界
    private RectF mBitmapBound;
    //删除图标边界
    private RectF mDelBitmapBound;
    //图像矩阵
    private Matrix mMatrix;
    //该贴纸是否获得焦点
    private boolean isFocus;
    //bitmap的中心点
    private PointF mCenterPoint;
    //上次双指移动的距离
    private float mLastDoubleDistance;
    //上次触摸的点
    private PointF mLastPoint = new PointF();
    //双指触控下 当前触摸的点1
    private PointF mFirstPoint = new PointF();
    //双指触控下 当前触摸的点2
    private PointF mSecondPoint = new PointF();
    //记录矩阵的点坐标，矩阵变换后也要变更
    private float[] srcPoints;
    //因为矩阵变化后 原坐标也会变化，无法变回原来的，所以需要记录一个目标的矩阵
    private float[] dstPoints;
    //记录双指之间的向量
    private PointF mCurrentVector = new PointF();
    //记录上次双指的向量
    private PointF mLastVector = new PointF();
    //当前模式
    private int mMode;


    public RikkaStickerView(Context context, Bitmap bitmap) {
        mMatrix = new Matrix();
        mCenterPoint = new PointF();
        this.mBitmap = bitmap;

        mBitmapBound = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        srcPoints = new float[]{
                //左上
                0, 0,
                //右上
                bitmap.getWidth(), 0,
                //左下
                0, bitmap.getHeight(),
                //右下
                bitmap.getWidth(), bitmap.getHeight(),
                //中点
                bitmap.getWidth() / 2f, bitmap.getHeight() / 2f
        };
        dstPoints = srcPoints.clone();

        //创建删除图标并定义边界，加上padding
        mDelBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_delete);
        mDelBitmapBound = new RectF(0 - mDelBitmap.getWidth() / 2 - PADDING, 0 - mDelBitmap.getHeight() / 2 - PADDING,
                mDelBitmap.getWidth() / 2f + PADDING, mDelBitmap.getHeight() / 2f + PADDING);

        //将贴纸默认放在屏幕中间
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(displayMetrics);
        float dx = displayMetrics.widthPixels / 2f - mBitmap.getWidth() / 2f;
        float dy = displayMetrics.heightPixels / 2f - mBitmap.getHeight() / 2f;
        translate(dx, dy);
    }

    /**
     * 平移操作,偏移量为dx，dy
     * 使用matrix去做偏移
     * 完成偏移后还要更新 points坐标
     */
    void translate(float dx, float dy) {
        mMatrix.postTranslate(dx, dy);
        updatePoints();
    }

    /**
     * 缩放操作，跟平移操作一样
     */
    void scale(float scale) {
        //以View的中点为轴放大
        mMatrix.postScale(scale, scale, mCenterPoint.x, mCenterPoint.y);
        updatePoints();
    }

    /**
     * 旋转操作
     */
    void rotate(float degrees) {
        //以中心为轴旋转
        mMatrix.postRotate(degrees, mCenterPoint.x, mCenterPoint.y);
        updatePoints();
    }

    private void updatePoints() {
        //更新贴纸坐标
        mMatrix.mapPoints(dstPoints, srcPoints);
        // 更新贴纸中心坐标
        mCenterPoint.set(dstPoints[8], dstPoints[9]);
    }

    /**
     * 绘制贴纸本身，这个方法需要父View去调用
     * Canvas 是父View的canvas,paint也是
     */
    public void onDraw(Canvas canvas, Paint paint) {
        //绘制贴纸,带上matrix参数
        canvas.drawBitmap(mBitmap, mMatrix, paint);

        //如果该贴纸是被选中的目标，则要绘制其边框,以及移除按钮
        if (isFocus) {
            //画点要用points画，因为图片会变化，所以必须要有记录号的点，所以points在这里就派上用场了
            canvas.drawLine(dstPoints[0] - PADDING, dstPoints[1] - PADDING, dstPoints[2] + PADDING, dstPoints[3] - PADDING, paint);
            canvas.drawLine(dstPoints[2] + PADDING, dstPoints[3] - PADDING, dstPoints[6] + PADDING, dstPoints[7] + PADDING, paint);
            canvas.drawLine(dstPoints[6] + PADDING, dstPoints[7] + PADDING, dstPoints[4] - PADDING, dstPoints[5] + PADDING, paint);
            canvas.drawLine(dstPoints[4] - PADDING, dstPoints[5] + PADDING, dstPoints[0] - PADDING, dstPoints[1] - PADDING, paint);
            //绘制删除按钮
            canvas.drawBitmap(mDelBitmap, dstPoints[0] - mDelBitmap.getWidth() / 2f - PADDING, dstPoints[1] - mDelBitmap.getHeight() / 2f - PADDING, paint);
        }
    }

    /**
     * 自己定义onTouch方法，根据父View传的event来做各种操作
     * 既然走到这个方法了，那么就说明 已经触摸到贴纸了
     */
    public void onTouch(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mMode = MODE_SINGLE;
                //记录按下的位置
                mLastPoint.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    mMode = MODE_POINT;
                    //双指来记录两个point
                    mFirstPoint.set(event.getX(0), event.getY(0));
                    mSecondPoint.set(event.getX(1), event.getY(1));
                    //计算双指之间的距离
                    mLastDoubleDistance = calculateDistance(mFirstPoint, mSecondPoint);
                    //记录双指间的向量
                    mLastVector.set(mFirstPoint.x - mSecondPoint.x, mFirstPoint.y - mSecondPoint.y);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //通过模式来确定行为
                if (mMode == MODE_SINGLE) {
                    //如果是单指拖动，则移动到指定的位置
                    translate(event.getX() - mLastPoint.x, event.getY() - mLastPoint.y);
                    mLastPoint.set(event.getX(), event.getY());
                }
                if (mMode == MODE_POINT && event.getPointerCount() == 2) {
                    //记录双指点的位置
                    mFirstPoint.set(event.getX(0), event.getY(0));
                    mSecondPoint.set(event.getX(1), event.getY(1));
                    //操作自由缩放
                    float distance = calculateDistance(mFirstPoint, mSecondPoint);
                    //根据双指移动的距离获取缩放系数
                    float scale = distance / mLastDoubleDistance;
                    scale(scale);
                    mLastDoubleDistance = distance;
                    //操作旋转
                    mCurrentVector.set(mFirstPoint.x - mSecondPoint.x, mFirstPoint.y - mSecondPoint.y);
                    float rotate = calculateDegrees(mLastVector, mCurrentVector);
                    rotate(rotate);
                    mLastVector.set(mCurrentVector.x, mCurrentVector.y);
                }
                break;
            case MotionEvent.ACTION_UP:
                reset();
                break;
        }
    }

    /**
     * 每当手指抬起要重置一下数据
     */
    private void reset() {
        mMode = MODE_NONE;
        mLastPoint.set(0f, 0f);
        mLastDoubleDistance = 0f;
        mLastVector.set(0f, 0f);
        mCurrentVector.set(0f, 0f);
    }

    /**
     * 通过两个坐标来计算他们的距离
     */
    private float calculateDistance(PointF mFirstPoint, PointF mSecondPoint) {
        float x = mFirstPoint.x - mSecondPoint.x;
        float y = mFirstPoint.y - mSecondPoint.y;
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 计算旋转度数
     */
    private float calculateDegrees(PointF lastVector, PointF currentVector) {
        float lastDegrees = (float) Math.atan2(lastVector.y, lastVector.x);
        float currentDegrees = (float) Math.atan2(currentVector.y, currentVector.x);
        return (float) Math.toDegrees(currentDegrees - lastDegrees);
    }

    //-----------------------------------  getter and setter --------------------------------------------------------//
    public Bitmap getmBitmap() {
        return mBitmap;
    }

    public void setmBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    public Bitmap getmDelBitmap() {
        return mDelBitmap;
    }

    public void setmDelBitmap(Bitmap mDelBitmap) {
        this.mDelBitmap = mDelBitmap;
    }

    public RectF getmBitmapBound() {
        return mBitmapBound;
    }

    public void setmBitmapBound(RectF mBitmapBound) {
        this.mBitmapBound = mBitmapBound;
    }

    public RectF getmDelBitmapBound() {
        return mDelBitmapBound;
    }

    public void setmDelBitmapBound(RectF mDelBitmapBound) {
        this.mDelBitmapBound = mDelBitmapBound;
    }

    public Matrix getmMatrix() {
        return mMatrix;
    }

    public void setmMatrix(Matrix mMatrix) {
        this.mMatrix = mMatrix;
    }

    public boolean isFocus() {
        return isFocus;
    }

    public void setFocus(boolean focus) {
        isFocus = focus;
    }

    //-----------------------------------  getter and setter --------------------------------------------------------//
}
