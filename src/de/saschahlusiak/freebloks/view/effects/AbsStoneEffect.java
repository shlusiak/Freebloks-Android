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
			float x, float y, float z,
			float rotx, float roty, float rotz,
			float alpha, float scale) {
		gl.glPushMatrix();
		
	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * x,
	    		0,
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_y - 1) + BoardRenderer.stone_size * 2.0f * y);
	    
	    gl.glDisable(GL10.GL_DEPTH_TEST);
	    
		float offset = (float)(stone.get_stone_size()) - 1.0f;
		float m_alpha = 0.65f - z / 19.0f;
	    gl.glTranslatef(-2.5f * z * 0.11f, 0, 2.0f * z * 0.11f);

	    gl.glTranslatef(
	    		BoardRenderer.stone_size * offset,
	    		0,
	    		BoardRenderer.stone_size * offset);
		gl.glRotatef(roty, 0, 1, 0);
		gl.glRotatef(rotz, 0, 0, 1);
		gl.glRotatef(rotx, 1, 0, 0);
		gl.glScalef(1.09f, 0, 1.09f);

	    gl.glScalef(1.0f + z / 18.0f, 1, 1.0f + z / 18.0f);
	    gl.glScalef(scale, 0, scale);

	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * offset,
	    		0,
	    		-BoardRenderer.stone_size * offset);
	    
		renderer.renderStoneShadow(gl, player, stone, m_alpha * alpha);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glPopMatrix();
	}
	
	@Override
	public void renderShadow(GL10 gl, BoardRenderer renderer) {
		
	}

}