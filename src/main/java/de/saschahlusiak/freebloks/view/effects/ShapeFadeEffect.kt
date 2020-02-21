package de.saschahlusiak.freebloks.view.effects

import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.model.ViewModel
import javax.microedition.khronos.opengles.GL11
import kotlin.math.cos

class ShapeFadeEffect(model: ViewModel, turn: Turn, private val numberOfIterations: Float) : AbsShapeEffect(model, turn) {
    private val timePerIteration = 1.1f
    private val fromAlpha = 0.15f
    private val toAlpha = 1.0f

    override fun isDone(): Boolean {
        return time > timePerIteration * numberOfIterations
    }

    override fun render(gl: GL11, renderer: BoardRenderer) {
        /* every timePerIteration needs to match 2 * PI */
        val amp = cos(time / timePerIteration * Math.PI.toFloat() * 2.0f) * 0.5f + 0.5f
        val alpha = fromAlpha + amp * (toAlpha - fromAlpha)
        gl.glPushMatrix()
        gl.glTranslatef(
            -BoardRenderer.stone_size * (model.board.width - 1).toFloat() + BoardRenderer.stone_size * 2.0f * x.toFloat(),
            0f,
            -BoardRenderer.stone_size * (model.board.height - 1).toFloat() + BoardRenderer.stone_size * 2.0f * y.toFloat())
        renderer.renderPlayerStone(gl, color, shape, orientation, alpha * BoardRenderer.DEFAULT_ALPHA)
        gl.glPopMatrix()
    }
}