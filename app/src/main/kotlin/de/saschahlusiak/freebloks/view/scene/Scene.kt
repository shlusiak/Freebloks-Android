package de.saschahlusiak.freebloks.view.scene

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.model.*
import de.saschahlusiak.freebloks.theme.FeedbackProvider
import de.saschahlusiak.freebloks.theme.FeedbackType
import de.saschahlusiak.freebloks.utils.PointF
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

interface SceneDelegate {
    /**
     * Set the override of the player to show, when rotating the board.
     *
     * @param player the new player to show
     * @param isRotated whether the board is rotated or not
     */
    fun setSheetPlayer(player: Int, isRotated: Boolean)

    /**
     * The [CurrentStone] would like to commit this stone
     */
    fun commitCurrentStone(turn: Turn)
}

/**
 * This scene model is owned by the [Freebloks3DView] and
 * encapsulates 3D objects and effects and sounds.
 *
 * This is a View class and is allowed to have references to the current View and Activity.
 */
class Scene(
    private val delegate: SceneDelegate?,
    var intro: Intro?,
    private val sounds: FeedbackProvider?
) : ArrayList<SceneElement>(), SceneElement {

    var game = Game()
    var board = game.board
    val wheel = Wheel(this)
    val currentStone = CurrentStone(this)
    val boardObject = BoardObject(this, Board.DEFAULT_BOARD_SIZE)
    val effects = ArrayList<Effect>()
    var showSeeds = false
    var showOpponents = false
    var snapAid = false
    var showAnimations = AnimationType.Full
    var verticalLayout = true

    /**
     * If the scene has been invalidated since the last render pass and require rendering again
     */
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

    init {
        // events are processed in this order
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

    fun setGameClient(client: GameClient) {
        game = client.game
        board = game.board
        boardObject.resetRotation()
        wheel.update(boardObject.showWheelPlayer)
        boardObject.updateDetailsPlayer()
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
    override fun handlePointerUp(m: PointF) {
        for (i in 0 until size) get(i).handlePointerUp(m)
        invalidate()
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
        if (!game.isLocalPlayer()) return false
        if (!game.board.isValidTurn(turn)) return false

        delegate?.commitCurrentStone(turn)

        if (hasAnimations()) {
            val set = EffectSet()
            set.add(ShapeRollEffect(this, turn, currentStone.hoverHeightHigh, -15.0f))
            set.add(ShapeFadeEffect(this, turn, 1.0f))
            addEffect(set)
        }

        playSound(FeedbackType.StoneHasBeenSet, 1.0f, 0.9f + Random.nextFloat() * 0.2f)

        return true
    }

    fun getPlayerColor(player: Int) = game.gameMode.colorOf(player)

    fun playSound(sound: FeedbackType, volume: Float = 1.0f, speed: Float = 1.0f) = sounds?.play(sound, volume, speed)

    fun setShowPlayerOverride(player: Int, isRotated: Boolean) = delegate?.setSheetPlayer(player, isRotated)

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
        return PointF(
            point.x / (BoardRenderer.stoneSize * 2.0f) + 0.5f * (board.width - 1).toFloat(),
            point.y / (BoardRenderer.stoneSize * 2.0f) + 0.5f * (board.height - 1).toFloat()
        )
    }

    /**
     * Rotates p from board coordinates to screen coordinates.
     *
     * Relative board coordinates: yellow starting point is 0/0, blue starting point is 0/19
     * unified coordinates: bottom left corner is always 0/0
     *
     * @param p input and output
     */
    fun boardToScreenOrientation(p: PointF) = when (basePlayer) {
        1 -> PointF(p.y, p.x)
        2 -> p.copy(x = board.width - p.x - 1)
        3 -> PointF(board.height - p.y - 1, board.width - p.x - 1)
        else -> p.copy(y = board.height - p.y - 1)
    }
}