package de.saschahlusiak.freebloks.view.effects;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.view.BoardRenderer;
import de.saschahlusiak.freebloks.view.model.Sounds;
import de.saschahlusiak.freebloks.view.model.ViewModel;

public class StoneRollEffect extends AbsStoneEffect {
	float z, vz;
	boolean done = false;
	ViewModel model;
	boolean firstHit = true;
	
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
				float volume = 0.7f * (float)Math.pow(-vz / 16.0f, 1.9f);
				if (vz > -6.0f)
					vz = 0.0f;
				
				if (firstHit)
					model.activity.vibrate_on_place(Global.VIBRATE_SET_STONE);
				model.soundPool.play(model.soundPool.SOUND_CLICK, volume, 0.90f + (float)Math.random() * 0.2f);
				firstHit = false;

			}
		} else {
			z = 0.0f;
			done = true;
		}
		
		super.execute(elapsed);
		return true;
	}

	@Override
	public void render(GL10 gl, BoardRenderer renderer) {
		gl.glPushMatrix();
		gl.glTranslatef(0, z, 0);
		
	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * (float)x,
	    		0,
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_y - 1) + BoardRenderer.stone_size * 2.0f * (float)y);
		
		renderer.renderPlayerStone(gl, player, stone, BoardRenderer.DEFAULT_ALPHA);
		gl.glPopMatrix();
	}
}