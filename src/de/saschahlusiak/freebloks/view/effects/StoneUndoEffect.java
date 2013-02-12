package de.saschahlusiak.freebloks.view.effects;

import javax.microedition.khronos.opengles.GL10;

import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.view.BoardRenderer;

public class StoneUndoEffect extends AbsStoneEffect {
	static private final float TIME = 1.1f;
	
	public StoneUndoEffect(Stone stone, int player, int x, int y) {
		super(stone, player, x, y);
	}
	
	@Override
	public boolean isDone() {
		return time > TIME;
	}

	@Override
	public void render(GL10 gl, BoardRenderer renderer) {
		float z;
		float alpha;
		float rot;
		
		float phase = (float) Math.pow(time / TIME, 0.8);
		
		alpha = 1.0f - phase;
		z = 13.0f * phase;
		rot = phase * 65.0f;
		
		gl.glPushMatrix();
		gl.glTranslatef(0, z, 0);
		
	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(Spiel.DEFAULT_FIELD_SIZE_X - 1) + BoardRenderer.stone_size * 2.0f * (float)x,
	    		0,
	    		-BoardRenderer.stone_size * (float)(Spiel.DEFAULT_FIELD_SIZE_Y- 1) + BoardRenderer.stone_size * 2.0f * (float)y);
		
	    gl.glRotatef(rot, 0, 1, 0);
		renderer.renderPlayerStone(gl, player, stone, alpha * BoardRenderer.DEFAULT_ALPHA);
		gl.glPopMatrix();
	}
}