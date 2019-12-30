package com.zhou.biyongxposed;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.os.PowerManager.SCREEN_BRIGHT_WAKE_LOCK;
import static com.zhou.biyongxposed.StringTimeUtils.getTimeStr2;

/*
PARTIAL_WAKE_LOCK:保持CPU 运转，屏幕和键盘灯有可能是关闭的。
SCREEN_DIM_WAKE_LOCK：保持CPU 运转，允许保持屏幕显示但有可能是灰的，允许关闭键盘灯
SCREEN_BRIGHT_WAKE_LOCK：保持CPU 运转，允许保持屏幕高亮显示，允许关闭键盘灯
FULL_WAKE_LOCK：保持CPU 运转，保持屏幕高亮显示，键盘灯也保持亮度
*/
public class bingyongserver extends AccessibilityService {
    private final static String TAG = "BiyongRedPacket";
    private boolean enableKeyguard;
    private boolean Notifibiyong = false;
    public static boolean shoudong=false;
    private int findSleeper;
    private int clickSleeper;
    private int flishSleeper;
    private int lightSleeper;
    private DatabaseHandler dbhandler;
    private KeyguardManager.KeyguardLock kl;
    //唤醒屏幕相关
    private PowerManager pm;
    private PowerManager.WakeLock wl = null;
    private AccessibilityNodeInfo rootNode;
    private boolean noComeIn;
    private String coin_unit;
    private boolean zidonghuifustatus;
    public  ArrayList<String> huifusize = new ArrayList<>();
    private boolean zhunbeihuifu;
    private ArrayList<AccessibilityNodeInfo> findRedPacketSender = new ArrayList<>();
    private List<AccessibilityNodeInfo> sender_name;
    private int ran;
    private ArrayList<String> CoinList = new ArrayList<>();
    private BigDecimal coinBigDecimal;
    private boolean inputFlish;
    private Integer begin_time;
    private Integer end_time;
    private boolean clickOpenRedPacket;//判断是否是自动点击进去的
    private boolean clickFindRedPacket;//判断是否是自动点击找到的红包
    private boolean comeinflishpage;
    private boolean back_true;

