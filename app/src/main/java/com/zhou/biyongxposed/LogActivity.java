package com.zhou.biyongxposed;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LogActivity extends AppCompatActivity {
    private TextView logText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.run_log);
        Button cleanLog = findViewById(R.id.button14);
        logText= findViewById(R.id.textLog);
        logText.setMovementMethod(ScrollingMovementMethod.getInstance());
        logText.setText(readTxt("/sdcard/LogUtils/biyongdebuglog.log"));
        logText.setTextColor(Color.parseColor("#3333FF"));
        cleanLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"LogUtils";
                deleteFile(new File(path));
                logText.setText(readTxt("/sdcard/LogUtils/biyongdebuglog.log"));
                Toast.makeText(LogActivity.this, "日志巳清除!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String readTxt(String filepath){
        File file = new File(filepath);
        String result = "";
        if (file.exists()) {
            try {
                InputStream in = new BufferedInputStream(new FileInputStream(file));
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                StringBuffer sb = new StringBuffer();
                String temp;
                while ((temp = br.readLine()) != null) {
                    sb.append(temp+"\n");
                }
                result = sb.toString();
                br.close();
                in.close();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
        return result;
    }
    public  void deleteFile(File oldPath) {
        if (oldPath.isDirectory()) {
            File[] files = oldPath.listFiles();
            for (File file : files) {
                deleteFile(file);
                file.delete();
            }
        }else{
            oldPath.delete();
        }

    }
}
