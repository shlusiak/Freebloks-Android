package de.saschahlusiak.freebloks.view.gles2

import de.saschahlusiak.freebloks.model.Orientation
import de.saschahlusiak.freebloks.model.StoneColor
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.scene.Scene
import de.saschahlusiak.freebloks.view.scene.Wheel
import kotlin.math.abs
import kotlin.math.pow

/**
 * Draws the wheel of stones the current player can pick from.
 */
class WheelRenderer {
    fun render(ctx: Gles2Context, boardRenderer: Gles2BoardRenderer, scene: Scene) {
        val wheel = scene.wheel
        val mv = ctx.modelView

        synchronized(wheel) {
            val currentOffset = wheel.currentOffset
            val highlighted = wheel.currentStone
            val selectedStone = scene.currentStone.stone

            mv.translate(-currentOffset, 0f, BoardRenderer.stoneSize * (scene.board.width + 10))
            for (i in wheel.stones.indices.reversed()) {
                val s = wheel.stones[i]

                if (s.available - (if (s == selectedStone) 1 else 0) <= 0) continue

                val col = (i / 2).toFloat()
                val row = i % 2.toFloat()
                val offset = -(s.shape.size.toFloat() - 1.0f) * BoardRenderer.stoneSize
                val x = col * Wheel.STONE_SPACING
                val effect = 12.5f / (12.5f + (abs(x - currentOffset) * 0.5f).pow(2.5f))
                var y = 0.35f + effect * 0.75f
                val z = row * Wheel.STONE_SPACING
                val scale = 0.9f + effect * 0.3f
                var rotate = -scene.baseAngle
                if (!scene.verticalLayout) rotate -= 90.0f
                var alpha = 1.0f
                val isHighlighted = highlighted == s && s != selectedStone
                if (isHighlighted) y += 1.2f
                if (!wheel.movesLeft && !scene.game.isFinished) alpha *= 0.65f
                alpha *= 0.75f + effect * 0.25f

                mv.withPush {
                    mv.translate(x, 0f, z)
                    mv.scale(scale, scale, scale)

                    if (s.available > 1 && isHighlighted) {
                        mv.withPush {
                            mv.translate(BoardRenderer.stoneSize, 0f, BoardRenderer.stoneSize * 0.6f)
                            mv.rotate(rotate, 0f, 1f, 0f)
                            mv.scale(0.85f, 0.85f, 0.85f)
                            mv.translate(offset, 0f, offset)
                            boardRenderer.renderShape(ctx, wheel.playerColor, s.shape, Orientation.Default, alpha)
                        }
                    }
                    mv.rotate(rotate, 0f, 1f, 0f)
                    mv.translate(offset, 0f, offset)
                    mv.withPush {
                        boardRenderer.renderShapeShadow(
                            ctx,
                            s.shape,
                            wheel.playerColor,
                            Orientation.Default,
                            y,
                            0f,
                            0f,
                            1f,
                            0f,
                            rotate,
                            alpha,
                            1.0f
                        )
                    }
                    mv.translate(0f, y, 0f)
                    boardRenderer.renderShape(
                        ctx,
                        if (isHighlighted) StoneColor.White else wheel.playerColor,
                        s.shape,
                        Orientation.Default,
                        alpha
                    )
                }
            }
        }
    }
}
