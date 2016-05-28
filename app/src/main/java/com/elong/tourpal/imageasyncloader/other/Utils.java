package com.elong.tourpal.imageasyncloader.other;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import com.elong.tourpal.application.Env;
import com.elong.tourpal.application.TourPalApplication;
import com.elong.tourpal.utils.Constants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Utils
 *
 * @author tao.chen1
 */
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static File getExternalCacheDir(Context context) {
        File path = context.getExternalCacheDir();

        // In some case, even the sd card is mounted, getExternalCacheDir will return null, may be it is nearly full.
        if (path != null) {
            return path;
        }

        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    public static boolean isSDCardMounted() {
        String state = Environment.getExternalStorageState();
        if (state != null && state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 读取本地图片，返回BitmapDrawable
     *
     * @param context 上下文
     * @param path    路径
     * @return BitmapDrawable
     */
    public static RecyclableBitmapDrawable readBitmapDrawable(Context context, String path) {
        Bitmap bitmap = null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);

        try {
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            opts.inPurgeable = true;
            opts.inInputShareable = true;
            int i = 0;
            while (true) {
                if ((opts.outWidth >> i <= Constants.PIC_MAX_WIDTH)
                        && (opts.outHeight >> i <= Constants.PIC_MAX_HEIGHT)) {
                    opts.inSampleSize = (int) Math.pow(2.0D, i);// 2.0^i
                    opts.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeFile(path, opts);
                    break;
                }
                i += 1;
            }
        } catch (Throwable t) {
            if (Env.DEBUG) {
                Log.e(TAG, "e:", t);
            }
        }

        if (bitmap == null) {
            return null;
        } else {
            RecyclableBitmapDrawable bitmapDrawable = new RecyclableBitmapDrawable(context.getResources(), bitmap);
            bitmapDrawable = rotateBitmap(bitmapDrawable, path);
            return bitmapDrawable;
        }
    }

    public static RecyclableBitmapDrawable rotateBitmap(RecyclableBitmapDrawable bitmapDrawable, String path) {
        ExifInterface exi = null;
        int digree = 0;
        try {
            exi = new ExifInterface(path);
        } catch (IOException e) {
            exi = null;
            if (Env.DEBUG) {
                Log.e(TAG, "e:", e);
            }
        }

        if (exi != null) {
            int ori = exi.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            switch (ori) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    digree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    digree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    digree = 270;
                    break;
                default:
                    digree = 0;
                    break;
            }
        }

        if (digree != 0) {
            Matrix m = new Matrix();
            m.postRotate(digree);
            Bitmap bitmap = bitmapDrawable.getBitmap();
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            bitmapDrawable = new RecyclableBitmapDrawable(TourPalApplication.getAppContext().getResources(), bitmap);
        }
        return bitmapDrawable;
    }

    public static int getInSampleSize(int width, int height, int maxWidth, int maxHeight) {
        int inSampleSize = 2;
        int tmpWidth = width;
        int tmpHeight = height;
        while (true) {
            tmpWidth /= 2;
            tmpHeight /= 2;
            if (tmpWidth <= maxWidth && tmpHeight <= maxHeight) {
                break;
            }
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
