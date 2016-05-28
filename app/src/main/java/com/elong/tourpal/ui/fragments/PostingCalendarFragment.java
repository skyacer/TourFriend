package com.elong.tourpal.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elong.tourpal.R;
import com.elong.tourpal.ui.activities.PostingTourActivity;
import com.elong.tourpal.ui.supports.album.AlbumConstant;
import com.elong.tourpal.ui.supports.calendar.DatePickerController;
import com.elong.tourpal.ui.supports.calendar.DayPickerView;
import com.elong.tourpal.ui.supports.calendar.SimpleMonthAdapter;
import com.elong.tourpal.ui.views.CommonTitleBar;

import java.util.Calendar;

/**
 * Created by zhitao.xu on 2015/3/25.
 */
public class PostingCalendarFragment extends Fragment implements DatePickerController {
    private DayPickerView dayPickerView;
    private int mDateType;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDateType = getArguments().getInt(AlbumConstant.EXTRA_TIME_TYPE);
        setTitlebar();
        View view = inflater.inflate(R.layout.fragment_posting_calendar, container, false);
        dayPickerView = (DayPickerView) view.findViewById(R.id.posting_pickerView);
        dayPickerView.setController(this);
        return view;
    }

    private void setTitlebar() {
        CommonTitleBar tb = ((PostingTourActivity) getActivity()).mTitleBar;
        if (mDateType == AlbumConstant.POSTING_TIME_TYPE_START) {
            tb.setTitle(R.string.posting_calendar_start_title);
        } else {
            tb.setTitle(R.string.posting_calendar_end_title);
        }
        tb.setSettingVisible(false);
        tb.setBackImage(R.drawable.select_titlebar_back);
    }

    /**
     * ******************************日期控件回调******************************
     */
    @Override
    public int getMaxYear() {
        return 2015;
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
        PostingTourActivity pa = (PostingTourActivity) getActivity();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        if (mDateType == AlbumConstant.POSTING_TIME_TYPE_START) {
            pa.setStartTime(calendar);
        } else {
            pa.setEndTime(calendar);
        }
        getFragmentManager().popBackStack();
    }

    @Override
    public void onDateRangeSelected(SimpleMonthAdapter.SelectedDays<SimpleMonthAdapter.CalendarDay> selectedDays) {

    }
}
