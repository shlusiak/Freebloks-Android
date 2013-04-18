package de.saschahlusiak.freebloks.view.effects;

import javax.microedition.khronos.opengles.GL10;

import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.view.BoardRenderer;
import de.saschahlusiak.freebloks.view.model.ViewModel;

public abstract class AbsStoneEffect extends AbsEffect implements Effect {
	Stone stone;
	int player, x, y;
	ViewModel model;
	
	AbsStoneEffect(ViewModel model, Stone stone, int player, int x, int y) {
		this.model = model;
		this.stone = stone;
		this.player = player;
		this.x = x;
		this.y = y;
	}
	
	@Override
	public boolean isEffected(int x, int y) {
		x = x - this.x;
		y = y - this.y;

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
	
	public void renderShadow(GL10 gl, BoardRenderer renderer, 
			float height,
			float ang, float ax, float ay, float az,
			float alpha, float scale) {
	    gl.glDisable(GL10.GL_DEPTH_TEST);
	    
	    gl.glScalef(scale, 0.01f, scale);

	    
		float offset = (float)(stone.get_stone_size()) - 1.0f;
		float m_alpha = 0.65f - height / 19.0f;
	    gl.glTranslatef(-2.5f * height * 0.11f, 0, 2.0f * height * 0.11f);

	    gl.glTranslatef(
	    		BoardRenderer.stone_size * offset,
	    		0,
	    		BoardRenderer.stone_size * offset);
		gl.glRotatef(ang, ax, ay, az);

	    gl.glScalef(1.0f + height / 16.0f, 1, 1.0f + height / 16.0f);

	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * offset,
	    		0,
	    		-BoardRenderer.stone_size * offset);
	    
		renderer.renderStoneShadow(gl, player, stone, m_alpha * alpha);
		gl.glEnable(GL10.GL_DEPTH_TEST);
	}
	
	@Override
	public void renderShadow(GL10 gl, BoardRenderer renderer) {
		
	}

}