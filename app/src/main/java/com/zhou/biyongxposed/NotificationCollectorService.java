package com.zhou.biyongxposed;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import static android.os.PowerManager.SCREEN_DIM_WAKE_LOCK;
import static com.zhou.biyongxposed.bingyongserver.lightSleeper;
import static com.zhou.biyongxposed.bingyongserver.sleepTime;

public class NotificationCollectorService extends NotificationListenerService {
    private static final String TAG = "NotificationListener";
    public static boolean enableKeyguard;
    public static boolean biyongNotificationEvent;
    public static boolean noComeIn;
    public static boolean swipe_run;
    public static PowerManager pm;
    public static KeyguardManager.KeyguardLock kl;
    private PowerManager.WakeLock wl = null;
    public static KeyguardManager km;
    public static String TopActivityName;
    public static String TopName="";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn.getPackageName().equals("org.telegram.btcchat")&&!biyongNotificationEvent) {
            BiyongServer.run = true; //此处控制SERVER运行
                Log.d(TAG, "通知回调成功");
                LogUtils.i( "通知回调成功");
                Object  string = sbn.getNotification().extras.get("android.text");
                Log.d(TAG, "获取到通知栏消息内容----"+string);
                LogUtils.i("获取到通知栏红包消息!");
                Log.d(TAG, "群组:----"+sbn.getNotification().extras.get("android.title"));
                LogUtils.i("群组:----"+sbn.getNotification().extras.get("android.title"));
                if (!isScreenLocked()) {
                    wakeUpAndUnlock();
                    enableKeyguard=true;
                    Log.d(TAG, "唤醒屏幕!");
                    LogUtils.i("唤醒屏幕!");
                    sleepTime(lightSleeper);
                    TopName = "";
                }else {
                    if(TopActivityName!=null) TopName = TopActivityName;
                    Log.d(TAG, "顶层Activity====="+TopName);
                    LogUtils.i("顶层Activity====="+TopName);
                }
                if (getHigherPackageName() != null) {//获取当前运行于顶部的activity
                    TopActivityName = getHigherPackageName();
                }
                PendingIntent pendingIntent = sbn.getNotification().contentIntent;
                try {
                    biyongNotificationEvent = true;
                    noComeIn = true;
                    swipe_run = false;
                    pendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            }
    /**
     * @return  true为亮屏，false为黑屏
     */
    public boolean isScreenLocked() {
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return Objects.requireNonNull(pm).isInteractive();
    }

    /*
   PARTIAL_WAKE_LOCK:保持CPU 运转，屏幕和键盘灯有可能是关闭的。
   SCREEN_DIM_WAKE_LOCK：保持CPU 运转，允许保持屏幕显示但有可能是灰的，允许关闭键盘灯
   SCREEN_BRIGHT_WAKE_LOCK：保持CPU 运转，允许保持屏幕高亮显示，允许关闭键盘灯
   FULL_WAKE_LOCK：保持CPU 运转，保持屏幕高亮显示，键盘灯也保持亮度
   */
    public void wakeUpAndUnlock()
    {
        //获取电源管理器对象，ACQUIRE_CAUSES_WAKEUP这个参数能从黑屏唤醒屏幕
        //获取电源管理器对象
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //得到键盘锁管理器对象
        km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (pm != null) {
            wl = pm.newWakeLock(SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,"biyongxposed:TAG");
        }
        wl.acquire(10*60*1000L /*10 minutes*/); // 点亮屏幕
        wl.release(); // 释放
        //初始化一个键盘锁管理器对象
        kl = Objects.requireNonNull(km).newKeyguardLock("unLock");
        //若在锁屏界面则解锁直接跳过锁屏
        if(km.inKeyguardRestrictedInputMode()) {
            kl.disableKeyguard();//解锁
        }
    }
    /**
     * 高版本：获取顶层的activity的包名
     * @ return
     */
    private String getHigherPackageName() {
        String packagename = "";
        if(Build.VERSION.SDK_INT >= 22) {
            UsageStatsManager usage = (UsageStatsManager)getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> stats = usage != null ? usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time) : null;
            if (stats != null) {
                SortedMap<Long, UsageStats> runningTask = new TreeMap<>();
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