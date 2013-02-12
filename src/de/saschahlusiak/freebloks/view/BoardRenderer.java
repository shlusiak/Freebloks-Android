package de.saschahlusiak.freebloks.view;

import java.nio.ByteBuffer;

import javax.microedition.khronos.opengles.GL10;
import android.graphics.Bitmap;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;


public class BoardRenderer {
	final static float bevel_size = 0.18f;
	final public static float bevel_height = 0.25f;
	final static float border_bottom = -1.2f;
	final public static float stone_size = 0.45f;
	
	final public static float DEFAULT_ALPHA = 0.75f;
	
	SimpleModel field;
	SimpleModel border;
	SimpleModel stone;
	
    final float r1 = stone_size;
    final float r2 = stone_size - bevel_size;
    final float y1 = -bevel_height;
    final float y2 = 0.0f;
    

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
		w = stone_size * 20;

		border.addVertex(w, 0, w, 0, 0, 1, 0, 0);
		border.addVertex(-w, 0, w, 0, 0, 1, 0, 0);
		border.addVertex(-w, border_bottom, w, 0, 0, 1, 0, 0);
		border.addVertex(w, border_bottom, w, 0, 0, 1, 0, 0);
		border.addIndex(0, 1, 2);
		border.addIndex(0, 2, 3);
		border.commit();
	}
	
	BoardRenderer() {
		initField();
		initBorder();
		initStone();
	}
	
	final float board_diffuse_normal[] = {0.47f,0.47f,0.44f,1.0f};
	final float board_diffuse_available[] = {0.5f,0.65f,0.5f,1.0f};
	final float board_specular[] = {0.24f,0.22f,0.22f,1.0f};
	final float board_shininess[] = {35.0f};

	public void renderBoard(GL10 gl, Spiel spiel, int currentPlayer) {
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, board_specular, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, board_shininess, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, board_diffuse_normal, 0);
	 
	    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, field.getVertexBuffer());
	    gl.glNormalPointer(GL10.GL_FLOAT, 0, field.getNormalBuffer());

	    int w = 20, h = 20;
	    if (spiel != null) {
	    	w = spiel.m_field_size_x;
	    	h = spiel.m_field_size_y;
	    }
	    
	    gl.glPushMatrix();
	    gl.glTranslatef(-stone_size * (float)(w - 1), 0, -stone_size * (float)(h - 1) );
	    for (int y = 0; y < h; y++) {
	    	int x;
	    	for (x = 0; x < w; x++) {
	    		if (currentPlayer >= 0) {
	    			if (spiel.get_game_field(currentPlayer, y, x) == Stone.FIELD_ALLOWED)
	    				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, board_diffuse_available, 0);
	    			else
	    				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, board_diffuse_normal, 0);
	    		}

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
	final float stone_yellow[]={0.80f, 0.80f, 0, 0};
	final float stone_white[]={0.7f, 0.7f, 0.7f, 0};
	final float stone_color_a[][] = { stone_white, stone_blue, stone_yellow, stone_red, stone_green };
	final float stone_specular[]={0.3f, 0.3f, 0.3f, 1.0f};
	final float stone_shininess[]={ 30.0f };

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
	    
	    stone.drawElements(gl);
    	
    	gl.glDisable(GL10.GL_BLEND);
	}
	
	public void renderPlayerStone(GL10 gl, int player, Stone stone, float alpha) {
		int i;
		for (i = 0; i < stone.get_stone_size(); i++) {
			int j;
			for (j = 0; j < stone.get_stone_size(); j++) {				
				if (stone.get_stone_field(i, j) != Stone.STONE_FIELD_FREE)
					renderStone(gl, player, alpha);
				gl.glTranslatef(stone_size * 2.0f, 0, 0);
			}
			gl.glTranslatef(-j*stone_size * 2.0f, 0, stone_size * 2.0f);
		}
	}
	
	public static void myTexImage2D(GL10 gl, Bitmap bitmap) {
		// Don't loading using GLUtils, load using gl-method directly
//		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

		int[] pixels = extractPixels(bitmap);
		byte[] pixelComponents = new byte[pixels.length * 4];
		int byteIndex = 0;
		for (int i = 0; i < pixels.length; i++) {
			int p = pixels[i];
			// Convert to byte representation RGBA required by gl.glTexImage2D.
			// We don't use intbuffer, because then we
			// would be relying on the intbuffer wrapping to write the ints in
			// big-endian format, which means it would work for the wrong
			// reasons, and it might brake on some hardware.
			pixelComponents[byteIndex++] = (byte) ((p >> 16) & 0xFF); // red
			pixelComponents[byteIndex++] = (byte) ((p >> 8) & 0xFF); // green
			pixelComponents[byteIndex++] = (byte) ((p) & 0xFF); // blue
			pixelComponents[byteIndex++] = (byte) (p >> 24); // alpha
		}
		pixels = null;
		ByteBuffer pixelBuffer = ByteBuffer.wrap(pixelComponents);

		gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, bitmap.getWidth(),
				bitmap.getHeight(), 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE,
				pixelBuffer);
		
		pixelComponents = null;
	}

	private static int[] extractPixels(Bitmap src) {
		int x = 0;
		int y = 0;
		int w = src.getWidth();
		int h = src.getHeight();
		int[] colors = new int[w * h];
		src.getPixels(colors, 0, w, x, y, w, h);
		return colors;
	}
}
