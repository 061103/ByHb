package com.zhou.biyongxposed;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.zhou.biyongxposed.GuideActivity.run_ok;
import static com.zhou.biyongxposed.MainActivity.server_status_check;


public class BiyongServer extends Service {
    private static final String TAG = "biyongService";
    final DatabaseHandler dbhandler = new DatabaseHandler(this);
    private Handler handler = new Handler();
    private boolean run;
    private String status;
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
                if(server_status_check||status.equals("1")) {
                    if(getHigherPackageName().equals("org.telegram.btcchat")){
                        if(!run_ok) {
                            new GuideActivity().start(getApplicationContext());
                            Log.d(TAG,"创建一次");
                        }
                    }else run_ok=false;
                }
                handler.postDelayed(this, 500);
            }
        }
    };

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
            List<UsageStats> stats = usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);
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
