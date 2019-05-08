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
import java.util.Objects;
import java.util.Random;


import static android.os.PowerManager.SCREEN_BRIGHT_WAKE_LOCK;

//adb shell dumpsys window | findstr mCurrentFocus查看包名的ADB命令
//org.telegram.btcchat:id/red_packet_message 恭喜发财吉祥如意的ID
//org.telegram.btcchat:id/red_packet_open_button 点击那个开的ID
//org.telegram.btcchat:id/unread_message_count  会话上面的那个小角标ID
//org.telegram.btcchat:id/cell_red_paket_status 红包的状态是否己领完
//org.telegram.btcchat:id/scroll_text    主页上BIYONG通知的ID
//org.telegram.btcchat:id/cell_red_paket_icon 一个隐藏的红包小标识
//org.telegram.btcchat:id/close_button   你来晚了一步红包被抢完了的关闭ID
//org.telegram.btcchat:id/buy_and_sell_tab_text  聊天页面的聊天两字ID
//org.telegram.btcchat:id/tv_question   答题红包的选择题
//org.telegram.btcchat:id/tv_sender_name 这是谁的答题红包，不为空代表出现答题红包
public class bingyongserver extends AccessibilityService {
    private boolean enableKeyguard;
    private boolean Notifibiyong = false;
    private boolean answer_error;
    //锁屏、解锁相关
    private KeyguardManager km;
    private KeyguardManager.KeyguardLock kl;
    //唤醒屏幕相关
    private PowerManager pm;
    private PowerManager.WakeLock wl = null;

    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        CharSequence apkname = event.getPackageName();
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                try {
                    Notification notification = (Notification) event.getParcelableData();
                    Object notificationtitle = notification.extras.get(Notification.EXTRA_TITLE);
                    Object notificationText = notification.extras.get(Notification.EXTRA_TEXT);
                    if (notificationtitle == null || notificationText == null) {
                        return;
                    }
                    if (apkname.equals("org.telegram.btcchat")) {
                        boolean screenStatus = isScreenLocked();
                        if (!Notifibiyong) {
                            if (!screenStatus) {
                                wakeUpAndUnlock(false);
                            }
                            if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                                try {
                                    PendingIntent pendingIntent = notification.contentIntent;
                                    pendingIntent.send();
                                    Notifibiyong = true;
                                    return;
                                } catch (PendingIntent.CanceledException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                /*
                 * 窗口内容改变， 不同的事件走不同的处理方法
                 * */
                if (Notifibiyong) {
                    try {
                        List<AccessibilityNodeInfo> tab_text = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/buy_and_sell_tab_text");
                        if (!tab_text.isEmpty()) {
                            List<AccessibilityNodeInfo> red_paket_status = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cell_red_paket_status");
                            if (!red_paket_status.isEmpty()) {
                                LogUtils.i("进入聊天页面,寻找可点击的红包");
                                for (int i = 0; i < red_paket_status.size(); i++) {
                                    if (red_paket_status.get(i).getText().equals("领取红包")) {
                                        Random rand = new Random();
                                        int random = rand.nextInt(100) + 100;
                                        sleepTime(random);
                                        LogUtils.i("发现有未领取的红包,点击红包");
                                        red_paket_status.get(i).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        return;
                                    }
                                }
                                LogUtils.i("聊天页面没有红包了");
                                performBackClick();
                                sleepTime(300);
                                if (enableKeyguard) {
                                        lockScreen();
                                        return;
                                    } else {
                                            back2Home();
                                            Notifibiyong = false;
                                            return;
                                }
                            } else {/*
                             * 此处为处理聊天页面为空的情况下
                             * */
                                List<AccessibilityNodeInfo> buy_and_sell = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/buy_and_sell_tab_text");
                                if (!buy_and_sell.isEmpty()) {
                                    sleepTime(500);
                                    performBackClick();
                                    LogUtils.i("聊天页面为空，返回");
                                    sleepTime(300);
                                    if (enableKeyguard) {
                                        lockScreen();
                                        return;
                                    } else {
                                        back2Home();
                                        Notifibiyong = false;
                                        return;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    /*
                     * 此处为开红包按钮的开字
                     * org.telegram.btcchat:id/red_packet_open_button
                     * */
                    try {
                        List<AccessibilityNodeInfo> openhongbao = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/red_packet_open_button");
                        if (!openhongbao.isEmpty()) {
                            for (AccessibilityNodeInfo co : openhongbao) {
                                try {
                                    if (co.isClickable()) {
                                        Random rand = new Random();
                                        int random = rand.nextInt(100) + 200;
                                        sleepTime(random);
                                        LogUtils.i("拆红包");
                                        co.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        sleepTime(random);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                /*
                org.telegram.btcchat:id/sender_name  红包发送者的名字
                org.telegram.btcchat:id/received_coin_count 红包的金额
                org.telegram.btcchat:id/received_coin_unit  红包的类型
                * */
                    try {
                        List<AccessibilityNodeInfo> hongbaojilu = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/rec_packet_history");//红包记录
                        if (!hongbaojilu.isEmpty()) {
                            Random rand = new Random();
                            int random = rand.nextInt(500) + 1500;
                            sleepTime(random);
                            List<AccessibilityNodeInfo> sender_name = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/sender_name");
                            List<AccessibilityNodeInfo> received_coin_unit = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/received_coin_unit");
                            List<AccessibilityNodeInfo> received_coin_count = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/received_coin_count");
                            if (!sender_name.isEmpty() && !received_coin_unit.isEmpty() && !received_coin_count.isEmpty()) {
                                LogUtils.i("领取:" + sender_name.get(0).getText() + ":类型:" + received_coin_unit.get(0).getText() + "金额:" + received_coin_count.get(0).getText());
                            }
                            List<AccessibilityNodeInfo> go_back = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/go_back_button");
                            try {
                                if (!go_back.isEmpty()) {
                                    for (AccessibilityNodeInfo back : go_back) {
                                        back.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        LogUtils.i("领取完成,返回");
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    /*
                     * 此处为答题红包的页面，无法知到答案，只有随机选择
                     * //org.telegram.btcchat:id/cb_checked  答题红包的选择题checkBox ID
                     *
                     * */
                    try {
                        List<AccessibilityNodeInfo> cb_checked = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cb_checked");
                        sleepTime(500);
                        if (!cb_checked.isEmpty()) {
                            if(!answer_error) {
                                LogUtils.i("进入答题红包页面");
                                sleepTime(500);
                                LogUtils.i("找到答题红包提问数量:" + cb_checked.size());
                                Random rand = new Random();
                                int random = rand.nextInt(cb_checked.size()) + 1;
                                LogUtils.i("随机点击题目：" + random);
                                sleepTime(500);
                                cb_checked.get(random - 1).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                LogUtils.i("完成题目的选择并点击");
                                sleepTime(1000);
                                List<AccessibilityNodeInfo> get_red_packet = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/tv_get_red_packet");
                                if (!get_red_packet.isEmpty() && get_red_packet.get(0).getText().equals("领取")) {
                                    LogUtils.i("找到领取按钮，准备点击");
                                    for (AccessibilityNodeInfo get : get_red_packet) {
                                        sleepTime(1000);
                                        get.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        LogUtils.i("成功点击领取，等待下一步事件产生");
                                    }
                                }
                            }else {sleepTime(500); answer_error = false;performBackClick();}
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    /*
                     * 此处为答题红包回答错误的页面
                     * org.telegram.btcchat:id/red_packet_message_error  很遗憾-回答错误的ID
                     * org.telegram.btcchat:id/close_button 错误页面的关闭ID
                     * */
                    try {
                        List<AccessibilityNodeInfo> message_error = rootNode.findAccessibilityNodeInfosByText("很遗憾-回答错误");
                        if (!message_error.isEmpty()) {
                            LogUtils.i("异常信息：" + message_error.get(0).getText());
                            //org.telegram.btcchat:id/cb_checked  答题红包的选择题
                            List<AccessibilityNodeInfo> close_button = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/close_button");
                            if (!close_button.isEmpty()) {
                                for (AccessibilityNodeInfo cl : close_button) {
                                    sleepTime(500);
                                    answer_error = true;
                                    cl.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    LogUtils.i("回答错误，点击了关闭按钮");

                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    /*
                     * 此处为答题红包回答错误的另一个页面
                     * org.telegram.btcchat:id/red_packet_indicator   上面图片资源的ID
                     * org.telegram.btcchat:id/red_packet_detail_close 错误页面的关闭ID
                     * */
                    try {
                        List<AccessibilityNodeInfo> red_packet_indicator = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/red_packet_indicator");
                        if (!red_packet_indicator.isEmpty()) {
                            //org.telegram.btcchat:id/cb_checked  答题红包的选择题
                            List<AccessibilityNodeInfo> red_packet_detail_close = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/red_packet_detail_close");
                            if (!red_packet_detail_close.isEmpty()) {
                                for (AccessibilityNodeInfo cl : red_packet_detail_close) {
                                    sleepTime(500);
                                    answer_error = true;
                                    cl.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    LogUtils.i("回答错误，点击了关闭按钮");
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    /*
                     * 此处为处理答题红包网络错误
                     */
                    try {
                        List<AccessibilityNodeInfo> iv_back_button = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/iv_back_button");
                        List<AccessibilityNodeInfo> cbd_checked = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cb_checked");
                        if (!iv_back_button.isEmpty()&&cbd_checked.isEmpty()) {
                            sleepTime(500);
                            performBackClick();
                            LogUtils.i("异常信息：答题红包没有加载出来");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    /*
                     * 您来晚一步，红包已被抢完
                     */
                    try {
                        List<AccessibilityNodeInfo> hongbao_error = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/red_packet_message_error");
                        if (!hongbao_error.isEmpty()) {
                            LogUtils.i("异常信息：" + hongbao_error.get(0).getText());
                            sleepTime(500);
                            if (hongbao_error.get(0).getText().equals("您来晚一步，红包已被抢完")) {
                                inputClick();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    /*
                     * 此处为处理暂无信息的界面
                     */
                    try {
                        List<AccessibilityNodeInfo> hongbao_no_message = rootNode.findAccessibilityNodeInfosByText("暂无消息...");
                        if (!hongbao_no_message.isEmpty()) {
                            LogUtils.i("异常信息：" + hongbao_no_message.get(0).getText() + "窗口信息没有刷新出来！");
                            sleepTime(200);
                            performBackClick();
                            sleepTime(200);
                            Notifibiyong = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    /*
                     * 此处为处理BiYong崩溃的界面
                     */
                    try {
                        List<AccessibilityNodeInfo> button2 = rootNode.findAccessibilityNodeInfosByViewId("android:id/button2");
                        if (!button2.isEmpty()) {
                            LogUtils.i("异常信息：BiYong意外退出！");
                            if (button2.get(0).getText().equals("永不发送")) {
                                sleepTime(1000);
                                button2.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                Notifibiyong = false;
                                sleepTime(1000);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                break;
            case AccessibilityEvent.TYPE_ASSIST_READING_CONTEXT:
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                break;
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END:
                break;
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
                break;
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
                break;
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED:
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                break;
            case AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED:
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                break;
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                break;
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY:
                break;
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                break;
        }
    }

    /**
     * 根据系统之前的状态执行的操作
     */
    private void lockScreen(){
            back2Home();
            wakeUpAndUnlock(true);
            enableKeyguard = false;
            sleepTime(1500);
            Notifibiyong = false;
    }
    /**
     * 系统是否在锁屏状态
     *
     * @return  true为亮屏，false为黑屏
     */
    private boolean isScreenLocked() {
            pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return Objects.requireNonNull(pm).isScreenOn();
    }

    //唤醒屏幕和解锁
    @SuppressLint("InvalidWakeLockTag")
    private void wakeUpAndUnlock(boolean screenOn)
    {
        if(!screenOn){//获取电源管理器对象，ACQUIRE_CAUSES_WAKEUP这个参数能从黑屏唤醒屏幕
            wl = pm.newWakeLock(SCREEN_BRIGHT_WAKE_LOCK| PowerManager.ACQUIRE_CAUSES_WAKEUP, "bright");
            wl.setReferenceCounted(false);
            wl.acquire(1000);
            enableKeyguard=true;
            //若在锁屏界面则解锁直接跳过锁屏
            if(km.inKeyguardRestrictedInputMode()) {
                kl.disableKeyguard();//解锁
            }
        } else {
            execShellCmd("input keyevent " + 223 );
            wl.release();
            kl.reenableKeyguard();
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
    private void inputClick() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/close_button");
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
    @SuppressLint("SdCardPath")
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        checkRoot rootcheck= new checkRoot();
        LogUtils.init("/sdcard/LogUtils","/biyongdebuglog.log");
        //获取电源管理器对象
        pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
        //得到键盘锁管理器对象
        km= (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        //初始化一个键盘锁管理器对象
        kl = Objects.requireNonNull(km).newKeyguardLock("unLock");
        if (rootcheck.isDeviceRooted()){
            Toast.makeText(this, "你的设备巳获取ROOT|可以执行ADB指令|BiYong红包服务开启", Toast.LENGTH_SHORT).show();
        }else Toast.makeText(this, "你的设备没有获取ROOT权限|可以导致通知栏消息无法过滤|BiYong红包服务开启", Toast.LENGTH_SHORT).show();
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
