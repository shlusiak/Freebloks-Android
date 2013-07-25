package de.saschahlusiak.freebloks.view.effects;

import javax.microedition.khronos.opengles.GL10;

import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.view.BoardRenderer;
import de.saschahlusiak.freebloks.view.model.ViewModel;

public class StoneRollEffect extends AbsStoneEffect {
	float z, vz;
	float r, vr;
	float ax, ay, az;
	boolean done = false;
	static final float GRAVITY = 61.0f;
	
	public StoneRollEffect(ViewModel model, Stone stone, int player, int x, int y, float z, float vz) {
		super(model, stone, player, x, y);
		
		this.z = z;
		this.vz = vz;
		r = 8.0f;
		
		final float angx=(float)(Math.random() * 2.0 *Math.PI);
		final float angy=(float)(Math.random() * 2.0 *Math.PI);
		ax=(float)(Math.sin(angx)*Math.cos(angy));
		ay=(float)(Math.sin(angy));
		az=(float)(Math.cos(angx)*Math.cos(angy));
		
		final float p = vz / GRAVITY;
		final float q = 2.0f * -z / GRAVITY;
		if (p * p - q > 0.0) {
			final float time = -p + (float)Math.sqrt(p * p - q);
			vr = -r / time;
		} else vr = r = 0.0f;
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
			vz += elapsed * GRAVITY;
			if (z < 0.0f) {
				/* impact */
				vz *= -0.55f;
				z = 0.0f;
				/* reset rotation */
				r = 0.0f;
				vr = 0.0f;
				float volume = (float)Math.pow(-vz / 16.0f, 2.0f);
				if (vz > -6.0f)
					vz = 0.0f;
				
				model.soundPool.play(model.soundPool.SOUND_CLICK1, volume, 0.90f + (float)Math.random() * 0.2f);
			}
		} else {
			r = vr = 0.0f;
			z = 0.0f;
			done = true;
		}
		r += vr * elapsed;
		
		super.execute(elapsed);
		return true;
	}

	@Override
	public void renderShadow(GL10 gl, BoardRenderer renderer) {
		gl.glPushMatrix();
		
	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * x,
	    		0,
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_y - 1) + BoardRenderer.stone_size * 2.0f * y);

		renderer.renderShadow(gl, 
				stone, player,
				z, 
				r, ax, ay, az,
				90 * model.board.centerPlayer,
				1.0f, 1.0f);
		
		gl.glPopMatrix();
	}

	@Override
	public void render(GL10 gl, BoardRenderer renderer) {		
		gl.glPushMatrix();
		
		float offset = (float)(stone.get_stone_size()) - 1.0f;

	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * (float)x,
	    		z,
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_y - 1) + BoardRenderer.stone_size * 2.0f * (float)y);
	    gl.glTranslatef(
	    		BoardRenderer.stone_size * offset,
	    		0,
	    		BoardRenderer.stone_size * offset);
	    gl.glRotatef(r, ax, ay, az);
	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * offset,
	    		0,
	    		-BoardRenderer.stone_size * offset);
		renderer.renderPlayerStone(gl, player, stone, BoardRenderer.DEFAULT_ALPHA);
		
		gl.glPopMatrix();
	}
}