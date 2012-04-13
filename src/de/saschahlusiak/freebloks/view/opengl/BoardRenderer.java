package de.saschahlusiak.freebloks.view.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;

public class BoardRenderer {
	final static float bevel_size = 0.18f;
	final static float bevel_height = 0.15f;
	final static float border_bottom = -0.6f;
	final public static float stone_size = 0.45f;
	
	private ShortBuffer _indexBuffer_field;
	private FloatBuffer _vertexBuffer_field;
	private FloatBuffer _normalBuffer_field;

	private ShortBuffer _indexBuffer_border;
	private FloatBuffer _vertexBuffer_border;
	private FloatBuffer _normalBuffer_border;

	private ShortBuffer _indexBuffer_stone;
	private FloatBuffer _normalBuffer_stone;
	
    final float r1 = stone_size;
    final float r2 = stone_size - bevel_size;
    final float y1 = -bevel_height;
    final float y2 = 0.0f;
    
    Spiel spiel;
    
    final private float[] coords_field = {
    	/* lower */
        -r2, y1,  r2,	/* 0 */
         r2, y1,  r2,	/* 1 */
         r2, y1, -r2,	/* 2 */
        -r2, y1, -r2,	/* 3 */
        
        /* middle */
        -r1, y2,  r1,	/* 4 */
         r1, y2,  r1,	/* 5 */
         r1, y2, -r1,	/* 6 */
        -r1, y2, -r1,	/* 7 */
    };
    
    final private float[] normals_field = {
    	/* lower */
        0, 1, 0,	/* 0 */
        0, 1, 0,	/* 1 */
        0, 1, 0,	/* 2 */
        0, 1, 0,	/* 3 */
        
        /* upper */
         1, 1, -1,	/* 4 */
        -1, 1, -1,	/* 5 */
        -1, 1,  1,	/* 6 */
         1, 1,  1,	/* 7 */
    };
    
	final private short[] _indicesArray_field = {
		0, 1, 2,
		0, 2, 3,
		0, 5, 1,
		0, 4, 5,
		1, 5, 6,
		1, 6, 2,
		2, 6, 7,
		2, 7, 3,
		3, 7, 4,
		3, 4, 0
	};
	
    final private float[] normals_stone = {
    	/* lower */
        0, -1, 0,	/* 0 */
        0, -1, 0,	/* 1 */
        0, -1, 0,	/* 2 */
        0, -1, 0,	/* 3 */
        
        /* upper */
         1, 1, -1,	/* 4 */
        -1, 1, -1,	/* 5 */
        -1, 1,  1,	/* 6 */
         1, 1,  1,	/* 7 */
    };
    
	final private short[] _indicesArray_stone = {
		0, 2, 1,
		0, 3, 2,
		0, 1, 5,
		0, 5, 4,
		1, 6, 5,
		1, 2, 6,
		2, 7, 6,
		2, 3, 7,
		3, 4, 7,
		3, 0, 4
	};	

	final private short[] _indicesArray_border = {
			0, 1, 2,
			0, 2, 3,
	};

	private void initField() {
	    // float has 4 bytes
	    ByteBuffer vbb = ByteBuffer.allocateDirect(coords_field.length * 4);
	    vbb.order(ByteOrder.nativeOrder());
	    _vertexBuffer_field = vbb.asFloatBuffer();
	    
	    vbb = ByteBuffer.allocateDirect(normals_field.length * 4);
	    vbb.order(ByteOrder.nativeOrder());
	    _normalBuffer_field = vbb.asFloatBuffer();
	 
	    // short has 2 bytes
	    ByteBuffer ibb = ByteBuffer.allocateDirect(_indicesArray_field.length * 2);
	    ibb.order(ByteOrder.nativeOrder());
	    _indexBuffer_field = ibb.asShortBuffer();

	    _vertexBuffer_field.put(coords_field);
	    _normalBuffer_field.put(normals_field);
	    _indexBuffer_field.put(_indicesArray_field);

	    _vertexBuffer_field.position(0);
	    _normalBuffer_field.position(0);
	    _indexBuffer_field.position(0);
	}

