package de.saschahlusiak.freebloks.view.gles2

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLU
import android.opengl.Matrix
import android.util.Log
import de.saschahlusiak.freebloks.model.Board
import de.saschahlusiak.freebloks.model.colorOf
import de.saschahlusiak.freebloks.theme.ColorThemes
import de.saschahlusiak.freebloks.theme.Theme
import de.saschahlusiak.freebloks.utils.PointF
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.RendererBackend
import de.saschahlusiak.freebloks.view.scene.Scene
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shader based OpenGL ES 2.0 counterpart of [de.saschahlusiak.freebloks.view.FreebloksRenderer].
 */
class Gles2Renderer(private val context: Context, private val scene: Scene) : RendererBackend {
    private val tag = Gles2Renderer::class.java.simpleName

    override val glesVersion = 2

    /** Light position (w = 0, directional) in the eye space of the camera. */
    val lightPos = floatArrayOf(2.5f, 5f, -2.0f, 0.0f)

    private var width = 1f
    private var height = 1f

    val fixedZoom = 55.0f
    private val mAngleX = 70.0f

    override var zoom = 0f
    private val viewport = intArrayOf(0, 0, 1, 1)
    private val projectionMatrix = FloatArray(16).also { Matrix.setIdentityM(it, 0) }
    private val modelViewMatrix = FloatArray(16).also { Matrix.setIdentityM(it, 0) }
    private val outputFar = FloatArray(4)
    private val outputNear = FloatArray(4)
    override var updateModelViewMatrix = true

    val ctx = Gles2Context()
    val boardRenderer = Gles2BoardRenderer(context.resources)
    val backgroundRenderer = Gles2BackgroundRenderer(context.resources, ColorThemes.Blue)

    override fun init(boardSize: Int) {
        boardRenderer.setBoardSize(boardSize)
    }

    override fun setBoardSize(size: Int) {
        boardRenderer.setBoardSize(size)
    }

    override fun setTheme(backgroundTheme: Theme, boardTheme: Theme) {
        backgroundRenderer.setTheme(backgroundTheme)
        boardRenderer.setTheme(boardTheme)
    }

    override fun windowToModel(point: PointF): PointF {
        synchronized(outputFar) {
            GLU.gluUnProject(point.x, viewport[3] - point.y, 0.0f, modelViewMatrix, 0, projectionMatrix, 0, viewport, 0, outputNear, 0)
            GLU.gluUnProject(point.x, viewport[3] - point.y, 1.0f, modelViewMatrix, 0, projectionMatrix, 0, viewport, 0, outputFar, 0)
        }

        val x1 = outputFar[0] / outputFar[3]
        val y1 = outputFar[1] / outputFar[3]
        val z1 = outputFar[2] / outputFar[3]
        val x2 = outputNear[0] / outputNear[3]
        val y2 = outputNear[1] / outputNear[3]
        val z2 = outputNear[2] / outputNear[3]
        val u = (0.0f - y1) / (y2 - y1)

        return PointF(
            x1 + u * (x2 - x1),
            z1 + u * (z2 - z1)
        )
    }

