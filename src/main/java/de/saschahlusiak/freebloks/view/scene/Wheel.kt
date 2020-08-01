package de.saschahlusiak.freebloks.view.scene

import android.graphics.PointF
import android.os.Handler
import androidx.annotation.UiThread
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.model.Orientation
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.model.Stone
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.FreebloksRenderer
import javax.microedition.khronos.opengles.GL11
import kotlin.math.abs
import kotlin.math.pow

class Wheel(private val scene: Scene) : SceneElement {

    private enum class Status {
        IDLE, SPINNING, FLINGING
    }

    /* returns the currently highlighted stone in the wheel */
    var currentStone: Stone? = null

    private var currentOffset = 0f

    /* the offset on last touch down; rotate back to here when idle */
    private var lastOffset = 0f

    /* the maximum offset till reaching the right end */
    private var maxOffset = 0f
    private var originalX = 0f
    private var originalY = 0f
    private var flingSpeed = 0f
    private var lastFlingOffset = 0f
    private var status = Status.IDLE
    private val stones = mutableListOf<Stone>()

    /* returns the player number currently shown in the wheel (aka. last call of update) */
    var currentPlayer: Int = -1 /* the currently shown player */
        private set

    private var movesLeft = false
    private val lastPointerLocation = PointF()
    private val tmp = PointF()
    private val handler = Handler()

    private val hapticTimerRunnable = object : Runnable {
        override fun run() {
            val stone = currentStone ?: return
            if (status != Status.SPINNING) return
            if (abs(currentOffset - lastOffset) > 3.0f) return

            if (!scene.game.isLocalPlayer()) return
            tmp.x = lastPointerLocation.x
            tmp.y = lastPointerLocation.y
            scene.modelToBoard(tmp)

            if (!scene.soundPool.play(scene.soundPool.SOUND_CLICK2, 1.0f, 1f))
                scene.vibrate(Global.VIBRATE_START_DRAGGING)

            showStone(stone.shape.number)
            scene.currentStone.startDragging(tmp, stone, Orientation.Default, scene.getPlayerColor(currentPlayer))
            scene.currentStone.hasMoved = true
            scene.boardObject.resetRotation()
            status = Status.IDLE

            scene.invalidate()
        }
    }

    @Synchronized
    fun update(player: Int) {
        stones.clear()
        if (player < 0) return
        val p = scene.board.getPlayer(player)
        movesLeft = p.numberOfPossibleTurns > 0
        for (i in 0 until Shape.COUNT) {
            val s = p.getStone(i)
            if (s.isAvailable()) stones.add(s)
        }
        currentStone = null
        maxOffset = ((stones.size - 1) / 2).toFloat() * STONE_SPACING
        if (stones.size > 0) rotateTo((stones.size + 1) / 2 - 2)
        currentPlayer = player
    }

    private fun rotateTo(column: Int) {
        lastOffset = column.toFloat() * STONE_SPACING
        if (lastOffset < 0.0f) lastOffset = 0.0f
        if (lastOffset > maxOffset) lastOffset = maxOffset
        if (!scene.hasAnimations()) currentOffset = lastOffset
    }

    private fun getStonePositionInWheel(stone: Int): Int {
        for (i in stones.indices) if (stones[i].shape.number == stone) return i
        return 0
    }

    /* makes sure the given stone is visible in the wheel */
    fun showStone(stone: Int) {
        rotateTo(getStonePositionInWheel(stone) / 2)
    }

    @UiThread
    @Synchronized
    override fun handlePointerDown(m: PointF): Boolean {
        status = Status.IDLE
        lastOffset = currentOffset
        lastFlingOffset = currentOffset
        flingSpeed = 0.0f
        lastPointerLocation.x = m.x
        lastPointerLocation.y = m.y
        tmp.x = m.x
        tmp.y = m.y
        scene.modelToBoard(tmp)
        scene.boardToUnified(tmp)
        if (!scene.verticalLayout) {
            val t = tmp.x
            tmp.x = tmp.y
            tmp.y = scene.board.width - t - 1
        }
        originalX = tmp.x
        originalY = tmp.y
        if (tmp.y > 0) return false
        val row = (-(tmp.y + 2.0f) / 6.7f).toInt()
        val col = ((tmp.x - scene.board.width.toFloat() / 2.0f + lastOffset) / STONE_SPACING + 0.5f).toInt()
        if (!scene.game.isLocalPlayer() || scene.game.currentPlayer != currentPlayer) {
            status = Status.SPINNING
            return true
        }

        val nr = col * 2 + row
        currentStone = if (nr < 0 || nr >= stones.size || row > 1) null else stones[nr]
        if (currentStone?.isAvailable() == false) currentStone = null else if (currentStone != null) {
            /* we tapped on a stone; start timer */
            handler.removeCallbacks(hapticTimerRunnable)
            if (scene.currentStone.stone != null && scene.currentStone.stone != currentStone) {
                scene.soundPool.play(scene.soundPool.SOUND_CLICK2, 1.0f, 1f)
                status = Status.SPINNING
            } else {
                status = Status.SPINNING
                handler.postDelayed(hapticTimerRunnable, 500)
            }
        } else status = Status.SPINNING

        return true
    }

