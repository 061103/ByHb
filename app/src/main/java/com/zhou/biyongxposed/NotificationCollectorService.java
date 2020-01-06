package com.zhou.biyongxposed;

import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import static android.os.PowerManager.SCREEN_BRIGHT_WAKE_LOCK;
import static com.zhou.biyongxposed.bingyongserver.lightSleeper;
import static com.zhou.biyongxposed.bingyongserver.sleepTime;

public class NotificationCollectorService extends NotificationListenerService {
    private static final String TAG = "NotificationListener";
    private KeyguardManager.KeyguardLock kl;
    private PowerManager.WakeLock wl = null;
    public static boolean enableKeyguard;
    public static boolean biyongNotificationEvent;
    public static boolean noComeIn;
    public static boolean swipe_run;
    private PowerManager pm;
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn.getPackageName().contains("org.telegram.btcchat")) {
            Object  string = sbn.getNotification().extras.get("android.text");
            if(string!=null && string.toString().contains("下载BiYong") && !biyongNotificationEvent){
                Log.d(TAG, "获取到通知栏红包消息!");
                LogUtils.i("获取到通知栏红包消息!");
                Log.d(TAG, "群组:----"+sbn.getNotification().extras.get("android.title"));
                LogUtils.i("群组:----"+sbn.getNotification().extras.get("android.title"));
                if (!isScreenLocked()) {
                    wakeUpAndUnlock(false);
                    enableKeyguard=true;
                    Log.d(TAG, "唤醒屏幕!");
                    LogUtils.i("唤醒屏幕!");
                }
                PendingIntent pendingIntent = sbn.getNotification().contentIntent;
                try {
                    biyongNotificationEvent = true;
                    noComeIn = true;
                    swipe_run = false;
                    pendingIntent.send();
                    sleepTime(lightSleeper);
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    /**
     * 系统是否在锁屏状态
     *
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
    public void wakeUpAndUnlock(boolean screenOn)
    {
        if(!screenOn){//获取电源管理器对象，ACQUIRE_CAUSES_WAKEUP这个参数能从黑屏唤醒屏幕
            //获取电源管理器对象
            if (pm != null) {
                wl = pm.newWakeLock(SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,"com.zhou.biyongxposed:TAG");
            }
            wl.acquire(10*60*1000L /*10 minutes*/); // 点亮屏幕
            //得到键盘锁管理器对象
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            //初始化一个键盘锁管理器对象
            kl = Objects.requireNonNull(km).newKeyguardLock("unLock");
            //若在锁屏界面则解锁直接跳过锁屏
            if(km.inKeyguardRestrictedInputMode()) {
                kl.disableKeyguard();//解锁
            }
        } else {
            wl.release(); // 释放
            if(Build.VERSION.SDK_INT > 23){
                MainActivity.execShellCmd("input keyevent 223");
            }else {
                goToSleep(this);
            }
            kl.reenableKeyguard();
        }
    }
    /**
     *   反射关闭屏幕
     *
     */
    public static void goToSleep(Context context) {
        PowerManager powerManager= (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        try {
            powerManager.getClass().getMethod("goToSleep", long.class).invoke(powerManager, SystemClock.uptimeMillis());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
