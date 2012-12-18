package de.saschahlusiak.freebloks.view.opengl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.controller.SpielClientInterface;
import de.saschahlusiak.freebloks.game.ActivityInterface;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.NET_CHAT;
import de.saschahlusiak.freebloks.network.NET_SERVER_STATUS;
import de.saschahlusiak.freebloks.network.NET_SET_STONE;
import de.saschahlusiak.freebloks.view.ViewInterface;
import de.saschahlusiak.freebloks.view.opengl.AbsEffect.FadeEffect;
import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class Freebloks3DView extends GLSurfaceView implements ViewInterface, SpielClientInterface {
	private final static String tag = Freebloks3DView.class.getSimpleName();

	ViewModel model = new ViewModel(this);
	
	class MyRenderer implements GLSurfaceView.Renderer {
		final float light0_ambient[] = {0.35f, 0.35f, 0.35f, 1.0f};
		final float light0_diffuse[] = {0.8f, 0.8f, 0.8f, 1.0f};
		final float light0_specular[] = {1.0f, 1.0f, 1.0f, 1.0f};
		final float light0_pos[]    = {2.5f, 5f, -2.0f, 0.0f};
		float width = 1, height = 1;
		
		int viewport[] = new int[4];
		float projectionMatrix[] = new float[16];
		float modelViewMatrix[] = new float[16];

		BoardRenderer board;

		public MyRenderer() {
			init();
			currentPlayer = -1;
		}

		public void init() {
			board = new BoardRenderer(spiel);
		}

		private float outputfar[] = new float[4];
		private float outputnear[] = new float[4];
		public synchronized PointF windowToModel(PointF point) {
			float x1, y1, z1, x2, y2, z2, u;
			
			GLU.gluUnProject(point.x, viewport[3] - point.y, 0.0f, modelViewMatrix, 0, projectionMatrix, 0, viewport, 0, outputnear, 0);
			GLU.gluUnProject(point.x, viewport[3] - point.y, 1.0f, modelViewMatrix, 0, projectionMatrix, 0, viewport, 0, outputfar, 0);
//			Log.d("windowToModel", "(" + point.x + "/" + point.y + ")  => far  (" + outputfar[0] + "/" + outputfar[1] + "/" + outputfar[2] + "/" + outputfar[3] + ")");
//			Log.d("windowToModel", "(" + point.x + "/" + point.y + ")  => near (" + outputnear[0] + "/" + outputnear[1] + "/" + outputnear[2] + "/" + outputnear[3] + ")");
			
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
			float angle = getAngleY();
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
			gl.glTranslatef(0, 9.0f, 0);
			GLU.gluLookAt(gl, 
					(float) (camera_distance*Math.sin(angle * Math.PI/180.0)*Math.cos(mAngleX*Math.PI/180.0)),
					(float) (camera_distance*Math.sin(mAngleX*Math.PI/180.0)),
					(float) (camera_distance*Math.cos(mAngleX*Math.PI/180.0)*Math.cos(-angle*Math.PI/180.0)),
					0.0f, 0.0f, 0.0f,
					0.0f, 1.0f, 0.0f);
			if (updateModelViewMatrix) {
				GL11 gl11 = (GL11)gl;
//				Log.w("onDrawFrame", "updating modelViewMatrix");
				gl11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelViewMatrix, 0);
				updateModelViewMatrix = false;
			}

			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, light0_pos, 0);


			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
			
			if (spiel == null) {
				board.renderBoard(gl, -1);
				return;
			}

			/* render board */
			board.renderBoard(gl, spiel.is_local_player() ? model.showPlayer : -1);
			
			/* render player stones on board, unless they are "effected" */
			synchronized (model.effects) {
			    gl.glPushMatrix();
			    gl.glTranslatef(-BoardRenderer.stone_size * (float)(spiel.m_field_size_x - 1), 0, -BoardRenderer.stone_size * (float)(spiel.m_field_size_x - 1) );
			    for (int y = 0; y < spiel.m_field_size_y; y++) {
			    	int x;
			    	for (x = 0; x < spiel.m_field_size_x; x++) {
			    		if (spiel.get_game_field(y, x) != Stone.FIELD_FREE) {
			    			boolean effected = false;
			    			for (AbsEffect effect: model.effects)
			    				if (effect.isEffected(x, y)) {
			    					effected = true;
			    					break;
			    				}
			    			if (!effected)
			    				board.renderStone(gl, spiel.get_game_field(y, x), 0.65f);
			    		}
			    		gl.glTranslatef(BoardRenderer.stone_size * 2.0f, 0, 0);
			    	}
			    	gl.glTranslatef(- x * BoardRenderer.stone_size * 2.0f, 0, BoardRenderer.stone_size * 2.0f);
			    }
			    gl.glPopMatrix();
			}
			
			if (currentPlayer >= 0 && model.showPlayer >= 0) {
				gl.glPushMatrix();
				gl.glRotatef(angle, 0, 1, 0);
				model.wheel.render(this, gl, currentPlayer);
				gl.glPopMatrix();
			}
			
			/* render all effects */
			synchronized (model.effects) {
				for (AbsEffect effect: model.effects) {
					effect.render(gl, board);
				}
			}
			
			/* render current player stone on the field */
			if (spiel != null && spiel.is_local_player())
				model.currentStone.render(this, gl);
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
			
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			GLU.gluPerspective(gl, 60.0f, this.width / this.height, 1.0f, 300.0f);
			gl.glMatrixMode(GL10.GL_MODELVIEW);

			gl11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, projectionMatrix, 0);
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
			
			renderer.updateModelViewMatrix = true;
			model.currentStone.updateTexture(getContext(), gl);
		}

		float mAngleX;
		float zoom;
		
		synchronized void setAngle(float ax, float zoom) {
			mAngleX = ax;
			this.zoom = zoom;
			
			updateModelViewMatrix = true;
		}
	}
	
	SpielClient spiel;
	MyRenderer renderer;
	ActivityInterface activity;
	float zoom = 28;
	int currentPlayer;

	
	public Freebloks3DView(Context context, AttributeSet attrs) {
		super(context, attrs);

		renderer = new MyRenderer();
		setRenderer(renderer);
		renderer.setAngle(70.0f, zoom);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		setDebugFlags(DEBUG_CHECK_GL_ERROR);
	}
	
	public void setActivity(ActivityInterface activity) {
		model.activity = activity;
	}

	public void setSpiel(SpielClient spiel) {
		this.spiel = spiel;
		model.setSpiel(spiel);
		if (spiel != null) {
			spiel.addClientInterface(this);
			for (int i = 0; i < Spiel.PLAYER_MAX; i++) if (spiel.is_local_player(i)) {
				model.showPlayer = i;
				renderer.updateModelViewMatrix = true;
				break;
			}
			currentPlayer = spiel.current_player();
		}
		
		queueEvent(new Runnable() {
			@Override
			public void run() {
				renderer.init();
				requestRender();
			}
		});
	}

	@Override
	public SpielClient getSpiel() {
		return spiel;
	}
	
	private final static float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float)Math.sqrt(x * x + y * y);
	}
		
	
	float oldDist;
	
