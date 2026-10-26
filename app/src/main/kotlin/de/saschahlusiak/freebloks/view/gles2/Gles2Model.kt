package de.saschahlusiak.freebloks.view.gles2

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

/**
 * A static triangle mesh: interleaved vertex data (position 3, normal 3, texture coordinate 2 floats, 32 byte stride)
 * and unsigned short indices.
 *
 * The mesh is built on the CPU with [addVertex] and [addIndex], then [commit]ted. It is uploaded to the GPU on the
 * first [bind]. The data is kept, so the mesh can be uploaded again after the GL context was lost ([invalidate]).
 */
class Gles2Model(numVertices: Int, numTriangles: Int) {
    // short has 2 bytes
    internal val indexBuffer: ShortBuffer = ByteBuffer.allocateDirect(numTriangles * 3 * 2)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()

    // float has 4 bytes
    internal val vertexBuffer = ByteBuffer.allocateDirect(numVertices * 8 * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    val numIndices: Int = numTriangles * 3

    /** The buffer objects of the vertices and the indices, or null if not uploaded to the current context. */
    private var vbo: IntArray? = null

    fun addVertex(x: Float, y: Float, z: Float, nx: Float, ny: Float, nz: Float, tu: Float, tv: Float) {
        vertexBuffer.apply {
            put(x)
            put(y)
            put(z)
            put(nx)
            put(ny)
            put(nz)
            put(tu)
            put(tv)
        }
    }

    fun addIndex(v1: Int, v2: Int, v3: Int) {
        indexBuffer.apply {
            put(v1.toShort())
            put(v2.toShort())
            put(v3.toShort())
        }
    }

    fun addIndex(offset: Int, v1: Int, v2: Int, v3: Int) {
        addIndex(v1 + offset, v2 + offset, v3 + offset)
    }

    /** Finishes building the mesh. */
    fun commit() {
        vertexBuffer.position(0)
        indexBuffer.position(0)
    }

    /** Forget the buffer objects after the GL context was lost; the next [bind] uploads the mesh again. */
    fun invalidate() {
        vbo = null
    }

    /** Frees the buffer objects. The mesh can still be bound afterwards, which uploads it again. */
    fun release() {
        vbo?.let { GLES20.glDeleteBuffers(it.size, it, 0) }
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
        GLES20.glDrawElements(mode, numIndices, GLES20.GL_UNSIGNED_SHORT, 0)
    }

    private fun upload(): IntArray {
        val ids = IntArray(2)
        GLES20.glGenBuffers(2, ids, 0)

        vertexBuffer.rewind()
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, ids[0])
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GLES20.GL_STATIC_DRAW)

        indexBuffer.rewind()
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ids[1])
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * 2, indexBuffer, GLES20.GL_STATIC_DRAW)
        return ids
    }

    private companion object {
        const val STRIDE = 8 * 4
    }
}
