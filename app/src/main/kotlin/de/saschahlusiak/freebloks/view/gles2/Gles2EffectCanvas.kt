package de.saschahlusiak.freebloks.view.gles2

import de.saschahlusiak.freebloks.model.Orientation
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.model.StoneColor
import de.saschahlusiak.freebloks.view.MatrixStack
import de.saschahlusiak.freebloks.view.effects.EffectCanvas

/**
 * Lets the effects of the scene draw through the [Gles2BoardRenderer].
 */
class Gles2EffectCanvas(private val ctx: Gles2Context, private val boardRenderer: Gles2BoardRenderer) : EffectCanvas {
    override val matrix: MatrixStack get() = ctx.modelView

    override fun shape(color: StoneColor, shape: Shape, orientation: Orientation, alpha: Float) =
        boardRenderer.renderShape(ctx, color, shape, orientation, alpha)

    override fun shapeShadow(color: StoneColor, shape: Shape, orientation: Orientation, alpha: Float) =
        boardRenderer.renderShapeShadow(ctx, color, shape, orientation, alpha)

    override fun shapeShadow(
        shape: Shape,
        color: StoneColor,
        orientation: Orientation,
        height: Float,
        ang: Float, ax: Float, ay: Float, az: Float,
        lightAngle: Float,
        alpha: Float,
        scale: Float
    ) = boardRenderer.renderShapeShadow(ctx, shape, color, orientation, height, ang, ax, ay, az, lightAngle, alpha, scale)

    override fun stone(rgba: FloatArray) = boardRenderer.renderStone(ctx, rgba)
}