    @Synchronized
    override fun onDrawFrame(gl: GL10?) {
        val cameraDistance = zoom
        val cameraAngle = scene.baseAngle
        val boardAngle = scene.boardObject.currentAngle
        val mv = ctx.modelView

        System.arraycopy(projectionMatrix, 0, ctx.projection, 0, 16)
        mv.loadIdentity()
        ctx.texture.loadIdentity()
        ctx.lighting = true
        ctx.textured = false

        val intro = scene.intro
        if (intro != null) {
            intro.render(this)
            return
        }

        if (scene.verticalLayout) {
            mv.translate(0f, 7.0f, 0f)
        } else mv.translate(-5.0f, 0.6f, 0f)

        mv.lookAt(
            (fixedZoom / cameraDistance * sin(cameraAngle * Math.PI / 180.0) * cos(mAngleX * Math.PI / 180.0f)).toFloat(),
            (fixedZoom / cameraDistance * sin(mAngleX * Math.PI / 180.0f)).toFloat(),
            (fixedZoom / cameraDistance * cos(mAngleX * Math.PI / 180.0f) * cos(-cameraAngle * Math.PI / 180.0f)).toFloat(),
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f)

        if (updateModelViewMatrix) synchronized(outputFar) {
            System.arraycopy(mv.current, 0, modelViewMatrix, 0, 16)
            updateModelViewMatrix = false
        }
        ctx.setLightPosition(lightPos[0], lightPos[1], lightPos[2])

        /* render board */
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        mv.rotate(boardAngle, 0f, 1f, 0f)

        backgroundRenderer.render(ctx)
        boardRenderer.renderBoard(ctx, scene.board, scene.boardObject.showSeedsPlayer)

        val game = scene.game
        val gameMode = game.gameMode
        val board = game.board

        val colors = arrayOf(
            gameMode.colorOf(0),
            gameMode.colorOf(1),
            gameMode.colorOf(2),
            gameMode.colorOf(3)
        )

        /* render player stones on board, unless they are "effected" */
        mv.push()
        mv.translate(-BoardRenderer.stoneSize * (scene.board.width - 1).toFloat(), 0f, -BoardRenderer.stoneSize * (scene.board.width - 1).toFloat())
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        ctx.lighting = true
        ctx.textured = false
        ctx.setSpecular(Gles2BoardRenderer.materialStoneSpecular, Gles2BoardRenderer.materialStoneShininess)
        boardRenderer.stone.bind()

        synchronized(scene.effects) {
            // Synchronized with [Board.startNewGame]
            synchronized(board) {
                for (y in 0 until board.height) {
                    var x = 0
                    while (x < board.width) {
                        val field = board.getFieldPlayer(y, x)
                        if (field != Board.FIELD_FREE) {
                            // value between 0 and 3
                            if (scene.effects.none { it.isEffected(x, y) }) {
                                boardRenderer.renderSingleStone(ctx, colors[field], BoardRenderer.defaultStoneAlpha)
                            }
                        }
                        mv.translate(BoardRenderer.stoneSize * 2.0f, 0f, 0f)
                        x++
                    }
                    mv.translate(-x * BoardRenderer.stoneSize * 2.0f, 0f, BoardRenderer.stoneSize * 2.0f)
                }
            }
        }
        GLES20.glDisable(GLES20.GL_BLEND)
        mv.pop()
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)

        /* render all effects */
        synchronized(scene.effects) {
            for (i in scene.effects.indices) {
                scene.effects[i].renderShadow(ctx, boardRenderer)
            }
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            for (i in scene.effects.indices) {
                scene.effects[i].render(ctx, boardRenderer)
            }
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        mv.rotate(-boardAngle, 0f, 1f, 0f)
        mv.push()
        /* reverse the cameraAngle to always fix wheel in front of camera */
        mv.rotate(cameraAngle, 0f, 1f, 0f)
        if (!scene.verticalLayout) mv.rotate(90.0f, 0f, 1f, 0f)
        scene.wheel.render(this)
        mv.pop()

        /* render current player stone on the field */
        if (game.isLocalPlayer()) scene.currentStone.render(this)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(tag, "onSurfaceChanged: $width, $height")
        GLES20.glViewport(0, 0, width, height)
        synchronized(outputFar) {
            viewport[0] = 0
            viewport[1] = 0
            viewport[2] = width
            viewport[3] = height
        }
        this.width = width.toFloat()
        this.height = height.toFloat()
        scene.verticalLayout = height >= width

        val fovY = if (scene.verticalLayout) 35.0f else 21.0f

        synchronized(outputFar) {
            Matrix.perspectiveM(projectionMatrix, 0, fovY, this.width / this.height, 1.0f, 300.0f)
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.i(tag, "Renderer: ${GLES20.glGetString(GLES20.GL_RENDERER)}, version: ${GLES20.glGetString(GLES20.GL_VERSION)}")

        GLES20.glDisable(GLES20.GL_DITHER)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        ctx.create()
        updateModelViewMatrix = true
        scene.currentStone.updateTexture(context)
        boardRenderer.onSurfaceCreated()
        backgroundRenderer.onSurfaceCreated()
        scene.intro?.onSurfaceCreated()
    }
}
