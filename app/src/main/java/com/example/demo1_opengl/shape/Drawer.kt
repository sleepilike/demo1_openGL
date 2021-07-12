package com.example.demo1_opengl.shape

import android.content.Context
import android.opengl.GLES20
import com.example.demo1_opengl.config.BufferUtil
import com.example.demo1_opengl.config.GLUtil

class Drawer (context: Context){
    //顶点坐标
    val VERTEX_COORDS = floatArrayOf(
        -1.0f, 1.0f, 0.0f,
        -1.0f, -1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        1.0f, 1.0f, 0.0f
    )

    //纹理坐标
    val TEXTURE_COORDS = floatArrayOf(
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    )

    val INDEX = shortArrayOf(
        3,2,0,0,1,2
    )


    private var mVertexBuffer = BufferUtil.toFloatBuffer(VERTEX_COORDS)
    private var mTextureBuffer = BufferUtil.toFloatBuffer(TEXTURE_COORDS)
    private var mIndexBuffer = BufferUtil.toShortBuffer(INDEX)

    private var vertexShaderCode : String = GLUtil.readRawShaderCode(context,"shader/vertex_shader.glsl")
    private var fragmentShaderCode : String = GLUtil.readRawShaderCode(context, "shader/fragment_shader.glsl")

    var vertexShaderId : Int = GLUtil.compileShaderCode(GLES20.GL_VERTEX_SHADER,vertexShaderCode)
    var fragmentShaderId : Int = GLUtil.compileShaderCode(GLES20.GL_FRAGMENT_SHADER,fragmentShaderCode)

    var mProgramId : Int = GLUtil.linkProgram(vertexShaderId,fragmentShaderId)

    var textureId = GLUtil.createOESTexture()



    //句柄
    var aPosition : Int = 0
    var aTextCoord : Int = 0
    var uTexture : Int = 0
    init {
        aPosition = GLES20.glGetAttribLocation(mProgramId, "a_Position")
        aTextCoord = GLES20.glGetAttribLocation(mProgramId, "a_TexCoordinate")
        uTexture = GLES20.glGetUniformLocation(mProgramId, "u_Texture")
    }

    fun draw(){
        GLES20.glUseProgram(mProgramId)

        mVertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(aPosition)
        GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer)

        mTextureBuffer.position(0)
        GLES20.glEnableVertexAttribArray(aTextCoord)
        GLES20.glVertexAttribPointer(aTextCoord, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId)
        GLES20.glUniform1i(uTexture,0)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            INDEX.size,
            GLES20.GL_UNSIGNED_SHORT,
            mIndexBuffer
        )


    }
}