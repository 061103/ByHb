package com.zhou.biyongxposed;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;
import java.util.Random;

import static android.content.ContentValues.TAG;
//adb shell dumpsys window | findstr mCurrentFocus查看包名的ADB命令
//org.telegram.btcchat:id/red_packet_message 恭喜发财吉祥如意的ID
//org.telegram.btcchat:id/red_packet_open_button 点击那个开的ID
//org.telegram.btcchat:id/unread_message_count  会话上面的那个小角标ID
//org.telegram.btcchat:id/cell_red_paket_status 红包的状态是否己领完
//org.telegram.btcchat:id/scroll_text    主页上BIYONG通知的ID
//org.telegram.btcchat:id/cell_red_paket_icon 一个隐藏的红包小标识
public class bingyongserver extends AccessibilityService {
    private boolean clickok,ScreenStatus=true;
    private boolean enableKeyguard;
    //锁屏、解锁相关
    private KeyguardManager km;
    private KeyguardManager.KeyguardLock kl;
    //唤醒屏幕相关
    private PowerManager pm;
    private PowerManager.WakeLock wl = null;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                CharSequence apkname=event.getPackageName();
                Log.i(TAG, "通知事件包名:" + apkname);
                ScreenStatus=isScreenLocked();
                Log.i(TAG, "屏幕状态:" + isScreenLocked());
                if(apkname.equals("org.telegram.btcchat")) {
                    Log.i(TAG, "包名正确巳进入通知准备点击:");
                    wakeAndUnlock(ScreenStatus);
                    //模拟打开通知栏消息
                    if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                                try {
                                    Notification notification = (Notification) event.getParcelableData();
                                    PendingIntent pendingIntent = notification.contentIntent;
                                    pendingIntent.send();
                                    sleepTime(1000);
                                } catch (PendingIntent.CanceledException e) {
                                    e.printStackTrace();
                                }
                            }
                            return;
                        }
                        break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                List<AccessibilityNodeInfo> gethongbao = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cell_red_paket_message");//找到红包
                List<AccessibilityNodeInfo> hongbaostatus = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cell_red_paket_status");//红包状态
                if (!gethongbao.isEmpty()) {
                        for (int i = gethongbao.size() - 1; i >= 0; i--) {
                            try {
                                if (hongbaostatus.get(i).getText().equals("领取红包") && (!gethongbao.get(i).getText().equals("答题红包"))) {
                                    Random rand = new Random();
                                    int random = rand.nextInt(300) + 200;
                                    sleepTime(random);
                                    gethongbao.get(i).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    Log.i(TAG, "点击了红包,等待事件变化！");
                                    clickok = true;
                                    sleepTime(100);
                                    return;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                if(clickok) {
                    List<AccessibilityNodeInfo> openhongbao = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/red_packet_open_button");
                    if (openhongbao != null) {
                        for (AccessibilityNodeInfo co : openhongbao) {
                            try {
                                if (co.isClickable()) {
                                    Log.i(TAG, "正在拆红包");
                                    Random rand = new Random();
                                    int random = rand.nextInt(300) + 200;
                                    sleepTime(random);
                                    co.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    return;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                try {
                    List<AccessibilityNodeInfo> hongbaojilu = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/rec_packet_history");//红包记录
                    List<AccessibilityNodeInfo> hidehongbao = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cell_red_paket_icon");//一个隐藏的红包小标识
                    if (!hongbaojilu.isEmpty()) {
                        Log.i(TAG, "找到了红包记录！");
                        Random rand = new Random();
                        int random = rand.nextInt(2000) + 1000;
                        sleepTime(random);
                        List<AccessibilityNodeInfo> go_back = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/go_back_button");
                        try {
                            if (!go_back.isEmpty()) {
                                for (AccessibilityNodeInfo back : go_back) {
                                    back.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    clickok=false;
                                    if(ScreenStatus){
                                        if(!hidehongbao.isEmpty()) {
                                            performBackClick();
                                            back2Home();
                                            wakeAndUnlock(false);
                                        }
                                    }
                                    ScreenStatus=false;
                                    Log.i(TAG, "返回上一页");
                                    return;
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
        }
    }
    /**
     * 系统是否在锁屏状态
     *
     * @return
     */
    private boolean isScreenLocked() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.inKeyguardRestrictedInputMode();
    }
    //唤醒屏幕和解锁
    @SuppressLint("InvalidWakeLockTag")
    private void wakeAndUnlock(boolean unLock)
    {
        if(unLock)//如果是在锁屏状态
        {
            //若为黑屏状态则唤醒屏幕
            if(!pm.isScreenOn()) {
                //获取电源管理器对象，ACQUIRE_CAUSES_WAKEUP这个参数能从黑屏唤醒屏幕
                wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "bright");
                //点亮屏幕
                wl.acquire(10000);
                Log.i("demo", "亮屏");
            }
            //若在锁屏界面则解锁直接跳过锁屏
            if(km.inKeyguardRestrictedInputMode()) {
                //设置解锁标志，以判断抢完红包能否锁屏
                enableKeyguard = false;
                //解锁
                kl.disableKeyguard();
                Log.i("demo", "解锁");
            }
        }
        else//不在锁屏状态
        {
            //如果之前解过锁则加锁以恢复原样
            if(!enableKeyguard) {
                //锁屏
                kl.reenableKeyguard();
                Log.i("demo", "加锁");
            }
            //若之前唤醒过屏幕则释放之使屏幕不保持常亮
            if(wl != null) {
                wl.release();
                wl = null;
                Log.i("demo", "关灯");
            }
        }
    }
    /**
     * 回到系统桌面
     */
    private void back2Home() {
        Intent home = new Intent(Intent.ACTION_MAIN);

        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        home.addCategory(Intent.CATEGORY_HOME);

        startActivity(home);
    }
    /**
     * 模拟返回操作
     */
    public void performBackClick() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        performGlobalAction(GLOBAL_ACTION_BACK);
    }
    /**
     * 延时MS
     */
    public void sleepTime(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**
     * 服务连接
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        //获取电源管理器对象
        pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
        //得到键盘锁管理器对象
        km= (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        //初始化一个键盘锁管理器对象
        kl = km.newKeyguardLock("unLock");
        Toast.makeText(this, "BiYong服务开启", Toast.LENGTH_SHORT).show();
        super.onServiceConnected();

    }
    /**
     * 必须重写的方法：系统要中断此service返回的响应时会调用。在整个生命周期会被调用多次。
     */
    @Override
    public void onInterrupt() {
        Toast.makeText(this, "我快被终结了啊-----", Toast.LENGTH_SHORT).show();
    }

    /**
     * 服务断开
     */
    @Override
    public boolean onUnbind(Intent intent) {
        wakeAndUnlock(false);
        Toast.makeText(this, "Biyong服务关闭", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }
}
