package com.rikkatheworld.rikkastrickerdemo.stricker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

/**
 * 是用来装贴纸的Layout
 * 因为子View并不是传统的View，所以不用考虑拦截点击事件的问题，直接调用就行了
 */
public class RikkaStickerLayout extends View implements View.OnTouchListener {
    private static final String TAG = "RikkaStickerLayout";

    private Context mContext;
    //画笔,用来画边界
    private Paint mPaint;
    //记录当前按下的贴纸
    private RikkaStickerView stickerView;

    public RikkaStickerLayout(Context context) {
        this(context, null);
    }

    public RikkaStickerLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RikkaStickerLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setOnTouchListener(this);
        initPaint();
    }

    /**
     * 画边框的颜色设置成红色
     */
    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(2);
    }

    /**
     * 添加贴纸
     */
    public void addSticker(RikkaStickerView stickerView) {
        RikkaStickerManager.getInstance().addSticker(stickerView);
        RikkaStickerManager.getInstance().setFocusSticker(stickerView);
        invalidate();
    }

    /**
     * 移除贴纸
     */
    public void removeSticker(RikkaStickerView stickerView) {
        RikkaStickerManager.getInstance().removeSticker(stickerView);
        invalidate();
    }

    /**
     * 清空
     */
    public void removeAllSticker() {
        RikkaStickerManager.getInstance().removeAllSticker();
        invalidate();
    }

    /**
     * 点击事件，处理单指移动，双指缩放
     * 单指单击删除
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                //先判断是否是点到删除按钮了
                stickerView = RikkaStickerManager.getInstance().getDelButton(event.getX(), event.getY());
                if (stickerView != null) {
                    removeSticker(stickerView);
                    return true;
                }
                //再判断是否摸到某一个贴纸
                stickerView = RikkaStickerManager.getInstance().getSticker(event.getX(), event.getY());
                if (stickerView == null) {
                    //当不是单指的时候,可能会存在第二个手指摸到了贴纸（先按下两个，抬起一个的情况）
                    if (event.getPointerCount() == 2) {
                        stickerView = RikkaStickerManager.getInstance().getSticker(event.getX(1), event.getY(1));
                    }
                }
                if (stickerView != null) {
                    RikkaStickerManager.getInstance().setFocusSticker(stickerView);
                }
                break;
            default:
                break;
        }
        if (stickerView != null) {
            stickerView.onTouch(event);
        } else {
            //如果没有点击到，则取消所有贴纸的焦点
            RikkaStickerManager.getInstance().clearAllFocus();
        }
        invalidate();
        return true;
    }

    /**
     * 绘制所有的子贴纸，并且根据是否被选中来画
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        List<RikkaStickerView> stickerViews = RikkaStickerManager.getInstance().getmStickerList();
        for (int i = 0; i < stickerViews.size(); i++) {
            RikkaStickerView stickerView = stickerViews.get(i);
            stickerView.onDraw(canvas, mPaint);
        }
    }
}
