package com.mass.javacvffmpopencv;

import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.appcompat.app.AppCompatActivity;


public class mxActivity extends AppCompatActivity {
    private SurfaceView mVvPlayback;
    private SurfaceHolder surfaceHolder;
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

                record   r = new record(surfaceHolder, point.x);
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