    @SuppressLint({"SwitchIntDef", "WakelockTimeout"})
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //注意这个方法回调，是在主线程，不要在这里执行耗时操作
        if (!EventBus.getDefault().isRegistered(this)) {//加上判断
            EventBus.getDefault().register(this);
        }
        int eventType = event.getEventType();
        rootNode = getRootInActiveWindow();
        CharSequence apkname = event.getPackageName();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                try {
                    if (!Notifibiyong && !shoudong) {
                        if (apkname.equals("org.telegram.btcchat")) {
                            Log.d(TAG, "收到通知栏红包消息");
                            LogUtils.i("收到通知栏红包消息");
                            if (!isScreenLocked()) {
                                wakeUpAndUnlock(false);
                            }
                            sleepTime(lightSleeper);
                            if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                                try {
                                    Notification notification = (Notification) event.getParcelableData();
                                    PendingIntent pendingIntent = notification.contentIntent;
                                    pendingIntent.send();
                                    Notifibiyong = true;
                                    zhunbeihuifu = false;
                                    inputFlish=false;
                                    clickOpenRedPacket = false;
                                    comeinflishpage=false;
                                    noComeIn = false;
                                    break;
                                } catch (PendingIntent.CanceledException ignored) {
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                /*
                 * 跳过广告
                 */
                try {
                    List<AccessibilityNodeInfo> hongbaoxianqin = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/title_bar");
                    if(comeinflishpage&&back_true&&hongbaoxianqin.isEmpty()){
                        clickFindRedPacket=false;
                        comeinflishpage=false;
                        back_true =false;
                    }
                    if (inputFlish) {
                        inputFlish = false;
                        findSendView(rootNode, "发送");
                    }
                    List<AccessibilityNodeInfo> skip = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/skip");
                    if (!skip.isEmpty()) {
                        for (AccessibilityNodeInfo jump : skip) {
                            sleepTime(100);
                            jump.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                } catch (Exception ignored) {
                }
                /*
                 * 从此处开始通知栏没有收到消息须手动进群抢红包:自动模式
                 * */
                if (Notifibiyong && !shoudong) {
                    try {
                        findMessageSize(rootNode, "转到底部");
                        if (!noComeIn) {
                            clickFindRedPacket=false;
                            List<AccessibilityNodeInfo> red_paket_status = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cell_red_paket_status");
                            List<AccessibilityNodeInfo> red_paket_sender = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cell_red_paket_sender");
                            List<AccessibilityNodeInfo> red_paket_message = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cell_red_paket_message");
                            if (!red_paket_status.isEmpty()) {
                                int i = 0;
                                while (i < red_paket_status.size()) {
                                    if (red_paket_status.get(i).getText().equals("领取红包") && !red_paket_message.get(i).getText().equals("答题红包")) {
                                        findRedPacketSender.add(red_paket_sender.get(i));
                                    }
                                    i++;
                                }
                                if (findRedPacketSender.size() > 0) {
                                    noComeIn = true;
                                    Log.d(TAG, "发现红包,正在处理红包操作......");
                                    LogUtils.i("发现红包,正在处理红包操作......");
                                    findAndClickHongbao();
                                } else if (!autoHuiFu()) {//自动回复处理
                                        Log.d(TAG, "红包巳领完!");
                                        LogUtils.i("红包巳领完!");
                                        huifusize.clear();
                                        exitPage();
                                        break;
                                    }
                                    inputFlish = true;
                                    return;
                            } else
                                exitPage();
                        }
                    } catch (Exception ignored) {
                    }
                    openClickdhongbao();//点击红包上的开按钮
                    gethongbaoinfo();//红包领取完成获取相关信息存入数据库
                }
                /*
                 * 从此处开始通知栏没有收到消息须手动进群抢红包:手动模式
                 * */
                if (!Notifibiyong && shoudong) {
                    findMessageSize(rootNode, "转到底部");
                    randomOnclick(rootNode);//手动模式遍历红包点击
                    openClickdhongbao();//点击红包上的开按钮
                    gethongbaoinfo();//红包领取完成获取相关信息存入数据库
                }
                /*
                 * 此处通知栏没有收到消息但巳处于红包页面的自动点击模式:半自动模式
                 * */
                if (!Notifibiyong && !shoudong) {
                    randomOnclick(rootNode);//手动模式遍历红包点击
                    openClickdhongbao();//点击红包上的开按钮
                    gethongbaoinfo();//红包领取完成获取相关信息存入数据库
                }
                gethongbaoerror();//您来晚一步，红包已被抢完
                biyongerror();//biyong崩溃处理
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                biyongerror();//biyong崩溃处理
                gethongbaoerror();//您来晚一步，红包已被抢完
                openClickdhongbao();//点击红包上的开按钮
                break;
        }
    }
    private boolean autoHuiFu() {
        int sys_hh = (Integer.parseInt(getTimeStr2().substring(11, 12)) * 10) + Integer.parseInt(getTimeStr2().substring(12, 13));
        int sys_ss = (Integer.parseInt(getTimeStr2().substring(14, 15)) * 10) + Integer.parseInt(getTimeStr2().substring(15, 16));
        if (zhunbeihuifu && zidonghuifustatus && sys_hh > begin_time && sys_hh < end_time) {
            Log.d(TAG, "允许回复,进行回复处理!");
            LogUtils.i("允许回复,进行回复处理!");
            zhunbeihuifu=false;
            getDbhuifuCount();
            if (ran == 5) {
                int rand = (int) (Math.random() * 6);//产生0  -  5的整数随机数
                int rands = (int) (Math.random() * huifusize.size());//产生0  -  huifusize.size()的整数随机数
                BigDecimal getResult = coinBigDecimal.setScale(2, RoundingMode.HALF_UP);
                String senderName = sender_name.get(0).getText().toString().substring(0, sender_name.get(0).getText().toString().indexOf("红"));
                switch (rand) {
                    case 0:
                        fillInputBar("谢谢" + senderName + "!" + huifusize.get(rands));
                        Log.d(TAG, "准备回复:" + "谢谢" + senderName + "!" + huifusize.get(rands));
                        LogUtils.i("准备回复:" + "谢谢" + senderName + "!" + huifusize.get(rands));
                        sleepTime(1000);
                        break;
                    case 1:
                        fillInputBar("谢谢" + senderName + "," + huifusize.get(rands));
                        Log.d(TAG, "准备回复:" + "谢谢" + senderName + "," + huifusize.get(rands));
                        LogUtils.i("准备回复:" + "谢谢" + senderName + "," + huifusize.get(rands));
                        sleepTime(1500);
                        break;
                    case 2:
                        fillInputBar("呵呵！抢了" + getResult + "个,！！" + huifusize.get(rands));
                        Log.d(TAG, "准备回复:" + "呵呵！抢了" + getResult + "个,！！" + huifusize.get(rands));
                        LogUtils.i("准备回复:" + "呵呵！抢了" + getResult + "个,！！" + huifusize.get(rands));
                        sleepTime(1500);
                        break;
                    case 3:
                        fillInputBar("抢到" + getResult + "个," + huifusize.get(rands));
                        Log.d(TAG, "准备回复:" + "抢到" + getResult + "个," + huifusize.get(rands));
                        LogUtils.i("准备回复:" + "抢到" + getResult + "个," + huifusize.get(rands));
                        sleepTime(2000);
                        break;
                    case 4:
                        fillInputBar("终于抢到" + getResult + huifusize.get(rands));
                        Log.d(TAG, "准备回复:" + "终于抢到" + getResult + huifusize.get(rands));
                        LogUtils.i("准备回复:" + "终于抢到" + getResult + huifusize.get(rands));
                        sleepTime(1900);
                        break;
                    case 5:
                        fillInputBar("抢了" + getResult + "," + huifusize.get(rands));
                        Log.d(TAG, "准备回复:" + "抢了" + getResult + "," + huifusize.get(rands));
                        LogUtils.i("准备回复:" + "抢了" + getResult + "," + huifusize.get(rands));
                        sleepTime(2000);
                        break;
                }
                return true;
            }
            int rand = (int) (Math.random() * huifusize.size());//产生0  -  huifusize.size()的整数随机数
            Log.d(TAG, "准备回复:" + huifusize.get(rand));
            LogUtils.i("准备回复:" + huifusize.get(rand));
            fillInputBar(huifusize.get(rand));
            sleepTime(2000);
            return true;
        }
        return false;
    }

    private void exitPage() {
        sleepTime(500);
        performBackClick();
        sleepTime(1000);
        if (enableKeyguard) {
            back2Home();
            sleepTime(200);
            wakeUpAndUnlock(true);
            enableKeyguard = false;
            sleepTime(800);
            Notifibiyong = false;
            Log.d(TAG, "锁屏,开始监听!");
            LogUtils.i("锁屏,开始监听!");
        } else {
                back2Home();
                sleepTime(800);
                Notifibiyong = false;
                Log.d(TAG, "返回桌面，开始监听!");
                LogUtils.i("返回桌面，开始监听!");
        }
        Log.d(TAG, "系统时间:" + getTimeStr2());
        LogUtils.i("系统时间"+ getTimeStr2());
    }
    private void randomOnclick(AccessibilityNodeInfo rootNode) {
        try {
            List<AccessibilityNodeInfo >notifinotion_off_red_paket_status = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cell_red_paket_status");
                if (!notifinotion_off_red_paket_status.isEmpty()) {
                    for (int i = 0; i < notifinotion_off_red_paket_status.size(); i++) {
                        if (notifinotion_off_red_paket_status.get(i).getText().equals("领取红包")) {
                            List<AccessibilityNodeInfo> red_paket_message = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/cell_red_paket_message");
                            if(!red_paket_message.isEmpty()&&!red_paket_message.get(i).getText().equals("答题红包")) {
                                sleepTime(findSleeper);//发现红包延时控制
                                notifinotion_off_red_paket_status.get(i).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                clickFindRedPacket=true;
                                Log.d(TAG, "点击红包");
                                LogUtils.i("点击红包");
                            }
                        }
                    }
                }
        }catch (Exception ignored) {}
    }
    private void openClickdhongbao() {
        try {
            List<AccessibilityNodeInfo> openhongbao = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/red_packet_open_button");
            if (openhongbao != null && !openhongbao.isEmpty()) {
                for (AccessibilityNodeInfo co : openhongbao) {
                    sleepTime(clickSleeper);//点击拆红包延时控制
                    co.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    clickOpenRedPacket=true;
                    Log.d(TAG, "拆红包");
                    LogUtils.i("拆红包");
                    return;
                }
            }
        } catch (Exception ignored) {}
    }
    private void gethongbaoinfo() {
        try {
            List<AccessibilityNodeInfo> hongbaojilu = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/title_bar");//红包完成页面的标题栏
            if (!hongbaojilu.isEmpty()) {
                comeinflishpage=true;
                int random = (int)(1500+Math.random()*(flishSleeper-1500+1));//(数据类型)(最小值+Math.random()*(最大值-最小值+1))
                if (flishSleeper > 1500) {
                    sleepTime(random);
                } else { sleepTime(1500); }
                sender_name = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/sender_name");
                List<AccessibilityNodeInfo> received_coin_unit = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/received_coin_unit");
                List<AccessibilityNodeInfo> received_coin_count = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/received_coin_count");
                if (!sender_name.isEmpty() && !received_coin_unit.isEmpty() && !received_coin_count.isEmpty() && clickOpenRedPacket) {
                    clickOpenRedPacket=false;
                    coin_unit = (String) received_coin_unit.get(0).getText();//类型
                    double coin_count = Double.parseDouble((String) received_coin_count.get(0).getText());//数量
                    coinBigDecimal = new BigDecimal(coin_count);
                    Log.d(TAG, "领取:" + coin_unit + "金额:" + coin_count);
                    LogUtils.i("领取:" + coin_unit + "金额:" + coin_count);
                    for (int i = 0; i <dbhandler.dbquery().size(); i++) {
                        Eventvalue Result = dbhandler.dbquery().get(i);
                        if (Result.getName().contains(coin_unit)&&Result.getValue() == 1) {
                            BigDecimal coin_DB = new BigDecimal(Double.valueOf(Result.getCoincount()));
                            BigDecimal coin_result = coin_DB.add(coinBigDecimal);
                            BigDecimal setScale = coin_result.setScale(2, RoundingMode.HALF_UP);
                            Eventvalue eventvalue = new Eventvalue(Result.getId(), coin_unit, 1, String.valueOf(setScale));
                            dbhandler.addValue(eventvalue);
                            Log.d(TAG, "值巳存入数据库......");
                            LogUtils.i("值巳存入数据库......");
                            ran=(int)(Math.random()*15);//产生0  -  20的整数随机数
                            if (ran == 1 || ran == 3 || ran == 14 || ran == 5 || ran == 2 || ran == 0) {
                                zhunbeihuifu = true;
                            }
                            getFinish();
                            return;
                        }
                    }
                    Log.d(TAG, "数据库无相关信息，将创建新值");
                    LogUtils.i("数据库无相关信息，将创建新值");
                    Eventvalue eventvalue = new Eventvalue(null, coin_unit, 1, String.valueOf(coin_count));
                    dbhandler.addValue(eventvalue);
                    Log.d(TAG, "创建新值:" + coin_unit + "金额:" + coin_count+"巳写入数据库");
                    LogUtils.i("创建新值:" + coin_unit + "金额:" + coin_count+"巳写入数据库");
                    getFinish();
                }else getFinish();
            }
        } catch (Exception ignored){}
    }
    private void getFinish() {
        try {
            List<AccessibilityNodeInfo> go_back = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/go_back_button");//红包完成页面的返回按钮
            if (!go_back.isEmpty()&&clickFindRedPacket) {
                for (AccessibilityNodeInfo back : go_back) {
                    back.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    noComeIn = false;
                    coin_unit = null;
                    back_true =true;
                    findRedPacketSender.clear();
                }
            }
        } catch (Exception ignored) {

        }
    }
    private void gethongbaoerror() {
        try {
            List<AccessibilityNodeInfo> hongbao_error = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.btcchat:id/red_packet_message_error");
            if (!hongbao_error.isEmpty()&&clickFindRedPacket) {
                Log.d(TAG, "异常信息：" + hongbao_error.get(0).getText());
                LogUtils.i("异常信息：" + hongbao_error.get(0).getText());
                if (hongbao_error.get(0).getText().equals("您来晚一步，红包已被抢完") || hongbao_error.get(0).getText().equals("该红包已超过24小时未被领取，退还金额可在钱包中查看")) {
                    sleepTime(100);
                    inputClick();
                }
            }
        } catch (Exception ignored) {
        }
    }
    private void biyongerror() {
        try {
            List<AccessibilityNodeInfo> button2 = rootNode.findAccessibilityNodeInfosByViewId("android:id/button2");
            if (!button2.isEmpty()) {
                Log.d(TAG, "异常信息：BiYong意外退出!");
                LogUtils.i("异常信息：BiYong意外退出!");
                if (button2.get(0).getText().equals("不发送")) {
                    sleepTime(1000);
                    button2.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Notifibiyong = false;
                    shoudong=false;
                    sleepTime(1000);
                }
            }
        }catch (Exception ignored) {
        }
    }
    private void findAndClickHongbao (){
        CoinList.clear();
        getCoinList();
        for (int a = 0; a < CoinList.size(); a++) {
            int b = 0;
            while (b < findRedPacketSender.size()) {
                if (findRedPacketSender.get(b).getText().toString().contains(CoinList.get(a))) {
                    Log.d(TAG, "红包种类包含优先类型:" + CoinList.get(a) + " 准备点击");
                    LogUtils.i("红包种类包含优先类型:" + CoinList.get(a) + " 准备点击");
                    sleepTime(findSleeper);//发现红包延时控制
                    findRedPacketSender.get(b).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    clickFindRedPacket=true;
                    Log.d(TAG, "点击优先红包");
                    LogUtils.i("点击优先红包");
                    return;
                }
                b++;
            }
        }
        Log.d(TAG,"随机选择可领取的红包");
        LogUtils.i("随机选择可领取的红包");
        randomOnclick(rootNode);
    }
    /**
     * 填充输入框
     */
    private void fillInputBar(String reply) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            findInputBar(rootNode, reply);
        }
    }
    /**
     * 查找EditText控件
     * @param rootNode 根结点
     * @param reply 回复内容
     * @return 找到返回true, 否则返回false
     */
    private boolean findInputBar(AccessibilityNodeInfo rootNode, String reply) {
        int count = rootNode.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo node = rootNode.getChild(i);
            if ("android.widget.EditText".contentEquals(node.getClassName())) {   // 找到输入框并输入文本
                setText(node, reply);
                return true;
            }
            if (findInputBar(node, reply)) {    // 递归查找
                return true;
            }
        }
        return false;
    }
    /**
     * 设置文本
     */
    private void setText(AccessibilityNodeInfo node, String reply) {
        if (Build.VERSION.SDK_INT >= 22) {
            Bundle args = new Bundle();
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    reply);
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
        } else {
            ClipData data = ClipData.newPlainText("reply", reply);
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            Objects.requireNonNull(clipboardManager).setPrimaryClip(data);
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS); // 获取焦点
            node.performAction(AccessibilityNodeInfo.ACTION_PASTE); // 执行粘贴
        }
    }
    /**
     * 查找TextView控件
     * @param rootNode 根结点
     */
    private void findSendView(AccessibilityNodeInfo rootNode, String str1) {
        try {
        int count = rootNode.getChildCount();
            for (int i = 0; i < count; i++) {
                AccessibilityNodeInfo node = rootNode.getChild(i);
                if (null != node.getClassName() && "android.widget.ImageView".contentEquals(node.getClassName())) {
                    String text = (String) node.getContentDescription();
                    if (text != null && text.contentEquals(str1)) {
                        if (node.isClickable()) {
                            sleepTime(1000);
                            performClick(node);
                            Log.d(TAG, "回复成功");
                            LogUtils.i("回复成功");
                            sleepTime(1000);
                            return;
                        }
                    }
                }
                findSendView(node, str1);
            }
        } catch (Exception ignored) {
        }
    }
    /**
     * 查找TextView控件
     * @param rootNode 根结点
     */
    private void findMessageSize(AccessibilityNodeInfo rootNode , String str0) {
        try {
            int count = rootNode.getChildCount();
            for (int i = 0; i < count; i++) {
                    AccessibilityNodeInfo node = rootNode.getChild(i);
                    if (null != node.getClassName() && "android.widget.FrameLayout".contentEquals(node.getClassName())) {
                        String ls = (String) node.getContentDescription();
                        if (ls != null && ls.contentEquals(str0)) {
                            if (node.isClickable()) {
                                performClick(node);
                                Log.d(TAG, "点击转到底部");
                                LogUtils.i("点击转到底部");
                                sleepTime(1000);
                                return;
                            }
                        }
                    }
                    findMessageSize(node, str0);
                }
            } catch (Exception ignored) {
        }
    }
    private void performClick(AccessibilityNodeInfo targetInfo) {
        targetInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }
    public void getDbhuifuCount(){
        for (int i = 0; i < dbhandler.dbquery().size(); i++) {
            int Result = dbhandler.dbquery().get(i).getValue();
            if (Result != 5) {
                continue;
            }
            huifusize.add(dbhandler.dbquery().get(i).getCoincount());
        }
    }
    /**
     * 系统是否在锁屏状态
     *
     * @return  true为亮屏，false为黑屏
     */
    public boolean isScreenLocked() {
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return Objects.requireNonNull(pm).isScreenOn();
    }

    //唤醒屏幕和解锁
    private void wakeUpAndUnlock(boolean screenOn)
    {
        if(!screenOn){//获取电源管理器对象，ACQUIRE_CAUSES_WAKEUP这个参数能从黑屏唤醒屏幕
            //获取电源管理器对象
            if (pm != null) {
                wl = pm.newWakeLock(SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,"com.zhou.biyongxposed:TAG");
            }
            wl.acquire(10000);
            wl.release();
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
            //execShellCmd("input keyevent " + 223 );
            goToSleep(getApplicationContext());
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
    public  void sleepTime(int ms) {
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
     * 获取币种列表
     */
    private void getCoinList() {
        for (int i = 0; i <dbhandler.dbquery().size(); i++) {
            int Result = dbhandler.dbquery().get(i).getValue();
            if (Result != 2) {
                continue;
            }
            CoinList.add(dbhandler.dbquery().get(i).getName());
        }
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
                Eventvalue eventvalue = new Eventvalue(findResult.getId(), "findSleeper", findSleeper, "");
                dbhandler.addValue(eventvalue);
                Toast.makeText(this,"巳设置:"+findSleeper, Toast.LENGTH_SHORT).show();
            }else {Eventvalue eventvalue = new Eventvalue(null, "findSleeper", findSleeper, "");
                dbhandler.addValue(eventvalue);
                Toast.makeText(this,"巳设置:"+findSleeper, Toast.LENGTH_SHORT).show();}
        }
        if(msg.getType() == 1){
            clickSleeper = msg.getData();
            final Eventvalue findResult = dbhandler.getNameResult("clickSleeper");
            if(findResult!=null) {
                Eventvalue eventvalue = new Eventvalue(findResult.getId(), "clickSleeper", clickSleeper, "");
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
                Eventvalue eventvalue = new Eventvalue(findResult.getId(), "flishSleeper", flishSleeper, "");
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
                Eventvalue eventvalue = new Eventvalue(findResult.getId(), "lightSleeper", lightSleeper, "");
                dbhandler.addValue(eventvalue);
                Toast.makeText(this,"巳设置:"+lightSleeper, Toast.LENGTH_SHORT).show();
            }else {Eventvalue eventvalue = new Eventvalue(null, "lightSleeper", lightSleeper, "");
                dbhandler.addValue(eventvalue);
                Toast.makeText(this,"巳设置:"+lightSleeper, Toast.LENGTH_SHORT).show();}
        }
        if(msg.getType() == 6) {
            begin_time = msg.getData();
            final Eventvalue findResult = dbhandler.getNameResult("begin_time");
            if(findResult!=null) {
                Eventvalue eventvalue = new Eventvalue(findResult.getId(), "begin_time", begin_time, "");
                dbhandler.addValue(eventvalue);
                Toast.makeText(this,"巳设置自动回复开启时间:"+begin_time+"点整.", Toast.LENGTH_SHORT).show();
            }else {Eventvalue eventvalue = new Eventvalue(null, "begin_time", begin_time, "");
                dbhandler.addValue(eventvalue);
                Toast.makeText(this,"巳设置自动回复开启时间:"+begin_time+"点整.", Toast.LENGTH_SHORT).show();}
        }
        if(msg.getType() == 7) {
            end_time = msg.getData();
            final Eventvalue findResult = dbhandler.getNameResult("end_time");
            if(findResult!=null) {
                Eventvalue eventvalue = new Eventvalue(findResult.getId(), "end_time", end_time, "");
                dbhandler.addValue(eventvalue);
                Toast.makeText(this,"巳设置自动回复关闭时间:"+end_time+"点整.", Toast.LENGTH_SHORT).show();
            }else {Eventvalue eventvalue = new Eventvalue(null, "end_time", end_time, "");
                dbhandler.addValue(eventvalue);
                Toast.makeText(this,"巳设置自动回复关闭时间:"+end_time+"点整.", Toast.LENGTH_SHORT).show();}
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
        if(msg.getType()==5){
            zidonghuifustatus = msg.getData();
            int huifu;
            if(zidonghuifustatus){
                huifu =1;
                final Eventvalue findResult = dbhandler.getNameResult("huifu");
                if(findResult!=null) {
                    Eventvalue eventvalue = new Eventvalue(findResult.getId(), findResult.getName(), findResult.getValue(), String.valueOf(huifu));
                    dbhandler.addValue(eventvalue);
                }else {Eventvalue eventvalue = new Eventvalue(null, "huifu",4, String.valueOf(huifu));
                    dbhandler.addValue(eventvalue);}
                Toast.makeText(this,"自动回复开启", Toast.LENGTH_SHORT).show();
            }else {
                huifu =0;
                final Eventvalue findResult = dbhandler.getNameResult("huifu");
                if(findResult!=null) {
                    Eventvalue eventvalue = new Eventvalue(findResult.getId(), findResult.getName(), findResult.getValue(), String.valueOf(huifu));
                    dbhandler.addValue(eventvalue);
                }else {Eventvalue eventvalue = new Eventvalue(null, "huifu", 4, String.valueOf(huifu));
                    dbhandler.addValue(eventvalue);}
                Toast.makeText(this, "自动回复关闭", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class initInfo extends Thread{
        @Override
        public void run(){
            if(dbhandler.getNameResult("findSleeper")!=null) {
                findSleeper = dbhandler.getNameResult("findSleeper").getValue();
            }
            if(dbhandler.getNameResult("clickSleeper")!=null){
                clickSleeper=dbhandler.getNameResult("clickSleeper").getValue();
            }
            if(dbhandler.getNameResult("flishSleeper")!=null) {
                flishSleeper = dbhandler.getNameResult("flishSleeper").getValue();
            }
            if(dbhandler.getNameResult("lightSleeper")!=null) {
                lightSleeper = dbhandler.getNameResult("lightSleeper").getValue();
            }
            if(dbhandler.getNameResult("begin_time")!=null) {
                begin_time = dbhandler.getNameResult("begin_time").getValue();
            }
            if(dbhandler.getNameResult("end_time")!=null) {
                end_time = dbhandler.getNameResult("end_time").getValue();
            }
            if (dbhandler.getNameResult("huifu")!= null) {
                if(dbhandler.getNameResult("huifu").getCoincount().equals("1")){
                    zidonghuifustatus=true;
                }
            }
            getCoinList();
        }
    }
    /**
     * 服务连接
     */
    @SuppressLint("SdCardPath")
    protected void onServiceConnected() {
        super.onServiceConnected();
        LogUtils.init("/sdcard/LogUtils","/biyongdebuglog.log");
        dbhandler=new DatabaseHandler(this);
        pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
        Toast.makeText(this, "......正在初始化数据......", Toast.LENGTH_SHORT).show();
        new initInfo().start();
        Intent intent = new Intent(this,BiyongServer.class);
        startService(intent);
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