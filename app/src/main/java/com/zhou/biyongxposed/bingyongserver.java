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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static android.os.PowerManager.SCREEN_DIM_WAKE_LOCK;
import static com.zhou.biyongxposed.MainActivity.youxianlist;
/*
PARTIAL_WAKE_LOCK:保持CPU 运转，屏幕和键盘灯有可能是关闭的。

SCREEN_DIM_WAKE_LOCK：保持CPU 运转，允许保持屏幕显示但有可能是灰的，允许关闭键盘灯

SCREEN_BRIGHT_WAKE_LOCK：保持CPU 运转，允许保持屏幕高亮显示，允许关闭键盘灯

FULL_WAKE_LOCK：保持CPU 运转，保持屏幕高亮显示，键盘灯也保持亮度
*/


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
    private boolean nohongbao;
    private boolean slk;
    private boolean shoudong=false;
    private int findSleeper;
    private int flishSleeper;
    private int lightSleeper;
    private DatabaseHandler dbhandler;
    private AccessibilityNodeInfo [] findRedPacketSender;
    private KeyguardManager.KeyguardLock kl;
    //唤醒屏幕相关
    private PowerManager pm;
    private PowerManager.WakeLock wl = null;
    private boolean gethongbao;
    private AccessibilityNodeInfo rootNode;
    private boolean have;
    private boolean nocomein;

    @SuppressLint({"SwitchIntDef", "WakelockTimeout"})
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!EventBus.getDefault().isRegistered(this)) {//加上判断
            LogUtils.i("EventBus:没有注册,正在注册!");
            EventBus.getDefault().register(this);
            LogUtils.i("EventBus:注册成功!");
        }
        int eventType = event.getEventType();
        rootNode = getRootInActiveWindow();
        CharSequence apkname = event.getPackageName();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                try {
                    if (!Notifibiyong&&!shoudong) {
                        if (apkname.equals("org.telegram.btcchat")) {
                            boolean screenStatus = isScreenLocked();
                            if (!Notifibiyong) {
                                if (!screenStatus) {
                                    wakeUpAndUnlock(false);
                                }
                                if (lightSleeper > 200) {
                                    sleepTime(lightSleeper);
                                } else sleepTime(200);
                            }
                        }
                        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                            try {
                                Notification notification = (Notification) event.getParcelableData();
                                PendingIntent pendingIntent = notification.contentIntent;
                                pendingIntent.send();
                                Notifibiyong = true;
                                return;
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
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
                if (Notifibiyong && !shoudong) {
                    try {
                        if (!nocomein) {
                            slk = false;
                                List<AccessibilityNodeInfo> red_paket_status = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cell_red_paket_status");
                                List<AccessibilityNodeInfo> red_paket_sender = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cell_red_paket_sender");
                                findRedPacketSender = new AccessibilityNodeInfo[red_paket_status.size()];
                                if (!red_paket_status.isEmpty()) {
                                    sleepTime(findSleeper);
                                    Log.i("Biyong", "发现红包延时:" + findSleeper);
                                    for (int i = 0; i < red_paket_status.size(); i++) {
                                        if (red_paket_status.get(i).getText().equals("领取红包")) {
                                            have = true;
                                            findRedPacketSender[i] = red_paket_sender.get(i);
                                            Log.i("Biyong:", "发现红包数量:" + red_paket_status.size() + "个,第" + (i + 1) + "个红包类型为:" + findRedPacketSender[i].getText());
                                            LogUtils.i("发现:" + red_paket_status.size() + "个红包" + "第" + (i + 1) + "个红包为:" + findRedPacketSender[i].getText());
                                        }
                                    }
                                    findhongbao();//找最优红包
                                    if (!slk) {
                                        performBackClick();
                                        sleepTime(100);
                                        if (enableKeyguard) {
                                            lockScreen();
                                            return;
                                        } else {
                                            back2Home();
                                            Notifibiyong = false;
                                        }
                                    }
                                } else {/*
                                 * 此处为处理聊天页面为空的情况下
                                 * */
                                    List<AccessibilityNodeInfo> buy_and_sell = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/buy_and_sell_tab_text");
                                    if (!buy_and_sell.isEmpty()) {
                                        performBackClick();
                                        if (enableKeyguard) {
                                            lockScreen();
                                            return;
                                        } else {
                                            back2Home();
                                            Notifibiyong = false;
                                        }
                                    }
                                }
                            }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    openClickdhongbao();//点击红包上的开按钮
                    gethongbaofinsh();//红包领取完成获取相关信息存入数据库
                    /*
                     * 此处为答题红包的页面，无法知到答案，只有随机选择
                     * //org.telegram.btcchat:id/cb_checked  答题红包的选择题checkBox ID
                     *
                     * */
                    try {
                            List<AccessibilityNodeInfo> cb_checked = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cb_checked");
                            sleepTime(500);
                            if (!cb_checked.isEmpty()) {
                                if (!answer_error) {
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
                                }
                            }
                    }catch (Exception e) {
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
                    }catch (Exception e) {
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
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    /*
                     * 此处为处理答题红包网络错误
                     */
                    try {
                            List<AccessibilityNodeInfo> iv_back_button = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/iv_back_button");
                            List<AccessibilityNodeInfo> cbd_checked = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cb_checked");
                            if (!iv_back_button.isEmpty() && cbd_checked.isEmpty() || nohongbao || answer_error) {
                                sleepTime(500);
                                nohongbao = false;
                                answer_error = false;
                                performBackClick();
                                LogUtils.i("异常信息：答题红包没有加载出来");
                            }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    gethongbaoerror();

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
                    }catch (Exception e) {
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
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    /*
                     * 此处为处理答题红包没出来转圈圈的界面
                     */
                    try {
                            List<AccessibilityNodeInfo> progress = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/progress");
                            if (!progress.isEmpty()) {
                                sleepTime(1000);
                                performBackClick();
                                LogUtils.i("异常信息：答题红包没出来转圈圈！第一次返回");
                                if (!progress.isEmpty()) {
                                    performBackClick();
                                    LogUtils.i("异常信息：答题红包没出来转圈圈！第二次返回");
                                }
                            }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                /*
                 * 从此处开始通知栏没有收到消息须手动进群抢红包:手动模式
                 * */
                if (!Notifibiyong&&shoudong) {
                    randomOnclick();//手动模式的第一个方法随便找一个红包点击
                    openClickdhongbao();//点击红包上的开按钮
                    gethongbaoerror();//领取红包出现错误
                    gethongbaofinsh();//红包领取完成获取相关信息存入数据库
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                sleepTime(250);
                openClickdhongbao();//点击红包上的开按钮
                break;
        }
    }

    private void randomOnclick() {
        try {
            /*
             * 此处为手机模式查找关键字
             * org.telegram.btcchat:id/cell_red_paket_status
             * */
                List<AccessibilityNodeInfo> notifinotion_off_red_paket_status = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cell_red_paket_status");
                if (!notifinotion_off_red_paket_status.isEmpty()) {
                    for (int i = 0; i < notifinotion_off_red_paket_status.size(); i++) {
                        if (notifinotion_off_red_paket_status.get(i).getText().equals("领取红包")) {
                            sleepTime(findSleeper);
                            notifinotion_off_red_paket_status.get(i).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            LogUtils.i("点击红包");
                            sleepTime(200);
                        }
                    }
                }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void openClickdhongbao() {
        /*
         * 此处为开红包按钮的开字
         * org.telegram.btcchat:id/red_packet_open_button
         * */
        try {
                List<AccessibilityNodeInfo> openhongbao = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/red_packet_open_button");
                if (!openhongbao.isEmpty()) {
                    for (AccessibilityNodeInfo co : openhongbao) {
                        co.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        LogUtils.i("拆红包");
                    }
                }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void gethongbaofinsh() {
        /*
                org.telegram.btcchat:id/sender_name  红包发送者的名字
                org.telegram.btcchat:id/received_coin_count 红包的金额
                org.telegram.btcchat:id/received_coin_unit  红包的类型
                * */
        try {
                List<AccessibilityNodeInfo> hongbaojilu = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/title_bar");//红包完成页面的标题栏
                if (!hongbaojilu.isEmpty() && !gethongbao) {
                    Random rand = new Random();
                    int random = rand.nextInt(500) + 700;
                    if (flishSleeper > 1200) {
                        sleepTime(flishSleeper);
                    } else sleepTime(random);
                    LogUtils.i("领取等待延时:" + random);
                    List<AccessibilityNodeInfo> sender_name = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/sender_name");
                    List<AccessibilityNodeInfo> received_coin_unit = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/received_coin_unit");
                    List<AccessibilityNodeInfo> received_coin_count = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/received_coin_count");
                    if (!sender_name.isEmpty() && !received_coin_unit.isEmpty() && !received_coin_count.isEmpty()) {
                        LogUtils.i("领取:" + sender_name.get(0).getText() + ":类型:" + received_coin_unit.get(0).getText() + "金额:" + received_coin_count.get(0).getText());
                        String coin_unit = (String) received_coin_unit.get(0).getText();//类型
                        double coin_count = Double.parseDouble((String) received_coin_count.get(0).getText());//数量
                        BigDecimal nowcoin = new BigDecimal(coin_count);
                        for (int i = 1; i <= dbhandler.getelementCounts(); i++) {
                            Eventvalue Result = dbhandler.getIdResult(String.valueOf(i));
                            if (Result.getName().contains(coin_unit)) {
                                gethongbao = true;
                                if (Result.getValue() == 1) {
                                    Log.i("biyongzhou", "在第<" + i + ">个找到符合条件的类型:" + coin_unit);
                                    BigDecimal coin_DB = new BigDecimal(Double.valueOf(Result.getCoincount()));
                                    Log.i("biyongzhou", "该类型之前的数据是:" + coin_DB);
                                    BigDecimal coin_result = coin_DB.add(nowcoin);
                                    Log.i("biyongzhou", "与新值相加后的数据是:" + coin_result);
                                    BigDecimal setScale = coin_result.setScale(2, RoundingMode.HALF_UP);
                                    Log.i("biyongzhou", "最少保留两个有效数字的结果是:" + setScale);
                                    Eventvalue eventvalue = new Eventvalue(i, coin_unit, 1, String.valueOf(setScale));
                                    dbhandler.addValue(eventvalue);
                                    Log.i("biyongzhou", "成功将数据写入数据库");
                                    return;
                                }
                            }
                        }
                        Eventvalue eventvalue = new Eventvalue(null, coin_unit, 1, String.valueOf(coin_count));
                        dbhandler.addValue(eventvalue);
                        Log.i("biyongzhou", "数据库无相关信息，将创建新值");

                    }
                }
                List<AccessibilityNodeInfo> go_back = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/go_back_button");
                    try {
                        if (!go_back.isEmpty() || gethongbao) {
                            for (AccessibilityNodeInfo back : go_back) {
                                back.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                gethongbao = false;
                                nocomein = false;
                                LogUtils.i("领取完成,返回");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void gethongbaoerror() {
        /*
         * 您来晚一步，红包已被抢完||该红包巳超过24小时
         */
        try {
                List<AccessibilityNodeInfo> hongbao_error = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/red_packet_message_error");
                if (!hongbao_error.isEmpty()) {
                    LogUtils.i("异常信息：" + hongbao_error.get(0).getText());
                    if (hongbao_error.get(0).getText().equals("您来晚一步，红包已被抢完") || hongbao_error.get(0).getText().equals("该红包已超过24小时，如果已领取可在领取记录中查看")) {
                        sleepTime(100);
                        inputClick();
                    }
                }
            } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 优先找最有价值的红包
     */
    private void findhongbao (){
            Log.i("Biyong:", "准备进入循环遍历优先红包,共有:"+youxianlist.size()+"种类型." );
            if(have) {
                have=false;
                for (int a = 0; a < youxianlist.size(); a++) {
                    Log.i("Biyong:", "准备遍历第" + (a + 1) + "种红包类型");
                    for (int b = 0; b < findRedPacketSender.length; b++) {
                        Log.i("Biyong:", "当前正在检测是否包含:" + youxianlist.get(a));
                        if (findRedPacketSender[b].toString().contains(youxianlist.get(a))) {
                            Log.i("Biyong", "巳确定包含:" + youxianlist.get(a) + " 准备点击");
                            findRedPacketSender[b].getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            slk = true;
                            nocomein=true;
                            sleepTime(200);
                            Log.i("Biyong", "点击完成");
                            LogUtils.i("点击最优红包" + findRedPacketSender[b].toString() + "完成");
                            return;
                        }
                    }
                }
                Log.i("Biyong","在优化列表没有找到该币种");
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
    @SuppressLint({"InvalidWakeLockTag", "WakelockTimeout"})
    private void wakeUpAndUnlock(boolean screenOn)
    {
        if(!screenOn){//获取电源管理器对象，ACQUIRE_CAUSES_WAKEUP这个参数能从黑屏唤醒屏幕
            //获取电源管理器对象
            if (pm != null) {
                wl = pm.newWakeLock(SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "bright");
            }
            wl.acquire();
            enableKeyguard=true;
            //得到键盘锁管理器对象
            //锁屏、解锁相关
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            //初始化一个键盘锁管理器对象
            kl = Objects.requireNonNull(km).newKeyguardLock("unLock");
            //若在锁屏界面则解锁直接跳过锁屏
            if(km.inKeyguardRestrictedInputMode()) {
                kl.disableKeyguard();//解锁
            }
        } else {
            execShellCmd("input keyevent " + 223 );
            kl.reenableKeyguard();
        }
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
    /*
     *  新版本需要手动的添加注解@Subscribe(这是必不可少的)
     */
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void IntegerEvent(Message<Integer> msg){
        if(msg.getType() == 0) {
            findSleeper = msg.getData();
            final Eventvalue findResult = dbhandler.getNameResult("findSleeper");
            if(findResult!=null) {
                Eventvalue eventvalue = new Eventvalue(findResult.getType(), "findSleeper", findSleeper, "");
                dbhandler.addValue(eventvalue);
                Toast.makeText(this,"巳设置:"+findSleeper, Toast.LENGTH_SHORT).show();
            }else {Eventvalue eventvalue = new Eventvalue(null, "findSleeper", findSleeper, "");
                dbhandler.addValue(eventvalue);
                Toast.makeText(this,"巳设置:"+findSleeper, Toast.LENGTH_SHORT).show();}
        }
        if(msg.getType() == 1){
            int clickSleeper = msg.getData();
            final Eventvalue findResult = dbhandler.getNameResult("clickSleeper");
            if(findResult!=null) {
                Eventvalue eventvalue = new Eventvalue(findResult.getType(), "clickSleeper", clickSleeper, "");
                dbhandler.addValue(eventvalue);
                Toast.makeText(this,"巳设置:"+ clickSleeper, Toast.LENGTH_SHORT).show();
            }else {Eventvalue eventvalue = new Eventvalue(null, "clickSleeper", clickSleeper, "");
                dbhandler.addValue(eventvalue);
                Toast.makeText(this,"巳设置:"+ clickSleeper, Toast.LENGTH_SHORT).show();}
        }
        if(msg.getType() == 2){
            flishSleeper=msg.getData();
            final Eventvalue findResult = dbhandler.getNameResult("flishSleeper");
            if(findResult!=null) {
                Eventvalue eventvalue = new Eventvalue(findResult.getType(), "flishSleeper", flishSleeper, "");
                dbhandler.addValue(eventvalue);
                Toast.makeText(this,"巳设置:"+flishSleeper, Toast.LENGTH_SHORT).show();
            }else {Eventvalue eventvalue = new Eventvalue(null, "flishSleeper", flishSleeper, "");
                dbhandler.addValue(eventvalue);
                Toast.makeText(this,"巳设置:"+flishSleeper, Toast.LENGTH_SHORT).show();}
            if(flishSleeper<1200){
                Toast.makeText(this,"值小于1200ms将随机延时", Toast.LENGTH_SHORT).show();
            }
        }
        if(msg.getType() == 3){
            lightSleeper=msg.getData();
            final Eventvalue findResult = dbhandler.getNameResult("lightSleeper");
            if(findResult!=null) {
                Eventvalue eventvalue = new Eventvalue(findResult.getType(), "lightSleeper", lightSleeper, "");
                dbhandler.addValue(eventvalue);
                Toast.makeText(this,"巳设置:"+lightSleeper, Toast.LENGTH_SHORT).show();
            }else {Eventvalue eventvalue = new Eventvalue(null, "lightSleeper", lightSleeper, "");
                dbhandler.addValue(eventvalue);
                Toast.makeText(this,"巳设置:"+lightSleeper, Toast.LENGTH_SHORT).show();}
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void BooleanEvent(Message<Boolean> msg){
        if(msg.getType()==4){
            shoudong = msg.getData();
            if(shoudong){

                Toast.makeText(this,"手动模式开启", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "自动模式开启", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * 服务连接
     */
    @SuppressLint("SdCardPath")
    protected void onServiceConnected() {
        super.onServiceConnected();
        checkRoot rootcheck= new checkRoot();
        LogUtils.init("/sdcard/LogUtils","/biyongdebuglog.log");
        dbhandler=new DatabaseHandler(this);
        pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
        if (rootcheck.isDeviceRooted()){
            Toast.makeText(this, "你的设备巳获取ROOT|可以执行ADB指令|BiYong红包服务开启", Toast.LENGTH_LONG).show();
        }else Toast.makeText(this, "你的设备没有获取ROOT权限|可以导致通知栏消息无法过滤|BiYong红包服务开启", Toast.LENGTH_LONG).show();
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
        EventBus.getDefault().unregister(this);
        Toast.makeText(this, "Biyong服务关闭", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }
}
