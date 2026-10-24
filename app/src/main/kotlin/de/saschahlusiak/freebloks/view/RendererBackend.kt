package de.saschahlusiak.freebloks.view

import android.opengl.GLSurfaceView
import de.saschahlusiak.freebloks.theme.Theme
import de.saschahlusiak.freebloks.utils.PointF

/**
 * Abstraction over the GL backend (fixed function GLES 1.x or shader based GLES 2.0) used by [Freebloks3DView].
 */
interface RendererBackend : GLSurfaceView.Renderer {
    /** The EGL context client version to request. */
    val glesVersion: Int

    var zoom: Float
    var updateModelViewMatrix: Boolean

    /** Must be called on the GL thread. */
    fun init(boardSize: Int)

    /** Must be called on the GL thread. */
    fun setBoardSize(size: Int)

    fun setTheme(backgroundTheme: Theme, boardTheme: Theme)

    fun windowToModel(point: PointF): PointF
}
