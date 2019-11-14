package com.zhou.biyongxposed;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import static com.zhou.biyongxposed.bingyongserver.runLog;

public class LogActivity extends AppCompatActivity {
    private ListView getCoinList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.run_log);
        Button cleanLog = findViewById(R.id.button14);
        getCoinList = findViewById(R.id.logView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(LogActivity.this, android.R.layout.simple_list_item_1, runLog);
        getCoinList.setAdapter(adapter);
        cleanLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runLog.clear();
                ArrayAdapter<String> adapter = new ArrayAdapter<>(LogActivity.this, android.R.layout.simple_list_item_1, runLog);
                getCoinList.setAdapter(adapter);
                Toast.makeText(LogActivity.this, "日志巳清除!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
