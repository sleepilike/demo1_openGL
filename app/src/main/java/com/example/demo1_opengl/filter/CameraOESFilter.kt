package com.example.demo1_opengl.filter

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import com.example.demo1_opengl.filter.base.AbstractFilter
import com.example.demo1_opengl.utils.GLUtil
import java.nio.FloatBuffer

/**
 * Created by zyy on 2021/7/14
 *
 */
class CameraOESFilter(context: Context) : AbstractFilter(context) {
    val VERTEX_FILE = "shader/oes_vertex_shader.glsl"
    val FRAGMNET_FILE = "shader/oes_fragment_shader.glsl"


    override fun createProgram(context: Context): Int {
        //TODO("Not yet implemented")
        var vertexShaderId = compileShader(VERTEX_FILE,GLES20.GL_VERTEX_SHADER)
        var fragmentShaderId = compileShader(FRAGMNET_FILE,GLES20.GL_FRAGMENT_SHADER)
        return GLUtil.linkProgram(vertexShaderId,fragmentShaderId)
    }

    override fun getGLSLHandle() {
        //TODO("Not yet implemented")
    }

    override fun bindTexture(textureId: Int) {
        TODO("Not yet implemented")
    }



    override fun getTextureType(): Int {
        //TODO("Not yet implemented")
        return GLES11Ext.GL_SAMPLER_EXTERNAL_OES
    }

    override fun onDraw(
        positionHandle: Int,
        vertexBuffer: FloatBuffer,
        coordHandle: Int,
        textureBuffer: FloatBuffer,
        matrix: Matrix,
        mtx: FloatArray,
        count: Int,
    ) {
        //TODO("Not yet implemented")
    }

    override fun releaseProgram() {
        //TODO("Not yet implemented")
    }
}