package com.example.demo1_opengl.render

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.view.Surface
import com.example.demo1_opengl.config.BufferUtil
import com.example.demo1_opengl.config.GLUtil
import com.example.demo1_opengl.holder.CameraPresenter
import com.example.demo1_opengl.shape.Drawer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by zyy on 2021/7/12
 *
 */
class CameraRender(context: Context) : GLSurfaceView.Renderer ,SurfaceTexture.OnFrameAvailableListener{

    private var context : Context = context
     var mDrawer: Drawer = Drawer(context)
    lateinit var mSurface : SurfaceTexture


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {


        mSurface = SurfaceTexture(mDrawer.textureId)
        mSurface.setOnFrameAvailableListener(this)

        //创建surface时打开相机
        CameraPresenter.camera.startPreview()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

        GLES20.glViewport(0,0,width,height)

    }

    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        mSurface.updateTexImage()

    }

    override fun onFrameAvailable(p0: SurfaceTexture?) {
        //TODO("Not yet implemented")
        //提示新的数据流到来

    }


}