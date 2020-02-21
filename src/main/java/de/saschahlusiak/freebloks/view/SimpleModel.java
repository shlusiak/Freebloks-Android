package de.saschahlusiak.freebloks.view;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class SimpleModel {
	private int num_triangles;

    private ShortBuffer _indexBuffer;
    private FloatBuffer _vertexBuffer;

	private boolean useVBO;
	private int vbo[];

    public SimpleModel(int num_vertices, int num_triangles, boolean useVBO) {
    	this.num_triangles = num_triangles;
		this.useVBO = useVBO;

	    // float has 4 bytes
	    ByteBuffer vbb = ByteBuffer.allocateDirect(num_vertices * 8 * 4);
	    vbb.order(ByteOrder.nativeOrder());
	    _vertexBuffer = vbb.asFloatBuffer();

	    // short has 2 bytes
	    ByteBuffer ibb = ByteBuffer.allocateDirect(num_triangles * 3 * 2);
	    ibb.order(ByteOrder.nativeOrder());
	    _indexBuffer = ibb.asShortBuffer();
    }

    public void commit() {
	    _vertexBuffer.position(0);
	    _indexBuffer.position(0);
    }

	public void addVertex(float x, float y, float z, float nx, float ny, float nz, float tu, float tv) {
		_vertexBuffer.put(x);
		_vertexBuffer.put(y);
		_vertexBuffer.put(z);
		_vertexBuffer.put(nx);
		_vertexBuffer.put(ny);
		_vertexBuffer.put(nz);
		_vertexBuffer.put(tu);
		_vertexBuffer.put(tv);
	}

	public void addIndex(int v1, int v2, int v3) {
		_indexBuffer.put((short)(v1));
		_indexBuffer.put((short)(v2));
		_indexBuffer.put((short)(v3));
	}

	public void bindBuffers(GL11 gl) {
		if (!useVBO) {
			gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
			gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);

			gl.glVertexPointer(3, GL10.GL_FLOAT, 4 * 8, _vertexBuffer.position(0));
			gl.glNormalPointer(GL10.GL_FLOAT, 4 * 8, _vertexBuffer.position(3));
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 4 * 8, _vertexBuffer.position(6));
		} else {
			if (vbo == null)
				updateVBO(gl);

			gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vbo[0]);

			gl.glVertexPointer(3, GL10.GL_FLOAT, 4 * 8, 0);
			gl.glNormalPointer(GL10.GL_FLOAT, 4 * 8, 3 * 4);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 4 * 8, 6 * 4);

			gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, vbo[1]);
		}
	}

	public final void drawElements(GL11 gl) {
		if (useVBO)
			gl.glDrawElements(GL10.GL_TRIANGLES, num_triangles * 3, GL10.GL_UNSIGNED_SHORT, 0);
		else
			gl.glDrawElements(GL10.GL_TRIANGLES, num_triangles * 3, GL10.GL_UNSIGNED_SHORT, _indexBuffer);
	}

	public final void invalidate(GL11 gl) {
		if (gl != null && useVBO) {
			updateVBO(gl);
		} else
			vbo = null;
	}

	private void updateVBO(GL11 gl) {
		vbo = new int[2];
		gl.glGenBuffers(2, vbo, 0);

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vbo[0]);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER,
			_vertexBuffer.capacity() * 4,
			_vertexBuffer.rewind(),
			GL11.GL_STATIC_DRAW);
		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);

		gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, vbo[1]);
		gl.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER,
			_indexBuffer.capacity() * 2,
			_indexBuffer,
			GL11.GL_STATIC_DRAW);
		gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
}
