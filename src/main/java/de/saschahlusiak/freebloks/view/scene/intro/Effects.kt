package de.saschahlusiak.freebloks.view.scene.intro

import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.model.Board
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.Rotation
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.effects.PhysicalShapeEffect
import de.saschahlusiak.freebloks.view.scene.Scene
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Collection of [PhysicalShapeEffect] for the [Intro] with helper methods to write text.
 */
internal class Effects(val scene: Scene) : ArrayList<PhysicalShapeEffect>() {
    private val shapes = initShapes()

    /**
     * Initialise all shapes that we require
     */
    private fun initShapes(): Array<OrientedShape> {
        return arrayOf(
            // 0: XXX
            //      X
            OrientedShape(5, false, Rotation.Left),

            // 1: X
            //    X
            //    X
            //    X
            OrientedShape(8),
            // 2: XX
            //     X
            //    XX
            OrientedShape(10),

            // 3: X
            //    X
            //    XXX
            OrientedShape(12),

            // 4: X
            //    X
            OrientedShape(1),

            // 5: X
            //    X
            //    X
            //    X
            //    X
            OrientedShape(20),

            // 6:  X
            //     X
            //    XX
            OrientedShape(5),

            // 7: XX
            //     X
            OrientedShape(2).apply { rotate180Degrees() },

            // 8: X
            OrientedShape(0),

            // 9: X
            //    X
            //    X
            OrientedShape(3),

            // 10: X X
            //     XXX
            OrientedShape(10).apply { rotateRight() },

            // 11: XX
            OrientedShape(1).apply { rotateRight() },

            // 12:  X
            //    XXX
            OrientedShape(5).apply { rotateLeft(); mirrorVertically() },

            // 13: XXX
            //     X
            OrientedShape(5).apply { rotateRight(); mirrorVertically() }
        )
    }

    @Synchronized
    fun execute(elapsed: Float) {
        for (i in indices) {
            get(i).execute(elapsed)
        }
    }

    @Synchronized
    private fun add(os: OrientedShape, player: Int, dx: Int, dy: Int) {
        val angleX = Random.nextFloat() * 2.0f * Math.PI.toFloat()
        val angleY = Random.nextFloat() * 2.0f * Math.PI.toFloat()
        val axisX = sin(angleX) * cos(angleY)
        val axisY = sin(angleY)
        val axisZ = cos(angleX) * cos(angleY)

        val shape = os.shape
        val s = PhysicalShapeEffect(scene, shape, Global.getPlayerColor(player, GameMode.GAMEMODE_4_COLORS_4_PLAYERS), os.orientation)

        // convert board dx/dy coordinate to world coordinates
        val x = (-(Board.DEFAULT_BOARD_SIZE - 1) * BoardRenderer.stoneSize + (dx + shape.size.toFloat() / 2.0f) * BoardRenderer.stoneSize * 2.0f - BoardRenderer.stoneSize)
        val y = 22.0f + Random.nextFloat() * 18.0f
        val z = (-(Board.DEFAULT_BOARD_SIZE - 1) * BoardRenderer.stoneSize + (dy + shape.size.toFloat() / 2.0f) * BoardRenderer.stoneSize * 2.0f - BoardRenderer.stoneSize)

        // time in which the stone will hit the ground
        val time = sqrt(2.0f * y / PhysicalShapeEffect.gravity)

        // randomly move stone left/right
        val xOffs = Random.nextFloat() * 60.0f - 30.0f
        val zOffs = Random.nextFloat() * 60.0f - 30.0f

        s.setPos(x + xOffs, y, z + zOffs)
        s.setSpeed(-xOffs / time, 0f, -zOffs / time)
        s.setTarget(x, 0f, z)
        s.setRotationSpeed(360.0f / time, axisX, axisY, axisZ)

        add(s)
    }

    private fun add(stone: Int, player: Int, dx: Int, dy: Int) = add(shapes[stone], player, dx, dy)

