package de.saschahlusiak.freebloks.view.scene

import android.graphics.PointF
import de.saschahlusiak.freebloks.model.GameMode
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow

class BoardObject(private val scene: Scene, var lastSize: Int) : SceneElement {
    /**
     *  the "center" position of the board, usually the first local
     */
    @JvmField var centerPlayer = 0

    /**
     * The current display angle of the board around the Y axis
     */
    var currentAngle = 0.0f

    /**
     * When rotating and the pointer is released, this is the target angle to "settle" the board to.
     */
    private var targetAngle = 0f

    /**
     * True if the user is currently touching and rotating the board
     */
    private var rotating = false

    /**
     * If true, the board will automatically rotate in [execute] when the game is finished.
     */
    private var autoRotate = true

    /**
     * The last angle of the last touch event to the center of the board.
     * Used to calculate the new [currentAngle] when rotating.
     */
    private var lastTouchAngle = 0f

    /**
     * Stores the coordinates of the first "down" event, to detect whether we have moved at all when handling the "up" event
     */
    private val originalTouchPoint = PointF()

    /**
     * TODO: document me
     */
    private var lastDetailsPlayer = -1

    /**
     * FIXME: what is this compared to currentAngle?
     * FIXME: Ideally this would be always 0
     * @return the base angle for the camera, to focus on the center player
     */
    fun getCameraAngle() = if (centerPlayer < 0) 0.0f else -90.0f * centerPlayer.toFloat()

    fun updateDetailsPlayer() {
        val p = if (currentAngle > 0) (currentAngle.toInt() + 45) / 90 else (currentAngle.toInt() - 45) / 90
        lastDetailsPlayer = if (currentAngle < 10.0f && currentAngle >= -10.0f) -1 else (centerPlayer + p + 4) % 4
        val game = scene.game
        if (game.gameMode === GameMode.GAMEMODE_2_COLORS_2_PLAYERS || game.gameMode === GameMode.GAMEMODE_DUO || game.gameMode === GameMode.GAMEMODE_JUNIOR) {
            if (lastDetailsPlayer == 1) lastDetailsPlayer = 0
            if (lastDetailsPlayer == 3) lastDetailsPlayer = 2
        }
        scene.setShowPlayerOverride(showDetailsPlayer, lastDetailsPlayer >= 0)
    }

    /**
     * returns the number of the player whose seeds are to be shown
     *
     * @return -1 if seeds are disabled
     * detail player if board is rotated
     * current player, if local
     * -1 otherwise
     */
    val showSeedsPlayer: Int
        get() {
            if (!scene.showSeeds) return -1
            if (lastDetailsPlayer >= 0) return lastDetailsPlayer
            if (scene.game.isFinished) return centerPlayer
            return if (scene.game.isLocalPlayer()) scene.game.currentPlayer else -1
        }

    /**
     * Returns the player, whose details are to be shown.
     *
     * @return player, 0..3, never -1
     */
    // TODO: would be nice to show the last current local player instead of the center one
    //       needs caching of previous local player */
    private val showDetailsPlayer: Int
        get() {
            if (lastDetailsPlayer >= 0) return lastDetailsPlayer
            if (!scene.game.isStarted) return -1
            if (scene.game.isFinished) return centerPlayer
            return if (scene.game.currentPlayer >= 0) scene.game.currentPlayer else centerPlayer
        }

    /**
     * The player that should be shown on the wheel.
     *
     * @return number between 0 and 3
     */
    // TODO: would be nice to show the last current local player instead of the center one
	//       needs caching of previous local player */
    val showWheelPlayer: Int
        get() {
            if (lastDetailsPlayer >= 0) return lastDetailsPlayer
            if (scene.game.isFinished) {
                return centerPlayer
            }
            return if (scene.game.isLocalPlayer() || scene.showOpponents) scene.game.currentPlayer else centerPlayer
        }

    override fun handlePointerDown(m: PointF): Boolean {
        lastTouchAngle = atan2(m.y, m.x)
        originalTouchPoint.x = m.x
        originalTouchPoint.y = m.y
        rotating = true
        autoRotate = false
        return true
    }

    override fun handlePointerMove(m: PointF): Boolean {
        if (!rotating) return false

        scene.currentStone.stopDragging()
        val newAngle = atan2(m.y, m.x)
        currentAngle += (lastTouchAngle - newAngle) / Math.PI.toFloat() * 180.0f
        lastTouchAngle = newAngle
        while (currentAngle >= 180.0f) currentAngle -= 360.0f
        while (currentAngle <= -180.0f) currentAngle += 360.0f

        updateDetailsPlayer()
        val s = showWheelPlayer
        if (scene.wheel.currentPlayer != s) {
            scene.wheel.update(s)
        }

        scene.invalidate()
        return true
    }

    override fun handlePointerUp(m: PointF): Boolean {
        if (!rotating) return false

        if (abs(m.x - originalTouchPoint.x) < 1 && abs(m.y - originalTouchPoint.y) < 1) {
            resetRotation()
        } else {
            targetAngle = if (currentAngle > 0)
                ((currentAngle.toInt() + 45) / 90 * 90).toFloat()
            else
                ((currentAngle.toInt() - 45) / 90 * 90).toFloat()
        }
        rotating = false

        return false
    }

    fun resetRotation() {
        targetAngle = 0.0f
        autoRotate = true
        lastDetailsPlayer = -1
    }

    override fun execute(elapsed: Float): Boolean {
        if (!rotating && scene.game.isFinished && autoRotate) {
            val autoRotateSpeed = 25.0f // degrees / second
            currentAngle += elapsed * autoRotateSpeed

            while (currentAngle >= 180.0f) currentAngle -= 360.0f
            while (currentAngle <= -180.0f) currentAngle += 360.0f

            updateDetailsPlayer()
            val s = showWheelPlayer
            if (scene.wheel.currentPlayer != s) {
                scene.wheel.update(s)
            }

            return true
        } else if (!rotating && abs(currentAngle - targetAngle) > 0.05f) {
            val snapSpeed = 10.0f + abs(currentAngle - targetAngle).pow(0.65f) * 30.0f

            var lp = scene.wheel.currentPlayer
            if (currentAngle - targetAngle > 0.1f) {
                currentAngle -= elapsed * snapSpeed
                if (currentAngle - targetAngle <= 0.1f) {
                    currentAngle = targetAngle
                    lp = -1
                }
            }
            if (currentAngle - targetAngle < -0.1f) {
                currentAngle += elapsed * snapSpeed
                if (currentAngle - targetAngle >= -0.1f) {
                    currentAngle = targetAngle
                    lp = -1
                }
            }
            updateDetailsPlayer()
            val s = showWheelPlayer
            if (lp != s) {
                scene.wheel.update(s)
            }
            return true
        }
        return false
    }
}