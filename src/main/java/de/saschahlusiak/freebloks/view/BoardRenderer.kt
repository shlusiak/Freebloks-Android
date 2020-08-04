package de.saschahlusiak.freebloks.view

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLUtils
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.model.Board
import de.saschahlusiak.freebloks.model.Orientation
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.model.StoneColor
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11

/**
 * Renders the content of the board, including the board itself and the player's stones, or any stone, really.
 */
class BoardRenderer() {
    companion object {
        const val stoneSize = 0.45f
        const val defaultStoneAlpha = 0.75f

        private const val bevelSize = 0.18f
        private const val bevelHeight = 0.25f
        private const val borderBottom = -1.1f
        private const val borderTop = 0.4f
        private const val borderWidth = 0.5f

        private const val r1 = stoneSize
        private const val r2 = stoneSize - bevelSize
        private const val y1 = -bevelHeight
        private const val y2 = 0.0f

        private val materialBoardDiffuse = floatArrayOf(0.57f, 0.57f, 0.57f, 1.0f)
        private val materialBoardDiffuseSeed = floatArrayOf(0.45f, 0.8f, 0.55f, 1.0f)
        private val materialBoardSpecular = floatArrayOf(0.25f, 0.24f, 0.24f, 1.0f)
        private val materialBoardShininess = floatArrayOf(35.0f)
        val materialStoneSpecular = floatArrayOf(0.3f, 0.3f, 0.3f, 1.0f)
        val materialStoneShininess = floatArrayOf(30.0f)
        private val materialBlack = floatArrayOf(0f, 0f, 0f, 1f)
    }

    private val field: SimpleModel
    private var border: SimpleModel
    val stone: SimpleModel
    private val shadow: SimpleModel

    private var texture = IntArray(2)
    private val tmp = FloatArray(4)

    init {
        field = buildSingleBoardFieldModel()
        border = buildBorderModel(Board.DEFAULT_BOARD_SIZE)
        stone = buildStoneModel()
        shadow = buildStoneShadow()
    }

    /**
     * Builds a SimpleModel for a single field of the board, which is half a stone and is "deep".
     */
    private fun buildSingleBoardFieldModel(): SimpleModel {
        return SimpleModel(8, 10, true).apply {
            /* bottom, inner */
            addVertex(-r2, y1, +r2, 0f, 1f, 0f, -r2, r2)
            addVertex(+r2, y1, +r2, 0f, 1f, 0f, r2, r2)
            addVertex(+r2, y1, -r2, 0f, 1f, 0f, r2, -r2)
            addVertex(-r2, y1, -r2, 0f, 1f, 0f, -r2, -r2)

            /* top, outer */
            addVertex(-r1, y2, +r1, +1f, 1f, -1f, -r1, r1)
            addVertex(+r1, y2, +r1, -1f, 1f, -1f, r1, r1)
            addVertex(+r1, y2, -r1, -1f, 1f, +1f, r1, -r1)
            addVertex(-r1, y2, -r1, +1f, 1f, +1f, -r1, -r1)

            addIndex(0, 1, 2)
            addIndex(0, 2, 3)
            addIndex(0, 5, 1)
            addIndex(0, 4, 5)
            addIndex(1, 5, 6)
            addIndex(1, 6, 2)
            addIndex(2, 6, 7)
            addIndex(2, 7, 3)
            addIndex(3, 7, 4)
            addIndex(3, 4, 0)

            commit()
        }
    }

    /**
     * Builds the planar shadow quad for a single stone
     */
    private fun buildStoneShadow(): SimpleModel {
        return SimpleModel(4, 2, false).apply {
            addVertex(-r1, 0f, +r1, 0f, 1f, 0f, 0f, 0f)
            addVertex(+r1, 0f, +r1, 0f, 1f, 0f, 1f, 0f)
            addVertex(+r1, 0f, -r1, 0f, 1f, 0f, 1f, 1f)
            addVertex(-r1, 0f, -r1, 0f, 1f, 0f, 0f, 1f)
            addIndex(0, 1, 2)
            addIndex(0, 2, 3)
            commit()
        }
    }

