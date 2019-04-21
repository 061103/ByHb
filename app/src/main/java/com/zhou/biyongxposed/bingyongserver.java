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
public class bingyongserver extends AccessibilityService {
    private String LAUCHER = "org.telegram.ui.LaunchActivity";//主页面和群聊天页面是同一个
    //锁屏、解锁相关
    private KeyguardManager.KeyguardLock kl;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        String className = event.getClassName().toString();
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                List<AccessibilityNodeInfo> gethongbao = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cell_red_paket_message");//找到红包
                List<AccessibilityNodeInfo> hongbaostatus = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cell_red_paket_status");//红包状态
                if (!gethongbao.isEmpty()) {
                    for (int i = gethongbao.size() - 1; i >= 0; i--) {
                        try {
                            if (hongbaostatus.get(i).getText().equals("领取红包")) {
                                sleepTime(250);
                                gethongbao.get(i).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                Log.i(TAG, "点击了红包,等待事件变化！");
                                return;
                            } else Log.i(TAG, "状态：" + hongbaostatus.get(i).getText().toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                    List<AccessibilityNodeInfo> openhongbao = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/red_packet_open_button");
                    if (openhongbao != null) {
                        for (AccessibilityNodeInfo co : openhongbao) {
                            try {
                                if (co.isClickable()) {
                                    Log.i(TAG, "正在拆红包");
                                    sleepTime(1000);
                                    co.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    return;
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }

            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                List<AccessibilityNodeInfo> hongbaojilu = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/rec_packet_history");//红包记录
                if(!hongbaojilu.isEmpty()){
                    Log.i(TAG, "找到了红包记录！");
                    Random rand = new Random();
                    int random = rand.nextInt(2000) + 1000;
                    sleepTime(random);
                    List<AccessibilityNodeInfo> go_back = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/go_back_button");
                    if(!go_back.isEmpty()){
                        for(AccessibilityNodeInfo back:go_back){
                            back.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            Log.i(TAG, "返回上一页");
                        }
                    }
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
    private void wakeAndUnlock() {
        //获取电源管理器对象
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        //获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");

        //点亮屏幕
        wl.acquire(1000);

        //得到键盘锁管理器对象
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        kl = km.newKeyguardLock("unLock");

        //解锁
        kl.disableKeyguard();

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
     * 服务连接
     */
    @Override
    protected void onServiceConnected() {
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
        Toast.makeText(this, "Biyong红包服务关闭", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }
    /**
     * 延时MS
     */
    public void sleepTime(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
