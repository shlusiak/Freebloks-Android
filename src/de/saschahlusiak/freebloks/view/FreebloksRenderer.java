package de.saschahlusiak.freebloks.view;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.view.effects.Effect;
import de.saschahlusiak.freebloks.view.model.ViewModel;

public class FreebloksRenderer implements GLSurfaceView.Renderer {
	public final float light0_ambient[] = {0.35f, 0.35f, 0.35f, 1.0f};
	public final float light0_diffuse[] = {0.8f, 0.8f, 0.8f, 1.0f};
	public final float light0_specular[] = {1.0f, 1.0f, 1.0f, 1.0f};
	public final float light0_pos[]    = {2.5f, 5f, -2.0f, 0.0f};
	public float width = 1, height = 1;
	int currentPlayer;
	ViewModel model;
	Context context;
	public float fixed_zoom;
	float density;
	float mAngleX;
	float zoom;
	
	int viewport[] = new int[4];
	float projectionMatrix[] = new float[16];
	float modelViewMatrix[] = new float[16];

	public BoardRenderer board;

	public FreebloksRenderer(Context context, ViewModel model) {
		this.context = context;
		this.model = model;
		mAngleX = 70.0f;
		init(Spiel.DEFAULT_FIELD_SIZE_X);
		currentPlayer = -1;
	}

	public void init(int field_size) {
		board = new BoardRenderer(field_size);
	}

	private float outputfar[] = new float[4];
	private float outputnear[] = new float[4];
	
	public PointF windowToModel(PointF point) {
		float x1, y1, z1, x2, y2, z2, u;
		
		synchronized(outputfar) {		
			GLU.gluUnProject(point.x, viewport[3] - point.y, 0.0f, modelViewMatrix, 0, projectionMatrix, 0, viewport, 0, outputnear, 0);
			GLU.gluUnProject(point.x, viewport[3] - point.y, 1.0f, modelViewMatrix, 0, projectionMatrix, 0, viewport, 0, outputfar, 0);
		}
//		Log.d("windowToModel", "(" + point.x + "/" + point.y + ")  => far  (" + outputfar[0] + "/" + outputfar[1] + "/" + outputfar[2] + "/" + outputfar[3] + ")");
//		Log.d("windowToModel", "(" + point.x + "/" + point.y + ")  => near (" + outputnear[0] + "/" + outputnear[1] + "/" + outputnear[2] + "/" + outputnear[3] + ")");
		
		x1 = (outputfar[0] / outputfar[3]);
		y1 = (outputfar[1] / outputfar[3]);
		z1 = (outputfar[2] / outputfar[3]);
		x2 = (outputnear[0] / outputnear[3]);
		y2 = (outputnear[1] / outputnear[3]);
		z2 = (outputnear[2] / outputnear[3]);
		u = (0.0f - y1) / (y2 - y1);

		point.x = x1 + u * (x2 - x1);
		point.y = z1 + u * (z2 - z1);
		return point;
	}

	boolean updateModelViewMatrix = true;

	public synchronized void onDrawFrame(GL10 gl) {
		final float camera_distance = zoom;
		long t = System.currentTimeMillis();
		float angle = getAngleY();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		if (model.intro != null) {
			model.intro.render(gl, this);
			return;
		}
		
		gl.glLoadIdentity();
		if (height > width)
			gl.glTranslatef(0, 7.4f, 0);
		else
			gl.glTranslatef(5.0f, 0.6f, 0);
		
		GLU.gluLookAt(gl, 
				(float) (fixed_zoom/camera_distance*Math.sin(angle * Math.PI/180.0)*Math.cos(mAngleX*Math.PI/180.0)),
				(float) (fixed_zoom/camera_distance*Math.sin(mAngleX*Math.PI/180.0)),
				(float) (fixed_zoom/camera_distance*Math.cos(mAngleX*Math.PI/180.0)*Math.cos(-angle*Math.PI/180.0)),
				0.0f, 0.0f, 0.0f,
				0.0f, 1.0f, 0.0f);
		if (updateModelViewMatrix) synchronized(outputfar) {
			GL11 gl11 = (GL11)gl;
//				Log.w("onDrawFrame", "updating modelViewMatrix");
			gl11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelViewMatrix, 0);
			updateModelViewMatrix = false;
		}

		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, light0_pos, 0);
		
		/* render board */
		int player = -1;
		if (model.spiel != null && model.spiel.is_local_player() && model.showSeeds)
			player = model.spiel.current_player();
		board.renderBoard(gl, model.spiel, player);
		
		if (model.spiel == null)
			return;
		
		/* render player stones on board, unless they are "effected" */
	    gl.glPushMatrix();
	    gl.glTranslatef(-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1), 0, -BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) );
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	    synchronized (model.effects) {
		    for (int y = 0; y < model.spiel.m_field_size_y; y++) {
		    	int x;
		    	for (x = 0; x < model.spiel.m_field_size_x; x++) {
		    		int field = model.spiel.get_game_field(y, x);
		    		if (field != Stone.FIELD_FREE) {
		    			boolean effected = false;
		    			for (Effect effect: model.effects)
		    				if (effect.isEffected(x, y)) {
		    					effected = true;
		    					break;
		    				}
		    			if (!effected)
		    				board.renderStone(gl, field, BoardRenderer.DEFAULT_ALPHA);
		    		}
		    		gl.glTranslatef(BoardRenderer.stone_size * 2.0f, 0, 0);
		    	}
		    	gl.glTranslatef(- x * BoardRenderer.stone_size * 2.0f, 0, BoardRenderer.stone_size * 2.0f);
		    }
	    }
	    gl.glDisable(GL10.GL_BLEND);
	    gl.glPopMatrix();
		
		if (currentPlayer >= 0 && model.showPlayer >= 0) {
			gl.glPushMatrix();
			gl.glRotatef(angle, 0, 1, 0);
			if (width > height)
				gl.glRotatef(-90.0f, 0, 1, 0);
			model.wheel.render(this, gl);
			gl.glPopMatrix();
		}
		
		/* render all effects */
		synchronized (model.effects) {
			for (Effect effect: model.effects) {
				effect.render(gl, board);
			}
		}
		
		/* render current player stone on the field */
		if (model.spiel.is_local_player())
			model.currentStone.render(this, gl);
		
//		Log.d("Renderer", "render took " + (System.currentTimeMillis() - t) + " ms");
	}
	
	final float getAngleY() {
		if (model.showPlayer < 0)
			return 0.0f;
		return -90.0f * (float)model.showPlayer;
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GL11 gl11 = (GL11)gl;
		
		gl.glViewport(0, 0, width, height);
		viewport[0] = 0;
		viewport[1] = 0;
		viewport[2] = width;
		viewport[3] = height;
		
		
		this.width = (float)width;
		this.height = (float)height;
		model.vertical_layout = (height >= width);
		
		float fovy;
		if (height > width) {
			fovy = 35.0f;
		} else {
			fovy = 21.0f;
		}
		fixed_zoom = 55.0f;

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		GLU.gluPerspective(gl, fovy, this.width / this.height, 1.0f, 300.0f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);

		synchronized(outputfar) {
			gl11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, projectionMatrix, 0);
		}
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glDisable(GL10.GL_DITHER);

		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

		gl.glClearColor(0.05f, 0.10f, 0.25f, 1.0f);
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_NORMALIZE);

		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, light0_pos, 0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, light0_ambient, 0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, light0_diffuse, 0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, light0_specular, 0);
		
		updateModelViewMatrix = true;
		model.currentStone.updateTexture(context, gl);
	}
}