package com.example.demo1_opengl.holder

import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.opengl.GLES20
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import com.example.demo1_opengl.view.CameraSurfaceView


/**
 * Created by zyy on 2021/7/12
 *
 * 控制类
 */
class CameraPresenter(appCompatActivity: AppCompatActivity,cameraSurfaceView: CameraSurfaceView) {



    companion object{
        var cameraId : Int =getCameraId(Camera.CameraInfo.CAMERA_FACING_BACK)
        var camera : Camera = Camera.open(cameraId)
        lateinit var parameters : Camera.Parameters
        var orientation : Int = 0

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
    }


    private var surfaceTexture: SurfaceTexture
    private var cameraSurfaceView : CameraSurfaceView = cameraSurfaceView

    init {

        initParameters()
        surfaceTexture = SurfaceTexture(cameraSurfaceView.mRender.mDrawer.textureId)
        camera.setPreviewTexture(surfaceTexture)
        setPreviewSize()
        setCameraDisplayOrientation(appCompatActivity)


    }

    fun initParameters(){
        try{
            parameters = camera.parameters
            //预览格式
            //预览格式
            parameters.previewFormat = ImageFormat.NV21
            //判断是否支持连续自动对焦图像
            //判断是否支持连续自动对焦图像
            camera.parameters = parameters
        }catch (e:Exception){
            e.printStackTrace()
        }
    }


    /**
     * 创建纹理并绑定到surfaceTexture
     */
    fun bindSurfaceView(){
        var textureId = IntArray(1)
        GLES20.glGenTextures(1,textureId,0)
        surfaceTexture = SurfaceTexture(textureId[0])
        camera.setPreviewTexture(surfaceTexture)

    }




    /**
     * 设置预览大小
     */
    private fun setPreviewSize(){

        parameters.setPreviewSize(
            parameters.supportedPreviewSizes[0].width,
            parameters.supportedPreviewSizes[0].height
        )
    }

    /**
     * 调整预览方向
     * 官方推荐方法
     */
    private fun setCameraDisplayOrientation(appCompatActivity: AppCompatActivity) {
        val cameraInfo = CameraInfo()
        Camera.getCameraInfo(cameraId, cameraInfo)
        val rotation = appCompatActivity.windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result: Int
        //计算图像要旋转的角度
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360
            result = (360 - result) % 360
        } else {
            result = (cameraInfo.orientation - degrees + 360) % 360
        }
        orientation = result
        camera.setDisplayOrientation(orientation)

    }



}