package com.example.demo1_opengl.encoder.until

import android.opengl.*
import android.os.Build
import android.view.Surface
import androidx.annotation.RequiresApi


/**
 * Created by zyy on 2021/8/2
 *
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class EGLHelper(surface: Surface,sharedContext : EGLContext){
    val TAG = "EGLHelper"

    private lateinit var mEGL : EGL14
    private  var mEGLDisplay: EGLDisplay
    private  var mEGLContext: EGLContext
    private  var mEGLSurface: EGLSurface
    private lateinit var mEGLConfig: EGLConfig

    init {

        //获取并初始化EGLDisplay
        //获取默认的显示设备，渲染的目标
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY)
            throw RuntimeException("egl no display")
        //初始化显示设备 主次版本号
        var version = intArrayOf(2)
        if(!EGL14.eglInitialize(mEGLDisplay,version,0,version,1)){
            throw RuntimeException("eglInitialize error")
        }

        //获取EGLConfig
        //配置输出的格式 也就是制定FrameBuffer的配置参数
        val attrList = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,  //颜色缓冲区中红色的位数
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,  //渲染窗口支持的布局组成
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,  //egl支持的窗口类型
            EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(mEGLDisplay,
                attrList,
                0,
                configs,
                0,
                configs.size,
                numConfigs,
                0)
        ) {
            throw RuntimeException("unable to find RGB888 ES2 EGL config")
        }
        mEGLConfig = configs[0]!!


        //创建EGLContext
        val attriList = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        //第三个参数即为需要共享的上下文对象，资源包括纹理、FrameBuffer以及其他的Buffer等资源。
        mEGLContext =
            EGL14.eglCreateContext(mEGLDisplay, configs[0], sharedContext, attriList, 0)
        //checkEglError("eglCreateContext")
        if (mEGLContext == null) {
            throw java.lang.RuntimeException("context is null")
        }

        //创建EGLSurface
        mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay,mEGLConfig,surface,
            attrList, 0)

        //绑定eglcontext和surface到显示设备中
        if(!EGL14.eglMakeCurrent(mEGLDisplay,mEGLSurface,mEGLSurface,mEGLContext)){
            throw java.lang.RuntimeException("eglMakeCurrent fail")
        }


    }
    fun swapBuffers() : Boolean{
        return EGL14.eglSwapBuffers(mEGLDisplay,mEGLSurface)
    }

    fun destoryEGL(){
        if(mEGLSurface != null && mEGLSurface!= EGL14.EGL_NO_SURFACE){
            EGL14.eglMakeCurrent(mEGLDisplay,EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroySurface(mEGLDisplay,mEGLSurface)
            //mEGLSurface = null
        }

        if (mEGLContext != null){
            EGL14.eglDestroyContext(mEGLDisplay,mEGLContext)
        }

        if(mEGLDisplay != null){
            EGL14.eglTerminate(mEGLDisplay)
        }
    }

    fun getEGLContext() : EGLContext{
        return this.mEGLContext
    }
    fun setEGLContext(eglContext : EGLContext){
        this.mEGLContext = eglContext
    }

}