//	PointF originalPos = new PointF(); // original touch down in unified coordinates
	PointF modelPoint = new PointF();	// current position in field coordinates
	

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (spiel == null)
			return true;
		
		modelPoint.x = event.getX();
		modelPoint.y = event.getY();
		
		renderer.windowToModel(modelPoint);
		
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			model.handlePointerDown(modelPoint);
//			originalPos.x = unifiedPoint.x;
//			originalPos.y = unifiedPoint.y;

/*			if (spiel.is_local_player()) {
				if (originalPos.y < 0) {
					model.wheel.handleTouch();
				} else {
					model.currentStone.handleTouch();
				}
			} */
			requestRender();
			break;
			
		case MotionEvent.ACTION_MOVE:
			if (event.getPointerCount() > 1) {
				float newDist = spacing(event);
			    if (newDist > 10f) {
			    	zoom *= (oldDist / newDist);
			    	if (zoom > 400.0f)
			    		zoom = 400.0f;
			    	if (zoom < 2.0f)
			    		zoom = 2.0f;
			    	oldDist = newDist;
			    	renderer.updateModelViewMatrix = true;
					renderer.setAngle(70.0f, zoom);
			    }
			} else {
/*				angleY += (float)(event.getX() - mPreviousX) / (float)getWidth() * 180.0f;
				angleX += (float)(event.getY() - mPreviousY) / (float)getHeight() * 180.0f;
				if (angleX < 20)
					angleX = 20;
				if (angleX > 90)
					angleX = 90; 
				renderer.setAngle(angleX, angleY, zoom);
 				*/

				model.handlePointerMove(modelPoint);
				
			//	if (!model.currentStone.dragging) {
					/* only move wheel, if original touch was inside area */
			//		if (originalPos.y < 0)
			//			model.wheel.handleMove();
			//	} else {
			//		model.currentStone.handleMove();
			//	}
			}
			requestRender();
			break;
			
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			break;
			
		case MotionEvent.ACTION_UP:
			model.handlePointerUp(modelPoint);
			requestRender();
			break;
			
		default:
			break;
		}
		
