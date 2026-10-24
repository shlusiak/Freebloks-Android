package de.saschahlusiak.freebloks.view.gles2

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.opengl.GLES20
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.ktx.KTX
import de.saschahlusiak.freebloks.model.Board
import de.saschahlusiak.freebloks.model.Orientation
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.model.StoneColor
import de.saschahlusiak.freebloks.theme.ColorThemes
import de.saschahlusiak.freebloks.theme.Theme
import de.saschahlusiak.freebloks.view.BoardModels
import de.saschahlusiak.freebloks.view.BoardRenderer.stoneSize

/**
 * Renders the board, stones and shadows.
 */
class Gles2BoardRenderer(private val resources: Resources) {
    companion object {
        private val materialBoardDiffuseSeed = floatArrayOf(0.45f, 0.85f, 0.6f, 0.75f)
        private val materialBoardSpecular = floatArrayOf(0.25f, 0.25f, 0.25f)
        private const val materialBoardShininess = 35.0f
        val materialStoneSpecular = floatArrayOf(0.3f, 0.3f, 0.3f)
        const val materialStoneShininess = 30.0f
        private val materialBlack = floatArrayOf(0f, 0f, 0f)

        private const val textureScale = 0.12f
        private const val textureRotation = 1.0f
    }

    /** A single field, used to highlight seeds. */
    private val field = Gles2Model(BoardModels.buildSingleBoardFieldModel())

    /** All fields of a board of the given size in one model, cached per size. */
    private val boards = HashMap<Pair<Int, Int>, Gles2Model>()
    private var border = Gles2Model(BoardModels.buildBorderModel(Board.DEFAULT_BOARD_SIZE))
    val stone = Gles2Model(BoardModels.buildStoneModel())
    private val shadow = Gles2Model(BoardModels.buildStoneShadow())

    private var theme: Theme = ColorThemes.Black
    private var boardTexture = 0
    private var shadowTexture = 0
    private val tmp = FloatArray(4)
    private var valid = false
    private var hasTexture = false
    private val rgba = FloatArray(4)

    fun setTheme(theme: Theme) {
        this.theme = theme
        valid = false
    }

    fun setBoardSize(boardSize: Int) {
        border = Gles2Model(BoardModels.buildBorderModel(boardSize))
    }

