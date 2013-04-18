package de.saschahlusiak.freebloks.view.effects;

import javax.microedition.khronos.opengles.GL10;

import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.view.BoardRenderer;
import de.saschahlusiak.freebloks.view.model.ViewModel;

public class StoneUndoEffect extends AbsStoneEffect {
	static private final float TIME = 1.1f;
	
	float phase, z, alpha, rot;
		
	public StoneUndoEffect(ViewModel model, Stone stone, int player, int x, int y) {
		super(model, stone, player, x, y);
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
	public void renderShadow(GL10 gl, BoardRenderer renderer) {
		gl.glPushMatrix();
		
	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * x,
	    		0,
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_y - 1) + BoardRenderer.stone_size * 2.0f * y);

		renderShadow(gl, renderer, 
				z,
				rot, 0, 1, 0,
				alpha, 1.0f);
		
		gl.glPopMatrix();
	}

	@Override
	public void render(GL10 gl, BoardRenderer renderer) {		
		gl.glPushMatrix();
		gl.glTranslatef(0, z, 0);
		
	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * (float)x,
	    		0,
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_y - 1) + BoardRenderer.stone_size * 2.0f * (float)y);
		
	    gl.glRotatef(rot, 0, 1, 0);
		renderer.renderPlayerStone(gl, player, stone, alpha * BoardRenderer.DEFAULT_ALPHA);
		gl.glPopMatrix();
	}
}