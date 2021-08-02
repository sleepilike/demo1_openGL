package com.example.demo1_opengl.encoder.until

import android.media.MediaCodec
import android.media.MediaCodecInfo.CodecCapabilities
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi
import java.io.File
import java.nio.ByteBuffer

/**
 * Created by zyy on 2021/8/2
 *
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class EncoderUtil (width : Int, height:Int, bitRate : Int, outFile : File){
    companion object{
        private val DEBUG :Boolean = true
        private val FRAME_RATE : Int = 30
        private val I_FRAME_INTERVAL : Int =5

    }
    private var mEncoder : MediaCodec
    private var mInputSurface : Surface
    private var mMuxer : MediaMuxer

    init {

        //设置编码器类型
        var format : MediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,width,height)
        format.setInteger(MediaFormat.KEY_BIT_RATE,bitRate)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,  //设置输入源类型为原生Surface 重点1 参考下面官网复制过来的说明
            CodecCapabilities.COLOR_FormatSurface)

        //创建编码器 配置以上设置
        mEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mEncoder.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE)

        //获取输入源surface
        mInputSurface = mEncoder.createInputSurface()
        mEncoder.start()

        //创建混合器
        mMuxer = MediaMuxer(outFile.toString(),MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    }

    fun getSurface () : Surface{
        return mInputSurface
    }

    private lateinit var mBufferInfo : MediaCodec.BufferInfo
    private var mTrackIndex : Int = 0
    private var mMuxerStarted : Boolean = false
    private val TIMEOUT_USEC : Long = 10000

    fun drainEncoder(endOfStream : Boolean){
        if (endOfStream){
            mEncoder.signalEndOfInputStream()
        }

        var encoderOutputBuffers = mEncoder.outputBuffers
        while (true){
            var encoderStatus : Int = mEncoder.dequeueOutputBuffer(mBufferInfo,TIMEOUT_USEC)
            if(encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER){
                if(!endOfStream)
                    break
                else{
                    if (DEBUG){

                    }
                }
            }else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED){
                encoderOutputBuffers = mEncoder.outputBuffers
            }else if(encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                if(mMuxerStarted){
                    throw RuntimeException("format change twice")
                }
                var format : MediaFormat = mEncoder.outputFormat
                mTrackIndex = mMuxer.addTrack(format)
                mMuxer.start()
                mMuxerStarted = true
            }else if(encoderStatus < 0){
                Log.w("","unexpected result from encoder.dequeueOutputBuffer: $encoderStatus")
            }else{
                var encoderData : ByteBuffer = encoderOutputBuffers[encoderStatus]
                if(encoderData == null)
                    throw java.lang.RuntimeException("encoderOutputBuffer " + encoderStatus + " was null")
                if ((mBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0){
                    // 这表明，标记为这样的缓冲器包含编解码器初始化/编解码器特定数据而不是媒体数据。
                    if (DEBUG) {
                        Log.d("", "ignoring BUFFER_FLAG_CODEC_CONFIG")
                        mBufferInfo.size = 0
                    }
                }
                if(mBufferInfo.size != 0){
                    if(!mMuxerStarted){
                        throw  java.lang.RuntimeException("muxer hasn't started")
                    }
                    encoderData.position(mBufferInfo.offset)
                    encoderData.limit(mBufferInfo.offset+mBufferInfo.size)
                    mMuxer.writeSampleData(mTrackIndex,encoderData,mBufferInfo)
                    if (DEBUG){
                        Log.d("", "sent " + mBufferInfo.size + " bytes to muxer, ts=" +
                                mBufferInfo.presentationTimeUs)
                    }
                }
                mEncoder.releaseOutputBuffer(encoderStatus,false)

                if ((mBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
                    if(!endOfStream){
                        Log.w("", "reached end of stream unexpectedly", )
                    }else if( DEBUG){
                        Log.d("TAG", "end of stream reached ")
                    }
                    break
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun requestSyncFrame () {
        var  params : Bundle = Bundle()
        params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME,0)
        mEncoder.setParameters(params)
    }

    fun release(){
        if(DEBUG)
            Log.d("TAG", "releasing encoder objects ")
        mEncoder.stop()
        mEncoder.release()

        if(mTrackIndex != -1){
            mMuxer.stop()
            mMuxer.release()
        }
    }


}