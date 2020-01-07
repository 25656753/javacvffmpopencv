package com.mass.javacvffmpopencv;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

public class Main2Activity extends AppCompatActivity {

    private TextView tvdate=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        tvdate=findViewById(R.id.tvdata);
        final UserModel userModel = ViewModelProviders.of(this).get(UserModel.class);
        userModel.mUserLiveData.observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
              tvdate.setText(user.name);
            }
        });



        findViewById(R.id.btntest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击按钮  更新User数据  观察TextView变化
                userModel.doSomething();
            }
        });

    }
}
