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

	
	private ShortBuffer _indexBuffer;
	private FloatBuffer _vertexBuffer;
	private FloatBuffer _normalBuffer;
	 	 
    final float r1 = stone_size;
    final float r2 = stone_size - bevel_size;
    final float y1 = 0.0f;
    final float y2 = bevel_height;   
    float[] coords = {
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
    float[] normals = {
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
	short[] _indicesArray = {
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
	// code snipped
	 
	private void initField() {			
	    // float has 4 bytes
	    ByteBuffer vbb = ByteBuffer.allocateDirect(coords.length * 4);
	    vbb.order(ByteOrder.nativeOrder());
	    _vertexBuffer = vbb.asFloatBuffer();
	    
	    vbb = ByteBuffer.allocateDirect(normals.length * 4);
	    vbb.order(ByteOrder.nativeOrder());
	    _normalBuffer = vbb.asFloatBuffer();	    
	 
	    // short has 2 bytes
	    ByteBuffer ibb = ByteBuffer.allocateDirect(_indicesArray.length * 2);
	    ibb.order(ByteOrder.nativeOrder());
	    _indexBuffer = ibb.asShortBuffer();

	    _vertexBuffer.put(coords);
	    _normalBuffer.put(normals);
	    _indexBuffer.put(_indicesArray);
	 
	    _vertexBuffer.position(0);
	    _normalBuffer.position(0);
	    _indexBuffer.position(0);
	    
	}
	
	BoardRenderer() {
		initField();
	}
	
	public void render(GL10 gl, Spiel spiel) {
		float diffuse[]={0.43f,0.43f,0.30f,1.0f};
		float specular[]={0.27f,0.25f,0.25f,1.0f};
		float shininess[]={35.0f};	    

		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, diffuse, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, specular, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, shininess, 0);
	 
	    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, _vertexBuffer);
	    gl.glNormalPointer(GL10.GL_FLOAT, 0, _normalBuffer);

	    if (spiel == null)
	    	return;
	    
	    gl.glPushMatrix();
	    gl.glTranslatef(-stone_size * spiel.m_field_size_x, 0, -stone_size * spiel.m_field_size_x);
	    for (int y = 0; y < spiel.m_field_size_y; y++) {
	    	int x;
	    	for (x = 0; x < spiel.m_field_size_y; x++) {
	    		gl.glTranslatef(stone_size* 2.0f, 0, 0);
	    		gl.glDrawElements(GL10.GL_TRIANGLES, _indicesArray.length, GL10.GL_UNSIGNED_SHORT, _indexBuffer);
	    	}
	    	gl.glTranslatef(- x * stone_size * 2.0f, 0, stone_size * 2.0f);
	    }
	    gl.glPopMatrix();
	}
}