    /**
     * Builds the model of a full player's stone, including top and bottom
     */
    private fun buildStoneModel(): SimpleModel {
        return SimpleModel(12, 20, true).apply {
            /* top, inner */
            addVertex(-r2, -y1, +r2, 0f, 1f, 0f, 0f, 0f)
            addVertex(+r2, -y1, +r2, 0f, 1f, 0f, 0f, 0f)
            addVertex(+r2, -y1, -r2, 0f, 1f, 0f, 0f, 0f)
            addVertex(-r2, -y1, -r2, 0f, 1f, 0f, 0f, 0f)

            /* middle, outer */
            addVertex(-r1, y2, +r1, -1f, 0f, +1f, 0f, 0f)
            addVertex(+r1, y2, +r1, +1f, 0f, +1f, 0f, 0f)
            addVertex(+r1, y2, -r1, +1f, 0f, -1f, 0f, 0f)
            addVertex(-r1, y2, -r1, -1f, 0f, -1f, 0f, 0f)

            /* bottom, inner */
            addVertex(-r2, y1, +r2, 0f, -1f, 0f, 0f, 0f)
            addVertex(+r2, y1, +r2, 0f, -1f, 0f, 0f, 0f)
            addVertex(+r2, y1, -r2, 0f, -1f, 0f, 0f, 0f)
            addVertex(-r2, y1, -r2, 0f, -1f, 0f, 0f, 0f)

            /* top */
            addIndex(0, 1, 2)
            addIndex(0, 2, 3)
            addIndex(0, 5, 1)
            addIndex(0, 4, 5)
            addIndex(1, 5, 6)
            addIndex(1, 6, 2)
            addIndex(2, 6, 7)
            addIndex(2, 7, 3)
            addIndex(3, 7, 4)
            addIndex(3, 4, 0)

            /* bottom */
            addIndex(8, 10, 9)
            addIndex(8, 11, 10)
            addIndex(8, 9, 5)
            addIndex(8, 5, 4)
            addIndex(9, 6, 5)
            addIndex(9, 10, 6)
            addIndex(10, 7, 6)
            addIndex(10, 11, 7)
            addIndex(11, 4, 7)
            addIndex(11, 8, 4)
            commit()
        }
    }

    /**
     * Builds the square model of the border of the field with the given size
     */
    private fun buildBorderModel(boardSize: Int): SimpleModel {
        return SimpleModel(12, 6, true).apply {
            val w1 = stoneSize * boardSize
            val w2 = w1 + borderWidth

            /* front */addVertex(w2, borderTop, w2, 0f, 0f, 1f, w2, borderTop)
            addVertex(-w2, borderTop, w2, 0f, 0f, 1f, -w2, borderTop)
            addVertex(-w2, borderBottom, w2, 0f, 0f, 1f, -w2, borderBottom)
            addVertex(w2, borderBottom, w2, 0f, 0f, 1f, w2, borderBottom)

            /* top */addVertex(w2, borderTop, w2, 0f, 1f, 0f, w2, w2)
            addVertex(-w2, borderTop, w2, 0f, 1f, 0f, -w2, w2)
            addVertex(-w1, borderTop, w1, 0f, 1f, 0f, -w1, w1)
            addVertex(w1, borderTop, w1, 0f, 1f, 0f, w1, w1)

            /* inner */addVertex(w1, borderTop, w1, 0f, 0f, -1f, w1, borderTop)
            addVertex(-w1, borderTop, w1, 0f, 0f, -1f, -w1, borderTop)
            addVertex(-w1, 0f, w1, 0f, 0f, -1f, -w1, w1)
            addVertex(w1, 0f, w1, 0f, 0f, -1f, w1, w1)
            addIndex(0, 1, 2)
            addIndex(0, 2, 3)
            addIndex(7, 6, 4)
            addIndex(4, 6, 5)
            addIndex(9, 8, 10)
            addIndex(8, 11, 10)

            commit()
        }
    }

    fun setBoardSize(boardSize: Int) {
        this.border = buildBorderModel(boardSize)
    }

