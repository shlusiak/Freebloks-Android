package de.saschahlusiak.freebloks.view.effects

import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.theme.FeedbackType
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.scene.Scene
import de.saschahlusiak.freebloks.view.gles2.Gles2BoardRenderer
import de.saschahlusiak.freebloks.view.gles2.Gles2Context
import kotlin.math.*
import kotlin.random.Random

class ShapeRollEffect(
    model: Scene,
    turn: Turn,
    private var z: Float,
    private var vz: Float
) : AbsShapeEffect(model, turn) {

    private val gravity = 61.0f

    private var r = 8.0f
    private var vr = 0f
    private var ax: Float
    private var ay: Float
    private var az: Float
    private var done = false

    init {
        val angx = (Random.nextFloat() * 2.0f * PI.toFloat())
        val angy = (Random.nextFloat() * 2.0f * PI.toFloat())

        ax = sin(angx) * cos(angy)
        ay = sin(angy)
        az = cos(angx) * cos(angy)

        val p = vz / gravity
        val q = 2.0f * -z / gravity
        if (p * p - q > 0.0) {
            val time = -p + sqrt(p * p - q)
            vr = -r / time
        } else {
            r = 0.0f
            vr = r
        }
    }

    override fun isDone(): Boolean {
        return done
    }

    override fun execute(elapsed: Float): Boolean {
        val epsilon = 0.10f
        if (z > epsilon || vz > epsilon || vz < -epsilon) {
            z -= vz * elapsed
            vz += elapsed * gravity
            if (z < 0.0f) { /* impact */
                vz *= -0.55f
                z = 0.0f
                /* reset rotation */
                r = 0.0f
                vr = 0.0f
                val volume = (-vz / 16.0f).pow(2.0f)
                if (vz > -6.0f) vz = 0.0f
                scene.playSound(FeedbackType.StoneBounce, volume, 0.90f + Random.nextFloat() * 0.2f)
            }
        } else {
            vr = 0.0f
            r = vr
            z = 0.0f
            done = true
        }
        r += vr * elapsed
        super.execute(elapsed)
        return true
    }

    override fun renderShadow(ctx: Gles2Context, renderer: Gles2BoardRenderer) {
        ctx.modelView.push()
        ctx.modelView.translate(
            -BoardRenderer.stoneSize * (scene.board.width - 1).toFloat() + BoardRenderer.stoneSize * 2.0f * x,
            0f,
            -BoardRenderer.stoneSize * (scene.board.height - 1).toFloat() + BoardRenderer.stoneSize * 2.0f * y
        )
        renderer.renderShapeShadow(ctx,
            shape, color, orientation,
            z,
            r, ax, ay, az,
            scene.boardObject.currentAngle - scene.baseAngle,
            1.0f, 1.0f
        )
        ctx.modelView.pop()
    }

    override fun render(ctx: Gles2Context, renderer: Gles2BoardRenderer) {
        val mv = ctx.modelView
        mv.push()
        val offset = shape.size.toFloat() - 1.0f
        mv.translate(
            -BoardRenderer.stoneSize * (scene.board.width - 1).toFloat() + BoardRenderer.stoneSize * 2.0f * x.toFloat(),
            z,
            -BoardRenderer.stoneSize * (scene.board.height - 1).toFloat() + BoardRenderer.stoneSize * 2.0f * y.toFloat())
        mv.translate(BoardRenderer.stoneSize * offset, 0f, BoardRenderer.stoneSize * offset)
        mv.rotate(r, ax, ay, az)
        mv.translate(-BoardRenderer.stoneSize * offset, 0f, -BoardRenderer.stoneSize * offset)
        renderer.renderShape(ctx, color, shape, orientation, BoardRenderer.defaultStoneAlpha)
        mv.pop()
    }
}
