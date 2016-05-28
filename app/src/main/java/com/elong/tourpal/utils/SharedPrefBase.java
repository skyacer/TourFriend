package com.elong.tourpal.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Created by zhitao.xu on 2015/4/17.
 */
public class SharedPrefBase {
    private SharedPreferences mSp;

    protected SharedPrefBase(Context c, String spFileName, int mode) {
        mSp = c.getSharedPreferences(spFileName, mode);
    }

    public void setString(String key, String val) {
        if (mSp != null && !TextUtils.isEmpty(key)) {
            SharedPreferences.Editor editor = mSp.edit();
            editor.putString(key, val);
            editor.commit();
        }
    }

    public void setInt(String key, int val) {
        if (mSp != null) {
            SharedPreferences.Editor editor = mSp.edit();
            editor.putInt(key, val);
            editor.commit();
        }
    }

    public void setLong(String key, long val) {
        if (mSp != null) {
            SharedPreferences.Editor editor = mSp.edit();
            editor.putLong(key, val);
            editor.commit();
        }
    }

    public void setBoolean(String key, boolean val) {
        if (mSp != null) {
            SharedPreferences.Editor editor = mSp.edit();
            editor.putBoolean(key, val);
            editor.commit();
        }
    }

    public String getString(String key, String defValue) {
        if (mSp != null) {
            return mSp.getString(key, defValue);
        } else {
            return defValue;
        }
    }

    public int getInt(String key, int defValue) {
        if (mSp != null) {
            return mSp.getInt(key, defValue);
        } else {
            return defValue;
        }
    }

    public boolean getBoolean(String key, boolean defValue) {
        if (mSp != null) {
            return mSp.getBoolean(key, defValue);
        } else {
            return defValue;
        }
    }

    public long getLong(String key, long defValue) {
        if (mSp != null) {
            return mSp.getLong(key, defValue);
        } else {
            return defValue;
        }
    }

    public String[] getKeys() {
        if (mSp.getAll().keySet().toArray() == null) {
            return null;
        }
        return mSp.getAll().keySet().toArray(new String[]{});
    }

    public void removeKey(String key) {
        mSp.edit().remove(key);
    }

    public boolean contains(String key) {
        return mSp.contains(key);
    }

    public void clearAll() {
        mSp.edit().clear();
    }
}
