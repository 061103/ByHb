package com.zhou.biyongxposed;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class zidonghuihu extends AppCompatActivity {
    private Button zdhf;
    private boolean zdhfmessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_huifu);
        zdhf = findViewById(R.id.zidonghuifubutton);
        zdhf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!zdhfmessage) {
                    zdhfmessage=true;
                    zdhf.setText("回复开启");
                    zdhf.setTextColor(Color.parseColor("#33FF33"));
                    Toast.makeText(zidonghuihu.this, "回复开启", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(zdhfmessage){
                    zdhfmessage=false;
                    zdhf.setText("回复关闭");
                    zdhf.setTextColor(Color.parseColor("#999999"));
                    Toast.makeText(zidonghuihu.this, "回复关闭", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

    }
}