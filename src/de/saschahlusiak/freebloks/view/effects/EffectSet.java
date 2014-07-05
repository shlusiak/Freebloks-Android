package de.saschahlusiak.freebloks.view.effects;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;
import de.saschahlusiak.freebloks.view.BoardRenderer;

public class EffectSet extends ArrayList<AbsStoneEffect> implements Effect {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean isEffected(int x, int y) {
		if (size() > 0)
			return get(0).isEffected(x, y);
		return false;
	}

	@Override
	public void render(GL10 gl, BoardRenderer renderer) {
		if (size() > 0)
			get(0).render(gl, renderer);
	}

	@Override
	public void renderShadow(GL10 gl, BoardRenderer renderer) {
		if (size() > 0)
			get(0).renderShadow(gl, renderer);
	}


	@Override
	public boolean isDone() {
		return size() == 0;
	}

	@Override
	public boolean execute(float elapsed) {
		if (size() > 0) {
			boolean b = get(0).execute(elapsed);
			if (get(0).isDone()) {
				remove(0);
				/* don't flag rendering if effect is done */
				return false;
			}
			return b;
		}
		return false;
	}
}
