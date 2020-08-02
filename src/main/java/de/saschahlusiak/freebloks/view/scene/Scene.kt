package de.saschahlusiak.freebloks.view.scene

import android.graphics.PointF
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.game.FreebloksActivityViewModel
import de.saschahlusiak.freebloks.model.*
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.Freebloks3DView
import de.saschahlusiak.freebloks.view.effects.Effect
import de.saschahlusiak.freebloks.view.effects.EffectSet
import de.saschahlusiak.freebloks.view.effects.ShapeFadeEffect
import de.saschahlusiak.freebloks.view.effects.ShapeRollEffect
import de.saschahlusiak.freebloks.view.scene.intro.Intro
import java.util.*

/**
 * This scene model is owned by the [Freebloks3DView] and
 * encapsulates 3D objects and effects and sounds.
 *
 * This is a View class and is allowed to have references to the current View and Activity.
 */
class Scene(
    private val viewModel: FreebloksActivityViewModel
) : ArrayList<SceneElement>(), SceneElement {

    private var client: GameClient? = null

    var board = Board()
    var game = Game()
    val wheel = Wheel(this)
    val currentStone = CurrentStone(this)
    val boardObject = BoardObject(this, Board.DEFAULT_BOARD_SIZE)
    val effects = ArrayList<Effect>()
    val soundPool = viewModel.sounds
    var showSeeds = false
    var showOpponents = false
    var snapAid = false
    var showAnimations = AnimationType.Full
    var verticalLayout = true

    private var invalidated = false

    /**
     * The intro is part of the scene but owned by the viewModel
     *
     * @return current intro
     */
    val intro: Intro?
        get() = viewModel.intro

    init {
        add(currentStone)
        add(wheel)
        add(boardObject)
    }

    fun hasAnimations(): Boolean {
        return showAnimations != AnimationType.Off
    }

    fun reset() {
        currentStone.stopDragging()
        boardObject.resetRotation()
    }

    fun setGameClient(client: GameClient?) {
        this.client = client
        if (client != null) {
            game = client.game
            board = game.board
            boardObject.resetRotation()
            wheel.update(boardObject.showWheelPlayer)
            boardObject.updateDetailsPlayer()
        }
    }

    @Synchronized
    fun invalidate() {
        invalidated = true
    }

    @Synchronized
    fun isInvalidated(): Boolean {
        return invalidated.also {
            invalidated = false
        }
    }

    @UiThread
    override fun handlePointerDown(m: PointF): Boolean {
        val intro = intro
        if (intro != null) {
            intro.handlePointerDown()
            invalidate()
            return true
        }
        for (i in 0 until size) if (get(i).handlePointerDown(m)) {
            invalidate()
            return true
        }
        return false
    }

    @UiThread
    override fun handlePointerMove(m: PointF): Boolean {
        for (i in 0 until size) if (get(i).handlePointerMove(m)) return true
        return false
    }

    @UiThread
    override fun handlePointerUp(m: PointF): Boolean {
        for (i in 0 until size) if (get(i).handlePointerUp(m)) {
            invalidate()
            return true
        }
        return false
    }

    @WorkerThread
    override fun execute(elapsed: Float): Boolean {
        var redraw = false
        val intro = intro

        if (intro != null) {
            intro.execute(elapsed)
            return true
        }

        synchronized(effects) {
            var i = 0
            while (i < effects.size) {
                redraw = redraw or effects[i].execute(elapsed)
                if (effects[i].isDone()) {
                    effects.removeAt(i)
                    redraw = true
                } else i++
            }
        }

        for (i in 0 until size) {
            redraw = redraw or get(i).execute(elapsed)
        }

        return redraw
    }

    fun addEffect(effect: Effect) {
        synchronized(effects) { effects.add(effect) }
    }

    fun clearEffects() {
        synchronized(effects) { effects.clear() }
    }

    fun commitCurrentStone(turn: Turn): Boolean {
        val client = client ?: return false

        if (!client.game.isLocalPlayer()) return false
        if (!client.game.board.isValidTurn(turn)) return false

        if (hasAnimations()) {
            val set = EffectSet()
            set.add(ShapeRollEffect(this, turn, currentStone.hoverHeightHigh, -15.0f))
            set.add(ShapeFadeEffect(this, turn, 1.0f))
            addEffect(set)
        }

        soundPool.play(soundPool.SOUND_CLICK1, 1.0f, 0.9f + Math.random().toFloat() * 0.2f)
        viewModel.vibrate(Global.VIBRATE_SET_STONE.toLong())
        client.setStone(turn)

        return true
    }

    fun getPlayerColor(player: Int) = game.gameMode.colorOf(player)

    fun vibrate(milliseconds: Long) = viewModel.vibrate(milliseconds)

    fun setShowPlayerOverride(player: Int, isRotated: Boolean) = viewModel.setSheetPlayer(player, isRotated)

    /**
     * Converts a point from model coordinates to (non-uniformed) board coordinates.
     * The top-left corner is 0/0, the blue starting point is 0/19.
     *
     * @param point
     * @return point
     */
    fun modelToBoard(point: PointF): PointF {
        point.x = point.x / (BoardRenderer.stoneSize * 2.0f)
        point.y = point.y / (BoardRenderer.stoneSize * 2.0f)
        point.x = point.x + 0.5f * (board.width - 1).toFloat()
        point.y = point.y + 0.5f * (board.height - 1).toFloat()
        return point
    }

    /**
     * converts p from relative board coordinates, to rotated board coordinates
     * relative board coordinates: yellow starting point is 0/0, blue starting point is 0/19
     * unified coordinates: bottom left corner is always 0/0
     * @param p
     */
    fun boardToUnified(p: PointF) {
        val tmp: Float
        when (boardObject.basePlayer) {
            0 -> p.y = board.height - p.y - 1
            1 -> {
                tmp = p.x
                p.x = p.y
                p.y = tmp
            }
            2 -> p.x = board.width - p.x - 1
            3 -> {
                tmp = p.y
                p.y = board.width - p.x - 1
                p.x = board.height - tmp - 1
            }
            else -> p.y = board.height - p.y - 1
        }
    }
}