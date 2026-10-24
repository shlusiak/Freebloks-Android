package de.saschahlusiak.freebloks.view.gles2

import android.opengl.GLES20
import de.saschahlusiak.freebloks.view.SimpleModel

/**
 * GLES 2.0 drawable for a [SimpleModel]. The geometry (interleaved pos3, normal3, uv2, 32 byte stride, unsigned short
 * indices) is shared with the GLES 1.x renderer; only the buffer objects are owned here. Always uses VBOs.
 */
class Gles2Model(private val model: SimpleModel) {
    private var vbo: IntArray? = null

    /** Drop buffer ids after the GL context was lost. */
    fun invalidate() {
        vbo = null
    }

    fun bind() {
        val ids = vbo ?: upload().also { vbo = it }
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, ids[0])
        GLES20.glEnableVertexAttribArray(ShaderProgram.ATTR_POSITION)
        GLES20.glEnableVertexAttribArray(ShaderProgram.ATTR_NORMAL)
        GLES20.glEnableVertexAttribArray(ShaderProgram.ATTR_TEXCOORD)
        GLES20.glVertexAttribPointer(ShaderProgram.ATTR_POSITION, 3, GLES20.GL_FLOAT, false, STRIDE, 0)
        GLES20.glVertexAttribPointer(ShaderProgram.ATTR_NORMAL, 3, GLES20.GL_FLOAT, false, STRIDE, 3 * 4)
        GLES20.glVertexAttribPointer(ShaderProgram.ATTR_TEXCOORD, 2, GLES20.GL_FLOAT, false, STRIDE, 6 * 4)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ids[1])
    }

    fun draw(mode: Int = GLES20.GL_TRIANGLES) {
        GLES20.glDrawElements(mode, model.numIndices, GLES20.GL_UNSIGNED_SHORT, 0)
    }

    private fun upload(): IntArray {
        val ids = IntArray(2)
        GLES20.glGenBuffers(2, ids, 0)

        model.vertexBuffer.rewind()
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, ids[0])
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, model.vertexBuffer.capacity() * 4, model.vertexBuffer, GLES20.GL_STATIC_DRAW)

        model.indexBuffer.rewind()
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ids[1])
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, model.indexBuffer.capacity() * 2, model.indexBuffer, GLES20.GL_STATIC_DRAW)
        return ids
    }

    private companion object {
        const val STRIDE = 8 * 4
    }
}
