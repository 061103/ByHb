package com.zhou.biyongxposed;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotificationService extends NotificationListenerService {
    public String packageName;
    public String text;
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification mNotification = sbn.getNotification();
        if (mNotification != null) {
            packageName=sbn.getPackageName();//发送通知的包名
            text=mNotification.tickerText.toString();//通知内容
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // TODO 自动生成的方法存根

    }
}
