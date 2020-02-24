package de.saschahlusiak.freebloks.view.scene

import android.graphics.PointF

interface ViewElement {
    fun handlePointerDown(m: PointF): Boolean
    fun handlePointerMove(m: PointF): Boolean
    fun handlePointerUp(m: PointF): Boolean

    fun execute(elapsed: Float): Boolean
}