package com.elong.tourpal.ui.supports.album;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.elong.tourpal.R;

import java.util.ArrayList;
import java.util.List;

public class PhotoReviewAdapter extends BaseAdapter {
    private LayoutInflater mInflater; // 视图容器
    private int mSelectedPosition = -1;// 选中的位置
    private boolean mShape;
    private Context mContext;
    public List<Bitmap> mBMP = new ArrayList<Bitmap>();

    public boolean isShape() {
        return mShape;
    }

    public void setShape(boolean shape) {
        this.mShape = shape;
    }

    public PhotoReviewAdapter(Context context, ArrayList<Bitmap> data) {
        mContext = context;
        mBMP = data;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return (mBMP.size() + 1);
    }

    public Object getItem(int arg0) {

        return null;
    }

    public long getItemId(int arg0) {

        return 0;
    }

    public void setSelectedPosition(int position) {
        mSelectedPosition = position;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    /**
     * ListView Item设置
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {

            convertView = mInflater.inflate(R.layout.item_photo_review,
                    parent, false);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView
                    .findViewById(R.id.item_grida_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position == mBMP.size()) {
            holder.image.setImageDrawable(mContext.getResources().getDrawable(R.drawable.select_posting_main_camera_add));
            if (position == AlbumConstant.UPLOAD_PHOTO_MAX) {
                holder.image.setVisibility(View.GONE);
            }
        } else {
            holder.image.setImageBitmap(mBMP.get(position));
        }

        return convertView;
    }

    public class ViewHolder {
        public ImageView image;
    }

}
