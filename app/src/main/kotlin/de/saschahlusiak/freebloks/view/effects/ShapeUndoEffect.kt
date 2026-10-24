package de.saschahlusiak.freebloks.view.effects

import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.scene.Scene
import kotlin.math.pow

class ShapeUndoEffect(model: Scene, turn: Turn) : AbsShapeEffect(model, turn) {
    private val timeMax = 1.1f

    private var phase = 0f
    private var z = 0f
    private var alpha = 0f
    private var rot = 0f

    override fun isDone(): Boolean {
        return time > timeMax
    }

    override fun execute(elapsed: Float): Boolean {
        super.execute(elapsed)
        phase = (time / timeMax).pow(0.8f)
        alpha = 1.0f - phase
        z = 13.0f * phase
        rot = phase * 65.0f
        return true
    }

    override fun renderShadow(canvas: EffectCanvas) {
        canvas.matrix.withPush {
            canvas.matrix.translate(
                -BoardRenderer.stoneSize * (scene.board.width - 1).toFloat() + BoardRenderer.stoneSize * 2.0f * x,
                0f,
                -BoardRenderer.stoneSize * (scene.board.height - 1).toFloat() + BoardRenderer.stoneSize * 2.0f * y
            )
            canvas.shapeShadow(
                shape, color, orientation,
                z,
                rot, 0f, 1f, 0f,
                scene.boardObject.currentAngle - scene.baseAngle,
                alpha, 1.0f
            )
        }
    }

    override fun render(canvas: EffectCanvas) {
        val mv = canvas.matrix
        mv.withPush {
            mv.translate(0f, z, 0f)
            mv.translate(
                -BoardRenderer.stoneSize * (scene.board.width - 1).toFloat() + BoardRenderer.stoneSize * 2.0f * x.toFloat(),
                0f,
                -BoardRenderer.stoneSize * (scene.board.height - 1).toFloat() + BoardRenderer.stoneSize * 2.0f * y.toFloat()
            )
            mv.rotate(rot, 0f, 1f, 0f)
            canvas.shape(color, shape, orientation, alpha * BoardRenderer.defaultStoneAlpha)
        }
    }
}
