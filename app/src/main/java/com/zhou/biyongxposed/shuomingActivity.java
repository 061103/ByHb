package com.zhou.biyongxposed;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

public class shuomingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuoming);
        Button runlog = findViewById(R.id.runlog);
        Button  open_User_Stats = findViewById(R.id.bt_USAGE_STATS);
        SeekBar dimAmountvalues = findViewById(R.id.seekBar_dimAmount);
        runlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(shuomingActivity.this, LogActivity.class);
                startActivity(intent);
            }
        });
        open_User_Stats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }
        });
        dimAmountvalues.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                System.out.println("progress = " + progress);
                System.out.println("fromUser = " + fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                System.out.println("com.example.screenBrightnessTest.MyActivity.onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println("com.example.screenBrightnessTest.MyActivity.onStopTrackingTouch");
            }
        });

    }
}
