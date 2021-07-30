package com.example.demo1_opengl.encoder.base

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.demo1_opengl.muxer.MediaMuxerManager
import java.nio.ByteBuffer

/**
 * Created by zyy on 2021/7/29
 *
 *
 * 记录：
 * 1）mMediaCodec还未初始化 release==null
 */
abstract class BaseEncoder(mediaMuxerManager: MediaMuxerManager,encoderListener: EncoderListener) : Runnable{

    protected val TIMEOUT_USEC  : Long= 10000

    protected lateinit var mMediaCodec : MediaCodec
    private  var mBufferInfo : MediaCodec.BufferInfo = MediaCodec.BufferInfo()

    protected var mMediaMuxerManager: MediaMuxerManager = mediaMuxerManager
    protected var mEncoderListener: EncoderListener = encoderListener

    interface EncoderListener{
        fun onPrepared(encoder: BaseEncoder);
        fun onStopped(encoder: BaseEncoder);
    }


    //同步锁
    protected val mSync = java.lang.Object()
    //是否正在录制
    protected var mIsRecoding : Boolean = false
    //可用数据帧数量
    private var mRequestEncoderCount : Int = 0
    //结束录制
    protected var mRequestStop : Boolean = false
    protected var mIsEndOfStream :Boolean = false
    protected var mMuxerStarted : Boolean = false
    protected var mTrackIndex : Int = 0


