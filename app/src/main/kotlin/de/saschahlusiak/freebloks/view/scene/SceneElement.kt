package de.saschahlusiak.freebloks.view.scene

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.utils.PointF

interface SceneElement {
    /**
     * @param m model coordinates of touch event
     * @return true if event has been handled
     */
    @UiThread
    fun handlePointerDown(m: PointF): Boolean

    /**
     * @param m model coordinates of touch event
     * @return true if event has been handled; this does not mean a render pass, call [Scene.invalidate] to force rendering.
     */
    @UiThread
    fun handlePointerMove(m: PointF): Boolean

    /**
     * @param m model coordinates of touch event
     */
    @UiThread
    fun handlePointerUp(m: PointF)

    /**
     * @param elapsed time in seconds
     * @return true if element has changed and requires rendering
     */
    @WorkerThread
    fun execute(elapsed: Float): Boolean
}