    internal fun addChar(c: Char, color: Int, x: Int, y: Int) {
        when (c) {
            'a' -> {
                add(5, color, x - 2, y)
                add(2, color, x + 1, y)
                add(4, color, x + 1, y + 3)
            }
            'b' -> {
                add(0, color, x, y - 1)
                add(1, color, x - 2, y + 1)
                add(2, color, x + 1, y + 2)
            }
            'c' -> {
                add(5, color, x - 2, y)
                add(11, color, x + 1, y - 1)
                add(11, color, x + 1, y + 3)
            }
            'e' -> {
                add(11, color, x + 1, y - 1)
                add(4, color, x - 1, y)
                add(3, color, x, y + 2)
                add(8, color, x + 1, y + 2)
            }
            'f' -> {
                add(13, color, x, y - 1)
                add(9, color, x - 1, y + 2)
                add(8, color, x + 1, y + 2)
            }
            'l' -> {
                add(4, color, x - 1, y)
                add(3, color, x, y + 2)
            }
            'o' -> {
                add(5, color, x - 2, y)
                add(6, color, x + 1, y + 2)
                add(7, color, x + 1, y)
            }
            'h' -> {
                add(6, color, x + 1, y)
                add(5, color, x - 2, y)
                add(4, color, x + 1, y + 3)
            }
            'k' -> {
                add(5, color, x - 2, y)
                add(8, color, x + 1, y + 2)
                add(4, color, x + 1, y)
                add(4, color, x + 1, y + 3)
            }
            'n' -> {
                add(5, color, x - 2, y)
                add(5, color, x, y)
                add(4, color, x, y + 2)
            }
            'u' -> {
                add(9, color, x - 1, y)
                add(9, color, x + 1, y)
                add(10, color, x, y + 3)
            }
            'i' -> {
                add(4, color, x, y)
                add(9, color, x, y + 2)
            }
            'r' -> {
                add(0, color, x, y - 1)
                add(1, color, x - 2, y + 1)
                add(8, color, x + 1, y + 2)
                add(4, color, x + 1, y + 3)
            }
            's' -> {
                add(3, color, x, y)
                add(11, color, x + 1, y - 1)
                add(12, color, x, y + 3)
            }
            'x' -> {
                add(4, color, x - 1, y)
                add(4, color, x + 1, y)
                add(4, color, x - 1, y + 3)
                add(4, color, x + 1, y + 3)
                add(8, color, x + 1, y + 2)
            }
            'y' -> {
                add(4, color, x - 1, y)
                add(4, color, x + 1, y)
                add(9, color, x, y + 2)
            }
            else -> throw IllegalStateException("Don't know how to draw $c")
        }
    }

    /**
     * Accelerate all current stones to make them start flying off the flipping board.
     */
    @Synchronized
    internal fun startFlipBoard(wipeAngle: Float, wipeSpeed: Float) {
        forEach { e ->
            // calculate angle in which the stones would fly off the board
            val ang = wipeAngle / 180.0f * Math.PI.toFloat()

            // radial velocity
            val v = ang * wipeSpeed * (e.currentZ - 20 * BoardRenderer.stoneSize) - (Random.nextFloat() * 10.0f - 8.0f)

            // rotate stone only slightly, not fully random
            val a1 = 0.95f
            val a2 = ((if (Random.nextBoolean()) 1 else -1) * sqrt((1.0f - a1 * a1) / 2.0f))
            val a3 = ((if (Random.nextBoolean()) 1 else -1) * sqrt((1.0f - a1 * a1) / 2.0f))

            // mainly rotate in direction of board
            e.setRotationSpeed(wipeAngle * wipeSpeed + Random.nextFloat() * 6.6f, a1, a2, a3)

            // convert angle and angle velocity into absolute speed vector
            e.setSpeed(Random.nextFloat() * 5.0f, cos(ang) * v, -sin(ang) * v)

            // stone will not hit the board again, so set destination y to infinity
            e.unsetDestination()
        }
    }

    @Synchronized
    override fun clear() {
        super.clear()
    }
}