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
//org.telegram.biyongx:id/red_packet_message 恭喜发财吉祥如意的ID
//org.telegram.biyongx:id/red_packet_open_button 点击那个开的ID
//org.telegram.biyongx:id/unread_message_count  会话上面的那个小角标ID
//org.telegram.biyongx:id/cell_red_paket_status 红包的状态是否己领完
//org.telegram.biyongx:id/scroll_text    主页上BIYONG通知的ID
//org.telegram.biyongx:id/cell_red_paket_icon 一个隐藏的红包小标识
//org.telegram.biyongx:id/close_button   你来晚了一步红包被抢完了的关闭ID
//org.telegram.biyongx:id/buy_and_sell_tab_text  聊天页面的聊天两字ID
//org.telegram.biyongx:id/tv_question   答题红包的选择题
//org.telegram.biyongx:id/tv_sender_name 这是谁的答题红包，不为空代表出现答题红包
public class bingyongserver extends AccessibilityService {
    private boolean enableKeyguard;
    private boolean Notifibiyong = false;
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
    private String coin_unit;
    private int huadong;
    private boolean meizhaodao;

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
                        if (apkname.equals("org.telegram.biyongx")) {
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
                                meizhaodao=false;
                                return;
                            } catch (PendingIntent.CanceledException e) {
                                Log.i("Biyong","pendingIntent.send(); Not Find");
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.i("Biyong","org.telegram.biyongx Not Find");
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                /*
                 * 跳过广告
                 */
                try {
                    List<AccessibilityNodeInfo> skip = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.biyongx:id/skip");
                    if (!skip.isEmpty()) {
                        for (AccessibilityNodeInfo jump : skip) {
                            sleepTime(50);
                            jump.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                } catch (Exception e) {
                    Log.i("Biyong","广告页面的跳过ID没有找到");
                }
                /*
                 * 从此处开始通知栏没有收到消息须手动进群抢红包:自动模式
                 * */
                if (Notifibiyong && !shoudong) {
                    try {
                        if (!nocomein) {
                            slk = false;
                            List<AccessibilityNodeInfo> red_paket_status = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.biyongx:id/cell_red_paket_status");
                            List<AccessibilityNodeInfo> red_paket_sender = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.biyongx:id/cell_red_paket_sender");
                            findRedPacketSender = new AccessibilityNodeInfo[red_paket_status.size()];
                            if (!red_paket_status.isEmpty()) {
                                sleepTime(findSleeper);//发现红包延时控制
                                Log.i("Biyong", "发现红包");
                                LogUtils.i("发现红包");
                                for (int i = 0; i < red_paket_status.size(); i++) {
                                    if (red_paket_status.get(i).getText().toString().equals("领取红包")){
                                        List<AccessibilityNodeInfo> red_paket_message = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.biyongx:id/cell_red_paket_message");
                                           if (!red_paket_message.isEmpty()&&!red_paket_message.get(i).getText().equals("答题红包")) {
                                                Log.i("Biyong:", "找到第"+(i+1)+"个红包的关键字."+red_paket_status.get(i).getText());
                                                LogUtils.i("找到第"+(i+1)+"个红包的关键字."+red_paket_status.get(i).getText());
                                                have = true;
                                                meizhaodao=true;
                                                findRedPacketSender[i] = red_paket_sender.get(i);
                                                Log.i("Biyong:", "第" + (i + 1) + "个红包类型为:" + findRedPacketSender[i].getText());
                                                LogUtils.i("第" + (i + 1) + "个红包为:" + findRedPacketSender[i].getText());
                                            }
                                    }else if (!meizhaodao) {
                                            Log.i("Biyong:", "红包可能巳被拆开或领完或过期.");
                                            LogUtils.i("红包可能巳被拆开或领完或过期.");
                                            execShellCmd("input swipe 1057 2093 1153 652");
                                            sleepTime(1500);
                                            Log.i("Biyong:", "没找到可领取的红包,都是拆过或被领完的红包,开始执行下滑.");
                                            LogUtils.i("没找到可领取的红包,都是拆过或被领完的红包,开始执行下滑.");
                                            return ;
                                        }
                                    }
                                Log.i("Biyong", "查找红包任务执行完成");
                                findhongbao();//找最优红包
                                if (!slk) {
                                    Log.i("swipe:", "经多次循环,并没有发现可点击的红包.");
                                    LogUtils.i("经多次循环,并没有发现可点击的红包.");
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
                             * 此处为处理聊天页面无红包的情况
                             * */
                                List<AccessibilityNodeInfo> buy_and_sell_tab_text = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.biyongx:id/view_image_fragment");
                                if(!buy_and_sell_tab_text.isEmpty()){
                                    Log.i("Biyong","有红包消息，不可能没有红包，准备下滑查找");
                                    LogUtils.i("有红包消息，不可能没有红包，准备下滑查找");
                                    if(huadong<3) { swipe(); }
                                    if(huadong==2){execShellCmd("input tap 1342 2284");
                                    Log.i("swipe:","滑动"+huadong+"次,没有找到红包，直接点击坐标！");
                                    LogUtils.i("滑动了"+huadong+"次,没有找到红包，直接点击坐标！");
                                    sleepTime(1800);}
                                    return;
                                }
                            }
                        }
                    }catch (Exception e){
                        Log.i("Biyong","领取红包的ID没有找到");
                    }
                    openClickdhongbao();//点击红包上的开按钮
                    gethongbao();//红包领取完成获取相关信息存入数据库
                    getFinish();//领取完成准备返回
                    gethongbaoerror();
                    noMessage();//暂无消息
                    biyongerror();//biyong崩溃处理
                }
                /*
                 * 从此处开始通知栏没有收到消息须手动进群抢红包:手动模式
                 * */
                if (!Notifibiyong&&shoudong) {
                    randomOnclick(rootNode);//手动模式遍历红包点击
                    openClickdhongbao();//点击红包上的开按钮
                    gethongbaoerror();//领取红包出现错误
                    gethongbao();//红包领取完成获取相关信息存入数据库
                    getFinish();//领取完成准备返回
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if(!Notifibiyong&&shoudong||Notifibiyong&&!shoudong) {
                    sleepTime(500);
                    openClickdhongbao();//点击红包上的开按钮
                }//只有处于这两种状态开启
                break;
        }
    }

    private void swipe() {
        execShellCmd("input swipe 1057 2093 1153 652");
        sleepTime(1500);
        huadong++;
        Log.i("swipe:","滑动次数:"+huadong);
        LogUtils.i("滑动次数:"+huadong);
    }

    private void randomOnclick(AccessibilityNodeInfo rootNode) {
        try {
            /**
             * 遍历查找红包
             * */
            List<AccessibilityNodeInfo> buy_and_sell = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.biyongx:id/user_avatar");
            if (!buy_and_sell.isEmpty()){
                List<AccessibilityNodeInfo> notifinotion_off_red_paket_status = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.biyongx:id/cell_red_paket_status");
                if (!notifinotion_off_red_paket_status.isEmpty()) {
                    for (int i = 0; i < notifinotion_off_red_paket_status.size(); i++) {
                        if (notifinotion_off_red_paket_status.get(i).getText().equals("领取红包")) {
                            List<AccessibilityNodeInfo> red_paket_message = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.biyongx:id/cell_red_paket_message");
                            if(!red_paket_message.isEmpty()&&!red_paket_message.get(i).getText().equals("答题红包")) {
                                sleepTime(findSleeper);
                                notifinotion_off_red_paket_status.get(i).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                LogUtils.i("点击红包");
                            }
                        }
                    }
                }
            }
        }catch (Exception e) {
            Log.i("Biyong","领取红包的关键字没有找到");
        }
    }
    private void openClickdhongbao() {
        /*
         * 此处为开红包按钮的开字
         * org.telegram.biyongx:id/red_packet_open_button
         * */
        try {
                List<AccessibilityNodeInfo> openhongbao = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.biyongx:id/red_packet_open_button");
                if (!openhongbao.isEmpty()) {
                    for (AccessibilityNodeInfo co : openhongbao) {
                        co.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        meizhaodao=true;
                        sleepTime(1000);
                        Log.i("Biyong","拆红包");
                        LogUtils.i("拆红包");
                    }
                }
        }catch (Exception e) {
            Log.i("Biyong","拆红包的关键字没有找到");
        }
    }
    private void gethongbao() {
        /*
                org.telegram.biyongx:id/sender_name  红包发送者的名字
                org.telegram.biyongx:id/received_coin_count 红包的金额
                org.telegram.biyongx:id/received_coin_unit  红包的类型
                * */
        try {
                List<AccessibilityNodeInfo> hongbaojilu = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.biyongx:id/title_bar");//红包完成页面的标题栏
                if (!hongbaojilu.isEmpty() && !gethongbao) {
                    Random rand = new Random();
                    int random = rand.nextInt(500) + 700;
                    if (flishSleeper > 1200) {
                        sleepTime(flishSleeper);
                    } else sleepTime(random);
                    LogUtils.i("领取等待延时:" + random);
                    List<AccessibilityNodeInfo> sender_name = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.biyongx:id/sender_name");
                    List<AccessibilityNodeInfo> received_coin_unit = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.biyongx:id/received_coin_unit");
                    List<AccessibilityNodeInfo> received_coin_count = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.biyongx:id/received_coin_count");
                    if (!sender_name.isEmpty() && !received_coin_unit.isEmpty() && !received_coin_count.isEmpty()) {
                        LogUtils.i("领取:" + sender_name.get(0).getText() + ":类型:" + received_coin_unit.get(0).getText() + "金额:" + received_coin_count.get(0).getText());
                        coin_unit = (String) received_coin_unit.get(0).getText();//类型
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
                                    Log.i("biyongzhou", "领取的红包金额:" + nowcoin);
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
            } catch (Exception e) {
            Log.i("Biyong","领取完成页面的标题栏ID没有找到");
        }
    }
    private void getFinish() {
        try {
            List<AccessibilityNodeInfo> go_back = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.biyongx:id/go_back_button");
            if (!go_back.isEmpty() || gethongbao) {
                    for (AccessibilityNodeInfo back : go_back) {
                        back.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        nocomein = false;
                        gethongbao = false;
                        coin_unit = null;
                        huadong=0;
                        Log.i("Biyong", "之前步骤巳点击完成，请查看页面领取详情");
                        LogUtils.i("之前步骤巳点击完成，请查看页面领取详情");
                    }
            }
        }catch (Exception e) {
            Log.i("Biyong", "请查看页面领取详情");
            LogUtils.i("请查看页面领取详情");
        }
    }
    private void gethongbaoerror() {
        /*
         * 您来晚一步，红包已被抢完||该红包巳超过24小时
         */
        try {
                List<AccessibilityNodeInfo> hongbao_error = rootNode.findAccessibilityNodeInfosByViewId("org.telegram.biyongx:id/red_packet_message_error");
                if (!hongbao_error.isEmpty()) {
                    LogUtils.i("异常信息：" + hongbao_error.get(0).getText());
                    if (hongbao_error.get(0).getText().equals("您来晚一步，红包已被抢完") || hongbao_error.get(0).getText().equals("该红包已超过24小时，如果已领取可在领取记录中查看")) {
                        sleepTime(100);
                        inputClick();
                    }
                }
            } catch (Exception e) {
            Log.i("Biyong","红包巳抢完或超过24小时的ID没有找到");
        }
    }
    private void noMessage() {
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
            Log.i("Biyong","暂无消息的ID没有找到");
        }
    }

    private void biyongerror() {
        /*
         * 此处为处理BiYong崩溃的界面
         */
        try {
            List<AccessibilityNodeInfo> button2 = rootNode.findAccessibilityNodeInfosByViewId("android:id/button2");
            if (!button2.isEmpty()) {
                LogUtils.i("异常信息：BiYong意外退出！");
                if (button2.get(0).getText().equals("不发送")) {
                    sleepTime(1000);
                    button2.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Notifibiyong = false;
                    sleepTime(1000);
                }
            }
        }catch (Exception e) {
            Log.i("Biyong","程序意外退出的ID没有找到");
        }
    }
    /**
     * 优先找最有价值的红包
     */
    private void findhongbao (){
            if(have) {
                Log.i("Biyong:", "准备进入循环遍历优先红包,共有:"+youxianlist.size()+"种类型." );
                have=false;
                for (int a = 0; a < youxianlist.size(); a++) {
                    Log.i("Biyong:", "准备遍历第" + (a + 1) + "种红包类型");
                    for (int b = 0; b < findRedPacketSender.length; b++) {
                        Log.i("Biyong:", "当前正在检测是否包含:" + youxianlist.get(a));
                        if (findRedPacketSender[b]==null){
                            continue;
                        }
                        if (findRedPacketSender[b].toString().contains(youxianlist.get(a))) {
                            Log.i("Biyong", "巳确定包含:" + youxianlist.get(a) + " 准备点击");
                            findRedPacketSender[b].getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            slk = true;
                            nocomein=true;
                            sleepTime(200);
                            Log.i("Biyong", "点击完成");
                            LogUtils.i("点击最优红包" + findRedPacketSender[b].getText()+ "完成");
                            return;
                        }
                    }
                }
                Log.i("Biyong","在优化列表没有找到该币种");
                LogUtils.i("在优化列表没有找到该币种");
                randomOnclick(rootNode);
                slk = true;
                nocomein=true;
                Log.i("Biyong","随机点击可领取的币种");
                LogUtils.i("随机点击可领取的币种");
            }else {
                Log.i("Biyong","红包巳被领完");
                LogUtils.i("红包巳被领完");
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
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("org.telegram.biyongx:id/close_button");
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
                Eventvalue eventvalue = new Eventvalue(findResult.getId(), "findSleeper", findSleeper, "");
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
