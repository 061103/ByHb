package com.zhou.biyongxposed;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private boolean run = false;
    private final Handler handler = new Handler();
    long lastBack = 0;
    int findredsleep;
    int clickredsleep;
    int flishredsleep;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        run = true;
        handler.postDelayed(task, 1000);//每秒刷新线程，更新Activity
        EditText findsleep=findViewById(R.id.findredsleep);
        EditText clicksleep=findViewById(R.id.clickredsleep);
        EditText flishsleep=findViewById(R.id.finshsleep);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(this);
        /*
        * 下面在editText获取文字用***.getText().toString().trim();
        * 获取数字用Integer.parseInt(***.getText().toString());
        * */
        try {
             findredsleep = Integer.parseInt(findsleep.getText().toString());
            clickredsleep = Integer.parseInt(clicksleep.getText().toString());
            flishredsleep = Integer.parseInt(flishsleep.getText().toString());
        } catch (NumberFormatException e) {
             findredsleep = 0;
             clickredsleep =0;
             flishredsleep =0;
        }
    }
    @Override
    public void onClick(View view) {
            switch (view.getId()){
                case R.id.button:
                    Intent intent = new Intent(MainActivity.this, shuomingActivity.class);
                    startActivity(intent);
                    break;
                case R.id.button3:
                    Toast.makeText(MainActivity.this,"点击了找到红包的确定按钮", Toast.LENGTH_SHORT).show();
                    Message message=new Message();
                    message.setShu(findredsleep);
                    EventBus.getDefault().postSticky(message);
                    break;
                case R.id.button4:
                    Message essage=new Message();
                    essage.setShu(clickredsleep);
                    EventBus.getDefault().postSticky(essage);
                    break;
                case R.id.button5:
                    Message ssage=new Message();
                    ssage.setShu(flishredsleep);
                    EventBus.getDefault().postSticky(ssage);
                    break;
            }
        }
    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (run) {
                Button serverstatus= findViewById(R.id.serverstatus);
                if(isAccessibilitySettingsOn(MainActivity.this)){
                    serverstatus.setText("开启");
                    serverstatus.setTextColor(Color.parseColor("#33FF33"));
                    serverstatus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Settings.Secure.putString(getContentResolver(), ENABLED_ACCESSIBILITY_SERVICES, "com.zhou.biyongxposed/com.zhou.biyongxposed.bingyongserver" );
                            Settings.Secure.putInt(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, 0);
                        }
                    });
                }else {
                    serverstatus.setText("关闭");
                    serverstatus.setTextColor(Color.parseColor("#999999"));
                    serverstatus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Settings.Secure.putString(getContentResolver(), ENABLED_ACCESSIBILITY_SERVICES, "com.zhou.biyongxposed/com.zhou.biyongxposed.bingyongserver");
                            Settings.Secure.putInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 1);
                        }
                    });
                }
                handler.postDelayed(this, 1000);
            }
        }
    };
    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        String accInfo = "com.zhou.biyongxposed/com.zhou.biyongxposed.bingyongserver";
        final String service = accInfo;
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("biyong", "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(), ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }else {
            Log.v("biyong", accInfo +"服务巳关闭");
        }
        return false;
    }
    /*
     *  新版本需要手动的添加注解@Subscribe(这是必不可少的)
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND,sticky = true)
    public void onEventMainThread(Message type) {
        if (type != null) {
            findredsleep = type.getShu();
            Toast.makeText(this,findredsleep , Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 再次返回键退出程序
     */
    @Override
    public void onBackPressed() {
        if (lastBack == 0 || System.currentTimeMillis() - lastBack > 2000) {
            Toast.makeText(MainActivity.this, "再按一次返回退出辅助程序", Toast.LENGTH_SHORT).show();
            lastBack = System.currentTimeMillis();
            return;
        }
        super.onBackPressed();
    }
    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this))//加上判断
            EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


}