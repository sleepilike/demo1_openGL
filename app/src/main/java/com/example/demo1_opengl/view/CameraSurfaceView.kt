package com.example.demo1_opengl.view

import android.content.Context
import android.opengl.GLSurfaceView
import com.example.demo1_opengl.render.CameraRender

/**
 * Created by zyy on 2021/7/12
 *
 */
class CameraSurfaceView (context: Context): GLSurfaceView(context) {


    private var mRender : CameraRender

    init {
        setEGLContextClientVersion(2)
        mRender = CameraRender(context)
        setRenderer(mRender)
    }



}