package com.mass.javacvffmpopencv;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.core.Core;
import org.opencv.imgproc.Imgproc;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_RGB2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

public class record {
   private final  String TAG="record";
    private OpenCVFrameConverter.ToIplImage imgeconvert = new OpenCVFrameConverter.ToIplImage();
    private AndroidFrameConverter bitmapConverter = new AndroidFrameConverter();
    private static final int TIMEOUT = 10; // In seconds.
    private  boolean isshot=false;
   private  String filepath="";
    public void setShot(shotinterface shot) {
        this.shot = shot;
    }

    private  shotinterface  shot;
    private static enum TimeoutOption {
        /**
         * Depends on protocol (FTP, HTTP, RTMP, SMB, SSH, TCP, UDP, or UNIX).
         * <p>
         * http://ffmpeg.org/ffmpeg-all.html
         */
        TIMEOUT,
        /**
         * Protocols
         * <p>
         * Maximum time to wait for (network) read/write operations to complete,
         * in microseconds.
         * <p>
         * http://ffmpeg.org/ffmpeg-all.html#Protocols
         */
        RW_TIMEOUT,
        /**
         * Protocols -> RTSP
         * <p>
         * Set socket TCP I/O timeout in microseconds.
         * <p>
         * http://ffmpeg.org/ffmpeg-all.html#rtsp
         */
        STIMEOUT;

        public String getKey() {
            return toString().toLowerCase();
        }

    }

    Canvas canvas = null;
    private int picwidth;
    private SurfaceHolder surfaceHolder;

    public record(SurfaceHolder holder, int width) {
        surfaceHolder = holder;
        picwidth = width;
    }

