package com.example.demo1_opengl.filter.base

import android.content.Context
import android.opengl.GLES20
import com.example.demo1_opengl.utils.GLUtil

/**
 * Created by zyy on 2021/7/14
 *
 */
abstract class AbstractFilter(context: Context) : IFilter{
    var context : Context = context
    var mProgramId : Int = createProgram(context)



    abstract fun createProgram(context: Context) : Int

    fun compileShader(file: String, type: Int): Int {
        var shaderCode = GLUtil.readRawShaderCode(context, file)
        return GLUtil.compileShaderCode(type, shaderCode)
    }

    abstract fun getGLSLHandle()

    fun useProgram(){
        GLES20.glUseProgram(mProgramId)
    }
    abstract fun bindTexture(textureId : Int)



}