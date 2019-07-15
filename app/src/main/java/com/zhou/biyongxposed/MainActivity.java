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
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES;
import static com.zhou.biyongxposed.bingyongserver.coin_unit;
import static com.zhou.biyongxposed.bingyongserver.cointype;
import static com.zhou.biyongxposed.bingyongserver.getCoinUnitOk;

public class MainActivity extends AppCompatActivity {
    private boolean run = false;
    private boolean shoudongsw;
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
    Button shoudong;
    Boolean isopen;
    ListView lv;
    SimpleAdapter mSimpleAdapter;
    private DatabaseHandler dbhandler;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
    /*定义一个动态数组*/
    ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String,Object>>();
    private int l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbhandler=new DatabaseHandler(this);
        run = true;
        handler.postDelayed(task, 1000);//每秒刷新线程，更新Activity
        findsleep = findViewById(R.id.findredsleep);
        clicksleep = findViewById(R.id.clickredsleep);
        flishsleep = findViewById(R.id.finshsleep);
        lightbrige = findViewById(R.id.lightsleep);
        Button button = findViewById(R.id.button);
        Button button2 = findViewById(R.id.button3);
        Button button3 = findViewById(R.id.button4);
        Button button4 = findViewById(R.id.button5);
        Button button5 = findViewById(R.id.button2);
        shoudong = findViewById(R.id.shoudongqiangbao);
        button.setOnClickListener(new clicklisten());
        button2.setOnClickListener(new clicklisten());
        button3.setOnClickListener(new clicklisten());
        button4.setOnClickListener(new clicklisten());
        button5.setOnClickListener(new clicklisten());
        shoudong.setOnClickListener(new clicklisten());
        final Eventvalue findResult = dbhandler.getValueResult("findSleeper");
        if(findResult!=null) {
            findsleep.setText(String.valueOf(findResult.getValue()));
            Log.i("SQL", "findSleeper:" + findResult.getValue());
            EventBus.getDefault().postSticky(new Message<Integer>(1, findResult.getValue()));
        }
        final Eventvalue clickResult = dbhandler.getValueResult("clickSleeper");
        if(clickResult!=null) {
            clicksleep.setText(String.valueOf(clickResult.getValue()));
            Log.i("SQL", "clickResult:" + clickResult.getValue());
            EventBus.getDefault().postSticky(new Message<Integer>(2, clickResult.getValue()));
        }
        final Eventvalue flishResult = dbhandler.getValueResult("flishSleeper");
        if(flishResult!=null) {
            flishsleep.setText(String.valueOf(flishResult.getValue()));
            Log.i("SQL", "flishResult:" + flishResult.getValue());
            EventBus.getDefault().postSticky(new Message<Integer>(3, flishResult.getValue()));
        }
        final Eventvalue lightResult = dbhandler.getValueResult("lightSleeper");
        if(lightResult!=null) {
            lightbrige.setText(String.valueOf(lightResult.getValue()));
            Log.i("SQL", "lightResult:" + lightResult.getValue());
            EventBus.getDefault().postSticky(new Message<Integer>(4, lightResult.getValue()));
        }
        lv= (ListView) findViewById(R.id.hongbaolistview);
        for (int i = 0; i < cointype.length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("coinunit", cointype[i]);
            listItem.add(map);
        }
        mSimpleAdapter = new SimpleAdapter(MainActivity.this, listItem,//需要绑定的数据
                R.layout.cointype,//每一行的布局
                new String[]{"coinunit", "coincount"},//动态数组中的数据源的键对应到定义布局的View中
                new int[]{R.id.coinunit, R.id.coincount}
        );
        lv.setAdapter(mSimpleAdapter);
        autoFlash();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this,"你点击了"+(position+1)+"按钮",Toast.LENGTH_SHORT).show();

            }
        });
    }
    public class clicklisten implements View.OnClickListener {
        public void onClick(View v) {
            /*
             * 下面在editText获取文字用***.getText().toString().trim();
             * 获取数字用Integer.parseInt(***.getText().toString());
             * */
            if (v.getId() == R.id.button) {
                Intent intent = new Intent(MainActivity.this, shuomingActivity.class);
                startActivity(intent);
            }
            if (v.getId() == R.id.button2) {
                try {
                    lightSleep = Integer.parseInt(lightbrige.getText().toString().trim());
                    EventBus.getDefault().postSticky(new Message<Integer>(4, lightSleep));
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "输入错误!", Toast.LENGTH_SHORT).show();
                }
            }
            if (v.getId() == R.id.button3) {
                try {
                    findredsleep = Integer.parseInt(findsleep.getText().toString().trim());
                    EventBus.getDefault().postSticky(new Message<Integer>(1, findredsleep));
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "输入错误!", Toast.LENGTH_SHORT).show();
                }
            }
            if (v.getId() == R.id.button4) {
                try {
                    clickredsleep = Integer.parseInt(clicksleep.getText().toString().trim());
                    EventBus.getDefault().postSticky(new Message<Integer>(2, clickredsleep));
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "输入错误!", Toast.LENGTH_SHORT).show();
                }
            }
            if (v.getId() == R.id.button5) {
                try {
                    flishredsleep = Integer.parseInt(flishsleep.getText().toString().trim());
                    EventBus.getDefault().postSticky(new Message<Integer>(3, flishredsleep));
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "输入错误!", Toast.LENGTH_SHORT).show();
                }
            }
            if (v.getId() == R.id.shoudongqiangbao) {
                if (!shoudongsw) {
                    shoudongsw = true;
                    shoudong.setText("手动模式");
                    shoudong.setTextColor(Color.parseColor("#33FF33"));
                    EventBus.getDefault().postSticky(new Message<Boolean>(5, shoudongsw));
                    return;
                }
                if (shoudongsw) {
                    shoudongsw = false;
                    shoudong.setText("自动模式");
                    shoudong.setTextColor(Color.parseColor("#242323"));
                    EventBus.getDefault().postSticky(new Message<Boolean>(5, shoudongsw));
                    return;
                }
            }
        }
    }
    private void autoFlash(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (run) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.i("biyong","进入线程");
                    for (int m = 0; m < cointype.length; m++) {
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        if (cointype[m].equals(coin_unit)) {
                            map.put("coinunit", cointype[m]);
                            final Eventvalue Result = dbhandler.getValueResult(cointype[m]);
                            map.put("coincout", Double.valueOf(Result.getCoincount()));
                            listItem.add(map);
                        }
                    }
                    mSimpleAdapter = new SimpleAdapter(MainActivity.this, listItem,//需要绑定的数据
                            R.layout.cointype,//每一行的布局
                            new String[]{"coinunit", "coincount"},//动态数组中的数据源的键对应到定义布局的View中
                            new int[]{R.id.coinunit, R.id.coincount}
                    );
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                                lv.setAdapter(mSimpleAdapter);
                        }
                    });
                }
            }
        });
        thread.start();
    }
    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (run) {
                Button serverstatus= findViewById(R.id.serverstatus);
                TextView time= findViewById(R.id.gettime);
                time.setText(df.format(new Date()));
                time.setTextColor(Color.parseColor("#242323"));
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
        if (!EventBus.getDefault().isRegistered(this)){//加上判断
            LogUtils.i("EventBus:没有注册,正在注册!");
            EventBus.getDefault().register(this);
            LogUtils.i("EventBus:注册成功!");
        }
        super.onDestroy();
    }


}