	private void initStone() {
	    ByteBuffer vbb = ByteBuffer.allocateDirect(normals_stone.length * 4);
	    vbb.order(ByteOrder.nativeOrder());
	    _normalBuffer_stone = vbb.asFloatBuffer();
	 
	    // short has 2 bytes
	    ByteBuffer ibb = ByteBuffer.allocateDirect(_indicesArray_stone.length * 2);
	    ibb.order(ByteOrder.nativeOrder());
	    _indexBuffer_stone = ibb.asShortBuffer();

	    _normalBuffer_stone.put(normals_stone);
	    _indexBuffer_stone.put(_indicesArray_stone);

	    _normalBuffer_stone.position(0);
	    _indexBuffer_stone.position(0);
	}
	
	private void initBorder() {
		float w;
	    final float[] normals_border = {
            0, 0, 1,	/* 0 */
            0, 0, 1,	/* 1 */
            0, 0, 1,	/* 2 */
            0, 0, 1,	/* 3 */
	    };
	    
	    if (spiel == null)
			w = 5.0f;
		else
			w = stone_size * spiel.m_field_size_x;

	    final float[] coords_border = {
	        	/* lower side */
	             w, 0,  w,	/* 0 */
	            -w, 0,  w,	/* 1 */
	            -w, border_bottom,  w,	/* 2 */
	             w, border_bottom,  w,	/* 3 */
	        };	    
	    
	    ByteBuffer vbb = ByteBuffer.allocateDirect(3 * 4 * 4);
	    vbb.order(ByteOrder.nativeOrder());
	    _vertexBuffer_border = vbb.asFloatBuffer();

	    vbb = ByteBuffer.allocateDirect(normals_border.length * 4);
	    vbb.order(ByteOrder.nativeOrder());
	    _normalBuffer_border = vbb.asFloatBuffer();

	    // short has 2 bytes
	    ByteBuffer ibb = ByteBuffer.allocateDirect(_indicesArray_border.length * 2);
	    ibb.order(ByteOrder.nativeOrder());
	    _indexBuffer_border = ibb.asShortBuffer();

	    _vertexBuffer_border.put(coords_border);
	    _normalBuffer_border.put(normals_border);
	    _indexBuffer_border.put(_indicesArray_border);

	    _vertexBuffer_border.position(0);
	    _normalBuffer_border.position(0);
	    _indexBuffer_border.position(0);
	}
	
	BoardRenderer(Spiel spiel) {
		this.spiel = spiel;
		initField();
		initBorder();
		initStone();
	}

