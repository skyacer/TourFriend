package com.elong.tourpal.ui.supports.album;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;

import com.elong.tourpal.application.Env;
import com.elong.tourpal.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 图片帮助类
 */
public class AlbumHelper {
    private final String TAG = getClass().getSimpleName();
    boolean mDataHasLoaded = false;
    Context mContext;
    ContentResolver mCR;
    // 缩略图列表
    HashMap<String, String> mThumbnailList = new HashMap<String, String>();
    private static AlbumHelper mInstance;

    public AlbumHelper(Context context) {
        init(context);
    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        this.mContext = context;
        mCR = context.getContentResolver();
    }

    /**
     * 得到缩略图
     */
    private void getThumbnail() {
        String[] projection = {Thumbnails._ID, Thumbnails.IMAGE_ID,
                Thumbnails.DATA};
        Cursor cursor = mCR.query(Thumbnails.EXTERNAL_CONTENT_URI, projection,
                null, null, null);
        getThumbnailColumnData(cursor);
    }

    /**
     * 从数据库中得到缩略图
     *
     * @param cur
     */
    private void getThumbnailColumnData(Cursor cur) {
        if (cur.moveToFirst()) {
            int image_id;
            String image_path;
            int image_idColumn = cur.getColumnIndex(Thumbnails.IMAGE_ID);
            int dataColumn = cur.getColumnIndex(Thumbnails.DATA);

            do {
                // Get the field values
                image_id = cur.getInt(image_idColumn);
                image_path = cur.getString(dataColumn);
                mThumbnailList.put("" + image_id, image_path);
                Log.e("thumbnail", "id=" + image_id + ", path=" + image_path);
            } while (cur.moveToNext());
        }
    }

    ArrayList<PhotoItem> mPhotoData = new ArrayList<PhotoItem>();

    /**
     * 是否创建了图片集
     */
    boolean hasBuildImagesBucketList = false;

    public List<PhotoItem> getImageData() {
        if (!mDataHasLoaded) {
            long startTime = System.currentTimeMillis();
            // 构造缩略图索引
            getThumbnail();
            // 构造相册索引
            String columns[] = new String[]{Media._ID, Media.BUCKET_ID,
                    Media.PICASA_ID, Media.DATA, Media.DISPLAY_NAME,
                    Media.TITLE, Media.SIZE, Media.BUCKET_DISPLAY_NAME};
            // 得到一个游标
            String sortByDate = Media.DATE_MODIFIED + " DESC";
            Cursor cur = mCR.query(Media.EXTERNAL_CONTENT_URI, columns, null,
                    null, sortByDate);
            if (cur.moveToFirst()) {
                // 获取指定列的索引
                int photoIDIndex = cur.getColumnIndexOrThrow(Media._ID);
                int photoPathIndex = cur.getColumnIndexOrThrow(Media.DATA);
                int photoNameIndex = cur
                        .getColumnIndexOrThrow(Media.DISPLAY_NAME);
                int photoTitleIndex = cur.getColumnIndexOrThrow(Media.TITLE);
                int photoSizeIndex = cur.getColumnIndexOrThrow(Media.SIZE);
                int bucketDisplayNameIndex = cur
                        .getColumnIndexOrThrow(Media.BUCKET_DISPLAY_NAME);
                int bucketIdIndex = cur.getColumnIndexOrThrow(Media.BUCKET_ID);
                int picasaIdIndex = cur.getColumnIndexOrThrow(Media.PICASA_ID);

                do {
                    String _id = cur.getString(photoIDIndex);
                    String name = cur.getString(photoNameIndex);
                    String path = cur.getString(photoPathIndex);
                    String title = cur.getString(photoTitleIndex);
                    String size = cur.getString(photoSizeIndex);
                    String bucketName = cur.getString(bucketDisplayNameIndex);
                    String bucketId = cur.getString(bucketIdIndex);
                    String picasaId = cur.getString(picasaIdIndex);

                    if (Env.DEBUG) {
                        Log.i(TAG, _id + ", bucketId: " + bucketId + ", picasaId: "
                                + picasaId + " name:" + name + " path:" + path
                                + " title: " + title + " size: " + size
                                + " bucket: " + bucketName + "---");
                    }
                    if (size == null) {
                        continue;
                    }
                    if (Integer.valueOf(size) < Constants.SHOW_PHOTO_MINIMUM_SIZE) {
                        continue;
                    }

                    PhotoItem imageItem = new PhotoItem();
                    imageItem.mImageId = _id;
                    imageItem.mImagePath = path;
                    imageItem.mThumbnailPath = mThumbnailList.get(_id);
                    mPhotoData.add(imageItem);

                } while (cur.moveToNext());
            }
            long endTime = System.currentTimeMillis();
            Log.d(TAG, "use time: " + (endTime - startTime) + " ms");
            mDataHasLoaded = true;
        }
        return mPhotoData;
    }

