package com.zhou.biyongxposed;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
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

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.zhou.biyongxposed.MainActivity.server_status_check;


public class BiyongServer extends Service {
    private static final String TAG = "biyongService";
    final DatabaseHandler dbhandler = new DatabaseHandler(this);
    private Handler handler = new Handler();
    private boolean run;
    private String status;
    private ConstraintLayout toucherLayout;
    private WindowManager windowManager;
    private String topActivity="";
    @Override
    public void onCreate(){
        super.onCreate();
        run=true;
        final Eventvalue server_status = dbhandler.getNameResult("server_status");
        if (server_status != null) {
            status = server_status.getCoincount();
        }
        handler.postDelayed(task, 500);//每秒刷新线程
        Log.d(TAG,"onCreate executed");
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
                if(getHigherPackageName()!=null&&!getHigherPackageName().isEmpty()) {
                    if(!topActivity.equals(getHigherPackageName())){
                        topActivity=getHigherPackageName();
                    }
                }
                if(server_status_check||status.equals("1")) {
                    if(topActivity.equals("org.telegram.btcchat")){
                        if(toucherLayout==null) {
                            createFloat();
                        }
                    }else removeFloat();
                }else removeFloat();
                handler.postDelayed(this, 500);
            }
        }
    };
    private void removeFloat(){
        if(toucherLayout!=null){
            windowManager.removeViewImmediate(toucherLayout);
            toucherLayout=null;
        }
    }
    private void createFloat(){
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT>=23) {
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
        TextView ms_message = toucherLayout.findViewById(R.id.messaeg_ms);
        ms_message.setText("红包任务正在执行");
    }
    /**
     * 高版本：获取顶层的activity的包名
     *
     * @ return
     */
    private String getHigherPackageName() {
        String packagename = "";
        if(Build.VERSION.SDK_INT >= 22) {
            UsageStatsManager usage = (UsageStatsManager)getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> stats = usage != null ? usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time) : null;
            if (stats != null) {
                SortedMap<Long, UsageStats> runningTask = new TreeMap<Long,UsageStats>();
                for (UsageStats usageStats : stats) {
                    runningTask.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (!runningTask.isEmpty()) {
                    packagename =  runningTask.get(runningTask.lastKey()).getPackageName();
                }
            }
        } else {// if sdk <= 20, can use getRunningTasks
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            //4.获取正在开启应用的任务栈
            List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(1);
            ActivityManager.RunningTaskInfo runningTaskInfo = runningTasks.get(0);
            //5.获取栈顶的activity,然后在获取此activity所在应用的包名
            packagename = runningTaskInfo.topActivity.getPackageName();
        }
        return packagename;
    }
}
