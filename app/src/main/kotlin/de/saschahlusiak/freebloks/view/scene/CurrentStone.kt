package de.saschahlusiak.freebloks.view.scene

import de.saschahlusiak.freebloks.model.*
import de.saschahlusiak.freebloks.theme.FeedbackType
import de.saschahlusiak.freebloks.utils.PointF
import de.saschahlusiak.freebloks.view.BoardRenderer
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.sin

class CurrentStone(private val scene: Scene) : SceneElement {
    enum class Status {
        IDLE, DRAGGING, ROTATING, FLIPPING_HORIZONTAL, FLIPPING_VERTICAL
    }

    var stone: Stone? = null
        private set

    internal var currentColor = StoneColor.White
        private set

    /**
     * Current position in board coordinates
     */
    internal var pos = PointF()

    /* has the stone been moved since it was touched? */
    var hasMoved = false

    /* is the stone commitable if it has not been moved? */
    private var canCommit = false

    internal var isValid = false
    private var stoneRelX = 0f
    private var stoneRelY = 0f
    internal var rotateAngle = 0f
        private set
    var status: Status = Status.IDLE
    internal var orientation = Orientation()
        private set
    val hoverHeightLow = 0.55f
    val hoverHeightHigh = 0.55f

    /**
     * Model coordinates of last touch down event, to detect taps
     */
    private var lastTouchPos = PointF()

    private fun moveTo(toX: Float, toY: Float): Boolean {
        var x = toX
        var y = toY
        if (stone == null) return false

        /* provide a weird but nice pseudo snapping feeling */
        if (scene.hasAnimations()) {
            var r = x - floor(x)
            if (r < 0.5f) {
                r *= 2.0f
                r = r * r * r * r
                r /= 2.0f
            } else {
                r = 1.0f - r
                r *= 2.0f
                r = r * r * r * r
                r /= 2.0f
                r = 1.0f - r
            }
            x = floor(x) + r
            r = y - floor(y)
            if (r < 0.5f) {
                r *= 2.0f
                r = r * r * r
                r /= 2.0f
            } else {
                r = 1.0f - r
                r *= 2.0f
                r = r * r * r
                r /= 2.0f
                r = 1.0f - r
            }
            y = floor(y) + r
        } else {
            x = floor(x + 0.5f)
            y = floor(y + 0.5f)
        }

        /* FIXME: lock stone inside 3 top walls, when board is always in the same orientation */
        /*
		for (int i = 0; i < stone.getSize(); i++)
			for (int j = 0; j < stone.getSize(); j++) {
				if (stone.getStoneField(j, i) == Stone.STONE_FIELD_ALLOWED) {
					if (x + i < 0)
						x = -i;
//					if (y + j < 0)
//						y = -j;

					if (x + i + 1 >= model.spiel.width)
						x = model.spiel.width - i - 1;
					if (y + j + 1 >= model.spiel.height)
						y = model.spiel.height - j - 1;
				}
			}
			*/

        return (floor(0.5f + pos.x) != floor(0.5f + x) || floor(pos.y + 0.5f) != floor(0.5f + y)).also {
            pos = PointF(x, y)
        }
    }

    @Synchronized
    override fun handlePointerDown(m: PointF): Boolean {
        status = Status.IDLE
        hasMoved = false
        canCommit = true
        val stone = stone

        if (stone != null) {
            lastTouchPos = scene.modelToBoard(m)
            stoneRelX = pos.x - lastTouchPos.x + stone.shape.size / 2
            stoneRelY = pos.y - lastTouchPos.y + stone.shape.size / 2

//			Log.d(tag, "rel = (" + stone_rel_x + " / " + stone_rel_y+ ")");
            if (abs(stoneRelX) <= overlayRadius + 3.0f && abs(stoneRelY) <= overlayRadius + 3.0f) {
                if (abs(stoneRelX) > overlayRadius - 1.5f && abs(stoneRelY) < 2.5f ||
                    abs(stoneRelX) < 2.5f && abs(stoneRelY) > overlayRadius - 1.5f) {
                    status = Status.ROTATING
                    rotateAngle = 0.0f
                } else {
                    status = Status.DRAGGING
                }
                return true
            }
        }

        return false
    }

