package de.saschahlusiak.freebloks.view.effects

import de.saschahlusiak.freebloks.view.BoardRenderer
import javax.microedition.khronos.opengles.GL11

interface Effect {
    fun isEffected(x: Int, y: Int): Boolean
    fun render(gl: GL11, renderer: BoardRenderer)
    fun renderShadow(gl: GL11, renderer: BoardRenderer)
    fun isDone(): Boolean
    fun execute(elapsed: Float): Boolean
}