package com.zhou.biyongxposed;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

public class zidonghuihu extends AppCompatActivity {
    private Button zdhf;
    private boolean zdhfmessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_huifu);
        DatabaseHandler dbhandler = new DatabaseHandler(this);
        zdhf = findViewById(R.id.zidonghuifubutton);
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
            }else Toast.makeText(zidonghuihu.this, "请先开启服务!", Toast.LENGTH_SHORT).show();
        }
    }
}