package de.saschahlusiak.freebloks.view.effects

import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.model.ViewModel
import javax.microedition.khronos.opengles.GL11
import kotlin.math.pow

class ShapeUndoEffect(model: ViewModel, turn: Turn) : AbsShapeEffect(model, turn) {
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

    override fun renderShadow(gl: GL11, renderer: BoardRenderer) {
        gl.glPushMatrix()
        gl.glTranslatef(
            -BoardRenderer.stone_size * (model.board.width - 1).toFloat() + BoardRenderer.stone_size * 2.0f * x,
            0f,
            -BoardRenderer.stone_size * (model.board.height - 1).toFloat() + BoardRenderer.stone_size * 2.0f * y
        )

        renderer.renderShadow(gl,
            shape, color, orientation,
            z,
            rot, 0f, 1f, 0f,
            90.0f * model.boardObject.centerPlayer.toFloat(),
            alpha, 1.0f

        )
        gl.glPopMatrix()
    }

    override fun render(gl: GL11, renderer: BoardRenderer) {
        gl.glPushMatrix()
        gl.glTranslatef(0f, z, 0f)
        gl.glTranslatef(
            -BoardRenderer.stone_size * (model.board.width - 1).toFloat() + BoardRenderer.stone_size * 2.0f * x.toFloat(),
            0f,
            -BoardRenderer.stone_size * (model.board.height - 1).toFloat() + BoardRenderer.stone_size * 2.0f * y.toFloat()
        )
        gl.glRotatef(rot, 0f, 1f, 0f)
        renderer.renderPlayerStone(gl, color, shape, orientation, alpha * BoardRenderer.DEFAULT_ALPHA)
        gl.glPopMatrix()
    }
}