    init {
        mMediaMuxerManager.setVideoEncoder(this@BaseEncoder)
        synchronized(mSync){

            Thread(this, javaClass.simpleName).start()
            try {
                mSync.wait()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun run() {
        //开启线程
        synchronized(mSync){
            mRequestStop = false
            mRequestEncoderCount = 0
            mSync.notify()
        }

        val isRunning : Boolean = true
        var localRequestStop : Boolean = false
        var localRequestEncoderFlag : Boolean = false

        while (isRunning){
            synchronized(mSync){
                localRequestStop = mRequestStop
                localRequestEncoderFlag = mRequestEncoderCount > 0
                if (localRequestEncoderFlag)
                    mRequestEncoderCount--;
            }

            //停止编码时调用
            if (localRequestStop){
                drainEncoder()
                signalEndOfInputStream()
                drainEncoder()
                release()
                break
            }

            if(localRequestEncoderFlag){
                drainEncoder()
            }else{
                //线程进入等待状态
                synchronized(mSync){
                    try {
                        mSync.wait()
                    }catch (e : InterruptedException){
                        e.printStackTrace()
                    }
                }
            }
        }

        synchronized(mSync){
            mRequestStop = true
            mIsRecoding = false

        }

    }

    /**
     * 主线程调用
     */
    abstract fun prepare()

    /**
     * 主线程调用
     */
    fun startRecording(){
        synchronized(mSync){
            mIsRecoding = true
            mRequestStop = false
            mSync.notifyAll()

        }
    }

    /**
     * 主线程调用
     */
    fun stopRecording(){
        synchronized(mSync){
            if (!mIsRecoding || mRequestStop){
                return
            }
            mRequestStop = true
            mSync.notifyAll()
        }
    }

    /**
     * 帧是否可用
     */
    fun isFrameAvailable() : Boolean{
        synchronized(mSync){
            //正在录制
            if (!mIsRecoding || mRequestStop){
                return false
            }
            mRequestEncoderCount++
            mSync.notifyAll()
        }
        return true
    }

    /**
     * 释放资源
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun release(){
        //回调停止
        try {
            mEncoderListener.onStopped(this@BaseEncoder)
        }catch (e:Exception){
            e.printStackTrace()
        }

        mIsRecoding = false

        if(mMediaCodec != null){
            try {
                mMediaCodec.stop()
                mMediaCodec.release()
               // mMediaCodec = null
            }catch (e : Exception){
                e.printStackTrace()
            }
        }
        if (mMuxerStarted){
            if (mMediaMuxerManager != null){
                try {
                    mMediaMuxerManager.stop()
                }catch (e : Exception){
                    e.printStackTrace()
                }
            }
        }
      //  mBufferInfo = null
    }

    /**
     * 停止录制
     */
    fun signalEndOfInputStream(){
        encode(null,0,getPTSUs())
    }

    fun encode(buffer: ByteBuffer?, length: Int, presentationTimeUS: Long){
        if (!mIsRecoding)
            return
        val inputBuffers = mMediaCodec.inputBuffers
        while (mIsRecoding){
            val inputBufferIndex : Int = mMediaCodec.dequeueInputBuffer(TIMEOUT_USEC)
            if(inputBufferIndex >= 0){
                var inputBuffer : ByteBuffer = inputBuffers[inputBufferIndex]
                inputBuffer.clear()

                if(buffer != null){
                    inputBuffer.put(buffer)
                }
                if (length <= 0){
                    mIsEndOfStream = true
                    mMediaCodec.queueInputBuffer(
                        inputBufferIndex,0,0,
                        presentationTimeUS,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    )
                    break
                }else{
                    mMediaCodec.queueInputBuffer(
                        inputBufferIndex,0,length,
                        presentationTimeUS,0
                    )
                }
                break
            }else if(inputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER){

            }
        }
    }

    /**
     * 从缓冲区取数据，交给mMuxer编码
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun drainEncoder(){
        if(mMediaCodec == null)
            return
        if (mMediaMuxerManager == null)
            return

        var count : Int = 0

        //拿到输出缓冲区的数据 硬编码后的数据
        var encoderOutputBuffer = mMediaCodec.outputBuffers

        while (mIsRecoding){
            //输出缓冲区索引
            var encoderStatus : Int = mMediaCodec.dequeueOutputBuffer(mBufferInfo,TIMEOUT_USEC)

            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER){
                //超时 没有获取到
                if (!mIsEndOfStream){
                    if(++count > 5){
                        break;
                    }
                }
            }else if(encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED){
                // this shoud not come when encoding
                //拿到输出缓冲区,用于取到编码后的数据
                encoderOutputBuffer = mMediaCodec.outputBuffers
            }else if(encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                //输出格式修改
                if(mMuxerStarted){
                    throw RuntimeException("format change twice")
                }
                val format : MediaFormat = mMediaCodec.outputFormat
                mTrackIndex = mMediaMuxerManager.addTrack(format)
                mMediaMuxerManager.start()
                mMuxerStarted = true
            }else if (encoderStatus < 0){

            }else{
                var encoderData : ByteBuffer = encoderOutputBuffer[encoderStatus]
                if(encoderData == null){
                    throw RuntimeException("encoderOutputBuffer $encoderStatus was null")
                }
                if(mBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM !== 0){
                    mBufferInfo.size = 0
                }
                if(mBufferInfo.size != 0){

                    // encoded data is ready, clear waiting counter
                    count = 0
                    if (!mMuxerStarted) {
                        // muxer is not ready...this will prrograming failure.
                        throw java.lang.RuntimeException("drain:muxer hasn't started")
                    }
                    // write encoded data to muxer(need to adjust presentationTimeUs.
                   // encoderData.position(mBufferInfo.offset)
                   // encoderData.limit(mBufferInfo.offset+mBufferInfo.size)
                    mBufferInfo.presentationTimeUs = getPTSUs()
                    // 编码
                    mMediaMuxerManager.writeSampleData(mTrackIndex,encoderData,mBufferInfo)
                }
                mMediaCodec.releaseOutputBuffer(encoderStatus,false)
                //
                if (mBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    // when EOS come.
                    mIsRecoding = false
                    break // out of while
                }
            }
        }
    }

    private var preOutputPTSUs : Long = 0
    protected fun getPTSUs() : Long{
        var res : Long = System.nanoTime() / 1000L
        if (res < preOutputPTSUs)
            res += (preOutputPTSUs - res)
        return res
    }
}