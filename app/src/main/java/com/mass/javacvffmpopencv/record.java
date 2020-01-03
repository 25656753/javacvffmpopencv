package com.mass.javacvffmpopencv;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.IPCameraFrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;

import static org.bytedeco.javacpp.avcodec.avcodec_register_all;
import static org.bytedeco.javacpp.avformat.av_register_all;
import static org.bytedeco.javacpp.avformat.av_register_input_format;
import static org.bytedeco.javacpp.avformat.avformat_network_init;
import static org.bytedeco.javacpp.avformat.avformat_open_input;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGBA;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

public class record {

    private   OpenCVFrameConverter.ToIplImage imgeconvert=new  OpenCVFrameConverter.ToIplImage();
    private AndroidFrameConverter bitmapConverter = new AndroidFrameConverter();
    private static final int TIMEOUT = 10; // In seconds.
    private static enum TimeoutOption {
        /**
         * Depends on protocol (FTP, HTTP, RTMP, SMB, SSH, TCP, UDP, or UNIX).
         *
         * http://ffmpeg.org/ffmpeg-all.html
         */
        TIMEOUT,
        /**
         * Protocols
         *
         * Maximum time to wait for (network) read/write operations to complete,
         * in microseconds.
         *
         * http://ffmpeg.org/ffmpeg-all.html#Protocols
         */
        RW_TIMEOUT,
        /**
         * Protocols -> RTSP
         *
         * Set socket TCP I/O timeout in microseconds.
         *
         * http://ffmpeg.org/ffmpeg-all.html#rtsp
         */
        STIMEOUT;

        public String getKey() {
            return toString().toLowerCase();
        }

    }
    Canvas canvas=null;
    private  int picwidth;
    private SurfaceHolder surfaceHolder;

    public record(SurfaceHolder holder,int width)
    {
        surfaceHolder=holder;
        picwidth=width;
    }

    public void frameRecord(String inputFile, String outputFile, int audioChannel) throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {

        boolean isStart = true;// 该变量建议设置为全局控制变量，用于控制录制结束
        // 获取视频源


        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
      //  FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
        grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
     /*   grabber.setOption(
                TimeoutOption.STIMEOUT.getKey(),
                String.valueOf(TIMEOUT * 1000000)
        ); // In microseconds.*/

        grabber.setOption("rtsp_transport","tcp");
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



            while (isStart&& (frame = grabber.grabFrame()) != null) {
                Log.i("fff------>",frame+"");
                opencv_core.Mat t=new opencv_core.Mat();
                opencv_core.Mat mat=imgeconvert.convertToMat(frame);
                int hei=picwidth*mat.rows()/mat.cols();
                resize(mat,t,new opencv_core.Size(picwidth,hei));

                Bitmap bitmap=    bitmapConverter.convert(imgeconvert.convert(t));

                try {
                    canvas = surfaceHolder.lockCanvas();
                    canvas.drawBitmap(bitmap, 0, 0, null);
                }finally {
                    surfaceHolder.unlockCanvasAndPost(canvas);

                }

            }

        }catch ( FrameGrabber.Exception re)
        {
            Log.i("fff",re.getMessage());
            re.printStackTrace();
        }
    }


    private static void recordByFrame(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder, Boolean status)
            throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
        try {//建议在线程中使用该方法
            grabber.start();
            recorder.start();
            Frame frame = null;
            while (status&& (frame = grabber.grabFrame()) != null) {
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
}