    @Synchronized
    override fun handlePointerMove(m: PointF): Boolean {
        if (status == Status.IDLE) return false
        val stone = stone ?: return false
        val fieldPoint = scene.modelToBoard(m)
        if (status == Status.DRAGGING) {
            val THRESHOLD = 1.0f
            val x = fieldPoint.x + stoneRelX - stone.shape.size / 2
            val y = fieldPoint.y + stoneRelY - stone.shape.size / 2
            if (!hasMoved && abs(lastTouchPos.x - fieldPoint.x) < THRESHOLD && abs(lastTouchPos.y - fieldPoint.y) < THRESHOLD)
                return true
            val mv = snap(x, y, false)
            hasMoved = hasMoved or mv
            if (mv || scene.hasAnimations()) {
                scene.invalidate()
            }
        }
        if (status == Status.ROTATING) {
            val rx = pos.x - fieldPoint.x + stone.shape.size / 2
            val ry = pos.y - fieldPoint.y + stone.shape.size / 2
            val a1 = atan2(stoneRelY, stoneRelX)
            val a2 = atan2(ry, rx)
            rotateAngle = (a1 - a2) * 180.0f / Math.PI.toFloat()
            if (abs(rx) + abs(ry) < overlayRadius * 0.9f && abs(rotateAngle) < 25.0f) {
                rotateAngle = 0.0f
                status = if (abs(stoneRelY) < 3) Status.FLIPPING_HORIZONTAL else Status.FLIPPING_VERTICAL
            }
            scene.invalidate()
        }
        if (status == Status.FLIPPING_HORIZONTAL) {
            val rx = pos.x - fieldPoint.x + stone.shape.size / 2
            var p: Float
            p = (stoneRelX - rx) / (stoneRelX * 2.0f)
            if (p < 0) p = 0f
            if (p > 1) p = 1f
            if (stoneRelX > 0) p = -p
            rotateAngle = p * 180
            scene.invalidate()
        }
        if (status == Status.FLIPPING_VERTICAL) {
            val ry = pos.y - fieldPoint.y + stone.shape.size / 2
            var p: Float
            p = (stoneRelY - ry) / (stoneRelY * 2.0f)
            if (p < 0) p = 0f
            if (p > 1) p = 1f
            if (stoneRelY < 0) p = -p
            rotateAngle = p * 180
            scene.invalidate()
        }
        return true
    }

    private fun rotateLef() {
        val stone = stone ?: return
        orientation = orientation.rotatedLeft(stone.shape.rotatable)
    }

    private fun rotateRight() {
        val stone = stone ?: return
        orientation = orientation.rotatedRight(stone.shape.rotatable)
    }

    private fun mirrorOverX() {
        val stone = stone ?: return
        if (stone.shape.mirrorable === Mirrorable.Not) return
        orientation = orientation.mirroredVertically()
    }

    private fun mirrorOverY() {
        if (stone?.shape?.mirrorable === Mirrorable.Not) return
        orientation = orientation.mirroredHorizontally()
    }

    private fun isValidTurn(x: Float, y: Float): Boolean {
        val stone = stone ?: return false
        if (!scene.game.isLocalPlayer()) return false
        return scene.board.isValidTurn(
            stone.shape,
            scene.game.currentPlayer,
            floor(y + 0.5f).toInt(),
            floor(x + 0.5f).toInt(),
            orientation
        )
    }

