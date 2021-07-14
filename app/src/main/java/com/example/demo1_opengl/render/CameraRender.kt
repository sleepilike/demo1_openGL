package com.example.demo1_opengl.render

import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.example.demo1_opengl.utils.GLUtil
import com.example.demo1_opengl.holder.CameraPresenter
import com.example.demo1_opengl.filter.Drawer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by zyy on 2021/7/12
 *
 */
class CameraRender(appCompatActivity: AppCompatActivity,glSurfaceView: GLSurfaceView) : GLSurfaceView.Renderer {


    private var cameraPresenter :CameraPresenter = CameraPresenter()
    private lateinit var mDrawer: Drawer
    private var mTexture : Int = -1
    private lateinit var mSurfaceTexture: SurfaceTexture
    private var glSurfaceView : GLSurfaceView = glSurfaceView

    //变换矩阵
    private var mtx = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //文字支持透明
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);


        mTexture = GLUtil.createOESTexture()
        mSurfaceTexture = SurfaceTexture(mTexture)
        mSurfaceTexture.setOnFrameAvailableListener(OnFrameAvailableListener {
            //触发 GLSurfaceView 的render的 onDrawFrame
            glSurfaceView.requestRender()
        })

        mDrawer = Drawer(glSurfaceView.context)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

        GLES20.glViewport(0,0,width,height)
        cameraPresenter.width = width
        cameraPresenter.height = height
        //开始预览
        cameraPresenter.startPreview(mSurfaceTexture)



    }

    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //将摄像头数据从surfaceTure中取出
        mSurfaceTexture.updateTexImage()
        mSurfaceTexture.getTransformMatrix(mtx)


        mDrawer.draw(mTexture,mtx )


    }




}