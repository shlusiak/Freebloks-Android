package de.saschahlusiak.freebloks.view.effects;

import javax.microedition.khronos.opengles.GL10;

import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.view.BoardRenderer;
import de.saschahlusiak.freebloks.view.model.ViewModel;

public class StoneRollEffect extends AbsStoneEffect {
	float z, vz;
	boolean done = false;
	ViewModel model;
	
	public StoneRollEffect(ViewModel model, Stone stone, int player, int x, int y, float z, float vz) {
		super(stone, player, x, y);
		this.model = model;
		this.z = z;
		this.vz = vz;
	}
	
	@Override
	public boolean isDone() {
		return done;
	}
	
	@Override
	public boolean execute(float elapsed) {
		final float EPSILON = 0.10f;
		if (z > EPSILON || (vz > EPSILON || vz < -EPSILON)) {
			z -= vz * elapsed;
			vz += elapsed * 62.0f;
			if (z < 0.0f) {
				/* impact */
				vz *= -0.55f;
				z = 0.0f;
				float volume = 0.5f * (float)Math.pow(-vz / 16.0f, 2.0f);
				if (vz > -6.0f)
					vz = 0.0f;
				
				model.soundPool.play(model.soundPool.SOUND_CLICK, volume, 0.90f + (float)Math.random() * 0.2f);
			}
		} else {
			z = 0.0f;
			done = true;
		}
		
		super.execute(elapsed);
		return true;
	}
	
	@Override
	public void renderShadow(GL10 gl, BoardRenderer renderer) {		
		gl.glPushMatrix();
		
	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * (float)x,
	    		0,
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_y - 1) + BoardRenderer.stone_size * 2.0f * (float)y);
	    
	    gl.glDisable(GL10.GL_DEPTH_TEST);
	    
		float offset = (float)(stone.get_stone_size()) - 1.0f;
		float alpha = 0.65f - z / 19.0f;
	    gl.glTranslatef(-2.5f * z * 0.11f, 0, 2.0f * z * 0.11f);

	    gl.glTranslatef(
	    		BoardRenderer.stone_size * offset,
	    		0,
	    		BoardRenderer.stone_size * offset);
	    gl.glScalef(1.0f + z / 18.0f, 1, 1.0f + z / 18.0f);
	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * offset,
	    		0,
	    		-BoardRenderer.stone_size * offset);
	    
		renderer.renderStoneShadow(gl, player, stone, alpha);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glPopMatrix();
	}


	@Override
	public void render(GL10 gl, BoardRenderer renderer) {		
		gl.glPushMatrix();
		
	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * (float)x,
	    		z,
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_y - 1) + BoardRenderer.stone_size * 2.0f * (float)y);
	    
		renderer.renderPlayerStone(gl, player, stone, BoardRenderer.DEFAULT_ALPHA);
		
		gl.glPopMatrix();
	}
}