package de.saschahlusiak.freebloks.view;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class SimpleModel {
	int num_triangles;
	
    private ShortBuffer _indexBuffer;
    private FloatBuffer _vertexBuffer;
    private FloatBuffer _normalBuffer;
    private FloatBuffer _textureBuffer;
        
    public SimpleModel(int num_vertices, int num_triangles) {
    	this.num_triangles = num_triangles;
    	
	    // float has 4 bytes
	    ByteBuffer vbb = ByteBuffer.allocateDirect(num_vertices * 3 * 4);
	    vbb.order(ByteOrder.nativeOrder());
	    _vertexBuffer = vbb.asFloatBuffer();
	    
	    vbb = ByteBuffer.allocateDirect(num_vertices * 3 * 4);
	    vbb.order(ByteOrder.nativeOrder());
	    _normalBuffer = vbb.asFloatBuffer();
	 
	    // short has 2 bytes
	    ByteBuffer ibb = ByteBuffer.allocateDirect(num_triangles * 3 * 2);
	    ibb.order(ByteOrder.nativeOrder());
	    _indexBuffer = ibb.asShortBuffer();
	    
	    ByteBuffer tbb = ByteBuffer.allocateDirect(num_vertices * 2 * 4);
	    tbb.order(ByteOrder.nativeOrder());
	    _textureBuffer = tbb.asFloatBuffer();
    }
    
    public void commit() {
	    _vertexBuffer.position(0);
	    _normalBuffer.position(0);
	    _indexBuffer.position(0);
	    _textureBuffer.position(0);   
   	
    }
    
	public void addVertex(float x, float y, float z, float nx, float ny, float nz, float tu, float tv) {
		_vertexBuffer.put(x);
		_vertexBuffer.put(y);
		_vertexBuffer.put(z);
		_normalBuffer.put(nx);
		_normalBuffer.put(ny);
		_normalBuffer.put(nz);
		_textureBuffer.put(tu);
		_textureBuffer.put(tv);
	}

	public void addIndex(int v1, int v2, int v3) {
		_indexBuffer.put((short)(v1));
		_indexBuffer.put((short)(v2));
		_indexBuffer.put((short)(v3));
	}
	
	public FloatBuffer getVertexBuffer() {
		return _vertexBuffer;
	}
	public FloatBuffer getNormalBuffer() {
		return _normalBuffer;
	}
	public FloatBuffer getTextureBuffer() {
		return _textureBuffer;
	}
	public ShortBuffer getIndexBuffer() {
		return _indexBuffer;
	}

	public void drawElements(GL10 gl) {
		gl.glDrawElements(GL10.GL_TRIANGLES, num_triangles * 3, GL10.GL_UNSIGNED_SHORT, getIndexBuffer());
	}
}
