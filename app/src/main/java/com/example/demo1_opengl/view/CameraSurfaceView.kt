package com.example.demo1_opengl.view

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.demo1_opengl.R
import com.example.demo1_opengl.render.CameraRender


/**
 * Created by zyy on 2021/7/12
 *
 */
class CameraSurfaceView : GLSurfaceView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)





    var mRender : CameraRender

   // var imageView : ImageView

    init {
        setEGLContextClientVersion(2)
        mRender = CameraRender(this)
        setRenderer(mRender)
        renderMode = RENDERMODE_WHEN_DIRTY;

       // imageView = findViewById(R.id.photo_img)
    }

    fun change(boolean: Boolean){

        mRender.changeType(boolean)

    }
    fun take(boolean: Boolean) {
        Log.d("TAG", "take: aaaa")
        mRender.setTaking(boolean)
    }






}