package com.zhou.biyongxposed;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationService extends NotificationListenerService {
    public String notificationPkg;
    public String notificationTitle;
    public String notificationText;
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // TODO Auto-generated method stub
        Bundle extras = sbn.getNotification().extras;
        // 获取接收消息APP的包名
        notificationPkg = sbn.getPackageName();
        // 获取接收消息的抬头
        notificationTitle = extras.getString(Notification.EXTRA_TITLE);
        // 获取接收消息的内容
        notificationText = extras.getString(Notification.EXTRA_TEXT);
        Log.i("XSL_Test", "Notification posted " + notificationTitle + " & " + notificationText);
    }
    public String getNotifitionPkg() {
        if (notificationPkg != null) {
            return notificationPkg;
        }
        return null;
    }
    public String getNotifitionTxt() {
        if (notificationText != null) {
            return notificationText;
        }
        return null;
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // TODO Auto-generated method stub
        Bundle extras = sbn.getNotification().extras;
        // 获取接收消息APP的包名
        String notificationPkg = sbn.getPackageName();
        // 获取接收消息的抬头
        String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
        // 获取接收消息的内容
        String notificationText = extras.getString(Notification.EXTRA_TEXT);
        Log.i("XSL_Test", "Notification removed " + notificationTitle + " & " + notificationText);
    }
    @Override
    public void onListenerConnected(){

    }
}
