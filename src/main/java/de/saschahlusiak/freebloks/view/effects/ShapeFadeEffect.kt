package de.saschahlusiak.freebloks.view.effects

import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.scene.Scene
import javax.microedition.khronos.opengles.GL11
import kotlin.math.cos

class ShapeFadeEffect(model: Scene, turn: Turn, private val numberOfIterations: Float) : AbsShapeEffect(model, turn) {
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
            -BoardRenderer.stoneSize * (scene.board.width - 1).toFloat() + BoardRenderer.stoneSize * 2.0f * x.toFloat(),
            0f,
            -BoardRenderer.stoneSize * (scene.board.height - 1).toFloat() + BoardRenderer.stoneSize * 2.0f * y.toFloat())
        renderer.renderShape(gl, color, shape, orientation, alpha * BoardRenderer.defaultStoneAlpha)
        gl.glPopMatrix()
    }
}