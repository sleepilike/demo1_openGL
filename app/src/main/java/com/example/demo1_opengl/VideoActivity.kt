package com.example.demo1_opengl

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.example.demo1_opengl.view.CameraSurfaceView

class VideoActivity : AppCompatActivity() ,View.OnClickListener{

    private lateinit var mView : CameraSurfaceView
    private lateinit var mRecordButton : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        if (supportActionBar != null){
            supportActionBar?.hide()
        }

        mView = findViewById(R.id.videoView)

        //mBackButton = findViewById(R.id.back_bt)
       // mBackButton.setOnClickListener(this)

        mRecordButton = findViewById(R.id.recoding_bt)
        mRecordButton.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id){

        }
    }
}