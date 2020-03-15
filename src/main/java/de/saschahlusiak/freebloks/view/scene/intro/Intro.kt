package de.saschahlusiak.freebloks.view.scene.intro

import android.content.Context
import android.opengl.GLU
import android.os.Handler
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.model.Board
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.OrientedShape
import de.saschahlusiak.freebloks.model.Rotation
import de.saschahlusiak.freebloks.theme.ColorThemes
import de.saschahlusiak.freebloks.view.BackgroundRenderer
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.FreebloksRenderer
import de.saschahlusiak.freebloks.view.effects.PhysicalShapeEffect
import de.saschahlusiak.freebloks.view.scene.Scene
import java.util.*
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11

class Intro(context: Context, private val scene: Scene, var listener: IntroCompleteListener?) {

    interface IntroCompleteListener {
        fun onIntroCompleted()
    }

    companion object {
        private const val INTRO_SPEED = 1.2f
        private const val WIPE_SPEED = 14.0f
        private const val WIPE_ANGLE = 28.0f
        private const val MATRIX_START_LEFT = 1.56f
        private const val MATRIX_START_RIGHT = MATRIX_START_LEFT + 0.25f
        private const val MATRIX_STOP_LEFT = 6.0f
        private const val MATRIX_STOP_RIGHT = MATRIX_STOP_LEFT + 0.25f
    }

    private var anim = 0.0f
    private val effects = ArrayList<PhysicalShapeEffect>()
    private var phase = 0
    private var field_up = false
    private var field_anim = 0.0f
    private val shapes: Array<OrientedShape>
    private val backgroundRenderer = BackgroundRenderer(context.resources, ColorThemes.Blue)
    private val board = Board(20)
    private val handler = Handler()

    init {
        shapes = initShapes()

        addTextFreebloks()
    }

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

    private fun addTextFreebloks() {
        addChar('f', 3, 4, 5)
        addChar('r', 2, 7, 6)
        addChar('e', 1, 10, 5)
        addChar('e', 0, 13, 6)
        addChar('b', 0, 2, 12)
        addChar('l', 1, 5, 11)
        addChar('o', 2, 8, 12)
        addChar('k', 3, 11, 11)
        addChar('s', 2, 14, 13)
    }

    fun handlePointerDown(): Boolean {
        cancel()
        return true
    }

    @UiThread
    fun cancel() {
        listener?.onIntroCompleted()
    }

    @WorkerThread
    fun execute(elapsed: Float): Boolean {
        val elapsed = elapsed * INTRO_SPEED

        anim += elapsed

        if (field_up || field_anim > 0.000001f) {
            if (field_up) {
                field_anim += elapsed * WIPE_SPEED
                if (field_anim > 1.0f) {
                    field_anim = 1.0f
                    field_up = false
                }
            } else {
                field_anim -= elapsed * 2.5f
                if (field_anim < 0.0) field_anim = 0.0f
            }
        }

        if (phase == 0) synchronized(effects) {
            // In phase there is a matrix move while the stones are in the air, where we slow down time
            val slowedTime = when (anim) {
                in MATRIX_START_LEFT..MATRIX_START_RIGHT ->
                    elapsed * (MATRIX_START_RIGHT - anim) / (MATRIX_START_RIGHT - MATRIX_START_LEFT)

                in MATRIX_START_RIGHT..MATRIX_STOP_LEFT -> 0.0f

                in MATRIX_STOP_LEFT..MATRIX_STOP_RIGHT ->
                    elapsed * (anim - MATRIX_STOP_LEFT) / (MATRIX_STOP_RIGHT - MATRIX_STOP_LEFT)

                else -> elapsed
            }

            executeEffects(slowedTime)

            if (anim > 10.5f) {
                // clear board and advance to next phase
                phase = 1
                wipe()
            }
        } else synchronized(effects) {
            executeEffects(elapsed)
            /* In these phases, speed up the falling of the stones a bit */
            if (phase == 2 || phase == 4 || phase == 5)
                executeEffects(elapsed * 0.7f)

            // each phase lasts to the 12th unit, but starts later.
            if (anim > 12.0) {
                phase++
                if (phase == 3) {
                    anim = 10.8f
                    wipe()
                }
                if (phase == 6) {
                    anim = 9.5f
                    wipe()
                }
                /* Oder neue Steine regnen lassen. */
                if (phase == 2) {
                    anim = 9.1f
                    effects.clear()
                    addChar('b', -1, 5, 9)
                    addChar('y', -1, 9, 9)
                }

                if (phase == 4) {
                    effects.clear()
                    anim = 10.2f
                    addChar('s', 0, 1, 5)
                    addChar('a', 2, 4, 5)
                    addChar('s', 3, 7, 5)
                    addChar('c', 2, 10, 5)
                    addChar('h', 1, 13, 5)
                    addChar('a', 0, 16, 5)
                }

                if (phase == 5) {
                    anim = 8.5f
                    addChar('h', 3, 0, 11)
                    addChar('l', 2, 3, 11)
                    addChar('u', 0, 6, 11)
                    addChar('s', 1, 9, 11)
                    addChar('i', 2, 11, 11)
                    addChar('a', 0, 13, 11)
                    addChar('k', 3, 16, 11)
                }

                // last phase
                if (phase == 7) {
                    handler.post { cancel() }
                }
            }
        }
        return true
    }

