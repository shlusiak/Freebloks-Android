package de.saschahlusiak.freebloks.view

import android.content.res.Resources
import de.saschahlusiak.freebloks.ktx.KTX
import de.saschahlusiak.freebloks.theme.Theme
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11

class BackgroundRenderer(private val resources: Resources, private var theme: Theme) : SimpleModel(4, 2, false) {
    private val rgba = floatArrayOf(0f, 0f, 0f, 1f)
    private val specular = floatArrayOf(0f, 0f, 0f, 1f)
    private val size = 80.0f
    private val textures = 15.0f

    private var texture: IntArray? = null
    private var hasTexture = false
    private var valid = false

    init {
        addVertex(-size, 0f, -size, 0f, 1f, 0f, 0f, 0f)
        addVertex(size, 0f, -size, 0f, 1f, 0f, textures, 0f)
        addVertex(size, 0f, size, 0f, 1f, 0f, textures, textures)
        addVertex(-size, 0f, size, 0f, 1f, 0f, 0f, textures)
        addIndex(0, 2, 1)
        addIndex(0, 3, 2)

        commit()
    }

    fun setTheme(theme: Theme) {
        this.theme = theme
        valid = false
    }

    fun updateTexture(gl: GL11) {
        valid = true
        val asset = theme.asset

        if (theme.isResource && asset != null) {
            hasTexture = true
            texture = IntArray(1).also { texture ->
                gl.glGenTextures(1, texture, 0)
                gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0])
            }

            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR_MIPMAP_LINEAR)
            gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE.toFloat())
            gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR)

            KTX.loadKTXTexture(resources.assets, asset)

            theme.getColor(resources, rgba)
            rgba[3] = 1.0f
        } else {
            hasTexture = false
            texture = null
            theme.getColor(resources, rgba)
            rgba[3] = 1.0f
        }
    }

    fun render(gl: GL11) {
        if (!valid) updateTexture(gl)

        if (hasTexture)
            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        else
            gl.glClearColor(rgba[0], rgba[1], rgba[2], rgba[3])

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)

        if (hasTexture) {
            texture?.let { texture ->
                with(gl) {
                    glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, rgba, 0)
                    glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, specular, 0)
                    glEnable(GL10.GL_TEXTURE_2D)
                    glBindTexture(GL10.GL_TEXTURE_2D, texture[0])
                    bindBuffers(gl)
                    drawElements(gl, GL10.GL_TRIANGLES)
                    glDisable(GL10.GL_TEXTURE_2D)
                }
            }
        }
    }
}