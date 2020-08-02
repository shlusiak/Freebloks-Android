package de.saschahlusiak.freebloks.view

import android.content.Context
import android.graphics.PointF
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.client.GameEventObserver
import de.saschahlusiak.freebloks.model.Board
import de.saschahlusiak.freebloks.model.Player
import de.saschahlusiak.freebloks.model.Stone
import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.network.message.MessageServerStatus
import de.saschahlusiak.freebloks.theme.Theme
import de.saschahlusiak.freebloks.view.effects.*
import de.saschahlusiak.freebloks.view.scene.AnimationType
import de.saschahlusiak.freebloks.view.scene.Scene
import kotlin.math.sqrt

class Freebloks3DView(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs), GameEventObserver {
	private lateinit var scene: Scene
    private lateinit var renderer: FreebloksRenderer

    private var scale = 1.0f
    private var thread: AnimateThread? = null

    private var oldDist = 0f
    private var modelPoint = PointF() // current position in field coordinates

    fun setScene(scene: Scene) {
        setEGLConfigChooser(GLConfigChooser(2))
        this.scene = scene
        renderer = FreebloksRenderer(context, scene).apply {
            zoom = scale
        }
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        debugFlags = DEBUG_CHECK_GL_ERROR
    }

    fun setTheme(theme: Theme) {
        renderer.backgroundRenderer.setTheme(theme)
    }

