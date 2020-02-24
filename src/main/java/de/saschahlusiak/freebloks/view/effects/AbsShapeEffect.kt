package de.saschahlusiak.freebloks.view.effects

import de.saschahlusiak.freebloks.model.Orientation
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.scene.Scene
import javax.microedition.khronos.opengles.GL11

abstract class AbsShapeEffect internal constructor(
    protected val model: Scene,
    protected val shape: Shape,
    protected val color: Int,
    protected val x: Int,
    protected val y: Int,
    protected val orientation: Orientation
) : AbsEffect(), Effect {

    internal constructor(model: Scene, turn: Turn) : this(model, turn.shape, model.getPlayerColor(turn.player), turn.x, turn.y, turn.orientation)

    override fun isEffected(x: Int, y: Int): Boolean {
        val sx = x - this.x
        val sy = y - this.y
        if (sx < 0 || sy < 0) return false
        if (sx >= shape.size) return false
        if (sy >= shape.size) return false

        return shape.isStone(sx, sy, orientation)
    }

    override fun renderShadow(gl: GL11, renderer: BoardRenderer) { }
}