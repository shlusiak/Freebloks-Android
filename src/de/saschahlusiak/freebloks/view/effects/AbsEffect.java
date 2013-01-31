package de.saschahlusiak.freebloks.view.effects;

import javax.microedition.khronos.opengles.GL10;

import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.view.BoardRenderer;

public abstract class AbsEffect {
	AbsEffect() {
		time = 0.0f;
	}
		
	float time;
	
	public abstract boolean isDone();
	
	public boolean execute(float elapsed) {
		time += elapsed;
		return true;
	}
	
	public abstract void render(GL10 gl, Spiel spiel, BoardRenderer renderer);

	public boolean isEffected(int x, int y) {
		return false;
	}
}
