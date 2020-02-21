package com.zhou.biyongxposed;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import org.greenrobot.eventbus.EventBus;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import static android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES;
import static com.zhou.biyongxposed.bingyongserver.isRoot;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "biyongmainactivity";
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
    Button Screen_on;
    ListView lv;
    public  SimpleAdapter mSimpleAdapter;
    public  DatabaseHandler dbhandler;
    public ArrayList<String> youxianlist = new ArrayList<>();
    /*定义一个动态数组*/
    ArrayList<HashMap<String, Object>> listItem = new ArrayList<>();
    private MyDialog myDialog;
    public static boolean keep_screen_on;
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbhandler=new DatabaseHandler(this);
        run = true;
        handler.postDelayed(task, 1000);//每秒刷新线程，更新Activity
        lv= findViewById(R.id.hongbaolistview);
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
        Button button8 = findViewById(R.id.zidonghuifu);
        Screen_on = findViewById(R.id.screen_on);
        shoudong = findViewById(R.id.shoudongqiangbao);
        Button dingshi =  findViewById(R.id.dingshikaiqi);
        button.setOnClickListener(new clicklisten());
        button2.setOnClickListener(new clicklisten());
        button3.setOnClickListener(new clicklisten());
        button4.setOnClickListener(new clicklisten());
        button5.setOnClickListener(new clicklisten());
        button6.setOnClickListener(new clicklisten());
        button7.setOnClickListener(new clicklisten());
        shoudong.setOnClickListener(new clicklisten());
        dingshi.setOnClickListener(new clicklisten());
        button8.setOnClickListener(new clicklisten());
        Screen_on.setOnClickListener(new clicklisten());
        biyong.setOnLongClickListener(new clicklonglisten());
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String ss;
                ss = listItem.get(position).toString().substring(listItem.get(position).toString().indexOf("coinunit="),listItem.get(position).toString().indexOf("}"));
                final String aa = ss.substring(9);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("提示");
                builder.setMessage("是否删除？");
                builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Eventvalue Result = dbhandler.getNameResult(aa);
                        if(Result!=null&&Result.getValue()==1) {
                            Eventvalue eventvalue = new Eventvalue(Result.getId(), Result.getName(), Result.getValue(),Result.getCoincount());
                            dbhandler.deleteCoincount(eventvalue);
                            updateListView();
                        }else
                            Toast.makeText(MainActivity.this, aa+"不存在,无法删除.", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("否", null);
                builder.show();
                return false;
            }
        });
        new updateInputParms().start();
        handler.post(new Runnable(){
            @Override
            public void run() {
                if(!isEnabled()) startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                float_permission();
            }
        });
    }
    public class clicklonglisten implements View.OnLongClickListener{
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
                    updateListView();
                }
            });
            normalDialog.setNegativeButton("取消",null);
            normalDialog.show();// 显示
            return false;
        }
    }
    public class clicklisten implements View.OnClickListener {
            @SuppressLint("WakelockTimeout")
            public void onClick(View v) {
                /*
                 * EditText获取数字用Integer.parseInt(***.getText().toString());
                 *
                 * */
                if (v.getId() == R.id.button) {
                    Intent intent = new Intent(MainActivity.this, shuomingActivity.class);
                    startActivity(intent);
                }
                if (v.getId() == R.id.button2) {
                    try {
                        lightSleep = Integer.parseInt(lightbrige.getText().toString().trim());
                        if (lightSleep > 0) {
                            EventBus.getDefault().postSticky(new Message<>(3, lightSleep));
                        } else
                            Toast.makeText(MainActivity.this, "请输入大于0的整数!", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "输入错误!", Toast.LENGTH_SHORT).show();
                    }
                }
                if (v.getId() == R.id.button3) {
                    try {
                        findredsleep = Integer.parseInt(findsleep.getText().toString().trim());
                        if (findredsleep > 10) {
                            EventBus.getDefault().postSticky(new Message<>(0, findredsleep));
                        } else
                            Toast.makeText(MainActivity.this, "请输入大于10的整数!", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "输入错误!", Toast.LENGTH_SHORT).show();
                    }
                }
                if (v.getId() == R.id.button4) {
                    try {
                        clickredsleep = Integer.parseInt(clicksleep.getText().toString().trim());
                        if (clickredsleep > 10) {
                            EventBus.getDefault().postSticky(new Message<>(1, clickredsleep));
                        } else
                            Toast.makeText(MainActivity.this, "请输入大于10的整数!", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "输入错误!", Toast.LENGTH_SHORT).show();
                    }
                }
                if (v.getId() == R.id.button5) {
                    try {
                        flishredsleep = Integer.parseInt(flishsleep.getText().toString().trim());
                        if (flishredsleep > 1500) {
                            EventBus.getDefault().postSticky(new Message<>(2, flishredsleep));
                        } else
                            Toast.makeText(MainActivity.this, "请输入大于1500的整数!", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "输入错误!", Toast.LENGTH_SHORT).show();
                    }
                }
                if (v.getId() == R.id.button6) {//优先币种
                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    @SuppressLint("InflateParams") final View myview = inflater.inflate(R.layout.addcoindialog, null);//引用自定义布局
                    final ListView youxian = myview.findViewById(R.id.youxianlistview);
                    final Button add = myview.findViewById(R.id.button9);
                    final Button del = myview.findViewById(R.id.button8);
                    final EditText coinTypeText = myview.findViewById(R.id.editText);
                    youxianlist.clear();
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, youxianlist);
                    youxian.setAdapter(adapter);
                    getcointype();
                    ArrayAdapter<String> adapterlist = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, youxianlist);//新建并配置ArrayAapeter
                    youxian.setAdapter(adapterlist);
                    youxian.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            coinTypeText.setText(youxianlist.get(position));
                        }
                    });
                    add.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!coinTypeText.getText().toString().isEmpty()) {
                                try {
                                    final Eventvalue Result = dbhandler.getNameResult(coinTypeText.getText().toString());
                                    if (Result != null && Result.getValue() == 2) {
                                        Toast.makeText(MainActivity.this, "该币种巳存在" + coinTypeText.getText().toString(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Eventvalue eventvalue = new Eventvalue(null, coinTypeText.getText().toString(), 2, "coin");
                                        dbhandler.addValue(eventvalue);
                                        youxianlist.clear();
                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, youxianlist);
                                        youxian.setAdapter(adapter);
                                        getcointype();
                                        ArrayAdapter<String> adapterlist = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, youxianlist);//新建并配置ArrayAapeter
                                        youxian.setAdapter(adapterlist);
                                        coinTypeText.setText("");
                                        Toast.makeText(MainActivity.this, "巳添加", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else
                                Toast.makeText(MainActivity.this, "请不要输入空值!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    del.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!coinTypeText.getText().toString().isEmpty()) {
                                final Eventvalue Result = dbhandler.getNameResult(coinTypeText.getText().toString());
                                if (Result != null && Result.getValue() == 2) {
                                    Eventvalue eventvalue = new Eventvalue(Result.getId(), coinTypeText.getText().toString(), 2, "coin");
                                    dbhandler.deleteValue(eventvalue);
                                    youxianlist.clear();
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, youxianlist);
                                    youxian.setAdapter(adapter);
                                    getcointype();
                                    ArrayAdapter<String> adapterlist = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, youxianlist);//新建并配置ArrayAapeter
                                    youxian.setAdapter(adapterlist);
                                    coinTypeText.setText("");
                                    Toast.makeText(MainActivity.this, "巳删除", Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(MainActivity.this, "该币种不存在!", Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(MainActivity.this, "请不要输入空值!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    new AlertDialog.Builder(MainActivity.this).setView(myview).show();

                }
                if (v.getId() == R.id.button7) {//清零币种计数
                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    @SuppressLint("InflateParams") final View myview = inflater.inflate(R.layout.deletecoinlayout, null);//引用自定义布局
                    final Button yes = myview.findViewById(R.id.button11);
                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText yesedit = myview.findViewById(R.id.editText2);
                            if (!yesedit.getText().toString().isEmpty()) {
                                final Eventvalue findResult = dbhandler.getNameResult(yesedit.getText().toString());
                                if (findResult != null && findResult.getValue() == 1) {
                                    Eventvalue eventvalue = new Eventvalue(findResult.getId(), findResult.getName(), 1, String.valueOf(0));
                                    dbhandler.addValue(eventvalue);
                                    updateListView();
                                    Toast.makeText(MainActivity.this, "巳清零" + yesedit.getText().toString(), Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(MainActivity.this, "没有该币种!", Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(MainActivity.this, "请不要输入空值!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    new AlertDialog.Builder(MainActivity.this).setView(myview).show();
                }
                if (v.getId() == R.id.shoudongqiangbao) {
                    if (!shoudongsw) {
                        shoudongsw = true;
                        shoudong.setText("手动模式");
                        shoudong.setTextColor(Color.parseColor("#242323"));
                        EventBus.getDefault().postSticky(new Message<>(4, shoudongsw));
                        return;
                    }
                    shoudongsw = false;
                    shoudong.setText("自动模式");
                    shoudong.setTextColor(Color.parseColor("#4CAF50"));
                    EventBus.getDefault().postSticky(new Message<>(4, shoudongsw));
                }
                if (v.getId() == R.id.zidonghuifu) {
                    Intent intent = new Intent(MainActivity.this, zidonghuihu.class);
                    startActivity(intent);
                }
                if (v.getId() == R.id.dingshikaiqi) {
                    @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.dialog_layout, null);
                    myDialog = new MyDialog(MainActivity.this, 0, 0, view, R.style.MyDialog);
                    myDialog.setCancelable(true);
                    myDialog.show();
                    Button bt_sure = view.findViewById(R.id.bt_sure);
                    Button bt_clean = view.findViewById(R.id.bt_clean);
                    final EditText editText_begin = view.findViewById(R.id.edit_begin);
                    final EditText editText_end = view.findViewById(R.id.edit_end);
                    if (dbhandler.getNameResult("begin_time") != null) {
                        editText_begin.setText("");
                        int begin_time = dbhandler.getNameResult("begin_time").getValue();
                        editText_begin.setText(String.valueOf(begin_time));
                    }
                    if (dbhandler.getNameResult("end_time") != null) {
                        editText_end.setText("");
                        int end_time = dbhandler.getNameResult("end_time").getValue();
                        editText_end.setText(String.valueOf(end_time));
                    }
                    bt_sure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String begin = editText_begin.getText().toString().trim();
                            String end = editText_end.getText().toString().trim();
                            if (begin.length() > 0 && end.length() > 0) {
                                EventBus.getDefault().postSticky(new Message<>(6, Integer.parseInt(editText_begin.getText().toString())));
                                EventBus.getDefault().postSticky(new Message<>(7, Integer.parseInt(editText_end.getText().toString())));
                                myDialog.cancel();
                            } else
                                Toast.makeText(MainActivity.this, "请不要输入空值!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    bt_clean.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            myDialog.cancel();
                        }
                    });
                }
                if (v.getId() == R.id.screen_on) {
                    if (!keep_screen_on) {
                        keep_screen_on = true;
                        Screen_on.setText("屏幕常亮");
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        Screen_on.setTextColor(Color.parseColor("#4CAF50"));
                        return;
                    }
                    keep_screen_on = false;
                    Screen_on.setText("屏幕常亮");
                    Screen_on.setTextColor(Color.parseColor("#242323"));
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
    }
    public void getcointype() {
        for (int i = 0; i <dbhandler.dbquery().size(); i++) {
            int Result = dbhandler.dbquery().get(i).getValue();
            if (Result != 2) {
                continue;
            }
            youxianlist.add(dbhandler.dbquery().get(i).getName());
        }
    }
    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            if (run) {
                Button serverstatus = findViewById(R.id.serverstatus);
                if (isAccessibilitySettingsOn(MainActivity.this)) {
                    serverstatus.setText("服务开启");
                    serverstatus.setTextColor(Color.parseColor("#990066"));
                    serverstatus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(Build.VERSION.SDK_INT<=23) {
                                    String enabledServicesSetting = Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                                    enabledServicesSetting = enabledServicesSetting.replace(":com.zhou.biyongxposed/com.zhou.biyongxposed.bingyongserver", "");
                                    Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, enabledServicesSetting);
                                    String checkenabledServicesSetting = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                                    if (checkenabledServicesSetting!=null&&enabledServicesSetting.contains(":com.zhou.biyongxposed/com.zhou.biyongxposed.bingyongserver")) {
                                        if(isRoot) {
                                            execShellCmd("settings put secure enabled_accessibility_services" + enabledServicesSetting);
                                            Log.i(TAG, "执行ADB命令关闭服务成功");
                                        }
                                    }
                                    Log.i(TAG, "关闭服务成功");
                                } else {
                                openAccessibilityWindown();
                            }
                        }
                    });
                    Eventvalue Result = dbhandler.getNameResult("server_status");
                    if (Result != null && Result.getName().equals("server_status") && Result.getValue() == 3) {
                        Eventvalue eventvalue = new Eventvalue(Result.getId(), Result.getName(), Result.getValue(), String.valueOf(1));
                        dbhandler.addValue(eventvalue);
                    } else {
                        Eventvalue eventvalue = new Eventvalue(null, "server_status", 3, String.valueOf(1));
                        dbhandler.addValue(eventvalue);
                    }
                }else {
                    serverstatus.setText("服务关闭");
                    serverstatus.setTextColor(Color.parseColor("#999999"));
                    serverstatus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(Build.VERSION.SDK_INT<=23) {
                                String enabledServicesSetting = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                                if (enabledServicesSetting==null|| !enabledServicesSetting.contains("com.zhou.biyongxposed/com.zhou.biyongxposed.bingyongserver")) {
                                    enabledServicesSetting = enabledServicesSetting + ":com.zhou.biyongxposed/com.zhou.biyongxposed.bingyongserver";
                                }
                                Settings.Secure.putString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,enabledServicesSetting);
                                Settings.Secure.putInt(getContentResolver(),Settings.Secure.ACCESSIBILITY_ENABLED, 1);
                                String checkenabledServicesSetting = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                                if (checkenabledServicesSetting==null||!enabledServicesSetting.contains(":com.zhou.biyongxposed/com.zhou.biyongxposed.bingyongserver")) {
                                    if(isRoot) {
                                        execShellCmd("settings put secure enabled_accessibility_services" + enabledServicesSetting);
                                        execShellCmd("settings put secure accessibility_enabled 1");
                                        Log.i(TAG, "执行ADB命令开启服务成功");
                                    }
                                }
                                Log.i(TAG, "开启服务成功");
                            }else {
                                openAccessibilityWindown();
                            }
                        }
                    });
                    Eventvalue Result = dbhandler.getNameResult("server_status");
                    if (Result != null && Result.getName().equals("server_status") && Result.getValue() == 3) {
                        Eventvalue eventvalue = new Eventvalue(Result.getId(), Result.getName(), Result.getValue(), String.valueOf(0));
                        dbhandler.addValue(eventvalue);
                    } else {
                        Eventvalue eventvalue = new Eventvalue(null, "server_status", 3, String.valueOf(0));
                        dbhandler.addValue(eventvalue);
                    }
                }
                handler.postDelayed(this, 1000);
            }
        }
    };

    private void openAccessibilityWindown() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        String accInfo = "com.zhou.biyongxposed/com.zhou.biyongxposed.bingyongserver";
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.v("BIYONGTAG","辅助服务列表没有找到包名为:"+ accInfo +"的服务!");
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(), ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(accInfo)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void updateListView() {
            listItem.clear();
            mSimpleAdapter = new SimpleAdapter(MainActivity.this, listItem,//需要绑定的数据
                    R.layout.cointype,//每一行的布局
                    new String[]{"coinunit", "coincount"},//动态数组中的数据源的键对应到定义布局的View中
                    new int[]{R.id.coinunit, R.id.coincount}
            );
            lv.setAdapter(mSimpleAdapter);
            for (int i = 0; i < dbhandler.dbquery().size(); i++) {
                HashMap<String, Object> map = new HashMap<>();
                int Result = dbhandler.dbquery().get(i).getValue();
                if(Result!=1){
                    continue;
                }
                map.put("coinunit", dbhandler.dbquery().get(i).getName());
                map.put("coincount", dbhandler.dbquery().get(i).getCoincount());
                listItem.add(map);
            }
            mSimpleAdapter = new SimpleAdapter(MainActivity.this, listItem,//需要绑定的数据
                    R.layout.cointype,//每一行的布局
                    new String[]{"coinunit", "coincount"},//动态数组中的数据源的键对应到定义布局的View中
                    new int[]{R.id.coinunit, R.id.coincount}
            );
            lv.setAdapter(mSimpleAdapter);
    }

    class updateInputParms extends Thread{
        @Override
        public void run(){
            final Eventvalue findResult = dbhandler.getNameResult("findSleeper");
            if(findResult!=null) {
                findsleep.setText(String.valueOf(findResult.getValue()));
                Log.i("Biyong", "findSleeper:" + findResult.getValue());
            }
            final Eventvalue clickResult = dbhandler.getNameResult("clickSleeper");
            if(clickResult!=null) {
                clicksleep.setText(String.valueOf(clickResult.getValue()));
                Log.i("Biyong", "clickResult:" + clickResult.getValue());
            }
            final Eventvalue flishResult = dbhandler.getNameResult("flishSleeper");
            if(flishResult!=null) {
                flishsleep.setText(String.valueOf(flishResult.getValue()));
                Log.i("Biyong", "flishResult:" + flishResult.getValue());
            }
            final Eventvalue lightResult = dbhandler.getNameResult("lightSleeper");
            if(lightResult!=null) {
                lightbrige.setText(String.valueOf(lightResult.getValue()));
                Log.i("Biyong", "lightResult:" + lightResult.getValue());
            }
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

    private boolean isEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            int i = 0;
            while (i < names.length) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
                i++;
            }
        }
        return false;
    }

    private void float_permission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(this, BiyongServer.class);
                startService(intent);
            } else {
                //若没有权限，提示获取.
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                Toast.makeText(this, "需要取得权限才能使用悬浮窗功能", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        } else{
            Toast.makeText(this, "需要手动开启悬浮窗功能", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     *
     * @return 应用程序是/否获取Root权限
     */
    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd="chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        try {
            return process.waitFor()==0;
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        Intent intent = new Intent(this,BiyongServer.class);
        startService(intent);
        updateListView();
    }
    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        youxianlist.clear();
        Intent intent = new Intent(this,BiyongServer.class);
        stopService(intent);
        super.onDestroy();
    }
}