    fun onSurfaceChanged(context: Context, gl: GL11) {
        stone.invalidateBuffers(gl)
        field.invalidateBuffers(gl)
        border.invalidateBuffers(gl)
        shadow.invalidateBuffers(gl)

        // wood texture
        gl.glGenTextures(texture.size, texture, 0)
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0])
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR_MIPMAP_NEAREST)
        gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE.toFloat())
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR)
        KTX.loadKTXTexture(context.assets, "textures/field_wood.ktx")

        // shadow texture
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.stone_shadow)
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[1])
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR_MIPMAP_NEAREST)
        gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE.toFloat())
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR)
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0)
    }

    /**
     * Render the border and fields of the board, optionally with seeds
     *
     * @param gl GL11
     * @param board the board to render
     * @param seedsPlayer the number of the player whose "seeds" to render or -1 if none
     */
    fun renderBoard(gl: GL11, board: Board, seedsPlayer: Int) {
        val textureScale = 0.12f
        val textureRotation = 1.0f

        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, materialBoardSpecular, 0)
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, materialBoardShininess, 0)
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, materialBoardDiffuse, 0)

        gl.glEnable(GL10.GL_TEXTURE_2D)
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0])
        field.bindBuffers(gl)

        gl.glMatrixMode(GL10.GL_TEXTURE)
        gl.glLoadIdentity()
        gl.glScalef(textureScale, textureScale, 1f)
        gl.glRotatef(textureRotation, 0f, 0f, 1f)
        gl.glPushMatrix()

        gl.glMatrixMode(GL10.GL_MODELVIEW)

        val w = board.width
        val h = board.height

        gl.glPushMatrix()
        gl.glTranslatef(-stoneSize * (w - 1).toFloat(), 0f, -stoneSize * (h - 1).toFloat())
        var lastFieldStatus = -1
        for (y in 0 until h) {
            var x = 0
            while (x < w) {
                if (seedsPlayer >= 0) {
                    val fieldStatus = board.getFieldStatus(seedsPlayer, y, x)
                    if (fieldStatus != lastFieldStatus) {
                        gl.glMaterialfv(
                            GL10.GL_FRONT_AND_BACK,
                            GL10.GL_AMBIENT_AND_DIFFUSE,
                            if (fieldStatus == Board.FIELD_ALLOWED) materialBoardDiffuseSeed else materialBoardDiffuse,
                            0
                        )
                        lastFieldStatus = fieldStatus
                    }
                }

                field.drawElements(gl, GL10.GL_TRIANGLES)

                gl.glMatrixMode(GL10.GL_TEXTURE)
                gl.glTranslatef(stoneSize * 2.0f, 0f, 0f)

                gl.glMatrixMode(GL10.GL_MODELVIEW)
                gl.glTranslatef(stoneSize * 2.0f, 0f, 0f)

                x++
            }
            gl.glMatrixMode(GL10.GL_TEXTURE)
            gl.glTranslatef(-x * stoneSize * 2.0f, stoneSize * 2.0f, 0f)

            gl.glMatrixMode(GL10.GL_MODELVIEW)
            gl.glTranslatef(-x * stoneSize * 2.0f, 0f, stoneSize * 2.0f)
        }

        gl.glMatrixMode(GL10.GL_TEXTURE)
        gl.glPopMatrix()

        gl.glMatrixMode(GL10.GL_MODELVIEW)
        gl.glPopMatrix()

        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, materialBoardDiffuse, 0)

        border.bindBuffers(gl)

        /* we want the border in the depth buffer so it can cover the stones rendered later */
        gl.glEnable(GL10.GL_DEPTH_TEST)
        for (i in 0..3) {
            border.drawElements(gl, GL10.GL_TRIANGLES)
            gl.glRotatef(90f, 0f, 1f, 0f)
        }

        gl.glMatrixMode(GL10.GL_TEXTURE)
        gl.glLoadIdentity()
        gl.glMatrixMode(GL10.GL_MODELVIEW)
        gl.glDisable(GL10.GL_TEXTURE_2D)
    }

    /**
     * Render a single stone with the given color and alpha
     *
     * The [stone] model MUST be bound before calling this.
     *
     * This is ONLY used when rendering non-effected player stones of the field
     */
    fun renderSingleStone(gl: GL11, color: StoneColor, alpha: Float) {
        val c = color.stoneColor
        tmp[0] = c[0] * alpha
        tmp[1] = c[1] * alpha
        tmp[2] = c[2] * alpha
        tmp[3] = alpha
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, tmp, 0)
        stone.drawElements(gl, GL10.GL_TRIANGLES)
    }

    /**
     * Render the shadow of the shape at the current position.
     */
    fun renderShapeShadow(gl: GL11, color: StoneColor, shape: Shape, orientation: Orientation, alpha: Float) {
        val c = color.shadowColor
        tmp[0] = c[0] * alpha
        tmp[1] = c[1] * alpha
        tmp[2] = c[2] * alpha
        tmp[3] = alpha

        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, tmp, 0)
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, materialBlack, 0)
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, materialBlack, 0)

        shadow.bindBuffers(gl)

        gl.glEnable(GL10.GL_BLEND)
        gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA)
        gl.glEnable(GL10.GL_TEXTURE_2D)
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[1])

        for (i in 0 until shape.size) {
            var j = 0
            while (j < shape.size) {
                if (shape.isStone(j, i, orientation))
                    shadow.drawElements(gl, GL10.GL_TRIANGLES)

                gl.glTranslatef(stoneSize * 2.0f, 0f, 0f)
                j++
            }
            gl.glTranslatef(-j * stoneSize * 2.0f, 0f, stoneSize * 2.0f)
        }
        gl.glDisable(GL10.GL_TEXTURE_2D)
        gl.glDisable(GL10.GL_BLEND)
    }

    fun renderShapeShadow(
        gl: GL11,
        shape: Shape,
        color: StoneColor,
        orientation: Orientation,
        height: Float,
        ang: Float, ax: Float, ay: Float, az: Float,
        lightAngle: Float,
        alpha: Float,
        scale: Float
    ) {
        val offset = shape.size.toFloat() - 1.0f
        val heightAlphaMultiplier = 0.80f - height / 16.0f
        if (heightAlphaMultiplier < 0.0f) return

        gl.glRotatef(-lightAngle, 0f, 1f, 0f)
        gl.glTranslatef(2.5f * height * 0.08f, 0f, 2.0f * height * 0.08f)
        gl.glRotatef(lightAngle, 0f, 1f, 0f)
        gl.glTranslatef(
            stoneSize * offset, 0f,
            stoneSize * offset
        )
        gl.glScalef(scale, 0.01f, scale)
        gl.glRotatef(ang, ax, ay, az)
        gl.glScalef(1.0f + height / 16.0f, 1f, 1.0f + height / 16.0f)
        gl.glTranslatef(
            -stoneSize * offset, 0f,
            -stoneSize * offset
        )

        renderShapeShadow(gl, color, shape, orientation, heightAlphaMultiplier * alpha)
    }

    /**
     * Renders the full shape at the current position and angle.
     */
    fun renderShape(gl: GL11, color: StoneColor, shape: Shape, orientation: Orientation, alpha: Float) {
        val c = color.stoneColor
        tmp[0] = c[0] * alpha
        tmp[1] = c[1] * alpha
        tmp[2] = c[2] * alpha
        tmp[3] = alpha

        gl.glEnable(GL10.GL_BLEND)
        gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA)
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, tmp, 0)
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, materialStoneSpecular, 0)
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, materialStoneShininess, 0)
        stone.bindBuffers(gl)

        var i = 0
        while (i < shape.size) {
            var j = 0
            while (j < shape.size) {
                if (shape.isStone(j, i, orientation))
                    stone.drawElements(gl, GL10.GL_TRIANGLES)
                gl.glTranslatef(stoneSize * 2.0f, 0f, 0f)
                j++
            }
            gl.glTranslatef(-j * stoneSize * 2.0f, 0f, stoneSize * 2.0f)
            i++
        }
        gl.glDisable(GL10.GL_BLEND)
    }
}