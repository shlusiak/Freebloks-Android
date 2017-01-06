package de.saschahlusiak.freebloks.view.effects;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.view.BoardRenderer;
import de.saschahlusiak.freebloks.view.model.ViewModel;

public class StoneFadeEffect extends AbsStoneEffect {
	private static final float TIME_PER_PERIOD = 1.1f;
	private static final float ALPHA_MIN = 0.15f;
	private static final float ALPHA_MAX = 1.0f;

	private float NUMBER_OF_PERIODS;
	
	public StoneFadeEffect(ViewModel model, Turn turn, float cycles) {
		super(model, turn);
		this.NUMBER_OF_PERIODS = cycles;
	}

	@Override
	public boolean isDone() {
		return time > TIME_PER_PERIOD * NUMBER_OF_PERIODS;
	}

	@Override
	public void render(GL11 gl, BoardRenderer renderer) {
		float alpha;
		/* every TIME_PER_PERIOD needs to match 2 * PI */
		alpha = (float)Math.cos(time / TIME_PER_PERIOD * (float)Math.PI * 2.0f) / 2.0f + 0.5f;
		alpha = ALPHA_MIN + alpha * (ALPHA_MAX - ALPHA_MIN);

		gl.glPushMatrix();

	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * (float)x,
	    		0,
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_y - 1) + BoardRenderer.stone_size * 2.0f * (float)y);

		renderer.renderPlayerStone(gl, color, stone, mirror, rotate, alpha * BoardRenderer.DEFAULT_ALPHA);
		gl.glPopMatrix();
	}
}