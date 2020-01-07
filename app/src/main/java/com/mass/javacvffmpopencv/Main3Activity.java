package com.mass.javacvffmpopencv;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Main3Activity extends AppCompatActivity {
    static {
        System.loadLibrary("native-lib");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        stringFromJNI(this.getClass());
    }

   public void show()
   {
       Toast.makeText(this,"from jni调用",Toast.LENGTH_LONG).show();
   }

    public native void stringFromJNI(Class t);
}
