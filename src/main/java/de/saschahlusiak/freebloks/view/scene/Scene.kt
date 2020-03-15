package de.saschahlusiak.freebloks.view.scene

import android.graphics.PointF
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.game.FreebloksActivityViewModel
import de.saschahlusiak.freebloks.model.Board
import de.saschahlusiak.freebloks.model.Game
import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.view.Freebloks3DView
import de.saschahlusiak.freebloks.view.effects.Effect
import de.saschahlusiak.freebloks.view.effects.EffectSet
import de.saschahlusiak.freebloks.view.effects.ShapeFadeEffect
import de.saschahlusiak.freebloks.view.effects.ShapeRollEffect
import java.util.*

/**
 * This scene model is owned by the [Freebloks3DView] and
 * encapsulates 3D objects and renderable effects and sounds.
 *
 * This is a View class and is allowed to have references to the current View and Activity.
 */
class Scene(
    private val view: Freebloks3DView,
    private val viewModel: FreebloksActivityViewModel
) : ArrayList<ViewElement>(), ViewElement {

    companion object {
        const val ANIMATIONS_FULL = 0
        const val ANIMATIONS_HALF = 1
        const val ANIMATIONS_OFF = 2
    }

    @JvmField var board = Board()
    @JvmField var game = Game()

    @JvmField val wheel = Wheel(this)
    @JvmField val currentStone = CurrentStone(this)
    private var client: GameClient? = null
    @JvmField val boardObject = BoardObject(this, Board.DEFAULT_BOARD_SIZE)
    @JvmField val effects = ArrayList<Effect>()
    @JvmField val soundPool = viewModel.sounds
    @JvmField var showSeeds = false
    @JvmField var showOpponents = false
    @JvmField var snapAid = false
    @JvmField var showAnimations = 0
    @JvmField var verticalLayout = true
    @JvmField var redraw = false

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
        return showAnimations != ANIMATIONS_OFF
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

    @UiThread
    override fun handlePointerDown(m: PointF): Boolean {
        redraw = false
        val intro = intro
        if (intro != null) {
            redraw = intro.handlePointerDown(m)
            return redraw
        }
        for (i in 0 until size) if (get(i).handlePointerDown(m)) return redraw
        return redraw
    }

    @UiThread
    override fun handlePointerMove(m: PointF): Boolean {
        redraw = false
        for (i in 0 until size) if (get(i).handlePointerMove(m)) return redraw
        return redraw
    }

    @UiThread
    override fun handlePointerUp(m: PointF): Boolean {
        redraw = false
        for (i in 0 until size) if (get(i).handlePointerUp(m)) return redraw
        return redraw
    }

    @WorkerThread
    override fun execute(elapsed: Float): Boolean {
        var redraw = false
        val intro = intro

        if (intro != null) {
            redraw = intro.execute(elapsed)
            return redraw
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
            set.add(ShapeRollEffect(this, turn, currentStone.hover_height_high, -15.0f))
            set.add(ShapeFadeEffect(this, turn, 1.0f))
            addEffect(set)
        }

        soundPool.play(soundPool.SOUND_CLICK1, 1.0f, 0.9f + Math.random().toFloat() * 0.2f)
        viewModel.vibrate(Global.VIBRATE_SET_STONE.toLong())
        client.setStone(turn)

        return true
    }

    @Deprecated("use Global.getPlayerColor instead")
    fun getPlayerColor(player: Int) = Global.getPlayerColor(player, game.gameMode)

    fun vibrate(milliseconds: Long) = viewModel.vibrate(milliseconds)

    fun setShowPlayerOverride(player: Int, isRotated: Boolean) = viewModel.setSheetPlayer(player, isRotated)

    fun requestRender() = view.requestRender()
}