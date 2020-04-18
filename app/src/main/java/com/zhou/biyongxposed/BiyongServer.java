package com.zhou.biyongxposed;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
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
import android.widget.Toast;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.zhou.biyongxposed.NotificationCollectorService.biyongNotificationEvent;
import static com.zhou.biyongxposed.bingyongserver.shoudong;
import static com.zhou.biyongxposed.shuomingActivity.dimAmount_num;

public class BiyongServer extends Service {
    private static final String TAG = "BiyongBackgroundService";
    final DatabaseHandler dbhandler = new DatabaseHandler(this);
    private final checkRoot checkRoot = new checkRoot();
    private Handler handler = new Handler();
    private boolean run;
    private String status;
    private ConstraintLayout toucherLayout;
    private WindowManager windowManager;
    public static String topActivity="";
    public static boolean Rooted;
    @Override
    public void onCreate(){
        super.onCreate();
        run = true;
        handler.postDelayed(task, 100);//每秒刷新线程
        if(checkRoot.isDeviceRooted()) {
            Rooted=true;
        }else {
            Toast.makeText(this,"当前系统没有Root权限,可能无法执行ADB指令.",Toast.LENGTH_LONG).show();
        }
        Log.d(TAG,"后台SERVER正在运行!");
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
        @SuppressLint("ObsoleteSdkInt")
        @Override
        public void run() {
                if (run) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        if (getHigherPackageName() != null && !topActivity.equals(getHigherPackageName())) {
                            topActivity = getHigherPackageName();
                        }
                    } else {
                        getLowerVersionPackageName();
                        if (!getLowerVersionPackageName().isEmpty() && !topActivity.equals(getLowerVersionPackageName())) {
                            topActivity = getLowerVersionPackageName();
                        }
                    }
                    final Eventvalue server_status = dbhandler.getNameResult("server_status");
                    if (server_status != null) status = server_status.getCoincount();
                    if (status != null && !status.isEmpty() && status.equals("1")) {
                        if (topActivity.equals("org.telegram.btcchat") && biyongNotificationEvent) {
                            if (!shoudong) {
                                    if (toucherLayout == null) {
                                        createFloat(getApplicationContext());
                                    }
                                }
                            }else {
                            removeFloat();
                        }
                    }
                    handler.postDelayed(this, 100);
            }
        }
    };
    public void removeFloat(){
        if(toucherLayout!=null){
            windowManager.removeViewImmediate(toucherLayout);
            toucherLayout=null;
        }
    }
    @SuppressLint("InflateParams")
    private void createFloat(Context context){
        windowManager =  (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP_MR1) {//android 5.1
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR ;
        }else{
            params.type= WindowManager.LayoutParams.TYPE_TOAST;
        }
        //设置效果为背景透明.
        params.format = PixelFormat.TRANSLUCENT;
        params.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN//当该窗口显示时, 隐藏所有屏幕装饰(如状态栏), 允许窗口使用整个屏幕
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN //让window占满整个手机屏幕，不留任何边界
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE//该窗口会消费所有的触摸事件, 无论触摸是否在窗口之内
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON//当窗口对于用户可见时, 保持设备屏幕常亮
                | WindowManager.LayoutParams.FLAG_DIM_BEHIND //Constant Value: 2 (0x00000002) 所有在这个window之后的会变暗
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED; //对窗口启用硬件加速
        //设置窗口初始停靠位置.
        params.x = 0;
        params.y = 0;
        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
        //如果你想完全对应布局设置，需要先获取到机器的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.dimAmount  = dimAmount_num;
        LayoutInflater inflater = LayoutInflater.from(getApplication());
        toucherLayout = (ConstraintLayout) inflater.inflate(R.layout.activity_fullscreen, null);
        windowManager.addView(toucherLayout, params);
    }
    /**
     * 高版本：获取顶层的activity的包名
     *
     * @return
     */
    @SuppressLint("ObsoleteSdkInt")
    private String getHigherPackageName() {
        String topPackageName = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (stats != null) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : stats) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    topPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                    //Log.i("TopPackage Name", topPackageName);
                }
            }
        } else {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName topActivity = activityManager.getRunningTasks(1).get(0).topActivity;
            topPackageName = topActivity.getPackageName();
        }
        return topPackageName;
    }

    /**
     * 低版本：获取栈顶app的包名
     *
     * @return
     */
    private String getLowerVersionPackageName() {
        String topPackageName;//低版本  直接获取getRunningTasks
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName topActivity = activityManager.getRunningTasks(1).get(0).topActivity;
        topPackageName = topActivity.getPackageName();
        return topPackageName;
    }
}
