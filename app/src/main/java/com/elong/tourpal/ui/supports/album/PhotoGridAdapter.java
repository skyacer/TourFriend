package com.elong.tourpal.ui.supports.album;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Env;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotoGridAdapter extends BaseAdapter {
    private static final boolean DEBUG = Env.DEBUG;
    private static final String TAG = PhotoGridAdapter.class.getSimpleName();
    List<PhotoItem> mDataList;
    Context mContext;
    public Map<String, String> mSelectPhotoMap = new HashMap<String, String>();
    BitmapCache mCache;
    private OnClickListener mCheckedClickListener;
    BitmapCache.ImageCallback mCallback = new BitmapCache.ImageCallback() {
        @Override
        public void imageLoad(ImageView imageView, Bitmap bitmap,
                              Object... params) {
            if (imageView != null && bitmap != null) {
                String url = (String) params[0];
                if (url != null && url.equals((String) imageView.getTag())) {
                    ((ImageView) imageView).setImageBitmap(bitmap);
                } else {
                    if (DEBUG) {
                        Log.e(TAG, "callback, bmp not match");
                    }
                }
            } else {
                if (DEBUG) {
                    Log.e(TAG, "callback, bmp null");
                }
            }
        }
    };

    public PhotoGridAdapter(Context c, List<PhotoItem> list, OnClickListener checkedChangeListener) {
        this.mContext = c;
        mDataList = list;
        mCache = new BitmapCache();
        mCheckedClickListener = checkedChangeListener;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (mDataList != null) {
            count = mDataList.size();
        }
        return count + 1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class Holder {
        private ImageView iv;
        private CheckBox selected;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.item_photo_grid_camera, parent, false);
//            ImageView iv = (ImageView) convertView.findViewById(R.id.image);
//            iv.setImageResource(R.drawable.posting_main_camera_add_nomal);
        } else {
            final Holder holder;
            if (convertView == null || convertView.getTag() == null) {
                holder = new Holder();
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.item_photo_grid, parent, false);
                holder.iv = (ImageView) convertView.findViewById(R.id.image);
                holder.selected = (CheckBox) convertView
                        .findViewById(R.id.isselected);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.selected.setTag(position);
            if (mCheckedClickListener == null) {
                holder.selected.setVisibility(View.GONE);
            } else {
                holder.selected.setVisibility(View.VISIBLE);
            }
            final PhotoItem item = mDataList.get(position - 1);
            holder.iv.setTag(item.mImagePath);
            mCache.displayBmp(holder.iv, item.mThumbnailPath, item.mImagePath,
                    mCallback);
            if (item.mIsSelected) {
                holder.selected.setChecked(true);
            } else {
                holder.selected.setChecked(false);
            }

            if (mCheckedClickListener != null) {
                holder.selected.setOnClickListener(mCheckedClickListener);
            }
        }
        return convertView;
    }
}
