package de.saschahlusiak.freebloks.view

import android.opengl.GLU
import android.opengl.Matrix
import de.saschahlusiak.freebloks.utils.PointF
import kotlin.math.cos
import kotlin.math.sin

/**
 * The camera looking at the board: projection, view and the conversion from window to board plane coordinates (picking).
 *
 * [onSurfaceChanged] and [loadView] run on the GL thread, [windowToModel] on the UI thread. The matrices picking
 * needs are snapshots, taken under [lock], so both always see a consistent pair.
 */
class Camera {
    /** The pinch-to-zoom factor. */
    var zoom = 1f

    /** Set to have the next [loadView] refresh the matrices used for picking. */
    @Volatile
    var updateModelViewMatrix = true

    /** Distance from the board at zoom 1. */
    val fixedZoom = 55.0f

    /** Projection matrix of the last [onSurfaceChanged]. */
    val projection = FloatArray(16).also { Matrix.setIdentityM(it, 0) }

    private val lock = Any()
    private val viewport = intArrayOf(0, 0, 1, 1)
    private val pickProjection = FloatArray(16).also { Matrix.setIdentityM(it, 0) }
    private val pickModelView = FloatArray(16).also { Matrix.setIdentityM(it, 0) }
    private val near = FloatArray(4)
    private val far = FloatArray(4)

    fun onSurfaceChanged(width: Int, height: Int, verticalLayout: Boolean) {
        val fovY = if (verticalLayout) 35.0f else 21.0f

        synchronized(lock) {
            viewport[2] = width
            viewport[3] = height
            Matrix.perspectiveM(projection, 0, fovY, width.toFloat() / height.toFloat(), 1.0f, 300.0f)
            System.arraycopy(projection, 0, pickProjection, 0, 16)
        }
    }

    /**
     * Loads the view of the board into [modelView], looking from [baseAngle] degrees around the board.
     * The board rotation is not part of it.
     */
    fun loadView(modelView: MatrixStack, verticalLayout: Boolean, baseAngle: Float) {
        modelView.loadIdentity()
        if (verticalLayout) {
            modelView.translate(0f, 7.0f, 0f)
        } else {
            modelView.translate(-5.0f, 0.6f, 0f)
        }

        val distance = fixedZoom / zoom
        val angle = Math.toRadians(baseAngle.toDouble())
        val pitch = Math.toRadians(PITCH.toDouble())
        modelView.lookAt(
            (distance * sin(angle) * cos(pitch)).toFloat(),
            (distance * sin(pitch)).toFloat(),
            (distance * cos(pitch) * cos(-angle)).toFloat(),
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f
        )

        if (updateModelViewMatrix) synchronized(lock) {
            modelView.copyTo(pickModelView)
            updateModelViewMatrix = false
        }
    }

    /**
     * Converts a window coordinate to the coordinate on the board plane (x and z in model space, y = 0).
     */
    fun windowToModel(point: PointF): PointF {
        synchronized(lock) {
            val y = viewport[3] - point.y
            GLU.gluUnProject(point.x, y, 0.0f, pickModelView, 0, pickProjection, 0, viewport, 0, near, 0)
            GLU.gluUnProject(point.x, y, 1.0f, pickModelView, 0, pickProjection, 0, viewport, 0, far, 0)
        }

        val x1 = far[0] / far[3]
        val y1 = far[1] / far[3]
        val z1 = far[2] / far[3]
        val x2 = near[0] / near[3]
        val y2 = near[1] / near[3]
        val z2 = near[2] / near[3]
        val u = (0.0f - y1) / (y2 - y1)

        return PointF(
            x1 + u * (x2 - x1),
            z1 + u * (z2 - z1)
        )
    }

    private companion object {
        const val PITCH = 70.0f
    }
}
