package com.zhou.biyongxposed;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.math.BigDecimal;

public class shuomingActivity extends AppCompatActivity {
    public static float dimAmount_num;
    public  DatabaseHandler dbhandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuoming);
        Button runlog = findViewById(R.id.runlog);
        Button open_User_Stats = findViewById(R.id.bt_USAGE_STATS);
        SeekBar seekBar = findViewById(R.id.seekBar_dimAmount);
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
        seekBar.setMax(255);
        seekBar.setProgress(255/2);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {//以免太暗
                    float values = (float) progress / 255;//因为这个值是[0, 1]范围的
                    BigDecimal b = new BigDecimal(values);
                    dimAmount_num   =  b.setScale(2,  BigDecimal.ROUND_HALF_UP).floatValue();
                    System.out.println("dimAmount_values" + dimAmount_num);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                System.out.println("com.example.screenBrightnessTest.MyActivity.onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int afloat_num = (int) (dimAmount_num * 1000);
                final Eventvalue dimAmount_values = dbhandler.getNameResult("dimAmount_values");
                if (dimAmount_values != null) {
                    Eventvalue eventvalue = new Eventvalue(dimAmount_values.getId(), dimAmount_values.getName(), afloat_num, "");
                    dbhandler.addValue(eventvalue);
                } else {
                    Eventvalue eventvalue = new Eventvalue(null, "dimAmount_values", afloat_num, "");
                    dbhandler.addValue(eventvalue);
                }
            }
        });
    }
}
