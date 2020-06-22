package com.mass.javacvffmpopencv;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;


import com.mass.javacvffmpopencv.javacvffmpopencv.helper.ipccamer;
import com.mass.javacvffmpopencv.utils.ShaderUtils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidx.appcompat.app.AppCompatActivity;

import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

public class CameraGlSurfaceShowActivity extends AppCompatActivity {


    public GLSurfaceView mCameraGlsurfaceView;
    public myrender mRenderer;
    private final String TAG = "CamerShowActivity";
    private ipccamer mipccamer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_gl_surface_show);
        mCameraGlsurfaceView = findViewById(R.id.glview);
        mCameraGlsurfaceView.setEGLContextClientVersion(2);
        mRenderer = new myrender();
        mCameraGlsurfaceView.setRenderer(mRenderer);
        mCameraGlsurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);

        final String inputFile = "rtsp://192.168.1.10:554/user=admin&password=&channel=1&stream=0.sdp?";
        mipccamer = new ipccamer();
        mipccamer.setBitmaprecvface(new ipccamer.bitmaprecvface() {
            @Override
            public void recv(Bitmap map) {
                Log.i(TAG,"recv"+map);
                mRenderer.setmBitmap(map);
                mCameraGlsurfaceView.requestRender();
            }
        });
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    mipccamer.frameRecord(inputFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

        }.execute();

    }


    class myrender implements GLSurfaceView.Renderer {
        private final float[] sPos = {
                -1.0f, 1.0f,
                -1.0f, -1.0f,
                1.0f, 1.0f,
                1.0f, -1.0f
        };

        private final float[] sCoord = {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
        };

        public void setmBitmap(Bitmap buf) {
            this.mBitmap =buf;
        }

        private Bitmap mBitmap;
        private int mProgram;
        private int glHPosition;
        private int glHTexture;
        private int glHCoordinate;
        private int glHMatrix;


        private FloatBuffer bPos;
        private FloatBuffer bCoord;
        private int textureId;
        private float[] mViewMatrix = new float[16];
        private float[] mProjectMatrix = new float[16];
        private float[] mMVPMatrix = new float[16];

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.i(TAG, "onSurfaceCreated");
            GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            GLES20.glEnable(GLES20.GL_TEXTURE_2D);
            mProgram = ShaderUtils.createProgram(getResources(),
                    "filter/default_vertex.sh",
                    "filter/default_fragment.sh");
            glHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
            glHCoordinate = GLES20.glGetAttribLocation(mProgram, "vCoordinate");
            glHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");
            glHMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");

            try {
                //   mBitmap = BitmapFactory.decodeStream(getResources().getAssets().open("texture/fengj.png"));
                mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.companyicon);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ByteBuffer bb = ByteBuffer.allocateDirect(sPos.length * 4);
            bb.order(ByteOrder.nativeOrder());
            bPos = bb.asFloatBuffer();
            bPos.put(sPos);
            bPos.position(0);
            ByteBuffer cc = ByteBuffer.allocateDirect(sCoord.length * 4);
            cc.order(ByteOrder.nativeOrder());
            bCoord = cc.asFloatBuffer();
            bCoord.put(sCoord);
            bCoord.position(0);
            textureId = createTexture();
        }

        private void bindTexture() {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            //根据以上指定的参数，生成一个2D纹理
         //   GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_RGBA,1920,1080,0,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,buffer);
            GLES20.glUniform1f(glHTexture,0);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);


           // mBitmap.recycle();
        }

        private int createTexture() {
            int[] texture = new int[1];

                Log.i(TAG, "createTexture");
                GLES20.glDeleteTextures(1, texture, 0);
                //生成纹理
                GLES20.glGenTextures(1, texture, 0);
                //生成纹理
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
                //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                //根据以上指定的参数，生成一个2D纹理
              //  GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);

                //     mBitmap.recycle();
                return texture[0];


        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.i(TAG, "onSurfaceChanged");
            GLES20.glViewport(0, 0, width, height);

            int w = mBitmap.getWidth();
            int h = mBitmap.getHeight();
            float sWH = w / (float) h;
            float sWidthHeight = width / (float) height;

            if (width > height) {
                if (sWH > sWidthHeight) {
                    Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight * sWH, sWidthHeight * sWH, -1, 1, 3, 5);
                } else {
                    Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight / sWH, sWidthHeight / sWH, -1, 1, 3, 5);
                }
            } else {
                if (sWH > sWidthHeight) {
                    Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1 / sWidthHeight * sWH, 1 / sWidthHeight * sWH, 3, 5);
                } else {
                    Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH / sWidthHeight, sWH / sWidthHeight, 3, 5);
                }
            }
            //设置相机位置
            Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 5.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
            //计算变换矩阵
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            Log.i(TAG, "onDrawFrame");
            Log.i(TAG, "mBitmap=" + mBitmap);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            GLES20.glUseProgram(mProgram);
            bindTexture();
            GLES20.glUniformMatrix4fv(glHMatrix, 1, false, mMVPMatrix, 0);
            GLES20.glEnableVertexAttribArray(glHPosition);
            GLES20.glEnableVertexAttribArray(glHCoordinate);
            GLES20.glUniform1i(glHTexture, 0);

            //传入顶点坐标
            GLES20.glVertexAttribPointer(glHPosition, 2, GLES20.GL_FLOAT, false, 0, bPos);
            //传入纹理坐标
            GLES20.glVertexAttribPointer(glHCoordinate, 2, GLES20.GL_FLOAT, false, 0, bCoord);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        }
    }

}
