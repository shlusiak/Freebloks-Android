package de.saschahlusiak.freebloks.view.effects

import de.saschahlusiak.freebloks.view.BoardRenderer
import javax.microedition.khronos.opengles.GL11

abstract class AbsEffect internal constructor() : Effect {
	internal var time = 0.0f

    override fun execute(elapsed: Float): Boolean {
        time += elapsed
        return true
    }

    override fun isEffected(x: Int, y: Int): Boolean {
        return false
    }

    override fun renderShadow(gl: GL11, renderer: BoardRenderer) { }
}