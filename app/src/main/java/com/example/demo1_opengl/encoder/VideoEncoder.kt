package com.example.demo1_opengl.encoder

import com.example.demo1_opengl.encoder.base.BaseEncoder
import com.example.demo1_opengl.muxer.MediaMuxerManager

/**
 * Created by zyy on 2021/7/29
 *
 */
class VideoEncoder(mediaMuxerManager: MediaMuxerManager, encoderListener: EncoderListener,width : Int,height:Int)
    : BaseEncoder(mediaMuxerManager, encoderListener) {

    private val mWidth = width
    private val mHeight = height

    override fun prepare() {

    }
}