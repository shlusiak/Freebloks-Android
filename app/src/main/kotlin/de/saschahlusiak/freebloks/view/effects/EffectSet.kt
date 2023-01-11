package de.saschahlusiak.freebloks.view.effects

import de.saschahlusiak.freebloks.view.BoardRenderer
import java.util.*
import javax.microedition.khronos.opengles.GL11

/**
 * Chain of effects, to be played one after each other.
 */
class EffectSet : ArrayList<AbsShapeEffect>(), Effect {
    override fun isEffected(x: Int, y: Int): Boolean {
        return if (size > 0) get(0).isEffected(x, y) else false
    }

    override fun render(gl: GL11, renderer: BoardRenderer) {
        if (size > 0) get(0).render(gl, renderer)
    }

    override fun renderShadow(gl: GL11, renderer: BoardRenderer) {
        if (size > 0) get(0).renderShadow(gl, renderer)
    }

    override fun isDone(): Boolean {
        return size == 0
    }

    override fun execute(elapsed: Float): Boolean {
        if (size > 0) {
            val b = get(0).execute(elapsed)
            if (get(0).isDone()) {
                removeAt(0)
                /* don't flag rendering if effect is done */
                return false
            }
            return b
        }
        return false
    }
}