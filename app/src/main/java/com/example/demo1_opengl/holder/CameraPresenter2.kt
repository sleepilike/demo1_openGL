package com.example.demo1_opengl.holder

import android.graphics.ImageFormat
import android.graphics.Point
import android.hardware.Camera
import java.lang.Exception
import java.util.*

/**
 * Created by zyy on 2021/7/23
 *
 */
class CameraPresenter2 : Camera.PreviewCallback{

    private var cameraId : Int = getCameraId(Camera.CameraInfo.CAMERA_FACING_BACK)
    private var camera : Camera = Camera.open(cameraId)

    private var width : Int = 0
    private var height : Int = 0

    fun setSize(width : Int,height : Int){
        this.width = width
        this.height = height
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {

    }

    private fun getCameraId(faceOrBack : Int) : Int{
        val numbers = Camera.getNumberOfCameras()
        for (i in 0 until numbers){
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(i,info)
            if(info.facing == faceOrBack)
                return i
        }
        return  -1
    }

    private fun initParameters(){
        try {
            var parameters = camera.parameters
            //预览格式
            parameters.previewFormat = ImageFormat.NV21
            //对焦模式
            val supportedFocusModes = parameters.supportedFocusModes
            if (supportedFocusModes != null && supportedFocusModes.size > 0) {
                when {
                    supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) -> {
                        parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                    }
                    supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) -> {
                        parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
                    }
                    supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO) -> {
                        parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                    }
                }
            }
            camera.parameters = parameters
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    private fun setPreViewSize(){
        var parameters = camera.parameters
        var previewSize : Camera.Size = parameters.previewSize

    }




}