    fun setGameClient(client: GameClient?) {
        if (client != null) {
            scene.setGameClient(client)
        }
        scene.clearEffects()
        queueEvent {
            if (client != null) {
                val game = client.game
                scene.boardObject.lastSize = game.board.width
                for (i in 0 until Board.PLAYER_MAX) if (game.isLocalPlayer(i)) {
                    scene.boardObject.basePlayer = i
                    break
                }
                renderer.updateModelViewMatrix = true
                scene.wheel.update(scene.boardObject.showWheelPlayer)
                newCurrentPlayer(game.currentPlayer)
            }
            renderer.init(scene.boardObject.lastSize)
            requestRender()
        }
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt(x * x + y * y)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        modelPoint.x = event.x
        modelPoint.y = event.y
        renderer.windowToModel(modelPoint)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                scene.handlePointerDown(modelPoint)
            }
            MotionEvent.ACTION_MOVE -> if (event.pointerCount > 1) {
                val newDist = spacing(event)
                if (newDist > 10f) {
                    scale *= newDist / oldDist
                    if (scale > 3.0f) scale = 3.0f
                    if (scale < 0.3f) scale = 0.3f
                    oldDist = newDist
                    renderer.updateModelViewMatrix = true
                    renderer.zoom = scale
                    requestRender()
                }
            } else {
                scene.handlePointerMove(modelPoint)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                scene.handlePointerUp(modelPoint)
                oldDist = spacing(event)
            }
            MotionEvent.ACTION_UP -> {
                scene.handlePointerUp(modelPoint)
            }
        }

        if (scene.isInvalidated()) {
            requestRender()
        }
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        renderer.updateModelViewMatrix = true
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun newCurrentPlayer(player: Int) {
        if (scene.game.isLocalPlayer() || scene.wheel.currentPlayer != scene.boardObject.showWheelPlayer)
            scene.wheel.update(scene.boardObject.showWheelPlayer)

        requestRender()
    }

    override fun stoneWillBeSet(turn: Turn) {
        queueEvent {
            if (scene.hasAnimations() && !scene.game.isLocalPlayer(turn.player)) {
                val e = ShapeRollEffect(scene, turn, 4.0f, -7.0f)
                val set = EffectSet()
                set.add(e)
                set.add(ShapeFadeEffect(scene, turn, 2.0f))
                scene.addEffect(set)
            }
        }
    }

    override fun stoneHasBeenSet(turn: Turn) {
        if (scene.game.isLocalPlayer(turn.player) || turn.player == scene.wheel.currentPlayer)
            scene.wheel.update(scene.boardObject.showWheelPlayer)
        requestRender()

        if (!scene.game.isLocalPlayer(turn.player)) {
            scene.soundPool.play(scene.soundPool.SOUND_CLICK1, 1.0f, 0.9f + Math.random().toFloat() * 0.2f)
            scene.vibrate(Global.VIBRATE_SET_STONE.toLong())
        }
    }

    override fun playerIsOutOfMoves(player: Player) {
        val game = scene.game
        val board = game.board

        if (scene.hasAnimations()) {
            val sx: Int
            val sy: Int
            val gameMode = game.gameMode
            val seed = board.getPlayerSeed(player.number, gameMode) ?: return
            for (x in 0 until board.width) for (y in 0 until board.height) if (board.getFieldPlayer(y, x) == player.number) {
                var effected = false
                synchronized(scene.effects) {
                    for (j in scene.effects.indices) if (scene.effects.get(j).isEffected(x, y)) {
                        effected = true
                        break
                    }
                }
                if (!effected) {
                    val distance = sqrt((x - seed.x) * (x - seed.x) + (y - seed.y) * (y - seed.y).toFloat())
                    val effect: Effect = BoardStoneGlowEffect(
                        (scene),
                        Global.getPlayerColor(player.number, gameMode),
                        x,
                        y,
                        distance)
                    scene.addEffect(effect)
                }
            }
        }
    }

    @WorkerThread
    override fun hintReceived(turn: Turn) {
        queueEvent {
            if (turn.player != scene.game.currentPlayer) return@queueEvent
            if (!scene.game.isLocalPlayer()) return@queueEvent
            scene.boardObject.resetRotation()
            scene.wheel.update(turn.player)
            scene.wheel.showStone(turn.shapeNumber)
            scene.soundPool.play(scene.soundPool.SOUND_HINT, 0.9f, 1.0f)
            val currentPlayer = scene.game.currentPlayer
            val st: Stone
            if (currentPlayer >= 0)
                st = scene.board.getPlayer(currentPlayer).getStone(turn.shapeNumber)
            else
                return@queueEvent

            val p = PointF()
            p.x = turn.x - 0.5f + st.shape.size.toFloat() / 2.0f
            p.y = turn.y - 0.5f + st.shape.size.toFloat() / 2.0f
            scene.currentStone.startDragging(p, st, turn.orientation, scene.getPlayerColor(turn.player))
            requestRender()
        }
    }

    override fun gameFinished() {
        scene.boardObject.resetRotation()
        requestRender()
    }

    override fun gameStarted() {
        scene.boardObject.basePlayer = 0
        for (i in 0 until Board.PLAYER_MAX) if (scene.game.isLocalPlayer(i)) {
            scene.boardObject.basePlayer = i
            break
        }
        scene.wheel.update(scene.boardObject.showWheelPlayer)
        renderer.updateModelViewMatrix = true
        scene.reset()
        requestRender()
    }

    override fun stoneUndone(t: Turn) {
        if (scene.hasAnimations()) {
            val e: Effect = ShapeUndoEffect(scene, t)
            scene.addEffect(e)
        }
        scene.currentStone.stopDragging()
        requestRender()
    }

    override fun serverStatus(status: MessageServerStatus) {
        if (status.width != scene.boardObject.lastSize) {
            scene.boardObject.lastSize = status.width
            queueEvent {
                renderer.boardRenderer.setBoardSize(scene.board.width)
                requestRender()
            }
        }
    }

    @UiThread
    override fun onConnected(client: GameClient) {
        newCurrentPlayer(client.game.currentPlayer)
    }

    /**
     * Execute the scene for the given amount of seconds.
     *
     * If the scene reports that it would like to be rendered
     *
     * @param elapsed time in seconds, but never 0.0f
     * @param lastRendered how long ago we last rendered the scene
     * @return true if the scene has been rendered, false otherwise
     */
    @WorkerThread
    fun execute(elapsed: Float, lastRendered: Float): Boolean {
        val currentRenderMode = renderMode
        if (scene.execute(elapsed) || scene.isInvalidated()) {
            // scene has changed and would like to be rendered
            // without animations we render exactly when the scene wants to, but ideally it would be smart
            // enough to not have animations at all
            if (scene.showAnimations == AnimationType.Off) {
                requestRender()
            } else {
                // we switch to continuous render mode whenever the scene has changed
                if (currentRenderMode == RENDERMODE_WHEN_DIRTY)
                    renderMode = RENDERMODE_CONTINUOUSLY
            }

            return true
        } else {
            // and revert to when_dirty when we had no animations for 0.3s
            if (lastRendered >= 0.3f && currentRenderMode == RENDERMODE_CONTINUOUSLY)
                renderMode = RENDERMODE_WHEN_DIRTY

            return false
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            thread?.goDown = true
            thread?.interrupt()
            thread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        scene.effects.clear()
        thread = null
    }

    override fun onResume() {
        super.onResume()
        scene.clearEffects()
        if (thread == null) {
            thread = AnimateThread(scene, this::execute).also { it.start() }
        }
    }

    fun setScale(scale: Float) {
        this.scale = scale
        renderer.zoom = scale
        renderer.updateModelViewMatrix = true
    }

    fun getScale(): Float {
        return scale
    }
}