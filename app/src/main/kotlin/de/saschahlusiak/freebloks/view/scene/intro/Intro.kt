package de.saschahlusiak.freebloks.view.scene.intro

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLU
import android.os.Handler
import android.os.Looper
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.model.Board
import de.saschahlusiak.freebloks.theme.ColorThemes
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.gles2.Gles2BackgroundRenderer
import de.saschahlusiak.freebloks.view.gles2.Gles2Renderer
import de.saschahlusiak.freebloks.view.scene.Scene
import kotlin.math.sin

class Intro(private val context: Context, private val scene: Scene, var listener: IntroDelegate?) {

    companion object {
        private const val INTRO_SPEED = 1.2f
        private const val WIPE_SPEED = 14.0f
        private const val WIPE_ANGLE = 28.0f
        private const val MATRIX_START_LEFT = 1.56f
        private const val MATRIX_START_RIGHT = MATRIX_START_LEFT + 0.25f
        private const val MATRIX_STOP_LEFT = 6.0f
        private const val MATRIX_STOP_RIGHT = MATRIX_STOP_LEFT + 0.25f
    }

    private val handler = Handler(Looper.getMainLooper())
    private val backgroundRenderer by lazy { Gles2BackgroundRenderer(context.resources, ColorThemes.Blue) }
    private val board = Board(20)
    internal val effects = Effects(scene)
    private var anim = 0.0f
    private var phase = Phase.Freebloks
    private var flipping = false
    private var flipAnimation = 0.0f

    init {
        phase.enter(this)
    }

    fun handlePointerDown(): Boolean {
        cancel()
        return true
    }

    @UiThread
    fun cancel() {
        listener?.onIntroCompleted()
    }

    /**
     * Return the slowed down effects animation duration during the matrix move
     */
    private fun slowedDownTime(elapsed: Float): Float {
        return when (anim) {
            in MATRIX_START_LEFT..MATRIX_START_RIGHT ->
                elapsed * (MATRIX_START_RIGHT - anim) / (MATRIX_START_RIGHT - MATRIX_START_LEFT)

            in MATRIX_START_RIGHT..MATRIX_STOP_LEFT -> 0.0f

            in MATRIX_STOP_LEFT..MATRIX_STOP_RIGHT ->
                elapsed * (anim - MATRIX_STOP_LEFT) / (MATRIX_STOP_RIGHT - MATRIX_STOP_LEFT)

            else -> elapsed
        }
    }

    @WorkerThread
    fun execute(elapsed_: Float) {
        val elapsed = elapsed_ * INTRO_SPEED

        anim += elapsed

        if (flipping || flipAnimation > 0.000001f) {
            if (flipping) {
                flipAnimation += elapsed * WIPE_SPEED
                if (flipAnimation > 1.0f) {
                    flipAnimation = 1.0f
                    flipping = false
                }
            } else {
                flipAnimation -= elapsed * 2.5f
                if (flipAnimation < 0.0) flipAnimation = 0.0f
            }
        }

        synchronized(effects) {
            when(phase) {
                Phase.Freebloks -> effects.execute(slowedDownTime(elapsed))

                // In these phases, speed up the falling of the stones a bit
                Phase.By,
                Phase.Sascha,
                Phase.Hlusiak -> effects.execute(elapsed * 1.7f)

                else -> effects.execute(elapsed)
            }

            if (anim >= phase.duration) {
                val next = phase.next()
                if (next == null) {
                    handler.post {
                        listener?.onIntroCompleted()
                    }
                    return
                }
                anim = 0.0f
                phase = next
                phase.enter(this)
            }
        }
    }

    /**
     * Begin flipping up the board and accelerate physical stones
     */
    internal fun flipBoard() {
        flipping = true
        flipAnimation = 0.0f

        effects.startFlipBoard(WIPE_ANGLE, WIPE_SPEED)
    }

    /** The GL context was (re)created. */
    fun onSurfaceCreated() {
        backgroundRenderer.onSurfaceCreated()
    }

    fun render(renderer: Gles2Renderer) {
        val ctx = renderer.ctx
        val mv = ctx.modelView
        mv.loadIdentity()
        backgroundRenderer.render(ctx)

        if (scene.verticalLayout) {
            mv.translate(0f, 4.5f, 0f)
        } else {
            mv.translate(0f, 1.5f, 0f)
        }
        mv.lookAt(0f, if (scene.verticalLayout) 4.0f else 1.0f, renderer.fixedZoom * 1.1f, 0f, 0f, 0f, 0f, 1f, 0f)

        mv.rotate(50f, 1f, 0f, 0f)
        val angle1 = 180.0f
        val angle2 = -60.0f
        val matrixAnim = sin((anim - MATRIX_START_LEFT) / (MATRIX_STOP_LEFT - MATRIX_START_LEFT) * Math.PI.toFloat() - Math.PI.toFloat() / 2.0f) / 2.0f + 0.5f

        if (phase == Phase.Freebloks) {
            if (anim < MATRIX_START_LEFT) {
                mv.rotate(angle2, 1f, 0f, 0f)
                mv.rotate(angle1, 0f, 1f, 0f)
            } else if (anim < MATRIX_STOP_LEFT) {
                mv.rotate(angle2 - matrixAnim * matrixAnim * angle2, 1f, 0f, 0f)
                mv.rotate(angle1 - matrixAnim * angle1, 0f, 1f, 0f)
            }

            if (anim < MATRIX_START_LEFT) {
                mv.translate(0.0f, -14.0f + anim / MATRIX_START_LEFT * 4.0f, 0.0f)
            } else if (anim < MATRIX_STOP_LEFT) {
                mv.translate(0f, -10 + 10 * (matrixAnim * matrixAnim), 0f)
            }
        }

        ctx.setLightPosition(renderer.lightPos[0], renderer.lightPos[1], renderer.lightPos[2])

        mv.push()
        if (flipAnimation > 0.0001f) {
            mv.translate(0f, 0f, 20 * BoardRenderer.stoneSize)
            mv.rotate(flipAnimation * WIPE_ANGLE, 1f, 0f, 0f)
            mv.translate(0f, 0f, -20 * BoardRenderer.stoneSize)
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        renderer.boardRenderer.renderBoard(ctx, board, -1)

        mv.pop()

        // render the stones, which are purely effects
        synchronized(effects) {
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
            for (i in effects.indices) effects[i].renderShadow(ctx, renderer.boardRenderer)

            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            for (i in effects.indices) effects[i].render(ctx, renderer.boardRenderer)
        }
    }
}
