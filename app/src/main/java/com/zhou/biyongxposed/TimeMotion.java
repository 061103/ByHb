package com.zhou.biyongxposed;

import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

public class TimeMotion {
    public static int currentTime=0;
    private Timer timer;
    private TimerTask task;
    final Handler mHandler = new Handler();
    private Thread mUiThread;
    private void initTimer() {
        // 初始化计时器
        task = new MyTask();
        timer = new Timer();
    }
    public final void runOnUiThread(Runnable action) {
        if (Thread.currentThread() != mUiThread) {
            mHandler.post(action);
        } else {
            action.run();
        }
    }
    class MyTask extends TimerTask {
        @Override
        public void run() {
            // 初始化计时器
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentTime++;
                }
            });
        }
    }
    private void startTimer() {
        //启动计时器
        /**
         * java.util.Timer.schedule(TimerTask task, long delay, long period)：
         * 这个方法是说，delay/1000秒后执行task,然后进过period/1000秒再次执行task，
         * 这个用于循环任务，执行无数次，当然，你可以用timer.cancel();取消计时器的执行。
         */
        initTimer();
        try {
            timer.schedule(task, 0, 1);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            initTimer();
            timer.schedule(task, 0, 1);
        }
    }
    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}

