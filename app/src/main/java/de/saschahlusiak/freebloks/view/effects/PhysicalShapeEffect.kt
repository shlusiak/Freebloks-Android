package de.saschahlusiak.freebloks.view.effects

import de.saschahlusiak.freebloks.model.Orientation
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.model.StoneColor
import de.saschahlusiak.freebloks.view.BoardRenderer
import de.saschahlusiak.freebloks.view.scene.Scene
import javax.microedition.khronos.opengles.GL11

class PhysicalShapeEffect(
    model: Scene,
    shape: Shape,
    color: StoneColor,
    orientation: Orientation
) : AbsShapeEffect(model, shape, color, 0, 0, orientation) {

    companion object {
        const val gravity = 17.0f
    }

    /**
     *  Current physical position in world coordinates
     */
    var currentX = 0.0f
        private set
    var currentY = 0.0f
        private set
    var currentZ = 0.0f
        private set

    /**
     * Current angle and axis of rotation
     */
    private var ang = 0.0f
    private var ax = 0.0f
    private var ay = 1.0f
    private var az = 0.0f

    /**
     * Current rotating speed around axis
     */
    private var angspeed = 0.0f

    /**
     * Current moving speed of stone along the axes
     */
    private var speedX = 0.0f
    private var speedY = 0.0f
    private var speedZ = 0.0f

    /**
     * The desired target position on the board. On landing the current position is adjusted to exactly hit
     * the target to fix minor inaccuracies during the calculations.
     */
    private var targetX = 0f
    private var targetY = 0f
    private var targetZ = 0f

    override fun renderShadow(gl: GL11, renderer: BoardRenderer) {
        val alpha = 0.60f - currentY / 20.0f

        gl.glPushMatrix()
        gl.glScalef(1f, 0.01f, 1f)
        gl.glTranslatef(currentX, 0f, currentZ)
        gl.glTranslatef(-2.5f * currentY * 0.11f, 0f, 2.0f * currentY * 0.11f)
        gl.glRotatef(ang, ax, ay, az)
        gl.glScalef(1.0f + currentY / 18.0f, 1f, 1.0f + currentY / 18.0f)
        gl.glTranslatef(
            +BoardRenderer.stoneSize - shape.size.toFloat() / 2.0f * BoardRenderer.stoneSize * 2.0f,
            0.0f,
            +BoardRenderer.stoneSize - shape.size.toFloat() / 2.0f * BoardRenderer.stoneSize * 2.0f
        )
        renderer.renderShapeShadow(gl, color, shape, orientation, alpha)
        gl.glPopMatrix()
    }

    override fun render(gl: GL11, renderer: BoardRenderer) {
        gl.glPushMatrix()
        gl.glTranslatef(currentX, currentY, currentZ)
        gl.glRotatef(ang, ax, ay, az)
        gl.glTranslatef(
            +BoardRenderer.stoneSize - shape.size.toFloat() / 2.0f * BoardRenderer.stoneSize * 2.0f,
            0.0f,
            +BoardRenderer.stoneSize - shape.size.toFloat() / 2.0f * BoardRenderer.stoneSize * 2.0f)
        renderer.renderShape(gl, color, shape, orientation, BoardRenderer.defaultStoneAlpha)
        gl.glPopMatrix()
    }

    override fun isDone(): Boolean {
        return false
    }

    fun setPos(sx: Float, sy: Float, sz: Float) {
        currentX = sx
        currentY = sy
        currentZ = sz
    }

    fun setRotationSpeed(angs: Float, ax: Float, ay: Float, az: Float) {
        this.angspeed = angs
        this.ax = ax
        this.ay = ay
        this.az = az
    }

    fun setSpeed(sx: Float, sy: Float, sz: Float) {
        speedX = sx
        speedY = sy
        speedZ = sz
    }

    fun setTarget(targetX: Float, targetY: Float, targetZ: Float) {
        this.targetX = targetX
        this.targetY = targetY
        this.targetZ = targetZ
    }

    override fun execute(elapsed: Float): Boolean {
        super.execute(elapsed)

        currentX += speedX * elapsed
        currentY -= speedY * elapsed
        currentZ += speedZ * elapsed

        ang += elapsed * angspeed
        /* wenn y unterhalb von dy gefallen ist, Stein auf dx/dy/dz setzen. */
        if (currentY < targetY && targetY > -100.0) {
            speedY = if (speedY > 0.5) -speedY * 0.32f else 0.0f
            angspeed = 0.0f
            ang = 0.0f
            speedX = 0.0f
            speedZ = 0.0f

            currentX = targetX
            currentY = targetY
            currentZ = targetZ
        }
        speedY += elapsed * gravity
        return true
    }

    fun unsetDestination() {
        targetY = -200.0f
    }
}