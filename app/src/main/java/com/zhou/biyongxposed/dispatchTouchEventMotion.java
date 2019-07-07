package com.zhou.biyongxposed;

import android.view.MotionEvent;

import java.util.Timer;
import java.util.TimerTask;

public class dispatchTouchEventMotion {
    public static int currentTime=0;
    private Timer mTimer1;
    private TimerTask mTask1;
    public void startTimer()  {
        if (mTimer1 == null && mTask1 == null) {
            mTimer1 = new Timer();
            mTask1 = new TimerTask() {
                @Override
                public void run() {
                    currentTime++;
                }
            };
            //用Timer实例启动计时器, 从0ms开始, 间隔1000ms.
            mTimer1.schedule(mTask1, 0, 1);
        }
    }

    public void stopTimer() {
        if (mTimer1 != null) {
            mTimer1.cancel();
            mTimer1 = null;
        }
        if (mTask1 != null) {
            mTask1.cancel();
            mTask1 = null;
        }
    }
    public boolean onTouchEvent(MotionEvent event) {
        // ▼ 注意这里使用的是 getAction()，先埋一个小尾巴。
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下
                startTimer();
                break;
            case MotionEvent.ACTION_MOVE:
                // 手指移动
                currentTime++;
                break;
            case MotionEvent.ACTION_UP:
                // 手指抬起
                stopTimer();
                break;
        }
        return onTouchEvent(event);
    }
}

