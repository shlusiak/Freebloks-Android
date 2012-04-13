package de.saschahlusiak.freebloks.view.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import de.saschahlusiak.freebloks.model.Spiel;

public class BoardRenderer {
	final static float bevel_size = 0.18f;
	final static float bevel_height = 0.15f;
	final static float border_bottom = -0.6f;
	final static float stone_size = 0.45f;
	
	private ShortBuffer _indexBuffer_field;
	private FloatBuffer _vertexBuffer_field;
	private FloatBuffer _normalBuffer_field;

	private ShortBuffer _indexBuffer_border;
	private FloatBuffer _vertexBuffer_border;
	private FloatBuffer _normalBuffer_border;
	
    final float r1 = stone_size;
    final float r2 = stone_size - bevel_size;
    final float y1 = 0.0f;
    final float y2 = bevel_height;
    
    Spiel spiel;
    
    final float[] coords_field = {
    	/* lower side */
        -r2, y1,  r2,	/* 0 */
         r2, y1,  r2,	/* 1 */
         r2, y1, -r2,	/* 2 */
        -r2, y1, -r2,	/* 3 */
        
        /* upper side */
        -r1, y2,  r1,	/* 4 */
         r1, y2,  r1,	/* 5 */
         r1, y2, -r1,	/* 6 */
        -r1, y2, -r1,	/* 7 */
    };
    final float[] normals_field = {
    	/* lower side */
        0, 1, 0,	/* 0 */
        0, 1, 0,	/* 1 */
        0, 1, 0,	/* 2 */
        0, 1, 0,	/* 3 */
        
        /* upper side */
         1, 1, -1,	/* 4 */
        -1, 1, -1,	/* 5 */
        -1, 1,  1,	/* 6 */
         1, 1,  1,	/* 7 */
    };
	short[] _indicesArray_field = {
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

	short[] _indicesArray_border = {
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
	             w, bevel_height,  w,	/* 0 */
	            -w, bevel_height,  w,	/* 1 */
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
	}

	public void render(GL10 gl) {
		float diffuse[]={0.43f,0.43f,0.30f,1.0f};
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
}