    @UiThread
    @Synchronized
    override fun handlePointerMove(m: PointF): Boolean {
        if (status != Status.SPINNING) return false
        tmp.x = m.x
        tmp.y = m.y
        scene.modelToBoard(tmp)
        scene.boardToUnified(tmp)
        if (!scene.verticalLayout) {
            val t = tmp.x
            tmp.x = tmp.y
            tmp.y = scene.board.width - t - 1
        }

        /* everything underneath row 0 spins the wheel */
        var offset = (originalX - tmp.x) * 1.7f
        offset *= 1.0f / (1.0f + Math.abs(originalY - tmp.y) / 2.3f)
        currentOffset += offset
        if (currentOffset < 0.0f) currentOffset = 0.0f
        if (currentOffset > maxOffset) currentOffset = maxOffset
        originalX = tmp.x
        if (!scene.game.isLocalPlayer() || scene.game.currentPlayer != currentPlayer) {
            scene.invalidate()
            return true
        }

        val currentStone = currentStone

        /* if the wheel is moved too far, deselect highlighted stone */
        if (abs(currentOffset - lastOffset) >= MAX_STONE_DRAG_DISTANCE) {
            this.currentStone = null
        }

        if (currentStone != null && (tmp.y >= 0.0f || abs(tmp.y - originalY) >= 3.5f)) {
            tmp.x = m.x
            tmp.y = m.y
            scene.modelToBoard(tmp)
            showStone(currentStone.shape.number)
            if (scene.currentStone.stone != currentStone) scene.soundPool.play(scene.soundPool.SOUND_CLICK2, 1.0f, 1f)
            scene.currentStone.startDragging(tmp, currentStone, Orientation.Default, scene.getPlayerColor(currentPlayer))
            status = Status.IDLE
            scene.boardObject.resetRotation()
        }

        scene.invalidate()
        return true
    }

    @UiThread
    override fun handlePointerUp(m: PointF): Boolean {
        handler.removeCallbacks(hapticTimerRunnable)
        if (status == Status.SPINNING) {
            val currentStone = currentStone
            if (currentStone != null && scene.currentStone.stone != currentStone && Math.abs(lastOffset - currentOffset) < 0.5f) {
                if (scene.currentStone.stone != null) scene.currentStone.startDragging(null, currentStone, Orientation.Default, scene.getPlayerColor(currentPlayer))
                scene.currentStone.status = CurrentStone.Status.IDLE
                showStone(currentStone.shape.number)
                status = Status.IDLE
            } else {
                lastOffset = currentOffset
                status = if (scene.hasAnimations()) Status.FLINGING else Status.IDLE
            }
            return true
        }
        return false
    }

