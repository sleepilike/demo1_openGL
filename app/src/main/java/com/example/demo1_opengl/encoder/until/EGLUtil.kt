package com.example.demo1_opengl.encoder.until

import android.graphics.SurfaceTexture
import android.opengl.*
import android.os.Build
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi


/**
 * Created by zyy on 2021/8/2
 *
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class EGLUtil {

    companion object{
        val FLAG_RECORDABLE = 0x01
        val FLAG_TRY_GLES3 = 0x04
        val EGL_RECORDABLE_ANDROID = 0x3142

    }

    private var mEGLDisplay : android.opengl.EGLDisplay? = EGL14.EGL_NO_DISPLAY
    private var mEGLContext : EGLContext = EGL14.EGL_NO_CONTEXT
    private lateinit var mEGLConfig : EGLConfig
    private var mGLVersion : Int = -1



    constructor(sharedContext : EGLContext?,flags : Int){
        if(mEGLDisplay != EGL14.EGL_NO_DISPLAY)
            throw RuntimeException("EGL already set up")

        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (mEGLDisplay === EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("unable to get EGL14 display")
        }
        val version = IntArray(2)
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            mEGLDisplay = null
            throw RuntimeException("unable to initialize EGL14")
        }

        if (flags and FLAG_TRY_GLES3 != 0) {
            val config: EGLConfig? = getConfig(flags,3)
            if (config != null) {
                val attrib3_list = intArrayOf(
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL14.EGL_NONE
                )
                val context =
                    EGL14.eglCreateContext(mEGLDisplay, config, sharedContext, attrib3_list, 0)
                if (EGL14.eglGetError() == EGL14.EGL_SUCCESS) {
                    Log.d("", "Got GLES 3 config")
                    mEGLConfig = config
                    mEGLContext = context
                    mGLVersion = 3
                }
            }
        }

        if (mEGLContext === EGL14.EGL_NO_CONTEXT) {  //如果只要求GLES版本2  又或者GLES3失败了。
            Log.d("", "Trying GLES 2")
            val config = getConfig(flags, 2)
            if (config != null) {
                val attrib2_list = intArrayOf(
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
                )
                val context =
                    EGL14.eglCreateContext(mEGLDisplay, config, sharedContext, attrib2_list, 0)
                if (EGL14.eglGetError() == EGL14.EGL_SUCCESS) {
                    Log.d("", "Got GLES 2 config")
                    mEGLConfig = config
                    mEGLContext = context
                    mGLVersion = 2
                }
            }
        }
    }

    /**
     * 查看当前的 EGLDisplay, EGLContext, EGLSurface.
     */
    fun logCurrent() {
        val display: EGLDisplay = EGL14.eglGetCurrentDisplay()
        val context: EGLContext = EGL14.eglGetCurrentContext()
        val surface: EGLSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW)
        Log.i("", "Current EGL state : display=" + display + ", context=" + context +
                    ", surface=" + surface)
    }

    /**
     * 从本地设备中寻找合适的 EGLConfig.
     */
    private fun getConfig(flags: Int, version: Int): EGLConfig? {
        var renderableType = EGL14.EGL_OPENGL_ES2_BIT
        if (version >= 3) {
            renderableType = renderableType or EGLExt.EGL_OPENGL_ES3_BIT_KHR
        }
        val attribList = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,  //EGL14.EGL_DEPTH_SIZE, 16,
            //EGL14.EGL_STENCIL_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, renderableType,
            EGL14.EGL_NONE, 0,  // placeholder for recordable [@-3]
            EGL14.EGL_NONE
        )
        if (flags and FLAG_RECORDABLE != 0) {
            attribList[attribList.size - 3] =
                EGL_RECORDABLE_ANDROID //EGLExt.EGL_RECORDABLE_ANDROID; (required 26)
            attribList[attribList.size - 2] = 1
        }
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(mEGLDisplay,
                attribList,
                0,
                configs,
                0,
                configs.size,
                numConfigs,
                0)
        ) {
            Log.w("", "unable to find RGB8888 / $version EGLConfig")
            return null
        }
        return configs[0]
    }


    /**
     * 释放EGL资源
     */
    fun release() {
        if (mEGLDisplay !== EGL14.EGL_NO_DISPLAY) {
            // Android 使用一个引用计数EGLDisplay。
            // 因此，对于每个eglInitialize，我们需要一个eglTerminate。
            EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(mEGLDisplay)
        }
        mEGLDisplay = EGL14.EGL_NO_DISPLAY
        mEGLContext = EGL14.EGL_NO_CONTEXT
        //mEGLConfig = null
    }

    /**
     * 创建一个 EGL+Surface
     * @param surface
     * @return
     */
    fun createWindowSurface(surface: Any): EGLSurface? {
        if (surface !is Surface && surface !is SurfaceTexture) {
            throw java.lang.RuntimeException("invalid surface: $surface")
        }
        // 创建EGLSurface, 绑定传入进来的surface
        val surfaceAttribs = intArrayOf(
            EGL14.EGL_NONE
        )
        val eglSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface,
            surfaceAttribs, 0)
        if (eglSurface == null) {
            throw java.lang.RuntimeException("surface was null")
        }
        return eglSurface
    }

    /**
     * 用旧版的Pbuffer，创建离屏的EGLSurface
     */
    fun createOffscreenSurface(width: Int, height: Int): EGLSurface? {
        val surfaceAttribs = intArrayOf(
            EGL14.EGL_WIDTH, width,
            EGL14.EGL_HEIGHT, height,
            EGL14.EGL_NONE
        )
        val eglSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig,
            surfaceAttribs, 0)
        //GlUtil.checkGlError("eglCreatePbufferSurface")
        if (eglSurface == null) {
            throw java.lang.RuntimeException("surface was null")
        }
        return eglSurface
    }

    /**
     * 查询当前surface的状态值。
     */
    fun querySurface(eglSurface: EGLSurface?, what: Int): Int {
        val value = IntArray(1)
        EGL14.eglQuerySurface(mEGLDisplay, eglSurface, what, value, 0)
        return value[0]
    }

    /**
     * Makes our EGL context current, using the supplied surface for both "draw" and "read".
     */
    fun makeCurrent(eglSurface: EGLSurface?) {
        if (mEGLDisplay === EGL14.EGL_NO_DISPLAY) {
            Log.d("", "NOTE: makeCurrent w/o display")
        }
        if (!EGL14.eglMakeCurrent(mEGLDisplay, eglSurface, eglSurface, mEGLContext)) {
            throw java.lang.RuntimeException("eglMakeCurrent failed")
        }
    }

    /**
     * Makes our EGL context current, using the supplied "draw" and "read" surfaces.
     */
    fun makeCurrent(drawSurface: EGLSurface?, readSurface: EGLSurface?) {
        if (mEGLDisplay === EGL14.EGL_NO_DISPLAY) {
            Log.d("", "NOTE: makeCurrent w/o display")
        }
        if (!EGL14.eglMakeCurrent(mEGLDisplay, drawSurface, readSurface, mEGLContext)) {
            throw java.lang.RuntimeException("eglMakeCurrent(draw,read) failed")
        }
    }

    /**
     * Makes no context current.
     */
    fun makeNothingCurrent() {
        if (!EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT)
        ) {
            throw java.lang.RuntimeException("eglMakeCurrent failed")
        }
    }


    /**
     * Calls eglSwapBuffers.  Use this to "publish" the current frame.
     * @return false on failure
     */
    fun swapBuffers(eglSurface: EGLSurface?): Boolean {
        return EGL14.eglSwapBuffers(mEGLDisplay, eglSurface)
    }

    /**
     * Sends the presentation time stamp to EGL.  Time is expressed in nanoseconds.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun setPresentationTime(eglSurface: EGLSurface?, nsecs: Long) {
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, eglSurface, nsecs)
    }

    /**
     * 判断当前的EGLContext 和 EGLSurface是否同一个EGL
     */
    fun isCurrent(eglSurface: EGLSurface): Boolean {
        return mEGLContext == EGL14.eglGetCurrentContext() && eglSurface == EGL14.eglGetCurrentSurface(
            EGL14.EGL_DRAW)
    }

    /**
     * Destroys the specified surface.
     * Note the EGLSurface won't actually be destroyed if it's still current in a context.
     */
    fun releaseSurface(eglSurface: EGLSurface?) {
        EGL14.eglDestroySurface(mEGLDisplay, eglSurface)
    }

}