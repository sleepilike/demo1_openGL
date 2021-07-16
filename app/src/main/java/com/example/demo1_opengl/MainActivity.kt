package com.example.demo1_opengl

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.demo1_opengl.utils.PermissionUtil
import com.example.demo1_opengl.utils.ToastUtil
import com.example.demo1_opengl.view.CameraSurfaceView
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams as LayoutParams1


class MainActivity : AppCompatActivity() {

    private lateinit var mView : CameraSurfaceView
    private lateinit var mButton : Button
    private lateinit var mLayout : ConstraintLayout
    private var mType : Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mLayout = findViewById(R.id.constrain_layout)
        if (supportActionBar != null){
            supportActionBar?.hide()
        }

        checkNeedPermissions()



        mView = CameraSurfaceView(this,this)
        mLayout.addView(mView)


        mButton = findViewById(R.id.cut_bt)

        mButton.setOnClickListener {
            mType = !mType
            mView.change(mType)
        }

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