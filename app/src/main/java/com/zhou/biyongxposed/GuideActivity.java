package com.zhou.biyongxposed;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class GuideActivity extends Activity {
    public static boolean run_ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!run_ok) {
            setContentView(R.layout.activity_fullscreen);
            Window window = getWindow();
            window.setGravity(Gravity.CENTER);
            WindowManager.LayoutParams params = window.getAttributes();
            params.x = 0;
            params.y = 0;
            //设置效果为背景透明.
            params.format = PixelFormat.TRANSLUCENT;
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            //设置窗口宽高
            params.height = ScreenUtil.dip2px(this,80);
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
            TextView ms_message = findViewById(R.id.messaeg_ms);
            ms_message.setText("红包任务正在执行");
            run_ok = true;
        }
    }
    public void start(Context context){
        Intent intent = new Intent(context, GuideActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);
    }
}
