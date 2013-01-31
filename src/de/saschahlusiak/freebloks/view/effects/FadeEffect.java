package de.saschahlusiak.freebloks.view.effects;

import javax.microedition.khronos.opengles.GL10;

import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.view.BoardRenderer;

public class FadeEffect extends AbsStoneEffect {
	float z, vz;
	
	public FadeEffect(Stone stone, int player, int x, int y, float z, float vz) {
		super(stone, player, x, y);
		this.z = z;
		this.vz = vz;
	}
	
	public FadeEffect(Stone stone, int player, int x, int y) {
		this(stone, player, x, y, 0.5f, -8.0f);
	}
	
	@Override
	public boolean isDone() {
		return time > 8.0f;
	}
	
	@Override
	public boolean execute(float elapsed) {
		final float EPSILON = 0.10f;
		if (z > EPSILON || (vz > EPSILON || vz < -EPSILON)) {
			z -= vz * elapsed;
			vz += elapsed * 60.0f;
			if (z < 0.0f) {
				vz *= -0.50f;
				z = 0.0f;
				if (vz > -4.5f)
					vz = 0.0f;
			}
		} else z = 0.0f;
		
		super.execute(elapsed);
		return true;
	}

	@Override
	public void render(GL10 gl, Spiel spiel, BoardRenderer renderer) {
		float alpha;
		gl.glPushMatrix();
		gl.glTranslatef(0, z, 0);
		if (time > 1.0f)
			alpha = 1.0f - (1.0f + (float)Math.sin((time - 1.0f) * 5.0f)) / 2.0f * 0.7f;
		else
			alpha = 1.0f;
		
	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * (float)x,
	    		0,
	    		-BoardRenderer.stone_size * (float)(spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * (float)y);
		
		renderer.renderPlayerStone(gl, player, stone, alpha);
		gl.glPopMatrix();
	}
}