package com.zhou.biyongxposed;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


public class BiyongServer extends Service {
    private final static String TAG = "CheckActivity";
    private Handler handler = new Handler();
    private boolean run;
    @Override
    public void onCreate(){
        super.onCreate();
        run = false;
        handler.postDelayed(task, 500);//每秒刷新线程
        Log.d(TAG,"onCreate execute");
    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId ){
        Log.d(TAG,"onStartCommand executed");
        return super.onStartCommand(intent,flags,startId);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG,"onDestroy execute");
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            if (run) {
                handler.postDelayed(this, 500);
            }
        }
    };
}
