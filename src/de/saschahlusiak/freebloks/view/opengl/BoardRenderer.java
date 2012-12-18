package de.saschahlusiak.freebloks.view.opengl;

import javax.microedition.khronos.opengles.GL10;
import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;

public class BoardRenderer {
	final static float bevel_size = 0.18f;
	final static float bevel_height = 0.25f;
	final static float border_bottom = -1.2f;
	final public static float stone_size = 0.45f;
	
	SimpleModel field;
	SimpleModel border;
	SimpleModel stone;
	
    final float r1 = stone_size;
    final float r2 = stone_size - bevel_size;
    final float y1 = -bevel_height;
    final float y2 = 0.0f;
    
    Spiel spiel;


	private void initField() {
		field = new SimpleModel(8, 10);
		/* bottom, inner */
		field.addVertex(-r2, y1, +r2,  0, 1, 0,  0, 0);
		field.addVertex(+r2, y1, +r2,  0, 1, 0,  0, 0);
		field.addVertex(+r2, y1, -r2,  0, 1, 0,  0, 0);
		field.addVertex(-r2, y1, -r2,  0, 1, 0,  0, 0);

		/* top, outer */
		field.addVertex(-r1, y2, +r1, +1, 1, -1,  0, 0);
		field.addVertex(+r1, y2, +r1, -1, 1, -1,  0, 0);
		field.addVertex(+r1, y2, -r1, -1, 1, +1,  0, 0);
		field.addVertex(-r1, y2, -r1, +1, 1, +1,  0, 0);
		
		field.addIndex(0, 1, 2);
		field.addIndex(0, 2, 3);
		field.addIndex(0, 5, 1);
		field.addIndex(0, 4, 5);
		field.addIndex(1, 5, 6);
		field.addIndex(1, 6, 2);
		field.addIndex(2, 6, 7);
		field.addIndex(2, 7, 3);
		field.addIndex(3, 7, 4);
		field.addIndex(3, 4, 0);
		
		field.commit();
	}

	private void initStone() {
		stone = new SimpleModel(12, 20);
		/* top, inner */
		stone.addVertex(-r2, -y1, +r2,  0, 1, 0,  0, 0);
		stone.addVertex(+r2, -y1, +r2,  0, 1, 0,  0, 0);
		stone.addVertex(+r2, -y1, -r2,  0, 1, 0,  0, 0);
		stone.addVertex(-r2, -y1, -r2,  0, 1, 0,  0, 0);

		/* middle, outer */
		stone.addVertex(-r1, y2, +r1, -1, 0, +1,  0, 0);
		stone.addVertex(+r1, y2, +r1, +1, 0, +1,  0, 0);
		stone.addVertex(+r1, y2, -r1, +1, 0, -1,  0, 0);
		stone.addVertex(-r1, y2, -r1, -1, 0, -1,  0, 0);

		/* bottom, inner */
		stone.addVertex(-r2, y1, +r2,  0, -1, 0,  0, 0);
		stone.addVertex(+r2, y1, +r2,  0, -1, 0,  0, 0);
		stone.addVertex(+r2, y1, -r2,  0, -1, 0,  0, 0);
		stone.addVertex(-r2, y1, -r2,  0, -1, 0,  0, 0);
		
		/* top */
		stone.addIndex(0, 1, 2);
		stone.addIndex(0, 2, 3);
		stone.addIndex(0, 5, 1);
		stone.addIndex(0, 4, 5);
		stone.addIndex(1, 5, 6);
		stone.addIndex(1, 6, 2);
		stone.addIndex(2, 6, 7);
		stone.addIndex(2, 7, 3);
		stone.addIndex(3, 7, 4);
		stone.addIndex(3, 4, 0);
		
		/* bottom */
		stone.addIndex(8, 10, 9);
		stone.addIndex(8, 11, 10);
		stone.addIndex(8, 9, 5);
		stone.addIndex(8, 5, 4);
		stone.addIndex(9, 6, 5);
		stone.addIndex(9, 10, 6);
		stone.addIndex(10, 7, 6);
		stone.addIndex(10, 11, 7);
		stone.addIndex(11, 4, 7);
		stone.addIndex(11, 8, 4);
		
		stone.commit();
	}
	
