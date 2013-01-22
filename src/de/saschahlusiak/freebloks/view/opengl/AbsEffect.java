package de.saschahlusiak.freebloks.view.opengl;

import javax.microedition.khronos.opengles.GL10;

import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;

public abstract class AbsEffect {
	AbsEffect() {
		time = 0.0f;
	}
		
	float time;
	
	abstract boolean isDone();
	
	boolean execute(float elapsed) {
		time += elapsed;
		return true;
	}
	
	abstract void render(GL10 gl, Spiel spiel, BoardRenderer renderer);

	boolean isEffected(int x, int y) {
		return false;
	}
	
	
	public static abstract class AbsStoneEffect extends AbsEffect {
		Stone stone;
		int player, x, y;
		AbsStoneEffect(Stone stone, int player, int x, int y) {
			this.stone = stone;
			this.player = player;
			/* x = 19 - y;
			 * y = x;
			 */
			this.y = 19 - y;
			this.x = x;
		}
		
		@Override
		boolean isEffected(int x, int y) {
			/* TODO: fix this nonesense once and for all! */
			y = 19 - y;
			x = x - this.x;
			y = this.y - y;

			if (x < 0 || y < 0)
				return false;
			if (x >= stone.get_stone_size())
				return false;
			if (y >= stone.get_stone_size())
				return false;
			
			if (stone.get_stone_field(y, x) == Stone.STONE_FIELD_FREE)
				return false;
			
			return true;			
		}
	}
	
	public static class FadeEffect extends AbsStoneEffect {
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
		boolean isDone() {
			return time > 8.0f;
		}
		
		@Override
		boolean execute(float elapsed) {
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
		void render(GL10 gl, Spiel spiel, BoardRenderer renderer) {
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
		    		+BoardRenderer.stone_size * (float)(spiel.m_field_size_x - 1) - BoardRenderer.stone_size * 2.0f * (float)y);
			
			renderer.renderPlayerStone(gl, player, stone, alpha);
			gl.glPopMatrix();
		}
	}
}
