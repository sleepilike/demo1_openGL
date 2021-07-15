package com.example.demo1_opengl.render

import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.demo1_opengl.utils.GLUtil
import com.example.demo1_opengl.holder.CameraPresenter
import com.example.demo1_opengl.filter.Drawer
import com.example.demo1_opengl.filter.FBODrawer
import com.example.demo1_opengl.filter.base.GLFrameBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by zyy on 2021/7/12
 *
 */
class CameraRender(appCompatActivity: AppCompatActivity,glSurfaceView: GLSurfaceView) : GLSurfaceView.Renderer {


    private var cameraPresenter :CameraPresenter = CameraPresenter()
    private lateinit var mDrawer: Drawer
    private lateinit var mFBODrawer: FBODrawer
    private var mTexture : Int = -1
    private lateinit var mSurfaceTexture: SurfaceTexture
    private var glSurfaceView : GLSurfaceView = glSurfaceView
    private lateinit var glFrameBuffer : GLFrameBuffer

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

        glFrameBuffer = GLFrameBuffer(mTexture)

        mDrawer = Drawer(glSurfaceView.context)
        mFBODrawer = FBODrawer(glSurfaceView.context)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {


        GLES20.glViewport(0,0,width,height )
        cameraPresenter.width = width
        cameraPresenter.height = height

        //glFrameBuffer.width = width
        //glFrameBuffer.height = 16*width/9
        glFrameBuffer.width =1000
        glFrameBuffer.height = 1000
        glFrameBuffer.prepare()
        Log.d("TAG", "cameraPresenter.width: $width")
        Log.d("TAG", "cameraPresenter.height: $height")
        Log.d("TAG", "glFrameBuffer.width: ${glFrameBuffer.width}")
        Log.d("TAG", "glFrameBuffer.height: ${glFrameBuffer.height}")
        mFBODrawer.setSize(width,height,glFrameBuffer.width,glFrameBuffer.height)


        //开始预览
        cameraPresenter.startPreview(mSurfaceTexture)



    }

    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //将摄像头数据从surfaceTure中取出
        mSurfaceTexture.updateTexImage()
        mSurfaceTexture.getTransformMatrix(mtx)



        //将数据绘制到fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,glFrameBuffer.mFrameBuffer)
        mDrawer.draw(mTexture,mtx )
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        mFBODrawer.setTextureId(glFrameBuffer.m2DTextureId)
        mFBODrawer.draw()


        //mDrawer.draw(mTexture,mtx)

    }




}