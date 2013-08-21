package de.saschahlusiak.freebloks.view.effects;

import javax.microedition.khronos.opengles.GL10;

import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.view.BoardRenderer;
import de.saschahlusiak.freebloks.view.model.ViewModel;

public class BoardStoneGlowEffect extends AbsEffect implements Effect {
	int player, x, y;
	ViewModel model;
	Stone stone;
	
	private static final float BLEND_END = 0.55f;
	
	float color[] = new float[4];
	final float[] color1;
	final float[] color2;

	public BoardStoneGlowEffect(ViewModel model, int player, int x, int y, float distance) {
		this.model = model;
		this.player = player;
		this.x = x;
		this.y = y;
		this.time = -0.6f - distance * 0.025f;
		
		this.stone = new Stone();
		stone.init(0);
		
	    color1 = BoardRenderer.stone_color_a[player + 1];
	    color2 = BoardRenderer.stone_color_a[0];
	    
	    color[0] = color1[0];
	    color[1] = color1[1];
	    color[2] = color1[2];
	    color[3] = BoardRenderer.DEFAULT_ALPHA;
	}
	
	@Override
	public boolean isEffected(int x, int y) {
		return (x == this.x && y == this.y);
	}
	
	@Override
	public boolean isDone() {
		return time > BLEND_END;
	}
	
	@Override
	public boolean execute(float elapsed) {
	    float blend = (float)Math.sin(time / BLEND_END * Math.PI) * 0.9f;
	    if (time < 0.0f || time > BLEND_END)
	    	blend = 0.0f;
	    
	    
	    color[0] = color1[0] * (1.0f - blend) + color2[0] * blend;
	    color[1] = color1[1] * (1.0f - blend) + color2[1] * blend;
	    color[2] = color1[2] * (1.0f - blend) + color2[2] * blend;
	    color[3] = BoardRenderer.DEFAULT_ALPHA;
	    
		return super.execute(elapsed);
	}

	@Override
	public void render(GL10 gl, BoardRenderer renderer) {
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		gl.glPushMatrix();
		
	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * (float)x,
	    		0,
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_y - 1) + BoardRenderer.stone_size * 2.0f * (float)y);
		
		renderer.renderStone(gl, color);
		gl.glPopMatrix();

		gl.glDisable(GL10.GL_BLEND);
	}

	@Override
	public void renderShadow(GL10 gl, BoardRenderer renderer) {
		return;
	}
}