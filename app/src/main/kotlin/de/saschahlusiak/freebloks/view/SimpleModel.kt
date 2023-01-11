package de.saschahlusiak.freebloks.view

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11

open class SimpleModel(numVertices: Int, private val numTriangles: Int, private val useVBO: Boolean = true) {
    // short has 2 bytes
    internal val indexBuffer: ShortBuffer = ByteBuffer.allocateDirect(numTriangles * 3 * 2)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()

    // float has 4 bytes
    internal val vertexBuffer = ByteBuffer.allocateDirect(numVertices * 8 * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    internal var vbo: IntArray? = null

    fun commit() {
        vertexBuffer.position(0)
        indexBuffer.position(0)
    }

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

    fun bindBuffers(gl: GL11) {
        if (!useVBO) {
            gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0)
            gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0)

            gl.glVertexPointer(3, GL10.GL_FLOAT, 4 * 8, vertexBuffer.position(0))
            gl.glNormalPointer(GL10.GL_FLOAT, 4 * 8, vertexBuffer.position(3))
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 4 * 8, vertexBuffer.position(6))
        } else {
            val vbo = vbo ?: makeVBO(gl)

            gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vbo[0])

            gl.glVertexPointer(3, GL10.GL_FLOAT, 4 * 8, 0)
            gl.glNormalPointer(GL10.GL_FLOAT, 4 * 8, 3 * 4)
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 4 * 8, 6 * 4)

            gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, vbo[1])
        }
    }

    fun invalidateBuffers(gl: GL11) {
        vbo = if (useVBO)
            makeVBO(gl)
        else
            null
    }

    fun drawElements(gl: GL11, mode: Int) {
        if (useVBO)
            gl.glDrawElements(mode, numTriangles * 3, GL10.GL_UNSIGNED_SHORT, 0)
        else
            gl.glDrawElements(mode, numTriangles * 3, GL10.GL_UNSIGNED_SHORT, indexBuffer)
    }

    private fun makeVBO(gl: GL11): IntArray {
        this.vbo?.let {
            gl.glDeleteBuffers(it.size, it, 0)
        }

        val vbo = IntArray(2)
        gl.glGenBuffers(2, vbo, 0)

        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vbo[0])
        gl.glBufferData(GL11.GL_ARRAY_BUFFER,
            vertexBuffer.capacity() * 4,
            vertexBuffer.rewind(),
            GL11.GL_STATIC_DRAW)
        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0)

        gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, vbo[1])
        gl.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER,
            indexBuffer.capacity() * 2,
            indexBuffer,
            GL11.GL_STATIC_DRAW)
        gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0)

        this.vbo = vbo

        return vbo
    }
}
