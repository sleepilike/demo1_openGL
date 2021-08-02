package com.example.demo1_opengl.encoder

import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLContext
import android.opengl.GLES20
import android.os.Build
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi
import com.example.demo1_opengl.encoder.until.EGLHelper
import com.example.demo1_opengl.encoder.until.EncoderUtil
import com.example.demo1_opengl.filter.FBODrawer
import com.example.demo1_opengl.filter.base.GLFrameBuffer
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import javax.microedition.khronos.egl.EGL


/**
 * Created by zyy on 2021/7/30
 *
 * EncoderConfig 缺失eglContext 采用glSurface
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class CameraRecordEncoder : Runnable {


    companion object{
        private val TAG = "CameraRecordEncoder"

        class EncoderConfig(
            outputFile: File, width: Int, height: Int,
            bitRate: Int, mEglContext : EGLContext, context: Context,
        ){
            val mOutputFile : File = outputFile
            val mWidth :Int = width
            val mHeight :Int = height
            val mBitRate : Int = bitRate
            val context : Context = context
            val mEGLContext : EGLContext = mEglContext
        }
    }

    @Volatile
    private lateinit var mHandler: EncoderHandler
    private var mSyncLock = java.lang.Object()
    private var mReady = false
    private var mRunning = false

    fun isRecording() : Boolean{
        synchronized(mSyncLock){
            return mRunning
        }
    }

    /**
     * 开始录制视频
     */
    fun startRecording(encoderConfig: EncoderConfig){
        Log.d(TAG, "startRecording()")
        synchronized(mSyncLock){
            if(mRunning){
                Log.w(TAG, "startRecording: already running")
                return
            }
            mRunning = true
            Thread(this, "CameraRecordEncoder").start()
            while (!mReady){
                try {
                    //等待编码器线程启动
                    mSyncLock.wait()
                }catch (e : InterruptedException){
                    e.printStackTrace()
                }
            }
        }
        mHandler?.sendMessage(
            mHandler!!.obtainMessage(EncoderHandler.MSG_START_RECORDING,encoderConfig)
        )
    }

    /**
     * 停止录像
     */
    fun stopRecording(){
        mHandler?.sendMessage(
            mHandler!!.obtainMessage(EncoderHandler.MSG_STOP_RECORDING)
        )
        mHandler?.sendMessage(
            mHandler!!.obtainMessage(EncoderHandler.MSG_QUIT)
        )
    }

    /**
     * 实时预览的时候渲染到CameraRecordEncoder
     */
    fun frameAvailable(){

    }
    override fun run() {
        Looper.prepare()
        synchronized(mSyncLock){
            mHandler = EncoderHandler(this)
            mReady = true
            mSyncLock.notify()
        }
        Looper.loop()

        synchronized(mSyncLock){
            mReady = false
            mRunning = false
            //mHandler = null
        }
    }

    class EncoderHandler (encoder: CameraRecordEncoder): android.os.Handler(){
        companion object{
            const val MSG_START_RECORDING = 0 //开始
            const val MSG_STOP_RECORDING = 1 //结束
            const val MSG_QUIT = 2
            const val MSG_FRAME_AVAILABLE = 3
            const val MSG_SET_TEXTURE_ID = 4
        }

        private var mWeakEncoder : WeakReference<CameraRecordEncoder> = WeakReference<CameraRecordEncoder>(encoder)
        override fun handleMessage(msg: Message) {
            var encoder : CameraRecordEncoder? = mWeakEncoder.get()
            if (encoder == null)
                return
            when(msg.what){

            }
        }
    }

    private var mTextureId : Int = 0
    private lateinit var mEncoderUtil : EncoderUtil
    private lateinit var mEGLHelper: EGLHelper
    private lateinit var mConfig : EncoderConfig
    private lateinit var mInputSurface : Surface
    private var frameBuffer : GLFrameBuffer = GLFrameBuffer()
    private var mDrawer : FBODrawer = FBODrawer(mConfig.context)

    private fun handleStartRecording(config : EncoderConfig){
        try {
            mEncoderUtil = EncoderUtil(config.mWidth,config.mHeight,config.mBitRate,config.mOutputFile)
            mConfig = config

            mInputSurface = mEncoderUtil.getSurface()

            mEGLHelper = EGLHelper(mInputSurface,mConfig.mEGLContext)

            frameBuffer.prepare()

        }catch (e:IOException){
            throw RuntimeException(e)
        }
    }

    private fun handleStopRecording(){
        mEncoderUtil.drainEncoder(true)
        releaseEncoder()
    }

    private fun releaseEncoder(){
        mEncoderUtil.release()
        mInputSurface.release()
    }

    private fun handleSetTexture (textureId : Int){
        mTextureId = textureId
        mDrawer.setSize(mConfig.mWidth,mConfig.mHeight,frameBuffer.width,frameBuffer.height)
    }

    private fun handleFrameAvailable(){
        //先推动一次编码器工作 把编码后的数据写入muxer
        mEncoderUtil.drainEncoder(false)

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        GLES20.glViewport(0, 0, mConfig.mWidth, mConfig.mHeight)

        /*
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,frameBuffer.mFrameBuffer)
        mDrawer.setTextureId(mTextureId)
        mDrawer.draw()
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

         */


        //如何将数据推给编码器？


    }


}