    public void frameRecord(String inputFile, String outputFile, int audioChannel) throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {

        boolean isStart = true;// 该变量建议设置为全局控制变量，用于控制录制结束
        FFmpegFrameGrabber grabber = null;
        // 获取视频源
        try {

            grabber = new FFmpegFrameGrabber(inputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //  FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
        grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
     /*   grabber.setOption(
                TimeoutOption.STIMEOUT.getKey(),
                String.valueOf(TIMEOUT * 1000000)
        ); // In microseconds.*/

        grabber.setOption("rtsp_transport", "tcp");
        grabber.setFrameRate(30);
        grabber.setVideoBitrate(3000000);
        //  FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
        //   grabber.setImageWidth(560);
        //    grabber.setImageHeight(480);
        //  grabber.setFrameRate(25);
        //grabber.setPixelFormat(AV_PIX_FMT_RGBA);
        // grabber.setFormat("H264");
        //    grabber.setVideoCodec(28);
        //      grabber.setVideoBitrate(3000000);
        // 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
     /*   FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 1280, 720,audioChannel);
        recorder.setFrameRate(30);
        recorder.setVideoBitrate(3000000);
        recordByFrame(grabber, recorder, isStart);*/
        Frame frame = null;
        try {//建议在线程中使用该方法
            grabber.start();

            opencv_core.Mat tmpmat = new opencv_core.Mat();
            opencv_core.Mat mat=null;
            Bitmap bitmap=null;
            while (isStart && (frame = grabber.grabFrame()) != null) {
                Log.i(TAG,  "接收至frame");

              //  if (frame == null) break;
             //   if (frame.image == null) continue;


                try {
                    mat = imgeconvert.convertToMat(frame);
                }catch (Exception e){
                    Log.i(TAG,  "error"+e.getMessage());
                }

                Log.i(TAG,  "mat="+mat);
                    if (mat == null) {

                        continue;
                    }


              if (isshot)
              {
                  shotpic(mat,this.filepath);
              }

                int hei = picwidth * mat.rows() / mat.cols();
                resize(mat, tmpmat, new opencv_core.Size(picwidth, hei));
                Log.i(TAG,  "size");
           //     cvtColor(tmpmat, displaymat, COLOR_RGB2GRAY);
          //      opencv_core.Mat lmat=matswitchColor(displaymat);
           //     Log.i(TAG,"------------>rows="+lmat.rows()+"  cols="+lmat.cols());
                 bitmap = bitmapConverter.convert(imgeconvert.convert(tmpmat));

            //    bitmap = switchColor(bitmap);
                //  Log.i("fff",bitmap.toString());
                try {
                    canvas = surfaceHolder.lockCanvas();
                    canvas.drawBitmap(bitmap, 0, 0, null);

                } finally {
                    surfaceHolder.unlockCanvasAndPost(canvas);

                 //   bitmap.recycle();
                  /*  tmpmat.close();
                   mat.close();*/

                }

            }

        } catch (FrameGrabber.Exception re) {
            Log.i("fff", re.getMessage());
            re.printStackTrace();
        }
    }

    public  void startshot(String path)
    {
        isshot=true;
        filepath=path;
    }

    public void  shotpic(opencv_core.Mat source,String path)
    {
        isshot=false;
        opencv_core.Mat dst=new opencv_core.Mat();
        cvtColor(source, dst, COLOR_RGB2GRAY);
        opencv_core.Mat math=matswitchColor(dst);
        String strDate = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date());

        String fullfileName = filepath+ "pic_" + strDate + ".jpg";
        Log.i("time",fullfileName);
        imwrite(fullfileName,math);
        if(this.shot!=null){
            this.shot.shotcomplete(fullfileName);
        }
    }


    public opencv_core.Mat matswitchColor(opencv_core.Mat source) {


        UByteIndexer srcindexer = source.createIndexer();


        Log.i(TAG,"rows="+source.rows()+"  cols="+source.cols());
        try {
            for (int i = 0; i < source.rows() - 1; i++) {
                for (int j = 0; j < source.cols() - 1; j++) {
                    int avg = srcindexer.get(i,j);
                    if (avg >= 210) {  //亮度：avg>=126
                        //设置颜色
                        srcindexer.put(i, j,  255);


                    } else if (avg < 210 && avg >= 80) {  //avg<126 && avg>=115
                        srcindexer.put(i, j, 108);

                    } else {
                        srcindexer.put(i, j,  0);

                    }
                }
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return source;
    }


    public Bitmap switchColor(Bitmap switchBitmap) {
        int width = switchBitmap.getWidth();
        int height = switchBitmap.getHeight();

        // Turn the picture black and white
//		Bitmap newBitmap=Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565);
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(switchBitmap, new Matrix(), new Paint());

        int current_color, red, green, blue, alpha, avg = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //获得每一个位点的颜色
                current_color = switchBitmap.getPixel(i, j);
                //获得三原色
                red = Color.red(current_color);
                green = Color.green(current_color);
                blue = Color.blue(current_color);
                alpha = Color.alpha(current_color);
                avg = (red + green + blue) / 3;// RGB average
                if (avg >= 210) {  //亮度：avg>=126
                    //设置颜色
                    newBitmap.setPixel(i, j, Color.argb(alpha, 255, 255, 255));// white
                } else if (avg < 210 && avg >= 80) {  //avg<126 && avg>=115
                    newBitmap.setPixel(i, j, Color.argb(alpha, 108, 108, 108));//grey
                } else {
                    newBitmap.setPixel(i, j, Color.argb(alpha, 0, 0, 0));// black
                }
            }
        }
        return newBitmap;
    }

    private static void recordByFrame(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder, Boolean status)
            throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
        try {//建议在线程中使用该方法
            grabber.start();
            recorder.start();
            Frame frame = null;
            while (status && (frame = grabber.grabFrame()) != null) {
                recorder.record(frame);
            }
            recorder.stop();
            grabber.stop();
        } finally {
            if (grabber != null) {
                grabber.stop();
            }
        }
    }

    public  interface  shotinterface
    {
        public  void shotcomplete(String filename);
    }


}
