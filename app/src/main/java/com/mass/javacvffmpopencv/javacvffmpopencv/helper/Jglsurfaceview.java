package com.mass.javacvffmpopencv.javacvffmpopencv.helper;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Jglsurfaceview extends GLSurfaceView implements GLSurfaceView.Renderer  {
    public Jglsurfaceview(Context context) {
        super(context);
        //为了可以激活log和错误检查，帮助调试3D应用，需要调用setDebugFlags()。
        this.setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);

        this.setRenderer(this);

    }

    public Jglsurfaceview(Context context, AttributeSet attrs) {
        super(context, attrs);
        //为了可以激活log和错误检查，帮助调试3D应用，需要调用setDebugFlags()。
        this.setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);

        this.setRenderer(this);
    }

    private void t()
    {
        Surface surface = this.getHolder().getSurface();

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }
}
