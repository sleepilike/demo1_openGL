package com.example.demo1_opengl.muxer

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.demo1_opengl.encoder.base.BaseEncoder
import java.nio.ByteBuffer

/**
 * Created by zyy on 2021/7/29
 *
 */
class MediaMuxerManager {

    private val DIR_NAME = "VIDEO_RECODE"

    private lateinit var mMediaMuxer : MediaMuxer
    private var mEncoderCount : Int = 0
    private var mStartedCount : Int = 0
    private lateinit var mVideoEncoder : BaseEncoder
    private var mIsStarted : Boolean = false

    fun setVideoEncoder(videoEncoder : BaseEncoder){
        this.mVideoEncoder = videoEncoder;
        mEncoderCount = 1
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun addTrack(format : MediaFormat) : Int{
        return mMediaMuxer.addTrack(format)
    }

    /**
     * true muxer开始编码
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Synchronized
    fun start(): Boolean {
        mStartedCount++
        if (mEncoderCount > 0 && mStartedCount == mEncoderCount) {
            mMediaMuxer.start()
            mIsStarted = true
            //notifyAll()
        }
        return mIsStarted
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Synchronized
    fun stop() {
        mStartedCount--
        if (mEncoderCount > 0 && mStartedCount <= 0) {
            mMediaMuxer.stop()
            mMediaMuxer.release()
            mIsStarted = false
        }
    }

    /**
     * 写入数据
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun writeSampleData(trackIndex : Int, byteBuffer: ByteBuffer, bytebufferInfo : MediaCodec.BufferInfo){
        if(mStartedCount > 0){
            mMediaMuxer.writeSampleData(trackIndex,byteBuffer,bytebufferInfo)
        }
    }


}