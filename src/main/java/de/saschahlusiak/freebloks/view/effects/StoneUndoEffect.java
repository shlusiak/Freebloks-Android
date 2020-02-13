package de.saschahlusiak.freebloks.view.effects;

import javax.microedition.khronos.opengles.GL11;

import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.view.BoardRenderer;
import de.saschahlusiak.freebloks.view.model.ViewModel;

public class StoneUndoEffect extends AbsStoneEffect {
	static private final float TIME = 1.1f;

	float phase, z, alpha, rot;

	public StoneUndoEffect(ViewModel model, Turn turn) {
		super(model, turn);
	}

	@Override
	public boolean isDone() {
		return time > TIME;
	}

	@Override
	public boolean execute(float elapsed) {
		super.execute(elapsed);

		phase = (float) Math.pow(time / TIME, 0.8);

		alpha = 1.0f - phase;
		z = 13.0f * phase;
		rot = phase * 65.0f;

		return true;
	}

	@Override
	public void renderShadow(GL11 gl, BoardRenderer renderer) {
		gl.glPushMatrix();

	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(model.board.width - 1) + BoardRenderer.stone_size * 2.0f * x,
	    		0,
	    		-BoardRenderer.stone_size * (float)(model.board.height - 1) + BoardRenderer.stone_size * 2.0f * y);

		renderer.renderShadow(gl,
				stone, color, orientation,
				z,
				rot, 0, 1, 0,
				90 * model.boardObject.centerPlayer,
				alpha, 1.0f);

		gl.glPopMatrix();
	}

	@Override
	public void render(GL11 gl, BoardRenderer renderer) {
		gl.glPushMatrix();
		gl.glTranslatef(0, z, 0);

	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(model.board.width - 1) + BoardRenderer.stone_size * 2.0f * (float)x,
	    		0,
	    		-BoardRenderer.stone_size * (float)(model.board.height - 1) + BoardRenderer.stone_size * 2.0f * (float)y);

	    gl.glRotatef(rot, 0, 1, 0);
		renderer.renderPlayerStone(gl, color, stone, orientation, alpha * BoardRenderer.DEFAULT_ALPHA);
		gl.glPopMatrix();
	}
}