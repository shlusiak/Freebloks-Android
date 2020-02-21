package de.saschahlusiak.freebloks.view.model

import android.graphics.PointF

interface ViewElement {
    fun handlePointerDown(m: PointF): Boolean
    fun handlePointerMove(m: PointF): Boolean
    fun handlePointerUp(m: PointF): Boolean

    fun execute(elapsed: Float): Boolean
}