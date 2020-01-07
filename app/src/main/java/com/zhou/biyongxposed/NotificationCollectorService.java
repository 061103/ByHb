package com.zhou.biyongxposed;

import android.app.PendingIntent;
import android.content.Context;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import java.util.Objects;

import static com.zhou.biyongxposed.bingyongserver.lightSleeper;
import static com.zhou.biyongxposed.bingyongserver.sleepTime;

public class NotificationCollectorService extends NotificationListenerService {
    private static final String TAG = "NotificationListener";
    public static boolean enableKeyguard;
    public static boolean biyongNotificationEvent;
    public static boolean noComeIn;
    public static boolean swipe_run;
    public static PowerManager pm;

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
                    bingyongserver bingyongserver = new bingyongserver();
                    bingyongserver.wakeUpAndUnlock(false);
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
}
