package de.saschahlusiak.freebloks.view.gles2

import android.opengl.GLES20
import android.util.Log

/**
 * A linked GLES 2.0 program. Must be created on the GL thread and recreated whenever the surface is created.
 */
class ShaderProgram(vertexSource: String, fragmentSource: String) {
    private val tag = ShaderProgram::class.java.simpleName
    private val uniforms = HashMap<String, Int>()

    val id: Int

    init {
        val vs = compile(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fs = compile(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        id = GLES20.glCreateProgram()
        check(id != 0) { "glCreateProgram failed" }
        GLES20.glAttachShader(id, vs)
        GLES20.glAttachShader(id, fs)
        GLES20.glBindAttribLocation(id, ATTR_POSITION, "aPosition")
        GLES20.glBindAttribLocation(id, ATTR_NORMAL, "aNormal")
        GLES20.glBindAttribLocation(id, ATTR_TEXCOORD, "aTexCoord")
        GLES20.glLinkProgram(id)

        val status = IntArray(1)
        GLES20.glGetProgramiv(id, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            val log = GLES20.glGetProgramInfoLog(id)
            Log.e(tag, "Link failed: $log")
            GLES20.glDeleteProgram(id)
            error("Program link failed: $log")
        }
        // shaders are kept alive by the program
        GLES20.glDeleteShader(vs)
        GLES20.glDeleteShader(fs)
    }

    fun use() = GLES20.glUseProgram(id)

    fun uniform(name: String): Int = uniforms.getOrPut(name) {
        GLES20.glGetUniformLocation(id, name)
    }

    private fun compile(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        check(shader != 0) { "glCreateShader failed" }
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            val log = GLES20.glGetShaderInfoLog(shader)
            Log.e(tag, "Compile failed: $log")
            GLES20.glDeleteShader(shader)
            error("Shader compile failed: $log")
        }
        return shader
    }

    companion object {
        const val ATTR_POSITION = 0
        const val ATTR_NORMAL = 1
        const val ATTR_TEXCOORD = 2
    }
}
