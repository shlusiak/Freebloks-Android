package de.saschahlusiak.freebloks.view.effects

import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.gles2.Gles2BoardRenderer
import de.saschahlusiak.freebloks.view.gles2.Gles2Context
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

    override fun renderShadow(ctx: Gles2Context, renderer: Gles2BoardRenderer) { }
}
