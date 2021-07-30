package com.example.demo1_opengl.encoder

import android.content.Context
import android.os.Looper
import android.os.Message
import android.util.Log
import java.io.File
import java.lang.ref.WeakReference

/**
 * Created by zyy on 2021/7/30
 *
 * EncoderConfig 缺失eglContext 采用glSurface
 */
class CameraRecordEncoder : Runnable {


    companion object{
        private val TAG = "CameraRecordEncoder"

        class EncoderConfig(
            outputFile: File, width: Int, height: Int,
            bitRate: Int, context: Context,
        ){
            val mOutputFile : File = outputFile
            val mWidth :Int = width
            val mHeight :Int = height
            val mBitRate : Int = bitRate
            val context : Context = context
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

        }
    }
}