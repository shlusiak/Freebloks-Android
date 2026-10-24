package de.saschahlusiak.freebloks.view.gles2

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import de.saschahlusiak.freebloks.model.Board
import de.saschahlusiak.freebloks.model.colorOf
import de.saschahlusiak.freebloks.theme.ColorThemes
import de.saschahlusiak.freebloks.theme.Theme
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.Camera
import de.saschahlusiak.freebloks.view.scene.Scene
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Shader based OpenGL ES 2.0 renderer.
 */
class Gles2Renderer(private val context: Context, private val scene: Scene) : GLSurfaceView.Renderer {
    private val tag = Gles2Renderer::class.java.simpleName

    /** Light position (w = 0, directional) in the eye space of the camera. */
    private val lightPos = floatArrayOf(2.5f, 5f, -2.0f, 0.0f)

    val camera = Camera()

    private val ctx = Gles2Context()
    private val boardRenderer = Gles2BoardRenderer(context.resources)
    private val backgroundRenderer = Gles2BackgroundRenderer(context.resources, ColorThemes.Blue)
    private val effectCanvas = Gles2EffectCanvas(ctx, boardRenderer)
    private val currentStoneRenderer = CurrentStoneRenderer(context)
    private val wheelRenderer = WheelRenderer()
    private var introRenderer: IntroRenderer? = null

    fun init(boardSize: Int) {
        boardRenderer.setBoardSize(boardSize)
    }

    fun setBoardSize(size: Int) {
        boardRenderer.setBoardSize(size)
    }

    fun setTheme(backgroundTheme: Theme, boardTheme: Theme) {
        backgroundRenderer.setTheme(backgroundTheme)
        boardRenderer.setTheme(boardTheme)
    }

    @Synchronized
    override fun onDrawFrame(gl: GL10?) {
        System.arraycopy(camera.projection, 0, ctx.projection, 0, 16)
        ctx.modelView.loadIdentity()
        ctx.texture.loadIdentity()
        ctx.lighting = true
        ctx.textured = false

        val intro = scene.intro
        if (intro != null) {
            val renderer = introRenderer?.takeIf { it.intro === intro }
                ?: IntroRenderer(context.resources, intro).also { introRenderer = it }
            renderer.render(ctx, boardRenderer, effectCanvas, scene, camera.fixedZoom, lightPos)
            return
        }
        introRenderer = null

        renderBoard()
        renderEffects()
        renderOverlays()
    }

    /** Sets up the camera and draws the background, the board and the stones on it. */
    private fun renderBoard() {
        val mv = ctx.modelView
        val board = scene.game.board

        camera.loadView(mv, scene.verticalLayout, scene.baseAngle)
        ctx.setLightPosition(lightPos[0], lightPos[1], lightPos[2])

        ctx.depthTest(false)
        mv.rotate(scene.boardObject.currentAngle, 0f, 1f, 0f)

        backgroundRenderer.render(ctx)
        boardRenderer.renderBoard(ctx, scene.board, scene.boardObject.showSeedsPlayer)

        val gameMode = scene.game.gameMode
        val colors = Array(4) { gameMode.colorOf(it) }

        /* render player stones on board, unless they are "effected" */
        mv.withPush {
            mv.translate(-BoardRenderer.stoneSize * (scene.board.width - 1).toFloat(), 0f, -BoardRenderer.stoneSize * (scene.board.width - 1).toFloat())
            ctx.blend(Gles2Context.Blend.Premultiplied)
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
            ctx.blend(Gles2Context.Blend.Off)
        }
        ctx.depthTest(false)
    }

    private fun renderEffects() {
        synchronized(scene.effects) {
            for (i in scene.effects.indices) {
                scene.effects[i].renderShadow(effectCanvas)
            }
            ctx.depthTest(true)
            for (i in scene.effects.indices) {
                scene.effects[i].render(effectCanvas)
            }
        }
    }

    /** Draws what is in front of the board: the wheel and the stone being dragged. */
    private fun renderOverlays() {
        val mv = ctx.modelView
        val cameraAngle = scene.baseAngle

        ctx.depthTest(false)
        mv.rotate(-scene.boardObject.currentAngle, 0f, 1f, 0f)
        mv.withPush {
            /* reverse the cameraAngle to always fix wheel in front of camera */
            mv.rotate(cameraAngle, 0f, 1f, 0f)
            if (!scene.verticalLayout) mv.rotate(90.0f, 0f, 1f, 0f)
            wheelRenderer.render(ctx, boardRenderer, scene)
        }

        /* render current player stone on the field */
        if (scene.game.isLocalPlayer()) currentStoneRenderer.render(ctx, boardRenderer, scene)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(tag, "onSurfaceChanged: $width, $height")
        GLES20.glViewport(0, 0, width, height)
        scene.verticalLayout = height >= width
        camera.onSurfaceChanged(width, height, scene.verticalLayout)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.i(tag, "Renderer: ${GLES20.glGetString(GLES20.GL_RENDERER)}, version: ${GLES20.glGetString(GLES20.GL_VERSION)}")

        ctx.create()
        camera.updateModelViewMatrix = true
        currentStoneRenderer.onSurfaceCreated()
        boardRenderer.onSurfaceCreated()
        backgroundRenderer.onSurfaceCreated()
        introRenderer?.onSurfaceCreated()
    }
}
