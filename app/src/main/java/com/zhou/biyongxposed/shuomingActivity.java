package com.zhou.biyongxposed;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class shuomingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuoming);
        //进行EventBus的注册
        EventBus.getDefault().register(this);

    }
    /*
     *  新版本需要手动的添加注解@Subscribe(这是必不可少的)
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(Message msgtype) {
        if (msgtype != null) {
            Toast.makeText(this, msgtype.getShu(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