    @Synchronized
    fun render(renderer: FreebloksRenderer, gl: GL11) {
        gl.glTranslatef(-currentOffset, 0f, BoardRenderer.stoneSize * (scene.board.width + 10))
        for (i in stones.indices.reversed()) {
            val s = stones[i]

            if (s.available - (if (s == scene.currentStone.stone) 1 else 0) > 0) {
                val col = (i / 2).toFloat()
                val row = i % 2.toFloat()
                val offset = -(s.shape.size.toFloat() - 1.0f) * BoardRenderer.stoneSize
                val x = col * STONE_SPACING
                val effect = 12.5f / (12.5f + (abs(x - currentOffset) * 0.5f).pow(2.5f))
                var y = 0.35f + effect * 0.75f
                val z = row * STONE_SPACING
                val scale = 0.9f + effect * 0.3f
                var rotate = -scene.boardObject.baseAngle
                if (!scene.verticalLayout) rotate -= 90.0f
                var alpha = 1.0f
                if (currentStone == s && s != scene.currentStone.stone) y += 1.2f
                if (!movesLeft && !scene.game.isFinished) alpha *= 0.65f
                alpha *= 0.75f + effect * 0.25f
                gl.glPushMatrix()
                gl.glTranslatef(x, 0f, z)
                gl.glScalef(scale, scale, scale)

                if (s.available > 1 && s == currentStone && s != scene.currentStone.stone) {
                    gl.glPushMatrix()
                    gl.glTranslatef(BoardRenderer.stoneSize, 0f, BoardRenderer.stoneSize * 0.6f)
                    gl.glRotatef(rotate, 0f, 1f, 0f)
                    gl.glScalef(0.85f, 0.85f, 0.85f)
                    gl.glTranslatef(offset, 0f, offset)

                    //	gl.glTranslatef(BoardRenderer.stone_size * 0.5f, y - 1.2f, BoardRenderer.stone_size * 0.5f);
                    renderer.boardRenderer.renderShape(gl, scene.getPlayerColor(currentPlayer), s.shape, Orientation.Default, alpha)
                    gl.glPopMatrix()
                }
                gl.glRotatef(rotate, 0f, 1f, 0f)
                gl.glTranslatef(offset, 0f, offset)
                gl.glPushMatrix()
                renderer.boardRenderer.renderShapeShadow(gl, s.shape, scene.getPlayerColor(currentPlayer), Orientation.Default, y, 0f, 0f, 0f, 0f, -rotate, alpha, 1.0f)
                gl.glPopMatrix()
                gl.glTranslatef(0f, y, 0f)
                renderer.boardRenderer.renderShape(gl, if (s == currentStone && s != scene.currentStone.stone) 0 else scene.getPlayerColor(currentPlayer), s.shape, Orientation.Default, alpha)
                gl.glPopMatrix()
            }
        }
    }

    override fun execute(elapsed: Float): Boolean {
        val EPSILON = 0.5f
        if (status == Status.IDLE && abs(currentOffset - lastOffset) > EPSILON) {
            val ROTSPEED = 3.0f + abs(currentOffset - lastOffset).pow(0.65f) * 7.0f
            if (!scene.hasAnimations()) {
                currentOffset = lastOffset
                return true
            }
            if (currentOffset < lastOffset) {
                currentOffset += elapsed * ROTSPEED
                if (currentOffset > lastOffset) currentOffset = lastOffset
            } else {
                currentOffset -= elapsed * ROTSPEED
                if (currentOffset < lastOffset) currentOffset = lastOffset
            }
            return true
        }
        if (status == Status.SPINNING) {
            flingSpeed *= 0.2f
            flingSpeed += 0.90f * (currentOffset - lastFlingOffset) / elapsed
            if (flingSpeed > MAX_FLING_SPEED) {
                flingSpeed = MAX_FLING_SPEED
            }
            if (flingSpeed < -MAX_FLING_SPEED) {
                flingSpeed = -MAX_FLING_SPEED
            }
            lastFlingOffset = currentOffset
        }
        if (status == Status.FLINGING) {
            if (abs(flingSpeed) < MIN_FLING_SPEED) {
                status = Status.IDLE
                lastOffset = currentOffset
                return true
            }
            currentOffset += flingSpeed * elapsed
            if (abs(currentOffset - lastOffset) >= MAX_STONE_DRAG_DISTANCE) {
                currentStone = null
            }
            flingSpeed *= 0.05f.pow(elapsed)
            if (currentOffset < 0) {
                currentOffset = 0.0f
                /* bounce */
                flingSpeed *= -0.4f
            }
            if (currentOffset > maxOffset) {
                currentOffset = maxOffset
                /* bounce */
                flingSpeed *= -0.4f
            }
            return true
        }
        return false
    }

    companion object {
        private val tag = Wheel::class.java.simpleName

        private const val STONE_SPACING = 5.5f * BoardRenderer.stoneSize * 2.0f
        private const val MAX_FLING_SPEED = 100.0f
        private const val MIN_FLING_SPEED = 2.0f
        private const val MAX_STONE_DRAG_DISTANCE = 3.5f
    }
}