    /** The GL context was (re)created: all GL objects are gone. */
    fun onSurfaceCreated() {
        stone.invalidate()
        field.invalidate()
        boards.clear()
        border.invalidate()
        shadow.invalidate()
        boardTexture = 0
        valid = false

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.stone_shadow)
        shadowTexture = Gles2Textures.fromBitmap(bitmap)
        bitmap.recycle()
    }

    private fun updateTexture() {
        val asset = theme.asset
        Gles2Textures.delete(boardTexture)
        boardTexture = 0

        if (theme.isResource && asset != null) {
            hasTexture = true
            val ids = IntArray(1)
            GLES20.glGenTextures(1, ids, 0)
            boardTexture = ids[0]
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, boardTexture)
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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    private fun draw(ctx: Gles2Context, model: Gles2Model) {
        ctx.apply()
        model.draw()
    }

    /**
     * Render the border and fields of the board, optionally with seeds
     *
     * @param seedsPlayer the number of the player whose "seeds" to render or -1 if none
     */
    fun renderBoard(ctx: Gles2Context, board: Board, seedsPlayer: Int) {
        if (!valid) {
            updateTexture()
            valid = true
        }

        ctx.lighting = true
        ctx.setSpecular(materialBoardSpecular, materialBoardShininess)
        ctx.setColor(rgba)

        ctx.textured = hasTexture
        if (hasTexture) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, boardTexture)
        }

        val mv = ctx.modelView
        val w = board.width
        val h = board.height

        // the texture coordinates are baked into the model
        ctx.texture.loadIdentity()
        val fields = boards.getOrPut(w to h) {
            Gles2Model(BoardModels.buildBoardModel(w, h, stoneSize * 4.0f, textureScale, textureRotation))
        }
        fields.bind()
        draw(ctx, fields)

        /* seeds, highlighted on top of their field */
        if (seedsPlayer >= 0) {
            var bound = false
            for (y in 0 until h) for (x in 0 until w) {
                if (board.getFieldStatus(seedsPlayer, y, x) != Board.FIELD_ALLOWED) continue

                if (!bound) {
                    bound = true
                    ctx.textured = false
                    ctx.blend(Gles2Context.Blend.Alpha)
                    ctx.setColor(materialBoardDiffuseSeed)
                    field.bind()
                }
                mv.withPush {
                    mv.translate(-stoneSize * (w - 1) + stoneSize * 2.0f * x, 0f, -stoneSize * (h - 1) + stoneSize * 2.0f * y)
                    draw(ctx, field)
                }
            }
            ctx.blend(Gles2Context.Blend.Off)
        }

        ctx.setColor(rgba)

        // the border uses plain model space coordinates for its texture
        val tex = ctx.texture
        tex.loadIdentity()
        tex.scale(textureScale, textureScale, 1f)
        tex.rotate(textureRotation, 0f, 0f, 1f)
        ctx.textured = hasTexture

        border.bind()

        /* we want the border in the depth buffer so it can cover the stones rendered later */
        ctx.depthTest(true)
        for (i in 0..3) {
            draw(ctx, border)
            mv.rotate(90f, 0f, 1f, 0f)
        }

        tex.loadIdentity()
        ctx.textured = false
    }

    /**
     * Render a single stone with the given color and alpha. The [stone] model MUST be bound before calling this.
     */
    fun renderSingleStone(ctx: Gles2Context, color: StoneColor, alpha: Float) {
        val c = color.stoneColor
        tmp[0] = c[0] * alpha
        tmp[1] = c[1] * alpha
        tmp[2] = c[2] * alpha
        tmp[3] = alpha
        ctx.setColor(tmp)
        draw(ctx, stone)
    }

    /**
     * Renders a single stone at the current position with the given premultiplied RGBA colour, blended.
     */
    fun renderStone(ctx: Gles2Context, rgba: FloatArray) {
        ctx.blend(Gles2Context.Blend.Premultiplied)
        ctx.lighting = true
        ctx.textured = false
        ctx.setColor(rgba)
        ctx.setSpecular(materialStoneSpecular, materialStoneShininess)
        stone.bind()
        draw(ctx, stone)
        ctx.blend(Gles2Context.Blend.Off)
    }

    /**
     * Render the shadow of the shape at the current position.
     */
    fun renderShapeShadow(ctx: Gles2Context, color: StoneColor, shape: Shape, orientation: Orientation, alpha: Float) {
        val c = color.shadowColor
        tmp[0] = c[0] * alpha
        tmp[1] = c[1] * alpha
        tmp[2] = c[2] * alpha
        tmp[3] = alpha

        ctx.lighting = true
        ctx.setColor(tmp)
        ctx.setSpecular(materialBlack, 0f)

        shadow.bind()

        ctx.blend(Gles2Context.Blend.Premultiplied)
        ctx.textured = true
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, shadowTexture)

        val mv = ctx.modelView
        for (i in 0 until shape.size) {
            var j = 0
            while (j < shape.size) {
                if (shape.isStone(j, i, orientation))
                    draw(ctx, shadow)

                mv.translate(stoneSize * 2.0f, 0f, 0f)
                j++
            }
            mv.translate(-j * stoneSize * 2.0f, 0f, stoneSize * 2.0f)
        }
        ctx.textured = false
        ctx.blend(Gles2Context.Blend.Off)
    }

    fun renderShapeShadow(
        ctx: Gles2Context,
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

        val mv = ctx.modelView
        mv.rotate(-lightAngle, 0f, 1f, 0f)
        mv.translate(2.5f * height * 0.08f, 0f, 2.0f * height * 0.08f)
        mv.rotate(lightAngle, 0f, 1f, 0f)
        mv.translate(stoneSize * offset, 0f, stoneSize * offset)
        mv.scale(scale, 0.01f, scale)
        mv.rotate(ang, ax, ay, az)
        mv.scale(1.0f + height / 16.0f, 1f, 1.0f + height / 16.0f)
        mv.translate(-stoneSize * offset, 0f, -stoneSize * offset)

        renderShapeShadow(ctx, color, shape, orientation, heightAlphaMultiplier * alpha)
    }

    /**
     * Renders the full shape at the current position and angle.
     */
    fun renderShape(ctx: Gles2Context, color: StoneColor, shape: Shape, orientation: Orientation, alpha: Float) {
        val c = color.stoneColor
        tmp[0] = c[0] * alpha
        tmp[1] = c[1] * alpha
        tmp[2] = c[2] * alpha
        tmp[3] = alpha

        ctx.blend(Gles2Context.Blend.Premultiplied)
        ctx.lighting = true
        ctx.textured = false
        ctx.setColor(tmp)
        ctx.setSpecular(materialStoneSpecular, materialStoneShininess)
        stone.bind()

        val mv = ctx.modelView
        var i = 0
        while (i < shape.size) {
            var j = 0
            while (j < shape.size) {
                if (shape.isStone(j, i, orientation))
                    draw(ctx, stone)
                mv.translate(stoneSize * 2.0f, 0f, 0f)
                j++
            }
            mv.translate(-j * stoneSize * 2.0f, 0f, stoneSize * 2.0f)
            i++
        }
        ctx.blend(Gles2Context.Blend.Off)
    }
}
