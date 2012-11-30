package de.saschahlusiak.freebloks.view.opengl;

import javax.microedition.khronos.opengles.GL10;

public abstract class AbsEffect {
	public AbsEffect() {
		time = 0.0f;
	}
		
	float time;
	
	abstract boolean isDone();
	
	boolean execute(float elapsed) {
		time += elapsed;
		return true;
	}
	
	abstract void render(GL10 gl, BoardRenderer renderer);

	boolean isEffected(int x, int y) {
		return false;
	}
	
	
	
	
	public static class FadeEffect extends AbsEffect {
		public FadeEffect() {
			
		}
		
		@Override
		boolean isDone() {
			return false;
		}

		@Override
		void render(GL10 gl, BoardRenderer renderer) {
			
		}
	}
}
