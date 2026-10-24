package de.saschahlusiak.freebloks.view.effects

import de.saschahlusiak.freebloks.view.gles2.Gles2BoardRenderer
import de.saschahlusiak.freebloks.view.gles2.Gles2Context

interface Effect {
    fun isEffected(x: Int, y: Int): Boolean
    fun render(ctx: Gles2Context, renderer: Gles2BoardRenderer)
    fun renderShadow(ctx: Gles2Context, renderer: Gles2BoardRenderer)
    fun isDone(): Boolean
    fun execute(elapsed: Float): Boolean
}