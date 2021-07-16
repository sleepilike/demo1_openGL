package com.example.demo1_opengl.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.example.demo1_opengl.render.CameraRender


/**
 * Created by zyy on 2021/7/12
 *
 */
class CameraSurfaceView (context: Context,appCompatActivity: AppCompatActivity): GLSurfaceView(context) {


    var mRender : CameraRender


    init {
        setEGLContextClientVersion(2)
        mRender = CameraRender(appCompatActivity,this)
        setRenderer(mRender)
        renderMode = RENDERMODE_WHEN_DIRTY;
    }

    fun change(boolean: Boolean){

        mRender.changeType(boolean)

    }
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        super.surfaceDestroyed(holder)

    }





}