package com.mym.rxjavademo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_test1, R.id.btn_muiltdownload})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_test1:
                startActivity(new Intent(this, TestOneActivity.class));
                break;
            case R.id.btn_muiltdownload:
                Toast.makeText(this, "TEST2", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