    private fun snap(x: Float, y: Float, forceSound: Boolean): Boolean {
        val hasMoved: Boolean
        if (!scene.snapAid) {
            hasMoved = moveTo(x, y)
            isValid = isValidTurn(x, y)
            if (isValid && (hasMoved || forceSound)) {
                scene.playSound(FeedbackType.Snap, volume = 0.2f)
            }
            return hasMoved
        }
        if (isValidTurn(x, y)) {
            isValid = true
            hasMoved = moveTo(floor(x + 0.5f), floor(y + 0.5f))
            if (hasMoved || forceSound) {
                scene.playSound(FeedbackType.Snap, volume = 0.2f)
            }
            return hasMoved
        }
        for (i in -1..1) for (j in -1..1) {
            if (isValidTurn(x + i, y + j)) {
                isValid = true
                hasMoved = moveTo(floor(0.5f + x + i.toFloat()), floor(0.5f + y + j.toFloat()))
                if (hasMoved) {
                    scene.playSound(FeedbackType.Snap, volume = 0.2f)
                }
                return hasMoved
            }
        }
        isValid = false
        return moveTo(x, y)
    }

    fun asTurn(): Turn? {
        val stone = stone ?: return null
        return Turn(
            scene.game.currentPlayer,
            stone.shape.number,
            floor(pos.y + 0.5f).toInt(),
            floor(pos.x + 0.5f).toInt(),
            orientation
        )
    }

    @Synchronized
    override fun handlePointerUp(m: PointF) {
        val stone = stone

        if (status == Status.DRAGGING && stone != null) {
            val fieldPoint = scene.modelToBoard(m)

            val x = floor(0.5f + fieldPoint.x + stoneRelX - stone.shape.size.toFloat() / 2f)
            val y = floor(0.5f + fieldPoint.y + stoneRelY - stone.shape.size.toFloat() / 2f)

            val wheelPoint = scene.wheel.modelToWheel(m)

            if (wheelPoint.y < -2.0f && hasMoved) {
                scene.wheel.currentStone = stone
                status = Status.IDLE
                this.stone = null
            } else if (canCommit && !hasMoved) {
                val turn = asTurn()
                if (turn != null && scene.commitCurrentStone(turn)) {
                    status = Status.IDLE
                    this.stone = null
                    isValid = false
                    scene.wheel.currentStone = null
                }
            } else if (hasMoved) {
                snap(x, y, false)
            }
        }
        if (status == Status.ROTATING) {
            while (rotateAngle < -45.0f) {
                rotateAngle += 90.0f
                rotateRight()
            }
            while (rotateAngle > 45.0f) {
                rotateAngle -= 90.0f
                rotateLef()
            }
            rotateAngle = 0.0f
            snap(pos.x, pos.y, true)
        }
        if (status == Status.FLIPPING_HORIZONTAL) {
            if (abs(rotateAngle) > 90.0f) mirrorOverY()
            rotateAngle = 0.0f
            snap(pos.x, pos.y, true)
        }
        if (status == Status.FLIPPING_VERTICAL) {
            if (abs(rotateAngle) > 90.0f) mirrorOverX()
            rotateAngle = 0.0f
            snap(pos.x, pos.y, true)
        }
        status = Status.IDLE
    }

    @Synchronized
    fun stopDragging() {
        stone = null
        currentColor = StoneColor.White
        status = Status.IDLE
    }

    @Synchronized
    fun startDragging(fieldPoint: PointF?, stone: Stone, orientation: Orientation, color: StoneColor) {
        this.stone = stone
        currentColor = color
        status = Status.DRAGGING
        hasMoved = false
        canCommit = false
        /* TODO: set this to about 3 above the touch event to make stone visible
		 * when started dragging */
        stoneRelX = 0f
        stoneRelY = 0f
        this.orientation = orientation
        if (fieldPoint != null) {
            val x = floor(0.5f + fieldPoint.x + stoneRelX - stone.shape.size.toFloat() / 2.0f).toInt()
            val y = floor(0.5f + fieldPoint.y + stoneRelY - stone.shape.size.toFloat() / 2.0f).toInt()
            moveTo(x.toFloat(), y.toFloat())
        }
        isValid = isValidTurn(pos.x, pos.y)
    }

    override fun execute(elapsed: Float) = false

    companion object {
        private val tag = CurrentStone::class.java.simpleName
        internal const val overlayRadius = 6.0f
    }
}