package de.saschahlusiak.freebloks.view

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

/**
 * Interleaved vertex data (position 3, normal 3, texture coordinate 2 floats) and unsigned short triangle indices.
 * Uploaded to the GPU by [de.saschahlusiak.freebloks.view.gles2.Gles2Model].
 */
class SimpleModel(numVertices: Int, numTriangles: Int) {
    // short has 2 bytes
    internal val indexBuffer: ShortBuffer = ByteBuffer.allocateDirect(numTriangles * 3 * 2)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()

    // float has 4 bytes
    internal val vertexBuffer = ByteBuffer.allocateDirect(numVertices * 8 * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    val numIndices: Int = numTriangles * 3

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
}
