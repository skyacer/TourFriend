package com.elong.tourpal.ui.supports.album;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.elong.tourpal.utils.Constants;

/**
 * Created by zhitao.xu on 2015/3/30.
 */
public class AlbumUtils {
    public static final int PIC_QUALITY = 100;

    public static String getFinishBtnText(int selectCount, int countTotal) {
        if (selectCount > 0) {
            return ("完成(" + selectCount + "/" + countTotal + ")");
        } else {
            return ("完成");
        }
    }

    public static Bitmap getScaledShareBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap mScaledBitmap = null;
        if (width > Constants.PIC_MAX_WIDTH_DEFAULT_UPLOAD || height > Constants.PIC_MAX_HEIGHT_DEFAULT_UPLOAD) {
            float mScaleX = (float) Constants.PIC_MAX_WIDTH_DEFAULT_UPLOAD / (float) width;
            float mScaleY = (float) Constants.PIC_MAX_HEIGHT_DEFAULT_UPLOAD / (float) height;

            float mScale = mScaleX < mScaleY ? mScaleX : mScaleY;
            Matrix mMatrix = new Matrix();
            mMatrix.postScale(mScale, mScale);
            mScaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, mMatrix, true);
        }

        if (mScaledBitmap == null) {
            return bitmap;
        } else {
//            recycleBitmap(bitmap);
            return mScaledBitmap;
        }
    }

    private static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            try {
                bitmap.recycle();
                bitmap = null;
            } catch (Exception e) {

            }
        }
    }

    public static int getFontTextWidth(String text, int fontSize) {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        return (int) paint.measureText(text, 0, text.length());
    }
}
