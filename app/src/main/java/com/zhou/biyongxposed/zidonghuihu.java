package com.zhou.biyongxposed;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import static com.zhou.biyongxposed.MainActivity.mSimpleAdapter;

public class zidonghuihu extends AppCompatActivity {
    private Button zdhf;
    private boolean zdhfmessage;
    private ListView zidonghuifuList;
    private Button click_true,click_clean;
    private EditText huifuyuju;
    /*定义一个动态数组*/
    ArrayList<HashMap<String, Object>> huifulistItem = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_huifu);
        final DatabaseHandler dbhandler = new DatabaseHandler(this);
        zdhf = findViewById(R.id.zidonghuifubutton);
        click_true=findViewById(R.id.huifu_true);
        click_clean=findViewById(R.id.huifu_clean);
        zidonghuifuList = findViewById(R.id.huifulistview);
        huifuyuju=findViewById(R.id.huifuyuju);
        /*
         * 自动回复的LiestView
         * */
        zidonghuifuList.setAdapter(mSimpleAdapter);
        for (int i = 0; i < dbhandler.dbquery().size(); i++) {
            HashMap<String, Object> map = new HashMap<>();
            int Result = dbhandler.dbquery().get(i).getValue();
            if(Result!=5){
                continue;
            }
            map.put("message_neirong", dbhandler.dbquery().get(i).getCoincount());
            huifulistItem.add(map);
        }
        mSimpleAdapter = new SimpleAdapter(zidonghuihu.this, huifulistItem,//需要绑定的数据
                R.layout.huifu_message,//每一行的布局
                new String[]{"message_neirong"},//动态数组中的数据源的键对应到定义布局的View中
                new int[]{R.id.message_neirong}
        );
        zidonghuifuList.setAdapter(mSimpleAdapter);
        zidonghuifuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(zidonghuihu.this,"你点击了"+(position+1)+"按钮",Toast.LENGTH_SHORT).show();
            }
        });

        final Eventvalue server_status = dbhandler.getNameResult("server_status");
        if (server_status != null) {
            String status = server_status.getCoincount();
            if(status.equals("1")) {
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
                        if(!yuju.isEmpty()){
                            Eventvalue eventvalue = new Eventvalue(null, "zidonghuifu",5, yuju);
                            dbhandler.addValue(eventvalue);
                            Toast.makeText(zidonghuihu.this,"成功添加:"+yuju, Toast.LENGTH_SHORT).show();
                        }else
                            Toast.makeText(zidonghuihu.this,"你确定你输入了吗?", Toast.LENGTH_SHORT).show();
                    }
                });
                click_clean.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String yuju = huifuyuju.getText().toString();
                        if(!yuju.isEmpty()){
                            huifuyuju.setText("");
                        }else
                            Toast.makeText(zidonghuihu.this,"你还没有输入任何文字!"+yuju, Toast.LENGTH_SHORT).show();
                    }
                });
            }else Toast.makeText(zidonghuihu.this, "请先开启服务!", Toast.LENGTH_SHORT).show();
        }
    }
}