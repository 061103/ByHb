package com.zhou.biyongxposed;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.TextView;

import static com.zhou.biyongxposed.MainActivity.server_status_check;


public class BiyongServer extends Service {
    private static final String TAG = "biyongService";
    //要引用的布局文件.
    ConstraintLayout toucherLayout;
    //布局参数.
    WindowManager.LayoutParams params;
    //实例化的WindowManager.
    WindowManager windowManager;
    private Handler handler = new Handler();
    private boolean run;
    private boolean drawableflish;

    @Override
    public void onCreate(){
        super.onCreate();
        run=true;
        drawableflish=false;
        handler.postDelayed(task, 100);//每秒刷新线程
    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId ){
        Log.d(TAG,"onStartCommand executed");
        new Thread(new Runnable() {
            @Override
              public void run() {

            }
          }).start();
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
                if(server_status_check){
                    if(toucherLayout==null){
                        if(!drawableflish){
                            createToucher();
                        }
                    }
                }else {
                    if(toucherLayout!=null&&drawableflish){
                        drawableflish=false;
                        windowManager.removeViewImmediate(toucherLayout);
                        toucherLayout=null;
                    }
                }
                handler.postDelayed(this, 100);
            }
        }
    };
    private void createToucher(){
        windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        //设置type
        if (Build.VERSION.SDK_INT>=23) {//8.0新特性
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY ;
        }else{
            params.type= WindowManager.LayoutParams.TYPE_TOAST;
        }
        //设置效果为背景透明.
        params.format = PixelFormat.TRANSLUCENT;
        params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        //设置窗口初始停靠位置.
        params.x = 0;
        params.y = 0;
        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
        //如果你想完全对应布局设置，需要先获取到机器的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        toucherLayout = (ConstraintLayout) inflater.inflate(R.layout.activity_fullscreen, null);
        //添加toucherlayout
        windowManager.addView(toucherLayout, params);
        drawableflish=true;
        TextView ms_message = toucherLayout.findViewById(R.id.messaeg_ms);
        ms_message.setText("红包任务正在执行");
    }
}