    private fun add(stone: Int, player: Int, dx: Int, dy: Int) {
        val x: Float
        val y: Float
        val z: Float
        /* Eine Rotationsachse berechnen */
        val angx = (Math.random() * 2.0 * Math.PI).toFloat()
        val angy = (Math.random() * 2.0 * Math.PI).toFloat()
        val axe_x = (Math.sin(angx.toDouble()) * Math.cos(angy.toDouble())).toFloat()
        val axe_y = Math.sin(angy.toDouble()).toFloat()
        val axe_z = (Math.cos(angx.toDouble()) * Math.cos(angy.toDouble())).toFloat()

        /* CPhysicalStone erstellen, aus stones[stone] */
        val st = shapes[stone]
        val shape = st.shape
        val s = PhysicalShapeEffect(scene, shape, Global.getPlayerColor(player, GameMode.GAMEMODE_4_COLORS_4_PLAYERS), st.orientation)

        /* Lokale dx/dy des Feldes in globale Welt-Koordinaten umrechnen. */x = (-(Board.DEFAULT_BOARD_SIZE - 1) * BoardRenderer.stoneSize + (dx.toDouble() + shape.size.toDouble() / 2.0) * BoardRenderer.stoneSize * 2.0 - BoardRenderer.stoneSize).toFloat()
        z = (-(Board.DEFAULT_BOARD_SIZE - 1) * BoardRenderer.stoneSize + (dy.toDouble() + shape.size.toDouble() / 2.0) * BoardRenderer.stoneSize * 2.0 - BoardRenderer.stoneSize).toFloat()
        /* Zufaellige Hoehe geben. */y = 22.0f + (Math.random() * 18.0f).toFloat()

        /* Der Stein wird in <time> sek den Boden erreichen. */
        val time = Math.sqrt(2.0f * y / PhysicalShapeEffect.gravity.toDouble()).toFloat()
        /* x/z Koordinaten zufaellig verschieben */
        val xoffs = Math.random().toFloat() * 60.0f - 30.0f
        val zoffs = Math.random().toFloat() * 60.0f - 30.0f
        /* Position setzen */s.setPos(x + xoffs, y, z + zoffs)
        /* x/z Geschwindigkeit setzen, y Geschw. ist 0 */s.setSpeed(-xoffs / time, 0f, -zoffs / time)
        /* Gewuenschtes Ziel in Stein speichern */s.setTarget(x, 0f, z)
        /* Stein dreht sich exakt um 360 Grad in <time> sek. */s.setRotationSpeed(360.0f / time, axe_x, axe_y, axe_z)
        /* Effekt der verketteten Liste hinzufuegen. */
        effects.add(s)
    }