	private void initBorder() {
		border = new SimpleModel(4, 2);
		float w;
	    
	    if (spiel == null)
			w = 5.0f;
		else
			w = stone_size * spiel.m_field_size_x;

		border.addVertex(w, 0, w, 0, 0, 1, 0, 0);
		border.addVertex(-w, 0, w, 0, 0, 1, 0, 0);
		border.addVertex(-w, border_bottom, w, 0, 0, 1, 0, 0);
		border.addVertex(w, border_bottom, w, 0, 0, 1, 0, 0);
		border.addIndex(0, 1, 2);
		border.addIndex(0, 2, 3);
		border.commit();
	}
	
	BoardRenderer(Spiel spiel) {
		this.spiel = spiel;
		initField();
		initBorder();
		initStone();
	}
	
	final float board_diffuse_normal[] = {0.52f,0.52f,0.50f,1.0f};
	final float board_diffuse_available[] = {0.5f,0.65f,0.5f,1.0f};
	final float board_specular[] = {0.27f,0.25f,0.25f,1.0f};
	final float board_shininess[] = {35.0f};

	public void renderBoard(GL10 gl, int currentPlayer) {
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, board_diffuse_normal, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, board_specular, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, board_shininess, 0);
	 
	    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, field.getVertexBuffer());
	    gl.glNormalPointer(GL10.GL_FLOAT, 0, field.getNormalBuffer());

	    if (spiel == null)
	    	return;
	    
	    gl.glPushMatrix();
	    gl.glTranslatef(-stone_size * (float)(spiel.m_field_size_x - 1), 0, -stone_size * (float)(spiel.m_field_size_x - 1) );
	    for (int y = 0; y < spiel.m_field_size_y; y++) {
	    	int x;
	    	for (x = 0; x < spiel.m_field_size_y; x++) {
	    		if (currentPlayer >= 0 && spiel.get_game_field(currentPlayer, y, x) == Stone.FIELD_ALLOWED)
	    			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, board_diffuse_available, 0);
	    		else
	    			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, board_diffuse_normal, 0);

	    		field.drawElements(gl);
	    		gl.glTranslatef(stone_size * 2.0f, 0, 0);
	    	}
	    	gl.glTranslatef(- x * stone_size * 2.0f, 0, stone_size * 2.0f);
	    }
	    gl.glPopMatrix();
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, board_diffuse_normal, 0);

	    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, border.getVertexBuffer());
	    gl.glNormalPointer(GL10.GL_FLOAT, 0, border.getNormalBuffer());
	    for (int i = 0; i < 4; i++) {
	    	border.drawElements(gl);
	    	gl.glRotatef(90, 0, 1, 0);
	    }
	}
	
	final float stone_red[]={0.75f, 0, 0, 0};
	final float stone_blue[]={0.0f, 0.05f, 0.8f, 0};
	final float stone_green[]={0.0f, 0.75f, 0, 0};
	final float stone_yellow[]={0.75f, 0.75f, 0, 0};
	final float stone_white[]={0.7f, 0.7f, 0.7f, 0};
	final float stone_color_a[][] = { stone_white, stone_blue, stone_yellow, stone_red, stone_green };
	final float stone_specular[]={0.4f,0.4f,0.4f,1.0f};
	final float stone_shininess[]={40.0f};

	public void renderStone(GL10 gl, int color, float alpha) {
		final float c[] = stone_color_a[color + 1];
		c[3] = alpha;
		
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, c, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, stone_specular, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, stone_shininess, 0);
		
	 
	    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, stone.getVertexBuffer());
	    gl.glNormalPointer(GL10.GL_FLOAT, 0, stone.getNormalBuffer());
	    
	    stone.drawElements(gl, GL10.GL_TRIANGLES);
    	
    	gl.glDisable(GL10.GL_BLEND);
	}
	
	public void renderPlayerStones(GL10 gl, int player, float wheelAngle, int highlightStone, Stone currentStone) {
		final float da = 17.0f;
		float angle = wheelAngle + 9.5f * 0.5f * da;
		
		if (spiel == null)
			return;
		
		
		gl.glPushMatrix();
		gl.glTranslatef(0, -stone_size * 27.0f, 0);
		gl.glRotatef(wheelAngle, 0, 0, 1);
		gl.glTranslatef(-stone_size * 5.1f * 6.5f, 0, stone_size * (spiel.m_field_size_x + 8));
		gl.glRotatef(9.5f * 0.5f * da, 0, 0, 1);
		gl.glPushMatrix();
//		gl.glScalef(0.6f, 0.6f, 0.6f);
		Player p = spiel.get_player(player);
		for (int i = 0; i < Stone.STONE_COUNT_ALL_SHAPES; i++) {
			Stone s = p.get_stone(i);
			float alpha = 1.0f;
			while (angle < -180f)
				angle += 360.0f;
			while (angle > 180.0f)
				angle -= 360.0f;
			
			alpha = 1.0f / (1.0f + Math.abs(angle) / 47.0f);
			
			if (s.get_available() - ((s == currentStone) ? 1 : 0) > 0) {
				gl.glRotatef(90 * player, 0, 1, 0);
				renderPlayerStone(gl, (i == highlightStone) ? -1 : player, s, alpha);
				gl.glRotatef(-90 * player, 0, 1, 0);
			}
			
			gl.glTranslatef(stone_size * 2.0f * 5.1f, 0, 0);
			if (i % 11 == 10) {
				gl.glPopMatrix();
				gl.glTranslatef(0, 0, stone_size * 2.0f * 5.1f);
				angle = wheelAngle + 9.5f * 0.5f * da;
				gl.glPushMatrix();
			} else {
				gl.glRotatef(-da, 0, 0, 1);
				angle -= da;
			}
		}
		
		gl.glPopMatrix();
		gl.glPopMatrix();
	}
	
	public void renderPlayerStone(GL10 gl, int player, Stone stone, float alpha) {
		int i;
		gl.glTranslatef(-stone.get_stone_size() * stone_size, 0, -stone.get_stone_size() * stone_size);
		for (i = 0; i < stone.get_stone_size(); i++) {
			int j;
			for (j = 0; j < stone.get_stone_size(); j++) {				
				if (stone.get_stone_field(i,  j) != Stone.STONE_FIELD_FREE)
					renderStone(gl, player, 0.65f * alpha);
				gl.glTranslatef(stone_size * 2.0f, 0, 0);
			}
			gl.glTranslatef(-j*stone_size * 2.0f, 0, stone_size * 2.0f);
		}
		gl.glTranslatef(0, 0, -i * stone_size * 2.0f);
		gl.glTranslatef(stone.get_stone_size() * stone_size, 0, stone.get_stone_size() * stone_size);
	}
	
	public void renderPlayerStone(GL10 gl, int player, Stone stone, float alpha, int x, int y) {
	    gl.glTranslatef(
	    		-stone_size * (float)(spiel.m_field_size_x - 1) + stone_size * 2.0f * (float)x,
	    		0,
	    		+stone_size * (float)(spiel.m_field_size_x - 1) - stone_size * 2.0f * (float)y);
	    
		for (int i = 0; i < stone.get_stone_size(); i++) {
			int j;
			for (j = 0; j < stone.get_stone_size(); j++) {				
				if (stone.get_stone_field(i,  j) != Stone.STONE_FIELD_FREE)
					renderStone(gl, player, alpha);
				gl.glTranslatef(stone_size * 2.0f, 0, 0);
			}
			gl.glTranslatef(-j*stone_size * 2.0f, 0, stone_size * 2.0f);
		}
	}
}
