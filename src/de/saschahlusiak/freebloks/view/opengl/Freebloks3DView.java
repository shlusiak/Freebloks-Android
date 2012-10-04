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
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class Freebloks3DView extends GLSurfaceView implements ViewInterface, SpielClientInterface {
	private final static String tag = Freebloks3DView.class.getSimpleName();

	private class MyRenderer implements GLSurfaceView.Renderer {
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
		
		public PointF modelToField(PointF point) {
			point.x = point.x + BoardRenderer.stone_size * (float)(spiel.m_field_size_x - 1);
			point.y = BoardRenderer.stone_size * (float)(spiel.m_field_size_x - 1) - point.y;
			
			point.x = point.x / (BoardRenderer.stone_size * 2.0f);
			point.y = point.y / (BoardRenderer.stone_size * 2.0f);
			
			return point;
		}

		boolean updateModelViewMatrix = true;

		public synchronized void onDrawFrame(GL10 gl) {
			final float camera_distance = zoom;
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
			gl.glTranslatef(0, 9.0f, 0);
			GLU.gluLookAt(gl, 
					(float) (camera_distance*Math.sin(getAngleY() * Math.PI/180.0)*Math.cos(mAngleX*Math.PI/180.0)),
					(float) (camera_distance*Math.sin(mAngleX*Math.PI/180.0)),
					(float) (camera_distance*Math.cos(mAngleX*Math.PI/180.0)*Math.cos(-getAngleY()*Math.PI/180.0)),
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

			board.renderBoard(gl);
			board.renderField(gl);
			if (currentStone.stone != null && spiel != null && spiel.is_local_player()) {
				currentStone.render(gl);
			}

			if (currentPlayer >= 0) {
				gl.glPushMatrix();
				gl.glRotatef(getAngleY(), 0, 1, 0);
				board.renderPlayerStones(gl, currentPlayer, wheel.currentAngle, wheel.highlightStone, currentStone.stone);
				gl.glPopMatrix();
			}
		}
		
		final float getAngleY() {
			return -90.0f * (float)showPlayer;
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
		}

		float mAngleX;
		float zoom;
		
		synchronized void setAngle(float ax, float zoom) {
			mAngleX = ax;
			this.zoom = zoom;
			
			updateModelViewMatrix = true;
		}
	}

	private class Wheel {
		int highlightStone = -1;
		float currentAngle = 0.0f;
		float originalAngle;
		
		void handleTouch() {
			originalAngle = currentAngle;
			
			int row = (int)(-originalPos.y / 5.5f);
			int col = (int)((originalPos.x - (float)spiel.m_field_size_x / 2.0f) / 7.0f + 5.5f + originalAngle / 17.0f);
			
			Log.d(tag, "currentWheelAngle = " + originalAngle);
			Log.d(tag, "unified coordinates (" + originalPos.x + ", " + originalPos.y + ")");
			Log.d(tag, "row " + row + ", col " + col);
			
			wheel.highlightStone = row * 11 + col;
			if (col > 11 || row > 1 || col < 0 || row < 0 || wheel.highlightStone >= 21)
				wheel.highlightStone = -1;
			Stone s = spiel.get_current_player().get_stone(wheel.highlightStone);
			if (s != null && s.get_available() <= 0)
				wheel.highlightStone = -1;
			
			if (currentStone.stone != null) {
				currentStone.stone = s;
				activity.selectCurrentStone(spiel, s);
			}
		}
		
		void handleMove() {
			/* everything underneath row 0 spins the wheel */
			currentAngle += 8.0f * (originalPos.x - unifiedPoint.x);
			while (currentAngle > 180)
				currentAngle -= 360.0f;
			while (currentAngle < -180)
				currentAngle += 360.0f;

			originalPos.x = unifiedPoint.x;

			if (Math.abs(currentAngle - originalAngle) >= 90.0f) {
				highlightStone = -1;
				activity.selectCurrentStone(spiel, null);
			}

			if (highlightStone >= 0 && unifiedPoint.y >= 0) {
				if (Math.abs(currentAngle - originalAngle) < 90.0f) {
					// renderer.currentWheelAngle = originalWheelAngle;
					Stone stone = spiel.get_current_player().get_stone(highlightStone);
					activity.selectCurrentStone(spiel, stone);
					highlightStone = -1;
					currentStone.startDragging(stone, (int)fieldPoint.x, (int)fieldPoint.y);
				}
			}
		}
	}
	
	private class CurrentStone {
		Stone stone;
		Point pos = new Point();
		boolean dragging, hasMoved;
		int stone_rel_x, stone_rel_y;
		
		CurrentStone() {
			pos.x = -50;
			pos.y = -50;
		}
		
		void render(GL10 gl) {
			if (pos.x > -50 && pos.y > -50) {
				gl.glPushMatrix();
				gl.glTranslatef(0, 0.3f, 0.0f);
				renderer.board.renderPlayerStone(gl, spiel.current_player(), currentStone.stone, currentStone.pos.x, currentStone.pos.y);
				gl.glPopMatrix();
			}
		}

		boolean moveTo(int x, int y) {
			if (stone == null)
				return false;
			
			for (int i = 0; i < stone.get_stone_size(); i++)
				for (int j = 0; j < stone.get_stone_size(); j++) {
					if (stone.get_stone_field(j, i) == Stone.STONE_FIELD_ALLOWED) {
						if (i + x < 0)
							x = -i;
						if (y - j < 0)
							y = j;
						
						if (i + x >= spiel.m_field_size_x)
							x = spiel.m_field_size_x - i - 1;
						if (y - j >= spiel.m_field_size_y)
							y = spiel.m_field_size_y + j - 1;
					}
					
				}
			
			if (currentStone.pos.x != x || currentStone.pos.y != y) {
				currentStone.pos.x = x;
				currentStone.pos.y = y;
				return true;
			}
			return false;
		}
		
		void handleTouch() {
			dragging = false;
			hasMoved = false;
			if (stone != null) {
				stone_rel_x = (int)(0.5f + pos.x - fieldPoint.x - (float)stone.get_stone_size() / 2.0f);
				stone_rel_y = (int)(0.5f + pos.y - fieldPoint.y - (float)stone.get_stone_size() / 2.0f);
				if ((Math.abs(stone_rel_x) < 9) &&
					(Math.abs(stone_rel_y) < 9)) {
					dragging = true;
				}
			}
		}
		
		void handleMove() {
			originalPos.x = unifiedPoint.x;
			int x = (int)fieldPoint.x + stone_rel_x + stone.get_stone_size() / 2;
			int y = (int)fieldPoint.y + stone_rel_y + stone.get_stone_size() / 2;
			hasMoved |= moveTo(x, y);
			unifiedPoint.x = x;
			unifiedPoint.y = y;
			fieldToUnified(unifiedPoint);
			if (unifiedPoint.y < -2.0f) {
				wheel.highlightStone = currentStone.stone.get_number();
				dragging = false;
				currentStone.stone = null;
			}
		}
		
		void startDragging(Stone stone, int x, int y) {
			this.stone = stone;
			if (stone == null)
				return;
			
			dragging = true;
			hasMoved = false;
			moveTo(x, y);
			stone_rel_x = 0;
			stone_rel_y = 0;
		}
	}
	
	SpielClient spiel;
	MyRenderer renderer;
	ActivityInterface activity;
	int showPlayer;
	float zoom = 30;
	int currentPlayer;
	
	Wheel wheel = new Wheel();
	CurrentStone currentStone = new CurrentStone();
	
	public Freebloks3DView(Context context, AttributeSet attrs) {
		super(context, attrs);

		renderer = new MyRenderer();
		setRenderer(renderer);
		renderer.setAngle(70.0f, zoom);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	public void setActivity(ActivityInterface activity) {
		this.activity = activity;
	}

	public void setSpiel(SpielClient spiel) {
		this.spiel = spiel;
		if (spiel != null) {
			spiel.addClientInterface(this);
			for (int i = 0; i < Spiel.PLAYER_MAX; i++) if (spiel.is_local_player(i)) {
				showPlayer = i;
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
		
	void fieldToUnified(PointF p) {
		float tmp;
		
		switch (showPlayer) {
		case 0: /* nothing */
			break;
		case 1:
			tmp = p.x;
			p.x = spiel.m_field_size_x - p.y;
			p.y = tmp;
			break;
		case 2: /* 180 degree */
			p.x = spiel.m_field_size_x - p.x;
			p.y = spiel.m_field_size_y - p.y;
			break;
		case 3:
			tmp = p.y;
			p.y = spiel.m_field_size_y - p.x;
			p.x = tmp;
			break;
		}
	}
	
	float oldDist;
	
	PointF originalPos = new PointF(); // original touch down in unified coordinates
	PointF fieldPoint = new PointF();	// current position in field coordinates
	PointF unifiedPoint = new PointF();	// current position in unified coordinates
	

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		fieldPoint.x = event.getX();
		fieldPoint.y = event.getY();
		
		renderer.windowToModel(fieldPoint);
		renderer.modelToField(fieldPoint);
		unifiedPoint.x = fieldPoint.x;
		unifiedPoint.y = fieldPoint.y;
		fieldToUnified(unifiedPoint);
		
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			originalPos.x = unifiedPoint.x;
			originalPos.y = unifiedPoint.y;
			
			if (spiel.is_local_player()) {
				if (originalPos.y < 0) {
					wheel.handleTouch();
				} else {
					currentStone.handleTouch();
				}
			}
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

				if (!currentStone.dragging) {
					/* only move wheel, if original touch was inside area */
					if (originalPos.y < 0)
						wheel.handleMove();
				} else {
					currentStone.handleMove();
				}
			}
			requestRender();
			break;
			
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			break;
			
		case MotionEvent.ACTION_UP:
			if (!currentStone.hasMoved && currentStone.dragging) {
				activity.commitCurrentStone(spiel, currentStone.stone, currentStone.pos.x, currentStone.pos.y);
				currentStone.stone = null;
				wheel.highlightStone = -1;
			}
			currentStone.dragging = false;
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
	}

	@Override
	public void stoneWasSet(NET_SET_STONE s) {
		// TODO Auto-generated method stub
		
		updateView();
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
			showPlayer = i;
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
}
