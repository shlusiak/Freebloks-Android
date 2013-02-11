package de.saschahlusiak.freebloks.view.effects;

import javax.microedition.khronos.opengles.GL10;

import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.view.BoardRenderer;

public interface Effect {
	public boolean isEffected(int x, int y);
	public void render(GL10 gl, Spiel spiel, BoardRenderer renderer);
	public boolean isDone();
	public boolean execute(float elapsed);
}
