package de.saschahlusiak.freebloks.view.opengl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.view.FreebloksViewInterface;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class Freebloks3DView extends GLSurfaceView implements FreebloksViewInterface {
	private final static String tag = Freebloks3DView.class.getSimpleName();

	private class MyRenderer implements GLSurfaceView.Renderer {
		final float light0_ambient[] = {0.35f, 0.35f, 0.35f, 1.0f};
		final float light0_diffuse[] = {0.8f, 0.8f, 0.8f, 1.0f};
		final float light0_specular[] = {1.0f, 1.0f, 1.0f, 1.0f};
		final float light0_pos[]    = {2.5f, 5f, -2.0f, 0.0f};

		BoardRenderer board;
		

		public MyRenderer() {
			init();
		}
		
		public void init() {
			board = new BoardRenderer();
		}

		public void onDrawFrame(GL10 gl) {
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
			gl.glTranslatef(0, 5, -zoom);
			gl.glRotatef(mAngleY, 1, 0, 0);
			gl.glRotatef(mAngleX, 0, 1, 0);
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, light0_pos, 0);
			

			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);

			board.render(gl, spiel);
		}

		public void onSurfaceChanged(GL10 gl, int width, int height) {
			gl.glViewport(0, 0, width, height);

			float ratio = (float) width / height;
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glFrustumf(-ratio, ratio, -1, 1, 1, 60);
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
		}

		float mAngleX;
		float mAngleY;
		float zoom;
		
		void setAngle(float ax, float ay, float zoom) {
			mAngleX = ax;
			mAngleY = ay;
			this.zoom = zoom;
		}
	}

	SpielClient spiel;
	MyRenderer renderer;
	float angleX = 0.0f;
	float angleY = 45.0f;
	float zoom = 13;
	
	public Freebloks3DView(Context context, AttributeSet attrs) {
		super(context, attrs);

		renderer = new MyRenderer();
		setRenderer(renderer);
		renderer.setAngle(angleX, angleY, zoom);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	public void setSpiel(SpielClient spiel) {
		this.spiel = spiel;
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
	
	float mPreviousX;
	float mPreviousY;
	float oldDist;

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_MOVE:
			if (event.getPointerCount() > 1) {
				float newDist = spacing(event);
			    if (newDist > 10f) {
			    	zoom *= (newDist / oldDist);
			    	if (zoom > 20.0f)
			    		zoom = 20.0f;
			    	if (zoom < 9.0f)
			    		zoom = 9.0f;
			    	oldDist = newDist;
			    }
			} else {
				angleX += (float)(event.getX() - mPreviousX) / (float)getWidth() * 180.0f;
				angleY += (float)(event.getY() - mPreviousY) / (float)getHeight() * 180.0f;
				if (angleY < 30)
					angleY = 30;
				if (angleY > 90)
					angleY = 90;
			}
			queueEvent(new Runnable() {
				@Override
				public void run() {
					renderer.setAngle(angleX, angleY, zoom);
					requestRender();
				}
			});
			break;
			
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			break;
			
		default:
			break;
		}
		
		mPreviousX = event.getX();
		mPreviousY = event.getY();
//		return super.onTouchEvent(event);
		return true;
	}
	
	@Override
	public void invalidate() {
//		requestRender();
		super.invalidate();
	}

}
