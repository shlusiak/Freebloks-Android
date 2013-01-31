package de.saschahlusiak.freebloks.view;


import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.controller.SpielClientInterface;
import de.saschahlusiak.freebloks.controller.Spielleiter;
import de.saschahlusiak.freebloks.game.ActivityInterface;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.NET_CHAT;
import de.saschahlusiak.freebloks.network.NET_SERVER_STATUS;
import de.saschahlusiak.freebloks.network.NET_SET_STONE;
import de.saschahlusiak.freebloks.view.effects.AbsEffect.FadeEffect;
import de.saschahlusiak.freebloks.view.model.ViewModel;
import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class Freebloks3DView extends GLSurfaceView implements ViewInterface, SpielClientInterface {
	private final static String tag = Freebloks3DView.class.getSimpleName();

	public ViewModel model = new ViewModel(this);
	
	
	FreebloksRenderer renderer;
	ActivityInterface activity;
	float zoom = 28;

	
	public Freebloks3DView(Context context, AttributeSet attrs) {
		super(context, attrs);

		renderer = new FreebloksRenderer(context, model);
		setRenderer(renderer);
		renderer.setAngle(70.0f, zoom);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		setDebugFlags(DEBUG_CHECK_GL_ERROR);
	}
	
	public void setActivity(ActivityInterface activity) {
		model.activity = activity;
	}

	public void setSpiel(SpielClient client, Spielleiter spiel) {
		model.setSpiel(spiel);
		if (spiel != null) {
			client.addClientInterface(this);
			for (int i = 0; i < Spiel.PLAYER_MAX; i++) if (spiel.is_local_player(i)) {
				model.showPlayer = i;
				renderer.updateModelViewMatrix = true;
				break;
			}
			renderer.currentPlayer = spiel.current_player();
			model.wheel.update(model.showOpponents ? renderer.currentPlayer : model.showPlayer);
		}
		
		queueEvent(new Runnable() {
			@Override
			public void run() {
				renderer.init();
				requestRender();
			}
		});
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
		if (model.spiel == null)
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
		renderer.currentPlayer = player;
		updateView();
		model.wheel.update(model.showOpponents ? player : model.showPlayer);
	}

	@Override
	public void stoneWasSet(NET_SET_STONE s) {
		updateView();
		model.wheel.update(model.showOpponents ? model.spiel.current_player() : model.showPlayer);
		
		if (model.showAnimations && !model.spiel.is_local_player(s.player)) {
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
		for (int i = 0; i < Spiel.PLAYER_MAX; i++) if (model.spiel.is_local_player(i)) {
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
