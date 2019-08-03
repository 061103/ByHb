package com.zhou.biyongxposed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import static android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES;

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
    EditText adddeletecoin;
    EditText delcountcoin;
    Button shoudong;
    ListView lv;
    public EditText editadd;
    public static SimpleAdapter mSimpleAdapter;
    private DatabaseHandler dbhandler;
    public static ArrayList<String> youxianlist = new ArrayList<>();
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
    /*定义一个动态数组*/
    ArrayList<HashMap<String, Object>> listItem = new ArrayList<>();

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
        adddeletecoin = findViewById(R.id.editText);
        delcountcoin = findViewById(R.id.editText2);
        TextView biyong = findViewById(R.id.biyong);
        Button button = findViewById(R.id.button);
        Button button2 = findViewById(R.id.button3);
        Button button3 = findViewById(R.id.button4);
        Button button4 = findViewById(R.id.button5);
        Button button5 = findViewById(R.id.button2);
        Button button6 = findViewById(R.id.button6);
        Button button7 = findViewById(R.id.button7);
        shoudong = findViewById(R.id.shoudongqiangbao);
        button.setOnClickListener(new clicklisten());
        button2.setOnClickListener(new clicklisten());
        button3.setOnClickListener(new clicklisten());
        button4.setOnClickListener(new clicklisten());
        button5.setOnClickListener(new clicklisten());
        button6.setOnClickListener(new clicklisten());
        button7.setOnClickListener(new clicklisten());
        shoudong.setOnClickListener(new clicklisten());
        final Eventvalue findResult = dbhandler.getNameResult("findSleeper");
        if(findResult!=null) {
            findsleep.setText(String.valueOf(findResult.getValue()));
            Log.i("biyongzhou", "findSleeper:" + findResult.getValue());
        }
        final Eventvalue clickResult = dbhandler.getNameResult("clickSleeper");
        if(clickResult!=null) {
            clicksleep.setText(String.valueOf(clickResult.getValue()));
            Log.i("biyongzhou", "clickResult:" + clickResult.getValue());
        }
        final Eventvalue flishResult = dbhandler.getNameResult("flishSleeper");
        if(flishResult!=null) {
            flishsleep.setText(String.valueOf(flishResult.getValue()));
            Log.i("biyongzhou", "flishResult:" + flishResult.getValue());
        }
        final Eventvalue lightResult = dbhandler.getNameResult("lightSleeper");
        if(lightResult!=null) {
            lightbrige.setText(String.valueOf(lightResult.getValue()));
            Log.i("biyongzhou", "lightResult:" + lightResult.getValue());
        }
        lv= findViewById(R.id.hongbaolistview);
        lv.setAdapter(mSimpleAdapter);
        for (int i = 1; i <= (dbhandler.getelementCounts()); i++) {
            HashMap<String, Object> map = new HashMap<>();
            Eventvalue Result = dbhandler.getIdResult(String.valueOf(i));
            if (Result != null && Result.getValue() == 1) {
                map.put("coinunit", Result.getName());
                map.put("coincount", Result.getCoincount());
                listItem.add(map);
            }
        }
        mSimpleAdapter = new SimpleAdapter(MainActivity.this, listItem,//需要绑定的数据
                R.layout.cointype,//每一行的布局
                new String[]{"coinunit", "coincount"},//动态数组中的数据源的键对应到定义布局的View中
                new int[]{R.id.coinunit, R.id.coincount}
        );
        lv.setAdapter(mSimpleAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this,"你点击了"+(position+1)+"按钮",Toast.LENGTH_SHORT).show();

            }
        });
        biyong.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final AlertDialog.Builder normalDialog = new AlertDialog.Builder(MainActivity.this);
                normalDialog.setTitle("警告！");
                normalDialog.setMessage("你正在执行清除数据库的操作,是否继续?");
                normalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbhandler.deleteDatabase();
                                Toast.makeText(MainActivity.this, "数据库巳清空!", Toast.LENGTH_SHORT).show();
                            }
                        });
                normalDialog.setNegativeButton("取消",null);
                normalDialog.show();// 显示
                return false;
            }
        });
        getcointype();
        new refreshcoin().start();//执行启动线程操作
    }
    public class clicklisten implements View.OnClickListener {

        @SuppressLint("WakelockTimeout")
        public void onClick(View v) {
            /*
             * EditText获取数字用Integer.parseInt(***.getText().toString());
             * */
            if (v.getId() == R.id.button) {
                Intent intent = new Intent(MainActivity.this, shuomingActivity.class);
                startActivity(intent);
            }
            if (v.getId() == R.id.button2) {
                try {
                    lightSleep = Integer.parseInt(lightbrige.getText().toString().trim());
                    if(lightSleep>100) {
                        EventBus.getDefault().postSticky(new Message<>(3, lightSleep));
                    }else Toast.makeText(MainActivity.this, "请输入大于200的整数!", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "输入错误!", Toast.LENGTH_SHORT).show();
                }
            }
            if (v.getId() == R.id.button3) {
                try {
                    findredsleep = Integer.parseInt(findsleep.getText().toString().trim());
                    if(findredsleep>100) {
                        EventBus.getDefault().postSticky(new Message<>(0, findredsleep));
                    }else Toast.makeText(MainActivity.this, "请输入大于100的整数!", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "输入错误!", Toast.LENGTH_SHORT).show();
                }
            }
            if (v.getId() == R.id.button4) {
                try {
                    clickredsleep = Integer.parseInt(clicksleep.getText().toString().trim());
                    if(clickredsleep>100) {
                        EventBus.getDefault().postSticky(new Message<>(1, clickredsleep));
                    }else Toast.makeText(MainActivity.this, "请输入大于100的整数!", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "输入错误!", Toast.LENGTH_SHORT).show();
                }
            }
            if (v.getId() == R.id.button5) {
                try {
                    flishredsleep = Integer.parseInt(flishsleep.getText().toString().trim());
                    if(flishredsleep>1200) {
                        EventBus.getDefault().postSticky(new Message<>(2, flishredsleep));
                    }else Toast.makeText(MainActivity.this, "请输入大于1200的整数!", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "输入错误!", Toast.LENGTH_SHORT).show();
                }
            }
            if(v.getId()== R.id.button6){//优先币种
                LayoutInflater inflater=LayoutInflater.from( MainActivity.this );
                @SuppressLint("InflateParams") final View myview=inflater.inflate(R.layout.addcoindialog,null);//引用自定义布局
                final ListView youxian = myview.findViewById(R.id.youxianlistview);
                final Button add = myview.findViewById(R.id.button9);
                final Button del = myview.findViewById(R.id.button8);
                youxianlist.clear();
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, youxianlist);
                youxian.setAdapter(adapter);
                getcointype();
                ArrayAdapter<String> adapterlist = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, youxianlist);//新建并配置ArrayAapeter
                youxian.setAdapter(adapterlist);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editadd = myview.findViewById(R.id.editText);
                        if(!editadd.getText().toString().isEmpty()) {
                            try {
                                    final Eventvalue Result = dbhandler.getNameResult(editadd.getText().toString());
                                    if(Result!=null&&Result.getValue()==2){
                                        Toast.makeText(MainActivity.this, "该币种巳存在" + editadd.getText().toString(), Toast.LENGTH_SHORT).show();
                                    }else {
                                            Eventvalue eventvalue = new Eventvalue(null, editadd.getText().toString(), 2, "coin");
                                            dbhandler.addValue(eventvalue);
                                            youxianlist.clear();
                                            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, youxianlist);
                                            youxian.setAdapter(adapter);
                                            getcointype();
                                            ArrayAdapter<String> adapterlist = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, youxianlist);//新建并配置ArrayAapeter
                                            youxian.setAdapter(adapterlist);
                                            editadd.setText("");
                                        Toast.makeText(MainActivity.this, "巳添加", Toast.LENGTH_SHORT).show();
                                    }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else Toast.makeText(MainActivity.this, "请不要输入空值!", Toast.LENGTH_SHORT).show();
                    }
                });
                del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final EditText editdel = myview.findViewById(R.id.editText);
                        if(!editdel.getText().toString().isEmpty()) {
                            final Eventvalue Result = dbhandler.getNameResult(editdel.getText().toString());
                            if(Result!=null&&Result.getValue()==2) {
                                Eventvalue eventvalue = new Eventvalue(Result.getType(), editdel.getText().toString(), Result.getValue(), Result.getCoincount());
                                dbhandler.deleteValue(eventvalue);
                                getcointype();
                                ArrayAdapter<String> adapterlist = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, youxianlist);//新建并配置ArrayAapeter
                                youxian.setAdapter(adapterlist);
                                editdel.setText("");
                                Toast.makeText(MainActivity.this, "巳删除", Toast.LENGTH_SHORT).show();
                            }else Toast.makeText(MainActivity.this, "该币种不存在!", Toast.LENGTH_SHORT).show();
                        }else Toast.makeText(MainActivity.this, "请不要输入空值!", Toast.LENGTH_SHORT).show();
                    }
                });
                new AlertDialog.Builder(MainActivity.this).setView(myview).show();

            }
            if(v.getId()== R.id.button7){//清零币种计数
                LayoutInflater inflater=LayoutInflater.from( MainActivity.this );
                @SuppressLint("InflateParams") final View myview=inflater.inflate(R.layout.deletecoinlayout,null);//引用自定义布局
                final Button yes = myview.findViewById(R.id.button11);
                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText yesedit = myview.findViewById(R.id.editText2);
                        if(!yesedit.getText().toString().isEmpty()){
                            final Eventvalue findResult = dbhandler.getNameResult(yesedit.getText().toString());
                            if(findResult!=null) {
                                Eventvalue eventvalue = new Eventvalue(findResult.getType(), findResult.getName(), 1, String.valueOf(0));
                                dbhandler.addValue(eventvalue);
                                Toast.makeText(MainActivity.this, "巳清零"+yesedit.getText().toString(), Toast.LENGTH_SHORT).show();
                            }else Toast.makeText(MainActivity.this, "没有该币种!", Toast.LENGTH_SHORT).show();
                        }else Toast.makeText(MainActivity.this, "请不要输入空值!", Toast.LENGTH_SHORT).show();
                    }
                });
                new AlertDialog.Builder(MainActivity.this).setView(myview).show();
            }
            if (v.getId() == R.id.shoudongqiangbao) {
                if (!shoudongsw) {
                    shoudongsw = true;
                    shoudong.setText("手动模式");
                    shoudong.setTextColor(Color.parseColor("#33FF33"));
                    EventBus.getDefault().postSticky(new Message<>(4, shoudongsw));
                    return;
                }
                if (shoudongsw) {
                    shoudongsw = false;
                    shoudong.setText("自动模式");
                    shoudong.setTextColor(Color.parseColor("#242323"));
                    EventBus.getDefault().postSticky(new Message<>(4, shoudongsw));
                }
            }
        }
    }

    private  void getcointype() {
        for (int i = 1; i <= (dbhandler.getelementCounts()); i++) {
            Eventvalue Result = dbhandler.getIdResult(String.valueOf(i));
            if (Result != null && Result.getValue() == 2) {
                youxianlist.add(Result.getName());
            }
        }
    }
    public class refreshcoin extends Thread{
        public void run(){
            while (run) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(new Runnable() {
                    public void run() {
                    }
                });
            }
        }
    private final Runnable task = new Runnable() {
        @Override
        public void run() {
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
    /**
     * 调用onCreate(), 目的是刷新数据,  从另一activity界面返回到该activity界面时, 此方法自动调用
     */
    @Override

    protected void onResume() {
        super.onResume();
        listItem.clear();
        mSimpleAdapter = new SimpleAdapter(MainActivity.this, listItem,//需要绑定的数据
                R.layout.cointype,//每一行的布局
                new String[]{"coinunit", "coincount"},//动态数组中的数据源的键对应到定义布局的View中
                new int[]{R.id.coinunit, R.id.coincount}
        );
        lv.setAdapter(mSimpleAdapter);
        for (int i = 1; i <= (dbhandler.getelementCounts()); i++) {
            HashMap<String, Object> map = new HashMap<>();
            Eventvalue Result = dbhandler.getIdResult(String.valueOf(i));
            if (Result != null && Result.getValue() == 1) {
                map.put("coinunit", Result.getName());
                map.put("coincount", Result.getCoincount());
                listItem.add(map);
            }
        }
        mSimpleAdapter = new SimpleAdapter(MainActivity.this, listItem,//需要绑定的数据
                R.layout.cointype,//每一行的布局
                new String[]{"coinunit", "coincount"},//动态数组中的数据源的键对应到定义布局的View中
                new int[]{R.id.coinunit, R.id.coincount}
        );
        lv.setAdapter(mSimpleAdapter);
    }
    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        youxianlist.clear();
        super.onDestroy();
    }


}