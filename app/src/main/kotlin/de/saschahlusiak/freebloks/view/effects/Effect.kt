package de.saschahlusiak.freebloks.view.effects

import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.gles2.Gles2BoardRenderer
import de.saschahlusiak.freebloks.view.gles2.Gles2Context
import javax.microedition.khronos.opengles.GL11

interface Effect {
    fun isEffected(x: Int, y: Int): Boolean
    fun render(gl: GL11, renderer: BoardRenderer)
    fun renderShadow(gl: GL11, renderer: BoardRenderer)
    /** GLES 2.0 variant of [render] */
    fun render(ctx: Gles2Context, renderer: Gles2BoardRenderer)
    /** GLES 2.0 variant of [renderShadow] */
    fun renderShadow(ctx: Gles2Context, renderer: Gles2BoardRenderer)
    fun isDone(): Boolean
    fun execute(elapsed: Float): Boolean
}