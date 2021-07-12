package com.example.demo1_opengl.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import com.example.demo1_opengl.render.CameraRender

/**
 * Created by zyy on 2021/7/12
 *
 */
class CameraSurfaceView (context: Context): GLSurfaceView(context),SurfaceTexture.OnFrameAvailableListener {


    var mRender : CameraRender


    init {
        setEGLContextClientVersion(2)
        mRender = CameraRender(context)
        setRenderer(mRender)
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    override fun onFrameAvailable(p0: SurfaceTexture?) {
        //TODO("Not yet implemented")
        //提示新的数据流到来
        this.requestRender()
    }



}