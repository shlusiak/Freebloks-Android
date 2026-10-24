package de.saschahlusiak.freebloks.view.gles2

import android.content.res.Resources
import de.saschahlusiak.freebloks.theme.ColorThemes
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.scene.Scene
import de.saschahlusiak.freebloks.view.scene.intro.Intro
import de.saschahlusiak.freebloks.view.scene.intro.Phase
import kotlin.math.sin

/**
 * Draws the [Intro]: the animated board and its falling stones, with its own camera.
 */
class IntroRenderer(resources: Resources, val intro: Intro) {
    private val backgroundRenderer = Gles2BackgroundRenderer(resources, ColorThemes.Blue)

    /** The GL context was (re)created: all GL objects are gone. */
    fun onSurfaceCreated() {
        backgroundRenderer.onSurfaceCreated()
    }

    fun render(ctx: Gles2Context, boardRenderer: Gles2BoardRenderer, canvas: Gles2EffectCanvas, scene: Scene, fixedZoom: Float, lightPos: FloatArray) {
        val mv = ctx.modelView
        val anim = intro.anim
        val matrixStartLeft = Intro.MATRIX_START_LEFT
        val matrixStopLeft = Intro.MATRIX_STOP_LEFT

        mv.loadIdentity()
        backgroundRenderer.render(ctx)

        if (scene.verticalLayout) {
            mv.translate(0f, 4.5f, 0f)
        } else {
            mv.translate(0f, 1.5f, 0f)
        }
        mv.lookAt(0f, if (scene.verticalLayout) 4.0f else 1.0f, fixedZoom * 1.1f, 0f, 0f, 0f, 0f, 1f, 0f)

        mv.rotate(50f, 1f, 0f, 0f)
        val angle1 = 180.0f
        val angle2 = -60.0f
        val matrixAnim = sin((anim - matrixStartLeft) / (matrixStopLeft - matrixStartLeft) * Math.PI.toFloat() - Math.PI.toFloat() / 2.0f) / 2.0f + 0.5f

        if (intro.phase == Phase.Freebloks) {
            if (anim < matrixStartLeft) {
                mv.rotate(angle2, 1f, 0f, 0f)
                mv.rotate(angle1, 0f, 1f, 0f)
            } else if (anim < matrixStopLeft) {
                mv.rotate(angle2 - matrixAnim * matrixAnim * angle2, 1f, 0f, 0f)
                mv.rotate(angle1 - matrixAnim * angle1, 0f, 1f, 0f)
            }

            if (anim < matrixStartLeft) {
                mv.translate(0.0f, -14.0f + anim / matrixStartLeft * 4.0f, 0.0f)
            } else if (anim < matrixStopLeft) {
                mv.translate(0f, -10 + 10 * (matrixAnim * matrixAnim), 0f)
            }
        }

        ctx.setLightPosition(lightPos[0], lightPos[1], lightPos[2])

        mv.withPush {
            val flipAnimation = intro.flipAnimation
            if (flipAnimation > 0.0001f) {
                mv.translate(0f, 0f, 20 * BoardRenderer.stoneSize)
                mv.rotate(flipAnimation * Intro.WIPE_ANGLE, 1f, 0f, 0f)
                mv.translate(0f, 0f, -20 * BoardRenderer.stoneSize)
            }

            ctx.depthTest(false)
            boardRenderer.renderBoard(ctx, intro.board, -1)
        }

        // render the stones, which are purely effects
        val effects = intro.effects
        synchronized(effects) {
            ctx.depthTest(false)
            for (i in effects.indices) effects[i].renderShadow(canvas)

            ctx.depthTest(true)
            for (i in effects.indices) effects[i].render(canvas)
        }
    }
}
