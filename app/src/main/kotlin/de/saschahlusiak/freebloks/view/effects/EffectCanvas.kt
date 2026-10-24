package de.saschahlusiak.freebloks.view.effects

import de.saschahlusiak.freebloks.model.Orientation
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.model.StoneColor
import de.saschahlusiak.freebloks.view.MatrixStack

/**
 * What an [Effect] can draw. The current [matrix] is the transform applied to everything drawn.
 *
 * Drawing leaves [matrix] changed (shapes are laid out cell by cell), so wrap it in [MatrixStack.withPush].
 */
interface EffectCanvas {
    val matrix: MatrixStack

    /** Draws the shape with lighting, [alpha] being its opacity. */
    fun shape(color: StoneColor, shape: Shape, orientation: Orientation, alpha: Float)

    /** Draws the flat shadow of the shape at the current matrix. */
    fun shapeShadow(color: StoneColor, shape: Shape, orientation: Orientation, alpha: Float)

    /**
     * Draws the shadow of a shape hovering [height] above the board, rotated by [ang] around the axis ([ax], [ay], [az]),
     * with the light coming from [lightAngle] degrees.
     */
    fun shapeShadow(
        shape: Shape,
        color: StoneColor,
        orientation: Orientation,
        height: Float,
        ang: Float, ax: Float, ay: Float, az: Float,
        lightAngle: Float,
        alpha: Float,
        scale: Float
    )

    /** Draws a single board stone with the given premultiplied RGBA colour. */
    fun stone(rgba: FloatArray)
}
