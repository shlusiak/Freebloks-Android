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
    val lightDir = floatArrayOf(0f, 1f, 0f)
    val lightAmbient = floatArrayOf(0.35f, 0.35f, 0.35f)
    val lightDiffuse = floatArrayOf(0.8f, 0.8f, 0.8f)
    val lightSpecular = floatArrayOf(1f, 1f, 1f)
    /** GL_LIGHT_MODEL_AMBIENT default. */
    val globalAmbient = floatArrayOf(0.2f, 0.2f, 0.2f)

    var lighting = true
    var textured = false

    /** Ambient and diffuse material, or the flat colour when [lighting] is off. */
    val color = floatArrayOf(1f, 1f, 1f, 1f)
    val specular = floatArrayOf(0f, 0f, 0f)
    var shininess = 0f

    private val mvp = FloatArray(16)
    private val inv = FloatArray(16)
    private val normalMatrix = FloatArray(9)

    fun create() {
        program = ShaderProgram(Shaders.VERTEX, Shaders.FRAGMENT)
        program.use()
        GLES20.glUniform1i(program.uniform("uSampler"), 0)

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
    }

    fun setColor(c: FloatArray) = System.arraycopy(c, 0, color, 0, 4)

    fun setSpecular(s: FloatArray, shininess: Float) {
        System.arraycopy(s, 0, specular, 0, 3)
        this.shininess = shininess
    }

    /** Uploads all state. Call after binding the [Gles2Model] and before [Gles2Model.draw]. */
    fun apply() {
        val p = program
        p.use()

        val mv = modelView.array
        val o = modelView.offset
        Matrix.multiplyMM(mvp, 0, projection, 0, mv, o)
        GLES20.glUniformMatrix4fv(p.uniform("uMVP"), 1, false, mvp, 0)
        GLES20.glUniformMatrix4fv(p.uniform("uTexMatrix"), 1, false, texture.array, texture.offset)

        if (lighting) {
            // inverse transpose of the upper 3x3: invert the full matrix, then read the transposed upper 3x3
            if (Matrix.invertM(inv, 0, mv, o)) {
                for (c in 0 until 3) for (r in 0 until 3) normalMatrix[c * 3 + r] = inv[r * 4 + c]
            } else {
                for (c in 0 until 3) for (r in 0 until 3) normalMatrix[c * 3 + r] = mv[o + c * 4 + r]
            }
            GLES20.glUniformMatrix3fv(p.uniform("uNormalMatrix"), 1, false, normalMatrix, 0)
            GLES20.glUniform3fv(p.uniform("uLightDir"), 1, lightDir, 0)
            GLES20.glUniform3fv(p.uniform("uLightAmbient"), 1, lightAmbient, 0)
            GLES20.glUniform3fv(p.uniform("uLightDiffuse"), 1, lightDiffuse, 0)
            GLES20.glUniform3fv(p.uniform("uLightSpecular"), 1, lightSpecular, 0)
            GLES20.glUniform3fv(p.uniform("uGlobalAmbient"), 1, globalAmbient, 0)
            GLES20.glUniform3fv(p.uniform("uSpecular"), 1, specular, 0)
            GLES20.glUniform1f(p.uniform("uShininess"), shininess)
        }
        GLES20.glUniform1i(p.uniform("uLighting"), if (lighting) 1 else 0)
        GLES20.glUniform1i(p.uniform("uTexture"), if (textured) 1 else 0)
        GLES20.glUniform4fv(p.uniform("uColor"), 1, color, 0)
    }
}
