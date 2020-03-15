package de.saschahlusiak.freebloks.view.effects

import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.scene.Scene
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11
import kotlin.math.sin

class BoardStoneGlowEffect(
    private var model: Scene,
    color: Int,
    private val x: Int,
    private val y: Int,
    distance: Float
) : AbsEffect() {
    private val timeMax = 0.55f

    private val color1 = Global.stone_color_a[color]
    private val color2 = Global.stone_color_a[0]
    private val color = FloatArray(4) { color1[it] * BoardRenderer.defaultStoneAlpha }

    init {
        time = -0.6f - distance * 0.025f
        this.color[3] = BoardRenderer.defaultStoneAlpha
    }

    override fun isEffected(x: Int, y: Int): Boolean {
        return x == this.x && y == this.y
    }

    override fun isDone(): Boolean {
        return time > timeMax
    }

    override fun execute(elapsed: Float): Boolean {
        var blend = sin(time / timeMax * Math.PI.toFloat()) * 0.9f

        if (time < 0.0f || time > timeMax) blend = 0.0f

        color[3] = BoardRenderer.defaultStoneAlpha
        color[0] = (color1[0] * (1.0f - blend) + color2[0] * blend) * color[3]
        color[1] = (color1[1] * (1.0f - blend) + color2[1] * blend) * color[3]
        color[2] = (color1[2] * (1.0f - blend) + color2[2] * blend) * color[3]

        return super.execute(elapsed)
    }

    override fun render(gl: GL11, renderer: BoardRenderer) {
        gl.glEnable(GL10.GL_BLEND)
        gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA)
        gl.glPushMatrix()
        gl.glTranslatef(
            -BoardRenderer.stoneSize * (model.board.width - 1).toFloat() + BoardRenderer.stoneSize * 2.0f * x.toFloat(),
            0f,
            -BoardRenderer.stoneSize * (model.board.height - 1).toFloat() + BoardRenderer.stoneSize * 2.0f * y.toFloat()
        )

        with (renderer) {
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, color, 0)
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, BoardRenderer.materialStoneSpecular, 0)
            gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, BoardRenderer.materialStoneShininess, 0)
            stone.bindBuffers(gl)
            stone.drawElements(gl, GL10.GL_TRIANGLES)
        }

        gl.glPopMatrix()
        gl.glDisable(GL10.GL_BLEND)
    }
}