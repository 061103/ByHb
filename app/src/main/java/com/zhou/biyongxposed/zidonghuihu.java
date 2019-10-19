package com.zhou.biyongxposed;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.greenrobot.eventbus.EventBus;

public class zidonghuihu extends AppCompatActivity {
    private Button zdhf;
    private boolean zdhfmessage;
    private DatabaseHandler dbhandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_huifu);
        dbhandler=new DatabaseHandler(this);
        zdhf = findViewById(R.id.zidonghuifubutton);
        final Eventvalue findResult = dbhandler.getNameResult("huifu");
        if(findResult!=null) {
            int readvalue = findResult.getValue();
            if(readvalue==1){
                zdhfmessage=true;
                zdhf.setText("回复开启");
                zdhf.setTextColor(Color.parseColor("#33FF33"));
            }else {
                zdhfmessage=false;
                zdhf.setText("回复关闭");
                zdhf.setTextColor(Color.parseColor("#999999"));
            }
        }
        zdhf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!zdhfmessage) {
                    zdhfmessage=true;
                    zdhf.setText("回复开启");
                    zdhf.setTextColor(Color.parseColor("#33FF33"));
                    EventBus.getDefault().postSticky(new Message<>(5, zdhfmessage));
                    return;
                }
                if(zdhfmessage){
                    zdhfmessage=false;
                    zdhf.setText("回复关闭");
                    zdhf.setTextColor(Color.parseColor("#999999"));
                    EventBus.getDefault().postSticky(new Message<>(5, zdhfmessage));
                    return;
                }
            }
        });

    }
}