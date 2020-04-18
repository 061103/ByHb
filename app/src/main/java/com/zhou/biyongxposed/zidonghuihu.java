package com.zhou.biyongxposed;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import org.greenrobot.eventbus.EventBus;
import java.util.ArrayList;
import java.util.HashMap;

public class zidonghuihu extends AppCompatActivity {
    private static final String TAG = "AutoReply";
    private Button zdhf;
    private boolean zdhfmessage;
    private ListView zidonghuifuList;
    private EditText huifuyuju;
    public  SimpleAdapter huifuadapter;
    /*定义一个动态数组*/
    public ArrayList<HashMap<String, Object>> huifulistItem = new ArrayList<>();
    final DatabaseHandler dbhandler = new DatabaseHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_huifu);
        zdhf = findViewById(R.id.zidonghuifubutton);
        Button click_true = findViewById(R.id.huifu_true);
        Button click_clean = findViewById(R.id.huifu_clean);
        zidonghuifuList = findViewById(R.id.huifulistview);
        huifuyuju = findViewById(R.id.huifuyuju);
        /*
         * 自动回复的LiestView
         * */
        huifulistItem.clear();
        gethuifulist();
        for(int i=0;i<huifulistItem.size();i++){
            Log.i(TAG,"回复语句:"+huifulistItem.get(i).toString().substring(17,huifulistItem.get(i).toString().indexOf("}")));
        }
        final SimpleAdapter huifulistAdapter = new SimpleAdapter(zidonghuihu.this, huifulistItem,//需要绑定的数据
                R.layout.huifu_message,//每一行的布局
                new String[]{"message_neirong"},//动态数组中的数据源的键对应到定义布局的View中
                new int[]{R.id.message_neirong}
        );
        zidonghuifuList.setAdapter(huifulistAdapter);
        zidonghuifuList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder  = new AlertDialog.Builder(zidonghuihu.this);
                builder.setTitle("提示" ) ;
                builder.setMessage("是否删除？" ) ;
                builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gethuifulist();
                        final Eventvalue Result = dbhandler.getStr(huifulistItem.get(position).toString().substring(17,huifulistItem.get(position).toString().indexOf("}")));
                        if(Result!=null&&Result.getValue()==5) {
                            Eventvalue eventvalue = new Eventvalue(Result.getId(), Result.getName(), Result.getValue(), huifulistItem.get(position).toString().substring(17, huifulistItem.get(position).toString().indexOf("}")));
                            dbhandler.deleteCoincount(eventvalue);
                            huifulistItem.clear();
                            huifuadapter = new SimpleAdapter(zidonghuihu.this, huifulistItem,//需要绑定的数据
                                    R.layout.huifu_message,//每一行的布局
                                    new String[]{"message_neirong"},//动态数组中的数据源的键对应到定义布局的View中
                                    new int[]{R.id.message_neirong}
                            );
                            zidonghuifuList.setAdapter(huifuadapter);
                            gethuifulist();
                            huifuadapter = new SimpleAdapter(zidonghuihu.this, huifulistItem,//需要绑定的数据
                                    R.layout.huifu_message,//每一行的布局
                                    new String[]{"message_neirong"},//动态数组中的数据源的键对应到定义布局的View中
                                    new int[]{R.id.message_neirong}
                            );
                            zidonghuifuList.setAdapter(huifuadapter);
                        }
                    }
                });
                builder.setNegativeButton("否", null);
                builder.show();
                return false;
            }
        });

        final Eventvalue server_status = dbhandler.getNameResult("server_status");
        if (server_status != null) {
            String status = server_status.getCoincount();
            if (status.equals("1")) {
                final Eventvalue findResult = dbhandler.getNameResult("huifu");
                if (findResult != null) {
                    String readvalue = findResult.getCoincount();
                    if (readvalue.equals("1")) {
                        zdhfmessage = true;
                        zdhf.setText("回复开启");
                        zdhf.setTextColor(Color.parseColor("#33FF33"));
                    } else {
                        zdhfmessage = false;
                        zdhf.setText("回复关闭");
                        zdhf.setTextColor(Color.parseColor("#999999"));
                    }
                }
                zdhf.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!zdhfmessage) {
                            zdhfmessage = true;
                            zdhf.setText("回复开启");
                            zdhf.setTextColor(Color.parseColor("#33FF33"));
                            EventBus.getDefault().postSticky(new Message<>(5, zdhfmessage));
                            return;
                        }
                        zdhfmessage = false;
                        zdhf.setText("回复关闭");
                        zdhf.setTextColor(Color.parseColor("#999999"));
                        EventBus.getDefault().postSticky(new Message<>(5, zdhfmessage));
                    }
                });
                click_true.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String yuju = huifuyuju.getText().toString();
                        if (!yuju.isEmpty()) {
                            Eventvalue eventvalue = new Eventvalue(null, "zidonghuifu", 5, yuju);
                            dbhandler.addValue(eventvalue);
                            huifulistItem.clear();
                            huifuadapter = new SimpleAdapter(zidonghuihu.this, huifulistItem,//需要绑定的数据
                                    R.layout.huifu_message,//每一行的布局
                                    new String[]{"message_neirong"},//动态数组中的数据源的键对应到定义布局的View中
                                    new int[]{R.id.message_neirong}
                            );
                            zidonghuifuList.setAdapter(huifuadapter);
                            gethuifulist();
                            huifuadapter = new SimpleAdapter(zidonghuihu.this, huifulistItem,//需要绑定的数据
                                    R.layout.huifu_message,//每一行的布局
                                    new String[]{"message_neirong"},//动态数组中的数据源的键对应到定义布局的View中
                                    new int[]{R.id.message_neirong}
                            );
                            zidonghuifuList.setAdapter(huifuadapter);
                            huifuyuju.setText("");
                            Toast.makeText(zidonghuihu.this, "成功添加:" + yuju, Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(zidonghuihu.this, "你确定你输入了吗?", Toast.LENGTH_SHORT).show();
                    }
                });
                click_clean.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String yuju = huifuyuju.getText().toString();
                        if (!yuju.isEmpty()) {
                            huifuyuju.setText("");
                        } else
                            Toast.makeText(zidonghuihu.this, "你还没有输入任何文字!" + yuju, Toast.LENGTH_SHORT).show();
                    }
                });
            } else Toast.makeText(zidonghuihu.this, "请先开启服务!", Toast.LENGTH_SHORT).show();
        }
    }

    public void gethuifulist() {
        for (int i = 0; i < dbhandler.dbquery().size(); i++) {
            HashMap<String, Object> map = new HashMap<>();
            int Result = dbhandler.dbquery().get(i).getValue();
            if (Result != 5) {
                continue;
            }
            map.put("message_neirong", dbhandler.dbquery().get(i).getCoincount());
            huifulistItem.add(map);
        }
    }
}