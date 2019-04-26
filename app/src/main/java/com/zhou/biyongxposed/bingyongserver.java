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
import java.io.DataOutputStream;
import java.io.OutputStream;
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
//org.telegram.btcchat:id/close_button   你来晚了一步红包被抢完了的关闭ID
public class bingyongserver extends AccessibilityService {
    private boolean ScreenStatus,enableKeyguard;
    private boolean screenOn,AgainNotifi;
    private boolean Notifibiyong,chai_ok;
    private int x;
    //锁屏、解锁相关
    private KeyguardManager km;
    private KeyguardManager.KeyguardLock kl;
    //唤醒屏幕相关
    private PowerManager pm;
    private PowerManager.WakeLock wl=null;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                CharSequence apkname = event.getPackageName();
                        Log.i(TAG, "当前Notifibiyong的状态:" + Notifibiyong);
                        if (apkname!=null&&apkname.equals("org.telegram.btcchat")) {
                            AgainNotifi = true;
                            if (!Notifibiyong) {
                                ScreenStatus = isScreenLocked();
                                if (!isScreenLocked()) {
                                    wakeUpAndUnlock(false);
                                    sleepTime(2000);
                                }
                                x++;
                                Log.i(TAG, "屏幕状态:" + ScreenStatus);
                                if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                                    try {
                                        Notification notification = (Notification) event.getParcelableData();
                                        PendingIntent pendingIntent = notification.contentIntent;
                                        pendingIntent.send();
                                        AgainNotifi = false;
                                        return;
                                    } catch (PendingIntent.CanceledException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                before:
                    try {//org.telegram.btcchat:id/cell_red_paket_status 领取红包的标识 org.telegram.btcchat:id/cell_red_paket_message 恭喜发财的标识
                        List<AccessibilityNodeInfo> red_paket_status = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cell_red_paket_status");
                        List<AccessibilityNodeInfo> red_paket_message = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cell_red_paket_message");
                        if (!red_paket_message.isEmpty()) {
                            Notifibiyong=true;
                                try {
                                    for(int i=0;i<red_paket_status.size();i++){
                                        if (red_paket_status.get(0).getText().equals("领取红包")&&!red_paket_message.get(0).getText().equals("答题红包")) {
                                            Log.i(TAG, "红包数量:" + red_paket_message.size());
                                            Random rand = new Random();
                                            int random = rand.nextInt(100) + 200;
                                            sleepTime(random);
                                            red_paket_status.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                            Log.i(TAG, "找到并点击了红包");
                                            return;
                                        }
                                    }
                                } catch (Exception e){
                                    e.printStackTrace();
                                }
                            Log.i(TAG, "确实没有可领取的红包了，也许有答题红包，但目前不领取！以下三顶满足的话即锁屏。");
                            performBackClick();
                            sleepTime(1000);
                            Notifibiyong=false;
                            sleepTime(200);
                            if(x<=1){ x=1;ScreenStatus= true;
                            }else if(x>1) {x=2;ScreenStatus=false;}
                            Log.i(TAG, "X1值："+x);
                            switch (x){
                                case 1: if (ScreenStatus&&!AgainNotifi&&enableKeyguard) {
                                    Log.i(TAG, "ScreenStatus状态:"+ScreenStatus);
                                    Log.i(TAG, "AgainNotifi状态:"+AgainNotifi);
                                    Log.i(TAG, "enableKeyguard状态:"+enableKeyguard);
                                    x=0;
                                    back2Home();
                                    wakeUpAndUnlock(true);
                                    enableKeyguard = false;
                                    sleepTime(2000);
                                    Notifibiyong=false;
                                    Log.i(TAG, "锁屏后Notifibiyong状态:"+Notifibiyong);
                                }
                                case 2: if (!ScreenStatus&&!AgainNotifi&&enableKeyguard) {
                                    Log.i(TAG, "ScreenStatus状态:"+ScreenStatus);
                                    Log.i(TAG, "AgainNotifi状态:"+AgainNotifi);
                                    Log.i(TAG, "enableKeyguard状态:"+enableKeyguard);
                                    x=0;
                                    back2Home();
                                    wakeUpAndUnlock(true);
                                    enableKeyguard = false;
                                    sleepTime(2000);
                                    Notifibiyong=false;
                                    Log.i(TAG, "锁屏后Notifibiyong状态:"+Notifibiyong);
                                }
                            }
                        }else {
                            break before;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try{
                        List<AccessibilityNodeInfo> openhongbao = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/red_packet_open_button");
                            if (!openhongbao.isEmpty()) {
                                for (AccessibilityNodeInfo co : openhongbao) {
                                    try {
                                        if (co.isClickable()) {
                                            Log.i(TAG, "正在拆红包");
                                            Random rand = new Random();
                                            int random = rand.nextInt(200) + 500;
                                            sleepTime(random);
                                            co.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                            chai_ok=true;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {//此处为异常信息的弹出窗口
                            List<AccessibilityNodeInfo> hongbao_error = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/red_packet_message_error");
                            if (!hongbao_error.isEmpty()) {
                                Log.i(TAG, "异常信息：" + hongbao_error.get(0).getText());
                                sleepTime(1000);
                                if (hongbao_error.get(0).getText().equals("您来晚一步，红包已被抢完")) {
                                    inputClick("org.telegram.btcchat:id/close_button");
                                    Log.i(TAG, "巳关闭此处异常！");
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                try {//此处为处理暂无信息的界面
                    List<AccessibilityNodeInfo> hongbao_no_message = rootNode.findAccessibilityNodeInfosByText("暂无消息...");
                    if (!hongbao_no_message.isEmpty()) {
                        Log.i(TAG, "异常信息：" + hongbao_no_message.get(0).getText()+"窗口信息没有刷新出来！");
                        sleepTime(1000);
                        performBackClick();
                        sleepTime(1000);
                        Notifibiyong=false;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                    try {
                        if(chai_ok) {
                            //org.telegram.btcchat:id/sender_name  红包发送者的名字
                            //org.telegram.btcchat:id/received_coin_count 红包的金额
                            //org.telegram.btcchat:id/received_coin_unit  红包的类型
                            List<AccessibilityNodeInfo> hongbaojilu = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/rec_packet_history");//红包记录
                            if (!hongbaojilu.isEmpty()) {
                                Log.i(TAG, "进入红包记录页面");
                                Random rand = new Random();
                                int random = rand.nextInt(1000) + 2500;
                                Log.i(TAG, "随机数:" + random);
                                sleepTime(random);
                                List<AccessibilityNodeInfo> hongbaosender_name = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/sender_name");
                                List<AccessibilityNodeInfo> hongbao_unit = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/received_coin_unit");
                                List<AccessibilityNodeInfo> hongbao_count = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/received_coin_count");
                                if (!hongbaosender_name.isEmpty() && !hongbao_unit.isEmpty() && !hongbao_count.isEmpty()) {
                                    Log.i(TAG, "发送红包者的名字:" + hongbaosender_name.get(0).getText() + "红包类型:" + hongbao_unit.get(0).getText() + "红包金额:" + hongbao_count.get(0).getText());
                                }
                                List<AccessibilityNodeInfo> go_back = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/go_back_button");
                                try {
                                    if (!go_back.isEmpty()) {
                                        for (AccessibilityNodeInfo back : go_back) {
                                            back.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                            chai_ok=false;
                                            Log.i(TAG, "返回上一页");
                                            break;
                                        }

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
    }
    /**
     * 系统是否在锁屏状态
     *
     * @return
     */
    private boolean isScreenLocked() {
        pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        return screenOn = pm.isScreenOn();
    }
    //唤醒屏幕和解锁
    @SuppressLint("InvalidWakeLockTag")
    private void wakeUpAndUnlock(boolean screenOn)
    {
        if(!screenOn){//获取电源管理器对象，ACQUIRE_CAUSES_WAKEUP这个参数能从黑屏唤醒屏幕
            wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "bright");
            wl.acquire(10000);
            wl.release();
            enableKeyguard=true;
            Log.i("demo", "亮屏");
            //若在锁屏界面则解锁直接跳过锁屏
            if(km.inKeyguardRestrictedInputMode()) {
                kl.disableKeyguard();//解锁
                Log.i("demo", "解锁");
            }
        } else {
            execShellCmd("input keyevent " + 223 );
            kl.reenableKeyguard();
            Log.i("demo", "息屏");
        }
    }
    /*
     * 全局滑动操作
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     */
    public static void perforGlobalSwipe(int x0, int y0, int x1, int y1) {
        execShellCmd("input swipe " + x0 + " " + y0 + " " + x1 + " " + y1);
    }
    /**
     * 执行shell命令
     *
     execShellCmd("input tap 168 252");点击某坐标
     execShellCmd("input swipe 100 250 200 280"); 滑动坐标
     */
    public static void execShellCmd(String cmd) {
        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            Process process = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    /**
     * 根据id,获取AccessibilityNodeInfo，并点击。
     */
    private void inputClick(String clickId) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo
                    .findAccessibilityNodeInfosByViewId(clickId);
            for (AccessibilityNodeInfo item : list) {
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
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
     * 服务连接
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        checkRoot rootcheck= new checkRoot();
        //获取电源管理器对象
        pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
        //得到键盘锁管理器对象
        km= (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        //初始化一个键盘锁管理器对象
        kl = km.newKeyguardLock("unLock");
        if (rootcheck.isDeviceRooted()){
            Toast.makeText(this, "你的设备可以获取ROOT|允许自动处理下滑|BiYong红包服务开启", Toast.LENGTH_SHORT).show();
        }else Toast.makeText(this, "你的设备不能获取ROOT权限|部分功能无法使用或导致程序异常|BiYong红包服务开启", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, "Biyong服务关闭", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }
}
