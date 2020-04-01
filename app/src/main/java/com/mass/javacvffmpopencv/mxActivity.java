package com.mass.javacvffmpopencv;

import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class mxActivity extends AppCompatActivity {
    private SurfaceView mVvPlayback;
    private SurfaceHolder surfaceHolder;
    private Button button;
    private   record   r;
    private final long shotcomplete=43433333L;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==shotcomplete)
                Toast.makeText(mxActivity.this,(String)msg.obj,Toast.LENGTH_LONG).show();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mx);
        mVvPlayback = findViewById(R.id.mx_playback);
        mVvPlayback.setBackgroundResource(R.color.black);
        mVvPlayback.setZOrderOnTop(true);
        mVvPlayback.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                surfaceHolder = holder;

                openvideo();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
      button=findViewById(R.id.btnshot);

      button.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              r.startshot("/sdcard/");
          }
      });
    }


    /**
     * 打开mx相机
     **/
    private void openvideo() {
        //rstp  mx相机
        final String inputFile = "rtsp://192.168.1.10:554/user=admin&password=&channel=1&stream=0.sdp?";
        String outputFile="";
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Display display = getWindowManager().getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);

                  r = new record(surfaceHolder, point.x);
                 r.setShot(new record.shotinterface() {
                    @Override
                    public void shotcomplete(String filename) {
                        Message msg=Message.obtain();
                        msg.what=(int)shotcomplete;
                        msg.obj=filename;
                     handler.sendMessage(msg);
                    }
                });
                try {
                    r.frameRecord(inputFile, outputFile, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

        }.execute();
    }
}
