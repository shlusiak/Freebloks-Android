package de.saschahlusiak.freebloks.view.gles2

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.SimpleModel
import de.saschahlusiak.freebloks.view.scene.CurrentStone
import de.saschahlusiak.freebloks.view.scene.CurrentStone.Status
import de.saschahlusiak.freebloks.view.scene.Scene
import kotlin.math.abs
import kotlin.math.sin

/**
 * Draws the stone the player is currently dragging: the stone, its shadow and the valid/invalid overlay below it.
 */
class CurrentStoneRenderer(private val context: Context) {
    private val overlayRadius = CurrentStone.overlayRadius

    private val overlay = Gles2Model(SimpleModel(4, 2).apply {
        addVertex(-overlayRadius, 0f, overlayRadius, 0f, -1f, 0f, 0f, 0f)
        addVertex(+overlayRadius, 0f, overlayRadius, 0f, -1f, 0f, 1f, 0f)
        addVertex(+overlayRadius, 0f, -overlayRadius, 0f, -1f, 0f, 1f, 1f)
        addVertex(-overlayRadius, 0f, -overlayRadius, 0f, -1f, 0f, 0f, 1f)
        addIndex(0, 1, 2)
        addIndex(0, 2, 3)
        commit()
    })

    private val white = floatArrayOf(1f, 1f, 1f, 1f)
    private val textures = IntArray(3)

    /** The GL context was (re)created: all GL objects are gone. */
    fun onSurfaceCreated() {
        overlay.invalidate()
        val ids = intArrayOf(R.drawable.stone_overlay_green, R.drawable.stone_overlay_red, R.drawable.stone_overlay_shadow)
        for (i in ids.indices) {
            val bitmap = BitmapFactory.decodeResource(context.resources, ids[i])
            Gles2Textures.delete(textures[i])
            textures[i] = Gles2Textures.fromBitmap(bitmap)
            bitmap.recycle()
        }
    }

    fun render(ctx: Gles2Context, boardRenderer: Gles2BoardRenderer, scene: Scene) {
        val currentStone = scene.currentStone
        synchronized(currentStone) {
            val stone = currentStone.stone ?: return
            if (scene.game.currentPlayer < 0) return

            val status = currentStone.status
            val rotateAngle = currentStone.rotateAngle
            val pos = currentStone.pos
            val orientation = currentStone.orientation
            val color = currentStone.currentColor

            var hoverHeight = if (status == Status.IDLE) currentStone.hoverHeightLow else currentStone.hoverHeightHigh
            val offset = stone.shape.size.toFloat() - 1.0f

            if (status == Status.FLIPPING_HORIZONTAL || status == Status.FLIPPING_VERTICAL)
                hoverHeight += abs(sin(rotateAngle / 180f * Math.PI.toFloat()) * overlayRadius * 0.4f)

            val mv = ctx.modelView
            val baseAngle = scene.baseAngle

            fun applyRotation() {
                if (status == Status.ROTATING) mv.rotate(rotateAngle, 0f, 1f, 0f)
                if (status == Status.FLIPPING_HORIZONTAL) mv.rotate(rotateAngle, 0f, 0f, 1f)
                if (status == Status.FLIPPING_VERTICAL) mv.rotate(rotateAngle, 1f, 0f, 0f)
            }

            /** Moves to where the shadow falls, offset away from the light by the hover height. */
            fun applyShadowOffset() {
                mv.rotate(baseAngle, 0f, 1f, 0f)
                mv.translate(2.5f * hoverHeight * 0.08f, 0f, 2.0f * hoverHeight * 0.08f)
                mv.rotate(-baseAngle, 0f, 1f, 0f)
                applyRotation()
            }

            // the overlay is seen from both sides
            ctx.cullFace(false)
            mv.withPush {
                mv.translate(
                    BoardRenderer.stoneSize * ((-(scene.board.width - 1)).toFloat() + 2.0f * pos.x + offset),
                    0f,
                    BoardRenderer.stoneSize * ((-(scene.board.width - 1)).toFloat() + 2.0f * pos.y + offset)
                )

                /* STONE SHADOW */
                mv.withPush {
                    applyShadowOffset()
                    mv.scale(1.09f, 0.01f, 1.09f)
                    mv.translate(-BoardRenderer.stoneSize * offset, 0f, -BoardRenderer.stoneSize * offset)
                    boardRenderer.renderShapeShadow(ctx, color, stone.shape, orientation, 0.80f)
                }

                ctx.blend(Gles2Context.Blend.Premultiplied)
                ctx.lighting = false
                ctx.textured = true
                ctx.setColor(white)
                overlay.bind()

                /* OVERLAY SHADOW */
                mv.withPush {
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[2])
                    applyShadowOffset()
                    mv.scale(1.0f, 0.01f, 1.0f)
                    ctx.apply()
                    overlay.draw()
                }

                /* OVERLAY */
                mv.withPush {
                    mv.translate(0f, hoverHeight, 0f)
                    applyRotation()
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, if (currentStone.isValid) textures[0] else textures[1])
                    ctx.apply()
                    overlay.draw()
                }
                ctx.cullFace(true)
                ctx.lighting = true
                ctx.textured = false

                /* STONE */
                mv.translate(0f, hoverHeight, 0f)
                applyRotation()
                mv.translate(-BoardRenderer.stoneSize * offset, 0f, -BoardRenderer.stoneSize * offset)
                ctx.depthTest(true)

                val alpha = if (status != Status.IDLE || currentStone.isValid) 1.0f else BoardRenderer.defaultStoneAlpha
                boardRenderer.renderShape(ctx, color, stone.shape, orientation, alpha)
            }
        }
    }
}
