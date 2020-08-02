package de.saschahlusiak.freebloks.view.scene

import android.graphics.PointF
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.game.FreebloksActivityViewModel
import de.saschahlusiak.freebloks.model.*
import de.saschahlusiak.freebloks.theme.FeedbackProvider
import de.saschahlusiak.freebloks.theme.FeedbackType
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.Freebloks3DView
import de.saschahlusiak.freebloks.view.effects.Effect
import de.saschahlusiak.freebloks.view.effects.EffectSet
import de.saschahlusiak.freebloks.view.effects.ShapeFadeEffect
import de.saschahlusiak.freebloks.view.effects.ShapeRollEffect
import de.saschahlusiak.freebloks.view.FreebloksRenderer
import de.saschahlusiak.freebloks.view.scene.intro.Intro
import java.util.*
import kotlin.random.Random

/**
 * This scene model is owned by the [Freebloks3DView] and
 * encapsulates 3D objects and effects and sounds.
 *
 * This is a View class and is allowed to have references to the current View and Activity.
 */
class Scene(
    private val viewModel: FreebloksActivityViewModel,
    val sounds: FeedbackProvider
) : ArrayList<SceneElement>(), SceneElement {

    constructor(viewModel: FreebloksActivityViewModel): this(
        viewModel,
        viewModel.sounds
    )

    private var client: GameClient? = null

    var board = Board()
    var game = Game()
    val wheel = Wheel(this)
    val currentStone = CurrentStone(this)
    val boardObject = BoardObject(this, Board.DEFAULT_BOARD_SIZE)
    val effects = ArrayList<Effect>()
    var showSeeds = false
    var showOpponents = false
    var snapAid = false
    var showAnimations = AnimationType.Full
    var verticalLayout = true

    private var invalidated = false

    /**
     * The "center" position of the board, usually the first local player.
     *
     * This controls the base position of the camera of the board.
     */
    var basePlayer = 0
        set(value) {
            field = value
            baseAngle = value * -90.0f
        }

    var baseAngle = 0f


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

        playSound(FeedbackType.StoneHasBeenSet, 1.0f, 0.9f + Random.nextFloat() * 0.2f)

        client.setStone(turn)

        return true
    }

    fun getPlayerColor(player: Int) = game.gameMode.colorOf(player)

    fun playSound(sound: FeedbackType, volume: Float = 1.0f, speed: Float = 1.0f) = sounds.play(sound, volume, speed)

    fun setShowPlayerOverride(player: Int, isRotated: Boolean) = viewModel.setSheetPlayer(player, isRotated)

    /**
     * In model coordinates, the center of the board is (0/0).
     *
     * The other coordinates depend on the direction of the camera and because the board is rotated, the blue corner
     * is always about being ~(-10/10) and yellow being ~(-10/-10), on a 20x20 board.
     *
     * This moves the center of this coordinate system to the yellow corner (top left, 0/0).
     *
     * Converts a point from model coordinates to absolute board coordinates, with blue starting point being 0/19
     * and yellow starting point being 0/0.
     *
     * @see [FreebloksRenderer.windowToModel]
     *
     * @param point, input will be modified
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
     * Rotates p from board coordinates to screen coordinates.
     *
     * Relative board coordinates: yellow starting point is 0/0, blue starting point is 0/19
     * unified coordinates: bottom left corner is always 0/0
     *
     * @param p input and output
     */
    fun boardToScreenOrientation(p: PointF) {
        val tmp: Float
        when (basePlayer) {
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