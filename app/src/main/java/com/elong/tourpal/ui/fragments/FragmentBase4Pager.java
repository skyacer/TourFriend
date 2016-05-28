package com.elong.tourpal.ui.fragments;

import android.support.v4.app.Fragment;

/**
 * FragmentBase4Pager
 */
public class FragmentBase4Pager extends Fragment {
    private int mPageIndex = 0;

    public int getPageIndex() {
        return mPageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.mPageIndex = pageIndex;
    }
}
