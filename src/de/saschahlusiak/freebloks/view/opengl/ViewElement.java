package de.saschahlusiak.freebloks.view.opengl;

import android.graphics.PointF;

public class ViewElement {
	ViewModel model;
	
	ViewElement(ViewModel model) {
		this.model = model;
	}
	
	boolean handlePointerDown(PointF m) { return false; }
	boolean handlePointerMove(PointF m) { return false; }
	boolean handlePointerUp(PointF m)   { return false; }
}
