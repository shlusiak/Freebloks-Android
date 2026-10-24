package de.saschahlusiak.freebloks.view.gles2

import android.opengl.Matrix

/**
 * Mirrors the fixed function matrix stack (glPushMatrix, glTranslatef, glRotatef, glScalef, ...), so ported
 * render code reads like the GLES 1.x original. All operations post-multiply, like OpenGL.
 */
class MatrixStack(depth: Int = 32) {
    private val stack = Array(depth) { FloatArray(16) }
    private var top = 0
    private val tmp = FloatArray(16)

    /** The current matrix, column-major. */
    val current: FloatArray get() = stack[top]

    init {
        Matrix.setIdentityM(current, 0)
    }

    fun loadIdentity() = Matrix.setIdentityM(current, 0)

    fun load(m: FloatArray) = System.arraycopy(m, 0, current, 0, 16)

    fun push() {
        check(top + 1 < stack.size) { "matrix stack overflow" }
        System.arraycopy(stack[top], 0, stack[top + 1], 0, 16)
        top++
    }

    fun pop() {
        check(top > 0) { "matrix stack underflow" }
        top--
    }

    fun translate(x: Float, y: Float, z: Float) = Matrix.translateM(current, 0, x, y, z)

    fun scale(x: Float, y: Float, z: Float) = Matrix.scaleM(current, 0, x, y, z)

    /** Angle in degrees. */
    fun rotate(angle: Float, x: Float, y: Float, z: Float) = Matrix.rotateM(current, 0, angle, x, y, z)

    fun multiply(m: FloatArray) {
        Matrix.multiplyMM(tmp, 0, current, 0, m, 0)
        System.arraycopy(tmp, 0, current, 0, 16)
    }

    /** Equivalent of GLU.gluPerspective. */
    fun perspective(fovY: Float, aspect: Float, near: Float, far: Float) {
        Matrix.perspectiveM(tmp, 0, fovY, aspect, near, far)
        multiply(tmp.copyOf())
    }

    /** Equivalent of GLU.gluLookAt. */
    fun lookAt(ex: Float, ey: Float, ez: Float, cx: Float, cy: Float, cz: Float, ux: Float, uy: Float, uz: Float) {
        val m = FloatArray(16)
        Matrix.setLookAtM(m, 0, ex, ey, ez, cx, cy, cz, ux, uy, uz)
        multiply(m)
    }
}
