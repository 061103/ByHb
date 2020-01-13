package com.zhou.biyongxposed;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.math.BigDecimal;

public class shuomingActivity extends AppCompatActivity {
    public static float dimAmount_num;
    private DatabaseHandler dbhandler;
    private TextView tx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuoming);
        dbhandler = new DatabaseHandler(this);
        Button runlog = findViewById(R.id.runlog);
        tx = findViewById(R.id.tx_seekBar);
        Button open_User_Stats = findViewById(R.id.bt_USAGE_STATS);
        SeekBar seekBar = findViewById(R.id.seekBar_dimAmount);
        seekBar.setMax(255);
        if(dbhandler.getNameResult("dimAmount_values")!=null) {
            tx.setText(String.valueOf(dbhandler.getNameResult("dimAmount_values").getValue()));
            float afloat = (float)dbhandler.getNameResult("dimAmount_values").getValue();
            System.out.println("afloat:"+ afloat);
            float values = afloat/100;
            System.out.println("values:"+ values);
            int s =(int) (values*255);
            System.out.println("s:"+ s);
            seekBar.setProgress(s);
        }
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
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {//以免太暗
                    float values = (float) progress / 255;//因为这个值是[0, 1]范围的
                    BigDecimal b = new BigDecimal(values);
                    dimAmount_num = b.setScale(2,  BigDecimal.ROUND_HALF_UP).floatValue();
                    tx.setText(String.valueOf((int)(dimAmount_num * 100)));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                System.out.println("com.example.screenBrightnessTest.MyActivity.onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int afloat_num = (int) (dimAmount_num * 100);
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
