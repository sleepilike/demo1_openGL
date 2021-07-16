package com.example.demo1_opengl.filter.base

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import com.example.demo1_opengl.utils.GLUtil
import java.nio.FloatBuffer

/**
 * Created by zyy on 2021/7/16
 * 最基本的filter
 */
class DefaultFilter(context: Context) : AbstractFilter(context) {

    val VERTEX_FILE = "shader/base_vertex_shader.glsl"
    val FRAGMNET_FILE = "shader/base_fragment_shader.glsl"

    override fun createProgram(context: Context): Int {

        return GLUtil.createProgram(context,VERTEX_FILE,FRAGMNET_FILE)
    }

    override fun getGLSLHandle() {

    }

    override fun bindTexture(textureId: Int) {

    }

    override fun bindGLSLValues(stride: Int) {

    }

    override fun getTextureType(): Int {
        return GLES20.GL_TEXTURE_2D
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

    }

    override fun releaseProgram() {

    }

}