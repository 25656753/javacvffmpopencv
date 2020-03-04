package com.mass.javacvffmpopencv;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;


public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private SurfaceView mVvPlayback;
    private SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        // Example of a call to a native method
        mVvPlayback = findViewById(R.id.vv_playback);

        mVvPlayback.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                surfaceHolder = holder;
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });


        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        processvideo();
                    }
                }).start();


                /*   rc();*/
            }
        });


    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return super.onTouchEvent(event);
    }

    private FFmpegFrameRecorder recorder = null;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(MainActivity.this, "proc over", Toast.LENGTH_LONG).show();

        }
    };
    private record r;

    /*
    转录
     */
    private void processvideo() {
        //初始化
        FrameGrabber videoGrabber = new FFmpegFrameGrabber(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/" + "movie2.mp4");
        try {
            videoGrabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
//获取视频源的参数
        double frameRate = videoGrabber.getFrameRate();
        int sampleRate = videoGrabber.getSampleRate();

        Frame frame = null;
        int count = 0;
        String ffmpeg_link = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/" + "obama.mp4";
        File result = new File(ffmpeg_link);
        if (result.exists()) result.delete();
        if (recorder == null) {
            //初始化recorder
            recorder = new FFmpegFrameRecorder(result,
                    videoGrabber.getImageWidth(), videoGrabber.getImageHeight(), videoGrabber.getAudioChannels());
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("mp4");
            recorder.setSampleRate(sampleRate);
            // Set in the surface changed method
            recorder.setFrameRate(frameRate);
            try {
                recorder.start();
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }

        while (true) {
            //获取下一帧数据
            try {
                frame = videoGrabber.grabFrame();
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
            if (frame == null)
                break;
            if (frame.image == null) continue;

            //转码  Frame ->Bitmap  操作Bitmap完成后Bitmap->Frame
            AndroidFrameConverter bitmapConverter = new AndroidFrameConverter();
            Bitmap currentImage = bitmapConverter.convert(frame);
            // currentImage = initScence(currentImage)；//操作Bitmap，
            //   Mat tt= converterToMat.convert(frame);
            //  imwrite("/sdcard/ppp.jpg",tt);
            Frame frame1 = bitmapConverter.convert(currentImage);
            //写入recorder
            try {
                recorder.record(frame1);
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();

            }
        }
        try {
            recorder.stop();
            recorder.close();
            recorder.release();
            videoGrabber.stop();
            videoGrabber.close();
        } catch (FrameGrabber.Exception e) {
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
        handler.sendEmptyMessage(1);
    }


    /**
     * 打开天时通相机
     **/
    private void rc() {
        //天时通rtsp地址
        final String inputFile = "rtsp://admin:123456@169.254.170.254:554/mpeg4";
        //    final String inputFile = "rtsp://169.254.170.254:554/mpeg4cif";
        // Decodes-encodes

        final String outputFile = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/recorde.mp4";
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Display display = getWindowManager().getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);

                r = new record(surfaceHolder, point.x);
                try {
                    r.frameRecord(inputFile, outputFile, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

        }.execute();

    }

    /*
    精子检测
     */
    private void processvideo3() {
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        Spermalgorithm pc = new Spermalgorithm(surfaceHolder, point.x);
        pc.mainrun(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/" + "t.mp4");
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    static OpenCVFrameConverter.ToIplImage matconverter = new OpenCVFrameConverter.ToIplImage();

    //截图
    private void processvideo1() {

        TextureView a = null;


        //初始化
        FrameGrabber videoGrabber = new FFmpegFrameGrabber(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/" + "110259-av-1.avi");
        try {
            videoGrabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
      //获取视频源的参数
        double frameRate = videoGrabber.getFrameRate();
        int sampleRate = videoGrabber.getSampleRate();

        Frame frame = null;
        int count = 0;
        String ffmpeg_link = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/" + "tt";


        while (true) {
            //获取下一帧数据
            try {
                frame = videoGrabber.grabFrame();
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
            if (frame == null)
                break;
            if (frame.image == null) continue;
            if (count >= 60)
                break;

            OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
            Mat tt = matconverter.convertToMat(frame);

            imwrite("/sdcard/temp" + count + ".jpg", tt);
            count++;
        }
        try {

            videoGrabber.stop();
            videoGrabber.close();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        handler.sendEmptyMessage(1);
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.CAMERA"};


    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