	public void renderBoard(GL10 gl) {
		float diffuse[]={0.43f,0.43f,0.38f,1.0f};
		float specular[]={0.27f,0.25f,0.25f,1.0f};
		float shininess[]={35.0f};	    

		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, diffuse, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, specular, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, shininess, 0);
	 
	    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, _vertexBuffer_field);
	    gl.glNormalPointer(GL10.GL_FLOAT, 0, _normalBuffer_field);

	    if (spiel == null)
	    	return;
	    
	    gl.glPushMatrix();
	    gl.glTranslatef(-stone_size * (float)(spiel.m_field_size_x - 1), 0, -stone_size * (float)(spiel.m_field_size_x - 1) );
	    for (int y = 0; y < spiel.m_field_size_y; y++) {
	    	int x;
	    	for (x = 0; x < spiel.m_field_size_y; x++) {
	    		gl.glDrawElements(GL10.GL_TRIANGLES, _indicesArray_field.length, GL10.GL_UNSIGNED_SHORT, _indexBuffer_field);	    		
	    		gl.glTranslatef(stone_size * 2.0f, 0, 0);
	    	}
	    	gl.glTranslatef(- x * stone_size * 2.0f, 0, stone_size * 2.0f);
	    }
	    gl.glPopMatrix();
	    
	    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, _vertexBuffer_border);
	    gl.glNormalPointer(GL10.GL_FLOAT, 0, _normalBuffer_border);
	    for (int i = 0; i < 4; i++) {
	    	gl.glDrawElements(GL10.GL_TRIANGLES, _indicesArray_border.length, GL10.GL_UNSIGNED_SHORT, _indexBuffer_border);
	    	gl.glRotatef(90, 0, 1, 0);
	    }
	}
	
	
	public void renderField(GL10 gl) {
	    if (spiel == null)
	    	return;
	    
	    gl.glPushMatrix();
	    gl.glTranslatef(-stone_size * (float)(spiel.m_field_size_x - 1), 0, -stone_size * (float)(spiel.m_field_size_x - 1) );
	    for (int y = 0; y < spiel.m_field_size_y; y++) {
	    	int x;
	    	for (x = 0; x < spiel.m_field_size_y; x++) {
	    		if (spiel.get_game_field(y, x) != Stone.FIELD_FREE) {
	    			renderStone(gl, spiel.get_game_field(y, x), 0.65f);
	    		}	    		
	    		gl.glTranslatef(stone_size * 2.0f, 0, 0);
	    	}
	    	gl.glTranslatef(- x * stone_size * 2.0f, 0, stone_size * 2.0f);
	    }
	    gl.glPopMatrix();
	}
	
	public void renderStone(GL10 gl, int color, float alpha) {
		float red[]={0.75f, 0, 0, alpha};
		float blue[]={0.0f, 0.05f, 0.8f, alpha};
		float green[]={0.0f, 0.75f, 0, alpha};
		float yellow[]={0.75f, 0.75f, 0, alpha};
		float white[]={0.7f, 0.7f, 0.7f, alpha};
		float color_a[][] = { white, blue, yellow, red, green };
		
		float specular[]={0.4f,0.4f,0.4f,1.0f};
		float shininess[]={40.0f};
		
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, color_a[color + 1], 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, specular, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, shininess, 0);
	 
	    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, _vertexBuffer_field);
	    gl.glNormalPointer(GL10.GL_FLOAT, 0, _normalBuffer_stone);
	    
   		gl.glDrawElements(GL10.GL_TRIANGLES, _indicesArray_stone.length, GL10.GL_UNSIGNED_SHORT, _indexBuffer_stone);
    	gl.glRotatef(180, 1, 0, 0);
   		gl.glDrawElements(GL10.GL_TRIANGLES, _indicesArray_stone.length, GL10.GL_UNSIGNED_SHORT, _indexBuffer_stone);
    	gl.glRotatef(180, 1, 0, 0);
    	
    	gl.glDisable(GL10.GL_BLEND);
	}
	
	public void renderPlayerStones(GL10 gl, int player) {
		if (spiel == null)
			return;
		
		gl.glPushMatrix();
		gl.glTranslatef(-stone_size * (spiel.m_field_size_x), 0, stone_size * (spiel.m_field_size_x + 5));
		
		gl.glScalef(0.6f, 0.6f, 0.6f);
		Player p = spiel.get_player(player);
		for (int i = 0; i < Stone.STONE_COUNT_ALL_SHAPES; i++) {
			Stone s = p.get_stone(i);
			if (s.get_available() > 0)
				renderPlayerStone(gl, player, s);
			gl.glTranslatef(stone_size * 2.0f * 5.5f, 0, 0);
			if (i % 7 == 6)
				gl.glTranslatef(-stone_size * 2.0f * 5.5f * 7, 0, stone_size * 2.0f * 5.5f);
		}
		
		gl.glPopMatrix();
	}
	
	public void renderPlayerStone(GL10 gl, int player, Stone stone) {
		int i;
		gl.glTranslatef(-stone.get_stone_size() * stone_size, 0, -stone.get_stone_size() * stone_size);
		for (i = 0; i < stone.get_stone_size(); i++) {
			int j;
			for (j = 0; j < stone.get_stone_size(); j++) {				
				if (stone.get_stone_field(j,  i) != Stone.STONE_FIELD_FREE)
					renderStone(gl, player, 0.65f);
				gl.glTranslatef(stone_size * 2.0f, 0, 0);
			}
			gl.glTranslatef(-j*stone_size * 2.0f, 0, stone_size * 2.0f);
		}
		gl.glTranslatef(0, 0, -i * stone_size * 2.0f);
		gl.glTranslatef(stone.get_stone_size() * stone_size, 0, stone.get_stone_size() * stone_size);
	}
}
