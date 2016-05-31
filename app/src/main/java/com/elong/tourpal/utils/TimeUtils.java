package com.elong.tourpal.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by LuoChangAn on 16/5/30.
 */
public class TimeUtils {
    private TimeUtils(){

    }

    /**
     * String转Calender
     * @param s yyyy-mm-dd
     * @return
     */
    public static Calendar stringToCalender(String s){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        try {
            date = sdf.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }
}
