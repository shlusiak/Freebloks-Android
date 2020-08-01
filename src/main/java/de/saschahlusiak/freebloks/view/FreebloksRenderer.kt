package de.saschahlusiak.freebloks.view

import android.content.Context
import android.graphics.PointF
import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.util.Log
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.model.Board
import de.saschahlusiak.freebloks.theme.ColorThemes
import de.saschahlusiak.freebloks.view.scene.Scene
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11
import kotlin.math.cos
import kotlin.math.sin

class FreebloksRenderer(private val context: Context, private val model: Scene) : GLSurfaceView.Renderer {
    private val tag = FreebloksRenderer::class.java.simpleName

    private val lightAmbientColor = floatArrayOf(0.35f, 0.35f, 0.35f, 1.0f)
    private val lightDiffuseColor = floatArrayOf(0.8f, 0.8f, 0.8f, 1.0f)
    private val lightSpecularColor = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)

    val lightPos = floatArrayOf(2.5f, 5f, -2.0f, 0.0f)

    private var width = 1f
    private var height = 1f

    private var isSoftwareRenderer = false
    private var isEmulator = false

    val fixedZoom = 55.0f
    private val mAngleX = 70.0f

	var zoom = 0f
    private val viewport = IntArray(4)
    private val projectionMatrix = FloatArray(16)
    private val modelViewMatrix = FloatArray(16)
    val boardRenderer = BoardRenderer()
    val backgroundRenderer = BackgroundRenderer(context.resources, ColorThemes.Blue)
    private val outputFar = FloatArray(4)
    private val outputNear = FloatArray(4)
    var updateModelViewMatrix = true

    fun init(boardSize: Int) {
        boardRenderer.setBoardSize(boardSize)
    }

    fun windowToModel(point: PointF): PointF {
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
        point.x = x1 + u * (x2 - x1)
        point.y = z1 + u * (z2 - z1)

        return point
    }

    @Synchronized
    override fun onDrawFrame(gl: GL10) {
        val gl11 = gl as GL11
        val cameraDistance = zoom
        val cameraAngle = model.boardObject.baseAngle
        val boardAngle = model.boardObject.currentAngle

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY)
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)
        gl.glMatrixMode(GL10.GL_MODELVIEW)

        val intro = model.intro
        if (intro != null) {
            intro.render(gl11, this)
            return
        }

        gl.glLoadIdentity()
        if (model.verticalLayout) {
            gl.glTranslatef(0f, 7.0f, 0f)
        } else gl.glTranslatef(-5.0f, 0.6f, 0f)

        GLU.gluLookAt(gl,
            (fixedZoom / cameraDistance * sin(cameraAngle * Math.PI / 180.0) * cos(mAngleX * Math.PI / 180.0f)).toFloat(),
            (fixedZoom / cameraDistance * sin(mAngleX * Math.PI / 180.0f)).toFloat(),
            (fixedZoom / cameraDistance * cos(mAngleX * Math.PI / 180.0f) * cos(-cameraAngle * Math.PI / 180.0f)).toFloat(),
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f)

        if (updateModelViewMatrix) synchronized(outputFar) {
            if (isSoftwareRenderer) {
                /* FIXME: add path for software renderer */
            } else {
                gl11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelViewMatrix, 0)
            }
            updateModelViewMatrix = false
        }
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPos, 0)

        /* render board */
        gl.glDisable(GL10.GL_DEPTH_TEST)
        gl.glRotatef(boardAngle, 0f, 1f, 0f)

        backgroundRenderer.render(gl11)
        boardRenderer.renderBoard(gl11, model.board, model.boardObject.showSeedsPlayer)

        val game = model.game
        val gameMode = game.gameMode
        val board = game.board

        /* render player stones on board, unless they are "effected" */
        gl.glPushMatrix()
        gl.glTranslatef(-BoardRenderer.stoneSize * (model.board.width - 1).toFloat(), 0f, -BoardRenderer.stoneSize * (model.board.width - 1).toFloat())
        gl.glEnable(GL10.GL_BLEND)
        gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA)
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, BoardRenderer.materialStoneSpecular, 0)
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, BoardRenderer.materialStoneShininess, 0)
        boardRenderer.stone.bindBuffers(gl11)

        synchronized(model.effects) {
            synchronized(board) {
                for (y in 0 until board.height) {
                    var x = 0
                    while (x < board.width) {
                        val field = board.getFieldPlayer(y, x)
                        if (field != Board.FIELD_FREE) {
                            if (model.effects.none { it.isEffected(x, y) }) {
                                boardRenderer.renderSingleStone(gl11, Global.getPlayerColor(field, gameMode), BoardRenderer.defaultStoneAlpha)
                            }
                        }
                        gl.glTranslatef(BoardRenderer.stoneSize * 2.0f, 0f, 0f)
                        x++
                    }
                    gl.glTranslatef(-x * BoardRenderer.stoneSize * 2.0f, 0f, BoardRenderer.stoneSize * 2.0f)
                }
            }
        }
        gl.glDisable(GL10.GL_BLEND)
        gl.glPopMatrix()
        gl.glDisable(GL10.GL_DEPTH_TEST)

        /* render all effects */
        synchronized(model.effects) {
            for (i in model.effects.indices) {
                model.effects[i].renderShadow(gl11, boardRenderer)
            }
            gl.glEnable(GL10.GL_DEPTH_TEST)
            for (i in model.effects.indices) {
                model.effects[i].render(gl11, boardRenderer)
            }
        }

        gl.glDisable(GL10.GL_DEPTH_TEST)
        gl.glRotatef(-boardAngle, 0f, 1f, 0f)
        gl.glPushMatrix()
        /* reverse the cameraAngle to always fix wheel in front of camera */
        gl.glRotatef(cameraAngle, 0f, 1f, 0f)
        if (!model.verticalLayout) gl.glRotatef(90.0f, 0f, 1f, 0f)
        model.wheel.render(this, gl11)
        gl.glPopMatrix()

        /* render current player stone on the field */
        if (game.isLocalPlayer()) model.currentStone.render(this, gl11)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        val gl11 = gl as GL11
        Log.d(tag, "onSurfaceChanged: $width, $height")
        gl.glViewport(0, 0, width, height)
        viewport[0] = 0
        viewport[1] = 0
        viewport[2] = width
        viewport[3] = height
        this.width = width.toFloat()
        this.height = height.toFloat()
        model.verticalLayout = height >= width

        val fovY = if (model.verticalLayout) 35.0f else 21.0f

        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glLoadIdentity()
        GLU.gluPerspective(gl, fovY, this.width / this.height, 1.0f, 300.0f)
        gl.glMatrixMode(GL10.GL_MODELVIEW)
        synchronized(outputFar) {
            if (isSoftwareRenderer) {
                /* FIXME: add path for software renderer */
            } else {
                gl11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, projectionMatrix, 0)
            }
        }
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        val renderer = gl.glGetString(GL10.GL_RENDERER)
        isEmulator = renderer.contains("Android Emulator OpenGL")
        isSoftwareRenderer = renderer.contains("PixelFlinger") || isEmulator

        Log.i(tag, "Renderer: $renderer")

        with(gl) {
            glDisable(GL10.GL_DITHER)
            glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST)
            glEnable(GL10.GL_CULL_FACE)
            glShadeModel(GL10.GL_SMOOTH)
            glEnable(GL10.GL_DEPTH_TEST)
            glEnable(GL10.GL_NORMALIZE)
            glEnable(GL10.GL_LIGHTING)
            glEnable(GL10.GL_LIGHT0)
            glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPos, 0)
            glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbientColor, 0)
            glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuseColor, 0)
            glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, lightSpecularColor, 0)
        }

        updateModelViewMatrix = true
        model.currentStone.updateTexture(context, gl)
        boardRenderer.onSurfaceChanged(context, (gl as GL11))
        backgroundRenderer.updateTexture(gl)
    }
}