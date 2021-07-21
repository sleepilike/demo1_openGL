package com.example.demo1_opengl

import android.Manifest
import android.graphics.Bitmap
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.demo1_opengl.render.CameraRender
import com.example.demo1_opengl.view.CameraSurfaceView
import kotlin.math.log


class MainActivity : BaseActivity(),View.OnClickListener{

    private lateinit var mView : CameraSurfaceView
    private lateinit var mButton : Button
    private lateinit var mTakeButton: Button
    private lateinit var mImageView : ImageView
    private lateinit var mLayout : RelativeLayout
    private var mType : Boolean = true

    private var handler : Handler = object : Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                1-> {
                    Log.d("TAG,","handleMessage: 111")
                    mImageView.setImageBitmap(msg.obj as Bitmap)
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mLayout = findViewById(R.id.relative_layout)
        if (supportActionBar != null){
            supportActionBar?.hide()
        }

        requestPermission("请给予相机、存储权限，以便app正常工作",
            null,
            *arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE))

       // mView = CameraSurfaceView(this)
       // mLayout.addView(mView)
        mView = findViewById(R.id.camera_view)



        mButton = findViewById(R.id.cut_bt)
        mButton.setOnClickListener (this)

        mTakeButton = findViewById(R.id.put_bt)
        mTakeButton.setOnClickListener(this)

        mImageView = findViewById(R.id.photo_iv)


    }
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.cut_bt -> {
                Log.d("TAG", "onClick: 1")
                mType = !mType
                mView.change(mType)
            }
            R.id.put_bt ->{
                mView.take(true)
                mView.mRender.setOnListener(object : CameraRender.MInterface{
                    override fun take(bitmap: Bitmap) {
                        var message  = Message()
                        message.what = 1
                        message.obj = bitmap
                       handler.sendMessage(message)
                    }

                })
            }
        }
    }









}