    // /**
    // * 得到图片集
    // */
    // void buildImagesBucketList() {
    // long startTime = System.currentTimeMillis();
    //
    // // 构造缩略图索引
    // getThumbnail();
    //
    // // 构造相册索引
    // String columns[] = new String[] { Media._ID, Media.BUCKET_ID,
    // Media.PICASA_ID, Media.DATA, Media.DISPLAY_NAME, Media.TITLE,
    // Media.SIZE, Media.BUCKET_DISPLAY_NAME };
    // // 得到一个游标
    // Cursor cur = cr.query(Media.EXTERNAL_CONTENT_URI, columns, null, null,
    // null);
    // if (cur.moveToFirst()) {
    // // 获取指定列的索引
    // int photoIDIndex = cur.getColumnIndexOrThrow(Media._ID);
    // int photoPathIndex = cur.getColumnIndexOrThrow(Media.DATA);
    // int photoNameIndex = cur.getColumnIndexOrThrow(Media.DISPLAY_NAME);
    // int photoTitleIndex = cur.getColumnIndexOrThrow(Media.TITLE);
    // int photoSizeIndex = cur.getColumnIndexOrThrow(Media.SIZE);
    // int bucketDisplayNameIndex = cur
    // .getColumnIndexOrThrow(Media.BUCKET_DISPLAY_NAME);
    // int bucketIdIndex = cur.getColumnIndexOrThrow(Media.BUCKET_ID);
    // int picasaIdIndex = cur.getColumnIndexOrThrow(Media.PICASA_ID);
    // // 获取图片总数
    // int totalNum = cur.getCount();
    //
    // do {
    // String _id = cur.getString(photoIDIndex);
    // String name = cur.getString(photoNameIndex);
    // String path = cur.getString(photoPathIndex);
    // String title = cur.getString(photoTitleIndex);
    // String size = cur.getString(photoSizeIndex);
    // String bucketName = cur.getString(bucketDisplayNameIndex);
    // String bucketId = cur.getString(bucketIdIndex);
    // String picasaId = cur.getString(picasaIdIndex);
    //
    // Log.i(TAG, _id + ", bucketId: " + bucketId + ", picasaId: "
    // + picasaId + " name:" + name + " path:" + path
    // + " title: " + title + " size: " + size + " bucket: "
    // + bucketName + "---");
    //
    // ImageBucket bucket = bucketList.get(bucketId);
    // if (bucket == null) {
    // bucket = new ImageBucket();
    // bucketList.put(bucketId, bucket);
    // bucket.imageList = new ArrayList<ImageItem>();
    // bucket.bucketName = bucketName;
    // }
    // bucket.count++;
    // ImageItem imageItem = new ImageItem();
    // imageItem.imageId = _id;
    // imageItem.imagePath = path;
    // imageItem.thumbnailPath = thumbnailList.get(_id);
    // bucket.imageList.add(imageItem);
    //
    // } while (cur.moveToNext());
    // }
    //
    // Iterator<Entry<String, ImageBucket>> itr = bucketList.entrySet()
    // .iterator();
    // while (itr.hasNext()) {
    // Map.Entry<String, ImageBucket> entry = (Map.Entry<String, ImageBucket>)
    // itr
    // .next();
    // ImageBucket bucket = entry.getValue();
    // Log.d(TAG, entry.getKey() + ", " + bucket.bucketName + ", "
    // + bucket.count + " ---------- ");
    // for (int i = 0; i < bucket.imageList.size(); ++i) {
    // ImageItem image = bucket.imageList.get(i);
    // Log.d(TAG, "----- " + image.imageId + ", " + image.imagePath
    // + ", " + image.thumbnailPath);
    // }
    // }
    // hasBuildImagesBucketList = true;
    // long endTime = System.currentTimeMillis();
    // Log.d(TAG, "use time: " + (endTime - startTime) + " ms");
    // }

    // /**
    // * 得到图片集
    // *
    // * @param refresh
    // * @return
    // */
    // public List<ImageBucket> getImagesBucketList(boolean refresh) {
    // if (refresh || (!refresh && !hasBuildImagesBucketList)) {
    // buildImagesBucketList();
    // }
    // List<ImageBucket> tmpList = new ArrayList<ImageBucket>();
    // Iterator<Entry<String, ImageBucket>> itr = bucketList.entrySet()
    // .iterator();
    // while (itr.hasNext()) {
    // Map.Entry<String, ImageBucket> entry = (Map.Entry<String, ImageBucket>)
    // itr
    // .next();
    // tmpList.add(entry.getValue());
    // }
    // return tmpList;
    // }

    /**
     * 得到原始图像路径
     *
     * @param image_id
     * @return
     */
    String getOriginalImagePath(String image_id) {
        String path = null;
        String[] projection = {Media._ID, Media.DATA};
        Cursor cursor = mCR.query(Media.EXTERNAL_CONTENT_URI, projection,
                Media._ID + "=" + image_id, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(Media.DATA));

        }
        return path;
    }

}
