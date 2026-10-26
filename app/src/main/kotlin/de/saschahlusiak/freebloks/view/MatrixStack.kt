package de.saschahlusiak.freebloks.view

import android.opengl.Matrix

/**
 * A stack of 4x4 column-major matrices, like the classic OpenGL matrix stack (push, pop, translate, rotate, scale):
 * all operations post-multiply the current matrix.
 *
 * The matrices live back to back in one array. The current one is at [array] from [offset], which is what
 * [android.opengl.Matrix] and glUniformMatrix4fv take, so nothing needs to be copied or allocated to use it.
 */
class MatrixStack(depth: Int = 32) {
    /** Storage of all matrices. Only the 16 floats starting at [offset] are the current matrix. */
    val array = FloatArray(depth * 16)

    /** Index of the first float of the current matrix in [array]. */
    var offset = 0
        private set

    /** Per matrix: whether it may contain a non-uniform scale. Without one, normals only need rotating. */
    private val nonUniform = BooleanArray(depth)

    /**
     * Whether the current matrix may scale differently along the axes. If not, it is a rotation, a translation and
     * at most a uniform scale, so the upper 3x3 transforms normals correctly up to their length.
     */
    val hasNonUniformScale: Boolean get() = nonUniform[offset / 16]

    private val product = FloatArray(16)
    private val scratch = FloatArray(16)

    init {
        loadIdentity()
    }

    fun loadIdentity() {
        Matrix.setIdentityM(array, offset)
        nonUniform[offset / 16] = false
    }

    /** Replaces the current matrix with the first 16 floats of [m]. */
    fun load(m: FloatArray) {
        System.arraycopy(m, 0, array, offset, 16)
        nonUniform[offset / 16] = true
    }

    /** Copies the current matrix into [dest]. */
    fun copyTo(dest: FloatArray) = System.arraycopy(array, offset, dest, 0, 16)

    fun push() {
        check(offset + 16 < array.size) { "matrix stack overflow" }
        System.arraycopy(array, offset, array, offset + 16, 16)
        nonUniform[offset / 16 + 1] = nonUniform[offset / 16]
        offset += 16
    }

    fun pop() {
        check(offset > 0) { "matrix stack underflow" }
        offset -= 16
    }

    /** Runs [block] and restores the current matrix afterwards. */
    inline fun withPush(block: () -> Unit) {
        push()
        try {
            block()
        } finally {
            pop()
        }
    }

    fun translate(x: Float, y: Float, z: Float) = Matrix.translateM(array, offset, x, y, z)

    fun scale(x: Float, y: Float, z: Float) {
        Matrix.scaleM(array, offset, x, y, z)
        if (x != y || y != z) nonUniform[offset / 16] = true
    }

    /** Angle in degrees. */
    fun rotate(angle: Float, x: Float, y: Float, z: Float) = Matrix.rotateM(array, offset, angle, x, y, z)

    /** Multiplies the current matrix with [m] from the right. [m] is assumed to contain a non-uniform scale. */
    fun multiply(m: FloatArray) {
        multiplyRigid(m)
        nonUniform[offset / 16] = true
    }

    private fun multiplyRigid(m: FloatArray) {
        Matrix.multiplyMM(product, 0, array, offset, m, 0)
        System.arraycopy(product, 0, array, offset, 16)
    }

    /** Equivalent of GLU.gluPerspective. */
    fun perspective(fovY: Float, aspect: Float, near: Float, far: Float) {
        Matrix.perspectiveM(scratch, 0, fovY, aspect, near, far)
        multiply(scratch)
    }

    /** Equivalent of GLU.gluLookAt. */
    fun lookAt(ex: Float, ey: Float, ez: Float, cx: Float, cy: Float, cz: Float, ux: Float, uy: Float, uz: Float) {
        Matrix.setLookAtM(scratch, 0, ex, ey, ez, cx, cy, cz, ux, uy, uz)
        multiplyRigid(scratch)
    }
}
