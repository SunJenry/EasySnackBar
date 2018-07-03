package com.sun.snackbar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.sun.easysnackbar.EasySnackBar;

public class MainActivity extends AppCompatActivity {

    private View mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mView = findViewById(R.id.root);


        findViewById(R.id.btn_up).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View v) {
                // Must create custom view in this way,so it can display normally
                View contentView = EasySnackBar.convertToContentView(mView, R.layout.layout_custom);
                EasySnackBar.make(mView, contentView, EasySnackBar.LENGTH_SHORT, true).show();
            }
        });

        findViewById(R.id.btn_down).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View v) {
                // Must create custom view in this way,so it can display normally
                View contentView = EasySnackBar.convertToContentView(mView, R.layout.layout_custom);
                EasySnackBar.make(mView, contentView, EasySnackBar.LENGTH_SHORT, false).show();
            }
        });
    }
}