    private fun addChar(c: Char, color: Int, x: Int, y: Int) {
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
            else -> throw IllegalStateException("Falscher char uebergeben: $c")
        }
    }

    private fun wipe() {
        /* Zu Beginn das Feld hoch klappen */
        field_up = true
        field_anim = 0.0f
        /* Komplette verkettete Liste durchgehen und fuer jeden enthaltenen CPhysicalStone...*/for (e in effects) {
            /* ...Geschwindigkeit setzen, dass die Steine tangential zur Drehung des
			   Felds wegfliegen */
            /* Winkel, in dem die Steine beschleunigt werden */
            val ANG = WIPE_ANGLE / 180.0f * Math.PI.toFloat()
            /* Radialgeschwindigkeit errechnen. */
            val v = ANG * WIPE_SPEED * (e.currentZ - 20 * BoardRenderer.stoneSize) - (Math.random() * 10.0 - 8.0).toFloat()
            /* Stein nur leicht rotieren lassen, und nicht ganz zufaellig */
            val a1 = 0.95f
            val a2 = ((if (Math.random() < 0.5) 1 else -1) * Math.sqrt((1.0 - a1 * a1) / 2.0)).toFloat()
            val a3 = ((if (Math.random() < 0.5) 1 else -1) * Math.sqrt((1.0 - a1 * a1) / 2.0)).toFloat()
            /* Stein hauptsaechlich in Richtung der Felddrehung rotieren lassen */e.setRotationSpeed(WIPE_ANGLE * WIPE_SPEED + (Math.random() * 6.6).toFloat(), a1, a2, a3)
            /* Geschwindigkeit und Winkel in Kartesische Koordinaten umrechnen */e.setSpeed((Math.random() * 5.0).toFloat(), Math.cos(ANG.toDouble()).toFloat() * v, (-Math.sin(ANG.toDouble())).toFloat() * v)
            /* Stein soll kein Ziel mehr haben, d.h. er faellt unendlich tief */e.unsetDestination()
        }
    }

    private fun executeEffects(elapsed: Float) {
        for (i in effects.indices) effects[i].execute(elapsed)
    }

    fun render(gl: GL11, renderer: FreebloksRenderer) {
        gl.glLoadIdentity()
        backgroundRenderer.render(gl)

        /* Kamera positionieren */if (scene.verticalLayout) {
            gl.glTranslatef(0f, 4.5f, 0f)
        } else {
            gl.glTranslatef(0f, 1.5f, 0f)
        }
        GLU.gluLookAt(gl, 0f, (if (scene.verticalLayout) 4 else 1).toFloat(), renderer.fixed_zoom * 0.9f, 0f, 0f, 0f, 0f, 1f, 0f)

        /* Kamera drehen, evtl. durch Matrix move */gl.glRotatef(50f, 1f, 0f, 0f)
        val winkel1 = 180.0f
        val winkel2 = -60.0f
        val matrix_anim = (Math.sin((anim - MATRIX_START_LEFT) / (MATRIX_STOP_LEFT - MATRIX_START_LEFT) * Math.PI - Math.PI / 2.0) / 2.0 + 0.5).toFloat()
        if (anim < MATRIX_START_LEFT) {
            gl.glRotatef(winkel2, 1f, 0f, 0f)
            gl.glRotatef(winkel1, 0f, 1f, 0f)
        } else if (anim < MATRIX_STOP_LEFT) {
            gl.glRotatef(winkel2 - matrix_anim * matrix_anim * winkel2, 1f, 0f, 0f)
            gl.glRotatef(winkel1 - matrix_anim * winkel1, 0f, 1f, 0f)
        }
        if (anim < MATRIX_START_LEFT) gl.glTranslatef(0.0f, -14.0f + anim / MATRIX_START_LEFT * 4.0f, 0.0f) else if (anim < MATRIX_STOP_LEFT) {
            gl.glTranslatef(0f, -10 + 10 * (matrix_anim * matrix_anim), 0f)
        }

        /* Licht setzen der neuen Kameraposition anpassen*/gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, renderer.light0_pos, 0)

        /* Umgebung und Feld rendern. */gl.glPushMatrix()
        if (field_anim > 0.0001f) {
            gl.glTranslatef(0f, 0f, 20 * BoardRenderer.stoneSize)
            gl.glRotatef(field_anim * WIPE_ANGLE, 1f, 0f, 0f)
            gl.glTranslatef(0f, 0f, -20 * BoardRenderer.stoneSize)
        }
        gl.glDisable(GL10.GL_DEPTH_TEST)
        renderer.boardRenderer.renderBoard(gl, board, -1)
        gl.glDisable(GL10.GL_DEPTH_TEST)
        gl.glPopMatrix()
        /* Alle Steine rendern. */synchronized(effects) {
            for (i in effects.indices) effects[i].renderShadow(gl, renderer.boardRenderer)
            gl.glEnable(GL10.GL_DEPTH_TEST)
            for (i in effects.indices) effects[i].render(gl, renderer.boardRenderer)
        }
    }
}