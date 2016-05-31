package com.elong.tourpal.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by LuoChangAn on 16/5/29.
 */
public class TourPostData implements Cloneable,Serializable{
    public ArrayList<String> mDestinationAndIds = new ArrayList<>();
    public Calendar mStartTime;
    public Calendar mEndTime;
    public ArrayList<String> mSelectTags = new ArrayList<>();
    public String mWeixin;
    public String mQQ;
    public String mPhone;
    public String mDetail;

    @Override
    public String toString() {
        return "mDestinationAndIds=" + mDestinationAndIds +
                ", mStartTime=" + mStartTime +
                ", mEndTime=" + mEndTime +
                ", mSelectTags=" + mSelectTags +
                ", mWeixin='" + mWeixin + '\'' +
                ", mQQ='" + mQQ + '\'' +
                ", mPhone='" + mPhone + '\'' +
                ", mDetail='" + mDetail ;
    }
}
