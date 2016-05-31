package com.elong.tourpal.utils;

/**
 * Created by LuoChangAn on 16/5/31.
 */
public class StringUtils {
    private StringUtils(){
    }

    public static String[] spliteUnderLine(String s){
        String[] arr = s.split("_");
        return arr;
    }
}
