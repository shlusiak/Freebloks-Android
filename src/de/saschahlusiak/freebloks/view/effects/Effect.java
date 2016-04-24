package de.saschahlusiak.freebloks.view.effects;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import de.saschahlusiak.freebloks.view.BoardRenderer;

public interface Effect {
	boolean isEffected(int x, int y);
	void render(GL11 gl, BoardRenderer renderer);
	void renderShadow(GL11 gl, BoardRenderer renderer);
	boolean isDone();
	boolean execute(float elapsed);
}
