package com.zhou.biyongxposed;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class zidonghuihu extends AppCompatActivity {
    private Button zdhf;
    private boolean hf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_huifu);
        zdhf = findViewById(R.id.zidonghuifubutton);
        if(!hf){
            zdhf.setText("回复关闭");
            zdhf.setTextColor(Color.parseColor("#999999"));
        }else{
            zdhf.setText("服务开启");
            zdhf.setTextColor(Color.parseColor("#33FF33"));
        }
        zdhf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(zidonghuihu.this, "点击完成", Toast.LENGTH_SHORT).show();
                if(!hf) {
                    hf=true;
                    zdhf.setText("服务开启");
                    zdhf.setTextColor(Color.parseColor("#33FF33"));
                }
                if(hf){
                    hf=false;
                    zdhf.setText("回复关闭");
                    zdhf.setTextColor(Color.parseColor("#999999"));
                }
            }
        });

    }
}