//		return super.onTouchEvent(event);
		return true;
	}

	@Override
	public void updateView() {
		requestRender();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		renderer.updateModelViewMatrix = true;
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	public void newCurrentPlayer(int player) {
		currentPlayer = player;
		updateView();
		model.wheel.update(player);
	}

	@Override
	public void stoneWasSet(NET_SET_STONE s) {
		updateView();
		model.wheel.update(spiel.current_player());
		
		if (!spiel.is_local_player(s.player)) {
			Stone st = new Stone();
			st.init(s.stone);
			st.mirror_rotate_to(s.mirror_count, s.rotate_count);
			FadeEffect e = new FadeEffect(st, s.player, s.x, s.y, 3.5f, 0.0f);
		
			model.addEffect(e);
		}
	}

	@Override
	public void hintReceived(NET_SET_STONE s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameFinished() {
		updateView();
	}

	@Override
	public void chatReceived(NET_CHAT c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameStarted() {
		for (int i = 0; i < Spiel.PLAYER_MAX; i++) if (spiel.is_local_player(i)) {
			model.showPlayer = i;
			renderer.updateModelViewMatrix = true;
			break;
		}
		
		updateView();
	}

	@Override
	public void stoneUndone(Stone s, Turn t) {
		updateView();
		
	}

	@Override
	public void serverStatus(NET_SERVER_STATUS status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Spiel spiel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnected(Spiel spiel) {
		// TODO Auto-generated method stub
		
	}
	
	
	class UpdateThread extends Thread {
		boolean goDown = false;
		
		@Override
		public void run() {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				return;
			}
			
			long time = System.currentTimeMillis(), tmp;
			while (!goDown) {
				try {
					Thread.sleep(33);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
				tmp = System.currentTimeMillis();
				
				execute((float)(tmp - time) / 1000.0f);
				time = tmp;
			}
			super.run();
		}
	}
	
	public void execute(float elapsed) {
		boolean animated = false;
		
		synchronized (model.effects) {
			int i = 0;
			while (i < model.effects.size()) {
				animated |= model.effects.get(i).execute(elapsed);
				if (model.effects.get(i).isDone()) {
					model.effects.remove(i);
				} else
					i++;
			}
		}
		
		if (animated) {
			requestRender();
		}
	}
	
	UpdateThread thread = null;
	
	@Override
	public void onPause() {
		super.onPause();
		thread.goDown = true;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		model.effects.clear();
		thread = null;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (thread == null) {
			thread = new UpdateThread();
			thread.start();
		}
	}
	
}
