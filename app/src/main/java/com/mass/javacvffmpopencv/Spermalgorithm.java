package com.mass.javacvffmpopencv;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;

import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacpp.indexer.IntIndexer;
import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static org.bytedeco.javacpp.opencv_core.CV_TERMCRIT_EPS;
import static org.bytedeco.javacpp.opencv_core.CV_TERMCRIT_ITER;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_RGB2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.circle;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.goodFeaturesToTrack;
import static org.bytedeco.javacpp.opencv_imgproc.line;
import static org.bytedeco.javacpp.opencv_imgproc.resize;
import static org.bytedeco.javacpp.opencv_video.calcOpticalFlowPyrLK;


public class
Spermalgorithm {

    private static final String TAG = "Spermalgorithm";
    private Mat frame, gray = new Mat();


    private Mat prev_frame = new Mat(), prev_gray = new Mat();
    private Mat features = new Mat(); // shi-tomasi角点检测 - 特征数据
    private Mat iniPoints = new Mat(); // 初始化特征数据
    private Mat[] fpts = new Mat[]{new Mat(), new Mat()}; // 保持当前帧和前一帧的特征点位置
    private Mat status = new Mat();// 特征点跟踪成功标志位

    private Mat errors = new Mat(); // 跟踪时候区域误差和


    private Vector<Mat> rsinitPoints = new Vector<Mat>();    // 初始化位置数组
    private Vector<Mat> rsPoints = new Vector<Mat>();         //尾桢精子位置
    private Vector<Integer> rstime = new Vector<Integer>();   // 检测时间
    private Vector<Integer> rscount = new Vector<Integer>();  //首桢检测到精子数量
    private Vector<Integer> rs1count = new Vector<Integer>();  //尾桢跟踪到精子数量
    private Vector<Vector<Double>> rsdist = new Vector<Vector<Double>>();   //精子位移数组

    private SurfaceHolder surfaceHolder;
    private int picwidth;
    private float rate = 1f;
    private float pixrate = 3;  //一像素为3um
    private float sht = 1;
    private ArrayList<segdata> finalrs = new ArrayList<>();  //存放精子活跃值
     private  Double wx=0.0,wh=0.0;
    private AndroidFrameConverter bitmapConverter = new AndroidFrameConverter();

    /**
     *
     * @param holder   视频显示窗口
     * @param width   视频显示窗口长度
     * @param pixrate   屏幕象素转长度系数
     * @param sht        精子活跃度系数
     */
    public Spermalgorithm(SurfaceHolder holder, int width,float pixrate,float sht) {
        this.surfaceHolder = holder;
        this.picwidth = width;
        this.pixrate=pixrate;
        this.sht=sht;
    }

    Float thresholdration = 0.98f;

    Canvas canvas = null;
    int i = 0;
    boolean p = false;


    public ArrayList<segdata> getfinalrs()
    {
        return  this.finalrs;
    }

    public boolean mainrun(String videopath) {

        boolean runrs = false;
        Frame avframe = null;

        OpenCVFrameConverter.ToOrgOpenCvCoreMat converterToMat = new OpenCVFrameConverter.ToOrgOpenCvCoreMat();
        float scale = 0.25f;

        //初始化
        FrameGrabber videoGrabber = new FFmpegFrameGrabber(videopath);
        try {
            videoGrabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            return runrs;
        }

        // 省略前面10帧
        while (i < 10) {
            try {
                avframe = videoGrabber.grabFrame();
                wx=(double)avframe.imageWidth;
                wh=(double)avframe.imageHeight;
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
                return runrs;
            }
            i++;
            continue;
        }
        i = 0;

        while (true) {
            //获取下一帧数据
            try {
                avframe = videoGrabber.grabFrame();
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
                return runrs;
            }
            if (avframe == null)
                break;
            if (avframe.image == null)
                continue;


            frame = converterToMat.convertToMat(avframe);
            //   Log.i(TAG,"x-------->"+(int) (frame.cols() * scale)+"y------"+(int)(frame.rows() * scale));
            resize(frame, frame, new Size((int) (frame.cols() * scale), (int) (frame.rows() * scale)));
            cvtColor(frame, gray, COLOR_RGB2GRAY);


            if ((fpts[0].total() < (features.total() * thresholdration)) ||
                    (iniPoints.total() == 0)) {

                detectFeatures(frame, gray);
                if (features.total() >= 500) {
                    thresholdration = 0.90f;
                } else if (features.total() >= 300) {
                    thresholdration = 0.92f;
                } else if (features.total() >= 100) {
                    thresholdration = 0.97f;
                }

                if (fpts[0].total() > 0) //如果不是视pin第一个frame，删除掉之前历史数据，重新开始跟踪
                {

                    rs1count.add((int) fpts[0].total());
                    rstime.add(i * 50);
                    rsinitPoints.add(iniPoints);
                    rsPoints.add(fpts[0]);
                    fpts[0] = new Mat();
                    iniPoints = new Mat();
                    i = 0;
                }
                //  rscount.push_back(features.size());
                rscount.add((int) features.total());
                fpts[0].push_back(features);
                iniPoints.push_back(features);

            } else {
                i++;
                Log.w(TAG, "跟踪成功精子数量..." + fpts[0].total());

            }

            if (prev_gray.empty()) {
                gray.copyTo(prev_gray);
            }

            klTrackFeature();
            drawFeature(frame);
            // 更新前一帧数据
            gray.copyTo(prev_gray);
            frame.copyTo(prev_frame);


            Mat t = new Mat();
            rate =(float) picwidth /(float) frame.cols();
            int hei = Math.round(rate * frame.rows());
            resize(frame, t, new Size(picwidth, hei));


            Bitmap bitmap = bitmapConverter.convert(converterToMat.convert(t));

            try {
                canvas = surfaceHolder.lockCanvas();
                canvas.drawBitmap(bitmap, 0, 0, null);
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas);

            }


        }
        //视pin结束取得最后一次检测时长
        rs1count.add((int) fpts[0].total());

        rstime.add(i * 50);
        rsinitPoints.add(iniPoints);
        rsPoints.add(fpts[0]);

        calcdist(rsinitPoints, rsPoints, rsdist);

      /*  try {

            File file = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/sin.txt");
            FileOutputStream fop = new FileOutputStream(file);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            fop.write(("总共检测" + rsinitPoints.size() + "次").getBytes());

            fop.write("------------------------------每次时长ms".getBytes());*/
            Double tj=wx*wh*9*1e-12;
            for (int item = 0; item < rsinitPoints.size(); item++) {
                Double sectime = rstime.get(item) * 0.001;
                int Sa=0,Sb=0,Sc=0;
                Vector<Double> tmpvec = rsdist.get(item);
                for (int k = 0; k < tmpvec.size(); k++) {
                    Double m = tmpvec.get(k) * rate * pixrate / sectime;  //计算速率 um/s
                    m*=sht;
                   if (m>=25)
                   {
                       Sa++;
                   } else if (m>=5)
                   {
                     Sb++;
                   } else
                   {
                       Sc++;
                   }
                }
                segdata tmpv=new segdata();
                tmpv.setSa(Sa);
                tmpv.setSb(Sb);
                tmpv.setSc(Sc);
                tmpv.setBc(rscount.get(item));
                tmpv.setLc(rs1count.get(item));

                finalrs.add(tmpv);
                Log.w(TAG, "分析结论：->首帧精子个数："+rscount.get(item)+
                        "，尾帧精子个数："+rs1count.get(item)+"，活跃数=" +Sa+
                        "，不活跃数=" +Sb+"，不活动数=" +Sc);
            }


       /*     fop.flush();
            fop.close();
        } catch (Exception e) {
            e.printStackTrace();

        }*/
        //关闭
        try {

            videoGrabber.stop();
            videoGrabber.close();
            videoGrabber.release();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        runrs = true;
        return runrs;
    }

    //特征点检测
    private void detectFeatures(Mat inFrame, Mat ingray) {
        int maxCorners = 5000;
        double qualitylevel = 0.005;
        double minDistance = 5;
        int blockSize = 3;
        double k = 0.04;

        goodFeaturesToTrack(ingray, features, maxCorners, qualitylevel, minDistance, new Mat(), blockSize, false, k);
        Log.w(TAG, "查找精子特征点..." + features.total());


    }

    //KLT光流跟踪
    void klTrackFeature() {
        // KLT

        calcOpticalFlowPyrLK(prev_gray, gray, fpts[0], fpts[1], status, errors);

        int k = 0;


        // Make an image of the results
        FloatIndexer fpts0_idx = fpts[0].createIndexer();
        FloatIndexer fpts1_idx = fpts[1].createIndexer();
        UByteIndexer status_idx = status.createIndexer();
        FloatIndexer iniPoints_idx = iniPoints.createIndexer();
        //  Log.i(TAG,"fpts[0]"+fpts0_idx.sizes()[0]+"fpts[1]"+fpts1_idx.sizes()[0]);

        // 特征点过滤
        for (int i = 0; i < fpts1_idx.sizes()[0]; i++) {
            double dist = Math.abs(fpts0_idx.get(i, 0) - fpts1_idx.get(i, 0)) + Math.abs(fpts0_idx.get(i, 1) - fpts1_idx.get(i, 1));

            if ((status_idx.get(i) == 1) && (dist < 15)) {
                iniPoints_idx.put(k, iniPoints_idx.get(i));
                fpts1_idx.put(k++, fpts1_idx.get(i));

            } else {
                ///  Log.i(TAG,"dist--------->"+dist+"status_idx-->"+status_idx.get(i));
            }
        }
        // 保存特征点并绘制跟踪轨迹
        iniPoints.resize(k);
        fpts[1].resize(k);


        //   Log.i(TAG,"last  fpts[0]"+fpts0_idx.sizes()[0]+"fpts[1]"+fpts1_idx.sizes()[0]);
        drawTrackLines();
        fpts[1].copyTo(fpts[0]);
        fpts0_idx = fpts[0].createIndexer();
        fpts1_idx = fpts[1].createIndexer();
        //  Log.i(TAG,"last  fpts[0]"+fpts0_idx.sizes()[0]+"fpts[1]"+fpts1_idx.sizes()[0]);
    }


    //计算初始原点和终点之前的距离
    void calcdist(Vector<Mat> orig, Vector<Mat> laig, Vector<Vector<Double>> rs) {
        for (int t = 0; t < orig.size(); t++) {
            Vector<Double> dists = new Vector<Double>();
            for (int k = 0; k < orig.get(t).total(); k++) {
                FloatIndexer orig_idx = orig.get(t).createIndexer();
                FloatIndexer laig_idx = laig.get(t).createIndexer();

                double dist = Math.abs(orig_idx.get(k, 0) - laig_idx.get(k, 0)) +
                        Math.abs(orig_idx.get(k, 1) - laig_idx.get(k, 1));


                dists.add(dist);
            }
            rs.add(dists);
        }
    }


    void drawFeature(Mat inFrame) {


        FloatIndexer fpts0_idx = fpts[0].createIndexer();

        for (int t = 0; t < fpts[0].total(); t++) {
            circle(inFrame, new Point((int) fpts0_idx.get(t, 0), (int) fpts0_idx.get(t, 1)),
                    3, Scalar.RED, 1, 8, 0);
        }


    }

    void drawTrackLines() {

        FloatIndexer fpts0_idx = iniPoints.createIndexer();
        FloatIndexer fpts1_idx = fpts[1].createIndexer();
        for (int t = 0; t < fpts[1].total(); t++) {
            line(frame, new Point((int) fpts0_idx.get(t, 0), (int) fpts0_idx.get(t, 1)),
                    new Point((int) fpts1_idx.get(t, 0), (int) fpts1_idx.get(t, 1)),
                    Scalar.GREEN, 1, 8, 0);
            circle(frame, new Point((int) fpts1_idx.get(t, 0), (int) fpts1_idx.get(t, 1)),
                    3, Scalar.RED, 1, 8, 0);

        }

    }
}
