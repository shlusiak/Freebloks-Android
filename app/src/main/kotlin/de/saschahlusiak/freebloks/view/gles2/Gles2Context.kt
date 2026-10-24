package de.saschahlusiak.freebloks.view.gles2

import android.opengl.GLES20
import android.opengl.Matrix
import de.saschahlusiak.freebloks.view.MatrixStack

/**
 * Holds the state the fixed function pipeline kept implicitly (matrices, light, material, texture on/off) and
 * uploads it to the shader right before a draw call. Also owns the fixed-function style GL switches (blending, depth
 * test, face culling), so the state is always known and redundant GL calls are skipped.
 *
 * Must only be used on the GL thread. Call [create] from onSurfaceCreated.
 */
class Gles2Context {
    /** How fragments are combined with the framebuffer. */
    enum class Blend {
        Off,

        /** Source colour is already multiplied by its alpha: (ONE, ONE_MINUS_SRC_ALPHA) */
        Premultiplied,

        /** (SRC_ALPHA, ONE_MINUS_SRC_ALPHA) */
        Alpha
    }

    private lateinit var program: ShaderProgram

    private var blendState: Blend? = null
    private var depthTestState: Boolean? = null
    private var cullFaceState: Boolean? = null

    val modelView = MatrixStack()
    val projection = FloatArray(16).also { Matrix.setIdentityM(it, 0) }
    val texture = MatrixStack(4)

    /** Light direction in eye space, pointing towards the light. */
    private val lightDir = floatArrayOf(0f, 1f, 0f)
    private var lightDirDirty = true

    /** Ambient and diffuse material, or the flat colour when [lighting] is off. */
    val color = floatArrayOf(1f, 1f, 1f, 1f)
    val specular = floatArrayOf(0f, 0f, 0f)
    var shininess = 0f

    var lighting = true
    var textured = false

    private val mvp = FloatArray(16)
    private val inv = FloatArray(16)
    private val normalMatrix = FloatArray(9)

    /** Uniform locations, resolved once per program. */
    private var uMVP = 0
    private var uTexMatrix = 0
    private var uNormalMatrix = 0
    private var uLightDir = 0
    private var uSpecular = 0
    private var uShininess = 0
    private var uColor = 0
    private var uLighting = 0
    private var uTexture = 0

    /** The values last sent to the program; NaN (or -1) never matches, so the first draw uploads everything. */
    private val sentMvp = FloatArray(16)
    private val sentTexMatrix = FloatArray(16)
    private val sentNormalMatrix = FloatArray(9)
    private val sentColor = FloatArray(4)
    private val sentSpecular = FloatArray(3)
    private var sentShininess = Float.NaN
    private var sentLighting = -1
    private var sentTexture = -1

    fun create() {
        program = ShaderProgram(Shaders.VERTEX, Shaders.FRAGMENT)
        val p = program
        p.use()
        GLES20.glUniform1i(p.uniform("uSampler"), 0)

        uMVP = p.uniform("uMVP")
        uTexMatrix = p.uniform("uTexMatrix")
        uNormalMatrix = p.uniform("uNormalMatrix")
        uLightDir = p.uniform("uLightDir")
        uSpecular = p.uniform("uSpecular")
        uShininess = p.uniform("uShininess")
        uColor = p.uniform("uColor")
        uLighting = p.uniform("uLighting")
        uTexture = p.uniform("uTexture")

        // the light never changes, except for its direction
        GLES20.glUniform3fv(p.uniform("uLightAmbient"), 1, floatArrayOf(0.35f, 0.35f, 0.35f), 0)
        GLES20.glUniform3fv(p.uniform("uLightDiffuse"), 1, floatArrayOf(0.8f, 0.8f, 0.8f), 0)
        GLES20.glUniform3fv(p.uniform("uLightSpecular"), 1, floatArrayOf(1f, 1f, 1f), 0)
        // GL_LIGHT_MODEL_AMBIENT default
        GLES20.glUniform3fv(p.uniform("uGlobalAmbient"), 1, floatArrayOf(0.2f, 0.2f, 0.2f), 0)

        lightDirDirty = true
        sentMvp.fill(Float.NaN)
        sentTexMatrix.fill(Float.NaN)
        sentNormalMatrix.fill(Float.NaN)
        sentColor.fill(Float.NaN)
        sentSpecular.fill(Float.NaN)
        sentShininess = Float.NaN
        sentLighting = -1
        sentTexture = -1

        // a new context: nothing is known about its state
        blendState = null
        depthTestState = null
        cullFaceState = null
        GLES20.glDisable(GLES20.GL_DITHER)
        blend(Blend.Off)
        depthTest(true)
        cullFace(true)
    }

