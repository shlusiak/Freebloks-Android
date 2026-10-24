package de.saschahlusiak.freebloks.view.effects

import java.util.*

/**
 * Chain of effects, to be played one after each other.
 */
class EffectSet : ArrayList<AbsShapeEffect>(), Effect {
    override fun isEffected(x: Int, y: Int): Boolean {
        return if (size > 0) get(0).isEffected(x, y) else false
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

    override fun render(canvas: EffectCanvas) {
        if (size > 0) get(0).render(canvas)
    }

    override fun renderShadow(canvas: EffectCanvas) {
        if (size > 0) get(0).renderShadow(canvas)
    }
}
