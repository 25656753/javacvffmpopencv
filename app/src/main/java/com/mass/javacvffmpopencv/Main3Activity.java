package com.mass.javacvffmpopencv;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
        try {
            stringFromJNI(this.getClass());
        } catch (Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
      findViewById(R.id.btnshare).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
           //   shareText(Main3Activity.this,"kkkk","fffff");
              shareImage();
          }
      });
    }


    /**
     * 分享文本内容
     * @param context
     * @param title
     * @param text
     */
    public static void shareText(Context context, String title, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(intent, title));
    }

    /**
     * 分享图片

     */
    public  void shareImage() {
        //将mipmap中图片转换成Uri
        Uri imgUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" +R.mipmap.ic_launcher);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
//其中imgUri为图片的标识符
        shareIntent.putExtra(Intent.EXTRA_STREAM, imgUri);
        shareIntent.setType("image/*");
//切记需要使用Intent.createChooser，否则会出现别样的应用选择框，您可以试试
        shareIntent = Intent.createChooser(shareIntent, "Here is the title of Select box");
        startActivity(shareIntent);
    }

   public void show()
   {
       Toast.makeText(this,"from jni调用",Toast.LENGTH_LONG).show();
   }

    public native void stringFromJNI(Class t);
}
