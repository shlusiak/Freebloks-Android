package de.saschahlusiak.freebloks.view.model;

import android.graphics.PointF;

public interface ViewElement {
	boolean handlePointerDown(PointF m);
	boolean handlePointerMove(PointF m);
	boolean handlePointerUp(PointF m);
	boolean execute(float elapsed);
}
