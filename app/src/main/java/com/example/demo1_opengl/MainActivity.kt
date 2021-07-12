package com.example.demo1_opengl

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.demo1_opengl.config.PermissionUtil
import com.example.demo1_opengl.config.ToastUtil
import com.example.demo1_opengl.holder.CameraPresenter
import com.example.demo1_opengl.view.CameraSurfaceView

class MainActivity : AppCompatActivity() {

    private lateinit var cameraSurfaceView: CameraSurfaceView
    private lateinit var cameraPresenter: CameraPresenter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraSurfaceView = CameraSurfaceView(this)
        cameraPresenter = CameraPresenter(this,cameraSurfaceView)

        setContentView(cameraSurfaceView)
        checkNeedPermissions()
    }



    /**
     * 检查所需要的权限
     */
    private fun checkNeedPermissions() {

        //Android6.0 以上 需要动态申请
        //Build.VERSION.SDK_INT 获取手机的操作系统版本号
        val pm = packageManager
        var permission = PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.CAMERA", this.packageName)
        if (permission) {
            //"有这个权限"
        } else {
            //"没有这个权限"
            //如果android版本大于等于6.0，权限需要动态申请
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 15)
            }
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
                ), 1)
            }
        }
    }

    /**
     * 权限回调
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    PermissionUtil.showPermissionSettingDialog(this, Manifest.permission.CAMERA)
                }
            } else {
                ToastUtil.showShortToast(this, "请重试~~")
            }
        }
    }

}