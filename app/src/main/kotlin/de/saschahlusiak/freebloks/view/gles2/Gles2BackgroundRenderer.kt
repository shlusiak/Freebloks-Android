package de.saschahlusiak.freebloks.view.gles2

import android.content.res.Resources
import android.opengl.GLES20
import de.saschahlusiak.freebloks.ktx.KTX
import de.saschahlusiak.freebloks.theme.Theme
import de.saschahlusiak.freebloks.view.SimpleModel

/**
 * Renders the background and clears the frame.
 */
class Gles2BackgroundRenderer(private val resources: Resources, private var theme: Theme) {
    private val rgba = floatArrayOf(0f, 0f, 0f, 1f)
    private val noSpecular = floatArrayOf(0f, 0f, 0f)
    private val size = 80.0f
    private val textures = 15.0f

    private val model = Gles2Model(SimpleModel(4, 2).apply {
        addVertex(-size, 0f, -size, 0f, 1f, 0f, 0f, 0f)
        addVertex(size, 0f, -size, 0f, 1f, 0f, textures, 0f)
        addVertex(size, 0f, size, 0f, 1f, 0f, textures, textures)
        addVertex(-size, 0f, size, 0f, 1f, 0f, 0f, textures)
        addIndex(0, 2, 1)
        addIndex(0, 3, 2)
        commit()
    })

    private var texture = 0
    private var hasTexture = false
    private var valid = false

    fun setTheme(theme: Theme) {
        this.theme = theme
        valid = false
    }

    /** The GL context was (re)created: all GL objects are gone. */
    fun onSurfaceCreated() {
        model.invalidate()
        texture = 0
        valid = false
    }

    private fun updateTexture() {
        valid = true
        val asset = theme.asset

        Gles2Textures.delete(texture)
        texture = 0
        if (theme.isResource && asset != null) {
            hasTexture = true
            val ids = IntArray(1)
            GLES20.glGenTextures(1, ids, 0)
            texture = ids[0]
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
            KTX.loadKTXTexture(resources.assets, asset)
        } else {
            hasTexture = false
        }
        theme.getColor(resources, rgba)
        rgba[3] = 1.0f
    }

    fun render(ctx: Gles2Context) {
        if (!valid) updateTexture()

        if (hasTexture)
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        else
            GLES20.glClearColor(rgba[0], rgba[1], rgba[2], rgba[3])

        // depth writes must be on for the depth clear to take effect
        GLES20.glDepthMask(true)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        if (hasTexture) {
            ctx.lighting = true
            ctx.setColor(rgba)
            ctx.setSpecular(noSpecular, 0f)
            ctx.textured = true
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
            model.bind()
            ctx.apply()
            model.draw()
            ctx.textured = false
        }
    }
}
