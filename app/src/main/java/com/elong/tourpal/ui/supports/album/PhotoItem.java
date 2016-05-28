package com.elong.tourpal.ui.supports.album;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * 一个图片对象
 * 
 */
public class PhotoItem implements Parcelable {
	public String mImageId;
	public String mThumbnailPath;
	public String mImagePath;
	public boolean mIsSelected = false;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mImageId);
        dest.writeString(mThumbnailPath);
        dest.writeString(mImagePath);
        dest.writeInt(mIsSelected ? 1 : 0);
    }

    public PhotoItem() {

    }

    private PhotoItem(Parcel in)
    {
        mImageId = in.readString();
        mThumbnailPath = in.readString();
        mImagePath = in.readString();
        mIsSelected = in.readInt() == 1 ? true : false;
    }

    public static final Parcelable.Creator<PhotoItem> CREATOR = new Parcelable.Creator<PhotoItem>()
    {
        public PhotoItem createFromParcel(Parcel in)
        {
            return new PhotoItem(in);
        }

        public PhotoItem[] newArray(int size)
        {
            return new PhotoItem[size];
        }
    };
}
