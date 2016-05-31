package com.elong.tourpal.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.elong.tourpal.R;

/**
 * Created by LuoChangAn on 16/5/31.
 */
public class AddNewAccountActivity extends ActionBarActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_acount);
        setTitle(R.string.add_new_passwd_title);
    }

    @Override
    public void onClick(View v) {

    }

    public static void start(Activity activity){
        Intent intent = new Intent(activity,AddNewAccountActivity.class);
        activity.startActivity(intent);
    }
}