    fun blend(mode: Blend) {
        if (mode == blendState) return
        blendState = mode
        when (mode) {
            Blend.Off -> GLES20.glDisable(GLES20.GL_BLEND)
            Blend.Premultiplied -> {
                GLES20.glEnable(GLES20.GL_BLEND)
                GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            }
            Blend.Alpha -> {
                GLES20.glEnable(GLES20.GL_BLEND)
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            }
        }
    }

    fun depthTest(enabled: Boolean) {
        if (enabled == depthTestState) return
        depthTestState = enabled
        if (enabled) GLES20.glEnable(GLES20.GL_DEPTH_TEST) else GLES20.glDisable(GLES20.GL_DEPTH_TEST)
    }

    fun cullFace(enabled: Boolean) {
        if (enabled == cullFaceState) return
        cullFaceState = enabled
        if (enabled) GLES20.glEnable(GLES20.GL_CULL_FACE) else GLES20.glDisable(GLES20.GL_CULL_FACE)
    }

    /** Sets the light from eye-space position (w = 0 means directional), like glLightfv(GL_POSITION). */
    fun setLightPosition(x: Float, y: Float, z: Float) {
        // the fixed function pipeline transforms the position by the model view matrix at call time
        val m = modelView.array
        val o = modelView.offset
        lightDir[0] = m[o] * x + m[o + 4] * y + m[o + 8] * z
        lightDir[1] = m[o + 1] * x + m[o + 5] * y + m[o + 9] * z
        lightDir[2] = m[o + 2] * x + m[o + 6] * y + m[o + 10] * z
        lightDirDirty = true
    }

    fun setColor(c: FloatArray) = System.arraycopy(c, 0, color, 0, 4)

    fun setSpecular(s: FloatArray, shininess: Float) {
        System.arraycopy(s, 0, specular, 0, 3)
        this.shininess = shininess
    }

    /**
     * Uploads the state that changed since the last call. Call after binding the [Gles2Model] and before
     * [Gles2Model.draw].
     */
    fun apply() {
        val mv = modelView.array
        val o = modelView.offset
        Matrix.multiplyMM(mvp, 0, projection, 0, mv, o)
        if (changed(sentMvp, mvp, 0, 16))
            GLES20.glUniformMatrix4fv(uMVP, 1, false, mvp, 0)
        if (changed(sentTexMatrix, texture.array, texture.offset, 16))
            GLES20.glUniformMatrix4fv(uTexMatrix, 1, false, texture.array, texture.offset)

        val lit = if (lighting) 1 else 0
        if (lit != sentLighting) {
            GLES20.glUniform1i(uLighting, lit)
            sentLighting = lit
        }
        val tex = if (textured) 1 else 0
        if (tex != sentTexture) {
            GLES20.glUniform1i(uTexture, tex)
            sentTexture = tex
        }
        if (changed(sentColor, color, 0, 4))
            GLES20.glUniform4fv(uColor, 1, color, 0)

        if (lighting) {
            updateNormalMatrix(mv, o)
            if (changed(sentNormalMatrix, normalMatrix, 0, 9))
                GLES20.glUniformMatrix3fv(uNormalMatrix, 1, false, normalMatrix, 0)

            if (lightDirDirty) {
                GLES20.glUniform3fv(uLightDir, 1, lightDir, 0)
                lightDirDirty = false
            }
            if (changed(sentSpecular, specular, 0, 3))
                GLES20.glUniform3fv(uSpecular, 1, specular, 0)
            if (shininess != sentShininess) {
                GLES20.glUniform1f(uShininess, shininess)
                sentShininess = shininess
            }
        }
    }

    /**
     * The matrix to transform normals with: the inverse transpose of the upper 3x3 of the model view matrix. Without a
     * non-uniform scale that is the upper 3x3 itself (the shader normalizes, so a uniform scale does not matter),
     * which saves inverting a 4x4 matrix per draw call.
     */
    private fun updateNormalMatrix(mv: FloatArray, o: Int) {
        if (!modelView.hasNonUniformScale || !Matrix.invertM(inv, 0, mv, o)) {
            for (c in 0 until 3) for (r in 0 until 3) normalMatrix[c * 3 + r] = mv[o + c * 4 + r]
        } else {
            for (c in 0 until 3) for (r in 0 until 3) normalMatrix[c * 3 + r] = inv[r * 4 + c]
        }
    }

    /** Copies [n] floats of [src] over [sent] and returns true if any differed. */
    private fun changed(sent: FloatArray, src: FloatArray, srcOffset: Int, n: Int): Boolean {
        var different = false
        for (i in 0 until n) {
            val v = src[srcOffset + i]
            if (sent[i] != v) {
                sent[i] = v
                different = true
            }
        }
        return different
    }
}
