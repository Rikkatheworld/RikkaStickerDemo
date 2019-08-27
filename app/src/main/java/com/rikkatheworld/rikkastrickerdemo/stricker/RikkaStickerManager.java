package com.rikkatheworld.rikkastrickerdemo.stricker;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 贴纸管理类，使用单例模式
 * 对每个“贴纸”进行保存、增加、删除
 */
public class RikkaStickerManager {
    private static final String TAG = "RikkaStickerManager";

    public static RikkaStickerManager instance;

    //贴纸List，统一进行管理
    private List<RikkaStickerView> mStickerList = new ArrayList<>();

    public static RikkaStickerManager getInstance() {
        if (instance == null) {
            synchronized (RikkaStickerManager.class) {
                if (instance == null) {
                    instance = new RikkaStickerManager();
                }
            }
        }
        return instance;
    }

    /**
     * 添加贴纸，就是往List里面添加
     */
    void addSticker(RikkaStickerView stickerView) {
        mStickerList.add(stickerView);
    }

    /**
     * 移除指定的贴纸
     */
    void removeSticker(RikkaStickerView stickerView) {
        Bitmap bitmap = stickerView.getmBitmap();
        if (bitmap != null && !bitmap.isRecycled()) {
            //即使回收
            bitmap.recycle();
        }
        mStickerList.remove(stickerView);
    }

    /**
     * 移除所有贴纸
     */
    void removeAllSticker() {
        for (int i = 0; i < mStickerList.size(); i++) {
            Bitmap bitmap = mStickerList.get(i).getmBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        mStickerList.clear();
    }

    /**
     * 设置当前贴纸为选中（焦点）贴纸
     */
    void setFocusSticker(RikkaStickerView focusSticker) {
        for (int i = 0; i < mStickerList.size(); i++) {
            RikkaStickerView sticker = mStickerList.get(i);
            if (sticker == focusSticker) {
                sticker.setFocus(true);
            } else {
                sticker.setFocus(false);
            }
        }
    }

    /**
     * 全部设为没有焦点
     */
    void clearAllFocus() {
        for (int i = 0; i < mStickerList.size(); i++) {
            RikkaStickerView stickerView = mStickerList.get(i);
            stickerView.setFocus(false);
        }
    }

    /**
     * 根据触摸的点来返回触摸的贴纸
     */
    RikkaStickerView getSticker(float x, float y) {
        for (int i = mStickerList.size() - 1; i >= 0; i--) {
            RikkaStickerView sticker = mStickerList.get(i);
            //因为points 映射之后都会改变所以必须每次都重置
            float[] points = new float[]{x, y};
            //根据invert来做转换
            Matrix matrix = new Matrix();
            sticker.getmMatrix().invert(matrix);
            matrix.mapPoints(points);
            //根据边界来判断 点是否在该View中
            if (sticker.getmBitmapBound().contains(points[0], points[1])) {
                return sticker;
            }
        }
        return null;
    }

    /**
     * 根据触摸的点来判断是否点击到删除按钮，如果点到就会删除
     */
    RikkaStickerView getDelButton(float x, float y) {
        for (int i = mStickerList.size() - 1; i >= 0; i--) {
            RikkaStickerView sticker = mStickerList.get(i);
            float[] points = new float[]{x, y};
            //根据invert来做转换
            Matrix matrix = new Matrix();
            sticker.getmMatrix().invert(matrix);
            matrix.mapPoints(points);
            //根据边界来判断 点是否在该View中
            if (sticker.getmDelBitmapBound().contains(points[0], points[1])) {
                return sticker;
            }
        }
        return null;
    }

    public List<RikkaStickerView> getmStickerList() {
        return mStickerList;
    }
}
