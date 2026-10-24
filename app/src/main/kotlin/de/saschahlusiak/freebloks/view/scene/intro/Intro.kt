package de.saschahlusiak.freebloks.view.scene.intro

import android.os.Handler
import android.os.Looper
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.model.Board
import de.saschahlusiak.freebloks.view.scene.Scene

class Intro(private val scene: Scene, var listener: IntroDelegate?) {

    companion object {
        private const val INTRO_SPEED = 1.2f
        private const val WIPE_SPEED = 14.0f
        internal const val WIPE_ANGLE = 28.0f
        internal const val MATRIX_START_LEFT = 1.56f
        private const val MATRIX_START_RIGHT = MATRIX_START_LEFT + 0.25f
        internal const val MATRIX_STOP_LEFT = 6.0f
        private const val MATRIX_STOP_RIGHT = MATRIX_STOP_LEFT + 0.25f
    }

    private val handler = Handler(Looper.getMainLooper())
    internal val board = Board(20)
    internal val effects = Effects(scene)
    internal var anim = 0.0f
        private set
    internal var phase = Phase.Freebloks
        private set
    private var flipping = false
    internal var flipAnimation = 0.0f
        private set

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
}
