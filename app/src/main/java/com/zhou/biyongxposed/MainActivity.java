package com.zhou.biyongxposed;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

import static android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES;


public class MainActivity extends AppCompatActivity {
    private boolean run = false;
    private final Handler handler = new Handler();
    long lastBack = 0;
    int findredsleep;
    int clickredsleep;
    int flishredsleep;
    int lightSleep;
    EditText findsleep;
    EditText clicksleep;
    EditText flishsleep;
    EditText lightbrige;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        run = true;
        handler.postDelayed(task, 1000);//每秒刷新线程，更新Activity
        Button button = findViewById(R.id.button);
        Button button2 = findViewById(R.id.button3);
        Button button3 = findViewById(R.id.button4);
        Button button4 = findViewById(R.id.button5);
        Button button5 = findViewById(R.id.button2);
        button.setOnClickListener(new clicklisten());
        button2.setOnClickListener(new clicklisten());
        button3.setOnClickListener(new clicklisten());
        button4.setOnClickListener(new clicklisten());
        button5.setOnClickListener(new clicklisten());
    }
    public class clicklisten implements View.OnClickListener {

        public void onClick(View v) {
            /*
             * 下面在editText获取文字用***.getText().toString().trim();
             * 获取数字用Integer.parseInt(***.getText().toString());
             * */
            findsleep = findViewById(R.id.findredsleep);
            clicksleep = findViewById(R.id.clickredsleep);
            flishsleep = findViewById(R.id.finshsleep);
            lightbrige = findViewById(R.id.lightsleep);
            if (v.getId() == R.id.button) {
                Intent intent = new Intent(MainActivity.this, shuomingActivity.class);
                startActivity(intent);
            }
            if (v.getId() == R.id.button2) {
                try{
                    lightSleep = Integer.parseInt(lightbrige.getText().toString().trim());
                    EventBus.getDefault().postSticky(new Message<Integer>(4, lightSleep));
                }catch (NumberFormatException e){Toast.makeText(MainActivity.this, "输入错误!", Toast.LENGTH_SHORT).show();}
            }
            if (v.getId() == R.id.button3) {
                try{
                    findredsleep = Integer.parseInt(findsleep.getText().toString().trim());
                    EventBus.getDefault().postSticky(new Message<Integer>(1, findredsleep));
                }catch (NumberFormatException e){Toast.makeText(MainActivity.this, "输入错误!", Toast.LENGTH_SHORT).show();}
            }
            if (v.getId() == R.id.button4) {
                try{
                    clickredsleep = Integer.parseInt(clicksleep.getText().toString().trim());
                    EventBus.getDefault().postSticky(new Message<Integer>(2, clickredsleep));
                }catch (NumberFormatException e){ Toast.makeText(MainActivity.this, "输入错误!", Toast.LENGTH_SHORT).show();}
            }
            if (v.getId() == R.id.button5) {
                try{
                    flishredsleep = Integer.parseInt(flishsleep.getText().toString().trim());
                    EventBus.getDefault().postSticky(new Message<Integer>(3, flishredsleep));
                }catch (NumberFormatException e){ Toast.makeText(MainActivity.this, "输入错误!", Toast.LENGTH_SHORT).show();}
            }
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
            Log.v("BIYONGTAG","辅助服务列表没有找到包名为:"+service+"的服务!");
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
            Log.v("BIYONGTAG", accInfo +"服务巳关闭");
        }
        return false;
    }
    /**
     * 判断object是否为基本类型
     * @param object
     * @return
     */
        public static boolean isBaseType(Object object) {
            Class className = object.getClass();
            if (className.equals(java.lang.Integer.class) ||
                    className.equals(java.lang.Byte.class) ||
                    className.equals(java.lang.Long.class) ||
                    className.equals(java.lang.Double.class) ||
                    className.equals(java.lang.Float.class) ||
                    className.equals(java.lang.Character.class) ||
                    className.equals(java.lang.Short.class) ||
                    className.equals(java.lang.Boolean.class)) {
                return true;
            }
            return false;
        }
    /**
     * 再次返回键退出程序
     */
    @Override
    public void onBackPressed() {
        if (lastBack == 0 || System.currentTimeMillis() - lastBack > 2000) {
            Toast.makeText(MainActivity.this, "再按一次返回退出", Toast.LENGTH_SHORT).show();
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