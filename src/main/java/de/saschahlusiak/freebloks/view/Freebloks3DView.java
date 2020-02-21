package de.saschahlusiak.freebloks.view;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import de.saschahlusiak.freebloks.client.GameClient;
import de.saschahlusiak.freebloks.client.GameEventObserver;
import de.saschahlusiak.freebloks.model.Board;
import de.saschahlusiak.freebloks.game.ActivityInterface;
import de.saschahlusiak.freebloks.model.Game;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.message.MessageServerStatus;
import de.saschahlusiak.freebloks.view.effects.Effect;
import de.saschahlusiak.freebloks.view.effects.EffectSet;
import de.saschahlusiak.freebloks.view.effects.ShapeFadeEffect;
import de.saschahlusiak.freebloks.view.effects.ShapeRollEffect;
import de.saschahlusiak.freebloks.view.effects.ShapeUndoEffect;
import de.saschahlusiak.freebloks.view.model.Theme;
import de.saschahlusiak.freebloks.view.model.ViewModel;

public class Freebloks3DView extends GLSurfaceView implements GameEventObserver {
	private final static String tag = Freebloks3DView.class.getSimpleName();

	public final ViewModel model = new ViewModel(this);

	private FreebloksRenderer renderer;
	private float scale = 1.0f;


	public Freebloks3DView(Context context, AttributeSet attrs) {
		super(context, attrs);

		setEGLConfigChooser(new GLConfigChooser(2));

		renderer = new FreebloksRenderer(context, model);
//		renderer.density = getResources().getDisplayMetrics().density;
		setRenderer(renderer);
		renderer.zoom = scale;
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		setDebugFlags(DEBUG_CHECK_GL_ERROR);
	}
	
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	    super.onWindowFocusChanged(hasFocus);
	    if (hasFocus) {
			if (model.immersiveMode)
				setSystemUiVisibility(SYSTEM_UI_FLAG_LOW_PROFILE |
					SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
					SYSTEM_UI_FLAG_LAYOUT_STABLE |
					SYSTEM_UI_FLAG_HIDE_NAVIGATION |
					SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			else
				setSystemUiVisibility(0);
		}
	}

	public void setActivity(ActivityInterface activity) {
		model.activity = activity;
	}

	public void setTheme(Theme theme) {
		renderer.backgroundRenderer.setTheme(theme);
	}

	public void setGameClient(final GameClient client) {
		if (client != null) {
			model.setGameClient(client);
		}

		queueEvent(new Runnable() {
			@Override
			public void run() {
				if (client != null) {
					final Game game = client.game;
					model.boardObject.last_size = game.getBoard().width;
					for (int i = 0; i < Board.PLAYER_MAX; i++) if (game.isLocalPlayer(i)) {
						model.boardObject.centerPlayer = i;
						break;
					}
					renderer.updateModelViewMatrix = true;
					model.wheel.update(model.boardObject.getShowWheelPlayer());
					newCurrentPlayer(game.getCurrentPlayer());
				}

				renderer.init(model.boardObject.last_size);
				requestRender();
			}
		});
	}

	private static float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float)Math.sqrt(x * x + y * y);
	}


	float oldDist;

//	PointF originalPos = new PointF(); // original touch down in unified coordinates
	PointF modelPoint = new PointF();	// current position in field coordinates


	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		modelPoint.x = event.getX();
		modelPoint.y = event.getY();

		renderer.windowToModel(modelPoint);

		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
		//	if (model.spiel != null && model.spiel.is_finished())
		//		model.activity.gameFinished();
			model.handlePointerDown(modelPoint);
			requestRender();
			break;

		case MotionEvent.ACTION_MOVE:
			if (event.getPointerCount() > 1) {
				float newDist = spacing(event);
			    if (newDist > 10f) {
			    	scale *= (newDist / oldDist);
			    	if (scale > 3.0f)
			    		scale = 3.0f;
			    	if (scale < 0.3f)
			    		scale = 0.3f;
			    	oldDist = newDist;
			    	renderer.updateModelViewMatrix = true;
			    	renderer.zoom = scale;
			    	requestRender();
			    }
			} else {
				if (model.handlePointerMove(modelPoint))
					requestRender();
			}
			break;

		case MotionEvent.ACTION_POINTER_DOWN:
			model.handlePointerUp(modelPoint);
			oldDist = spacing(event);
			break;

		case MotionEvent.ACTION_UP:
			model.handlePointerUp(modelPoint);
			requestRender();
			break;

		default:
			break;
		}

		return true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		renderer.updateModelViewMatrix = true;
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	public void newCurrentPlayer(int player) {
		if (model.game == null)
			return;

		if (model.game.isLocalPlayer() || model.wheel.getCurrentPlayer() != model.boardObject.getShowWheelPlayer())
			model.wheel.update(model.boardObject.getShowWheelPlayer());
		requestRender();
	}

	@Override
	public void stoneWillBeSet(@NonNull final Turn turn) {
		queueEvent(new Runnable() {
			@Override
			public void run() {
				if (model == null || model.game == null)
					return;

				if (model.hasAnimations() && !model.game.isLocalPlayer(turn.getPlayer())) {
					ShapeRollEffect e = new ShapeRollEffect(model, turn, 4.0f, -7.0f);

					EffectSet set = new EffectSet();
					set.add(e);
					set.add(new ShapeFadeEffect(model, turn, 2.0f));
					model.addEffect(set);
				}
			}
		});
	}

	@Override
	public void stoneHasBeenSet(@NonNull Turn turn) {
		if (model == null)
			return;
		if (model.game == null)
			return;

		if (model.game.isLocalPlayer(turn.getPlayer()) || turn.getPlayer() == model.wheel.getCurrentPlayer())
			model.wheel.update(model.boardObject.getShowWheelPlayer());

		requestRender();
	}

	@Override
	public void hintReceived(@NonNull final Turn turn) {
		queueEvent(new Runnable() {
			@Override
			public void run() {
				if (model == null || model.game == null)
					return;
				if (turn.getPlayer() != model.game.getCurrentPlayer())
					return;
				if (!model.game.isLocalPlayer())
					return;

				model.boardObject.resetRotation();
				model.wheel.update(turn.getPlayer());
				model.wheel.showStone(turn.getShapeNumber());

				model.soundPool.play(model.soundPool.SOUND_HINT, 0.9f, 1.0f);

				int currentPlayer = model.game.getCurrentPlayer();
				Stone st = null;
				if (currentPlayer >= 0)
					st = model.board.getPlayer(currentPlayer).getStone(turn.getShapeNumber());

				PointF p = new PointF();
				p.x = turn.getX() - 0.5f + st.getShape().getSize() / 2;
				p.y = turn.getY() - 0.5f + st.getShape().getSize() / 2;
				model.currentStone.startDragging(p, st, turn.getOrientation(), model.getPlayerColor(turn.getPlayer()));

				requestRender();
			}
		});
	}

	@Override
	public void gameFinished() {
		model.boardObject.resetRotation();
		requestRender();
	}

	@Override
	public void chatReceived(MessageServerStatus status, int client, int player, @NonNull String message) {

	}

	@Override
	public void playerJoined(@NotNull MessageServerStatus status, int client, int player) {

	}

	@Override
	public void playerLeft(@NotNull MessageServerStatus status, int client, int player) {

	}

	@Override
	public void gameStarted() {
		model.boardObject.centerPlayer = 0;
		for (int i = 0; i < Board.PLAYER_MAX; i++) if (model.game.isLocalPlayer(i)) {
			model.boardObject.centerPlayer = i;
			break;
		}
		model.wheel.update(model.boardObject.getShowWheelPlayer());
		renderer.updateModelViewMatrix = true;
		model.reset();
		requestRender();
	}

	@Override
	public void stoneUndone(@NonNull Turn t) {
		if (model.hasAnimations()) {
			Effect e = new ShapeUndoEffect(model, t);
			model.addEffect(e);
		}
		model.currentStone.stopDragging();
		requestRender();
	}

	@Override
	public void serverStatus(@NonNull MessageServerStatus status) {
		if (status.getWidth() != model.boardObject.last_size) {
			model.boardObject.last_size = status.getWidth();
			queueEvent(new Runnable() {
				@Override
				public void run() {
					renderer.board.initBorder(model.board.width);
					requestRender();
				}
			});
		}
	}

	@Override
	public void onConnected(@NonNull GameClient client) {
		newCurrentPlayer(client.game.getCurrentPlayer());
	}

	@Override
	public void onDisconnected(@NonNull GameClient client, @Nullable Exception error) {

	}

	private class AnimateThread extends Thread {
		boolean goDown = false;

		AnimateThread() {
			super("AnimateThread");
		}

		@Override
		public void run() {
			long delay;

			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				return;
			}

			long time, tmp, lastExecTime;
			time = System.currentTimeMillis();
			lastExecTime = 0;
			while (!goDown) {
				switch (model.showAnimations) {
				case ViewModel.ANIMATIONS_FULL:
					if (lastRender < 0.2f)
						delay = 1000 / 60;
					else
						delay = 1000 / 15;
					break;
				case ViewModel.ANIMATIONS_HALF:
					if (lastRender < 0.2f)
						delay = 1000 / 30;
					else
						delay = 1000 / 15;
					break;
				default:
					if (model.intro != null)
						delay = 1000 / 30;
					else
						delay = 1000 / 3;
					break;
				}
				
				
				try {
					if (delay - lastExecTime > 0)
						Thread.sleep(delay - lastExecTime);
				} catch (InterruptedException e) {
					break;
				}
				tmp = System.currentTimeMillis();

				lastExecTime = tmp;
				execute((float)(tmp - time) / 1000.0f);
				lastExecTime = System.currentTimeMillis() - lastExecTime;
				time = tmp;
			}
			super.run();
		}
	}
	
	float lastRender = 1.0f;

	public final void execute(float elapsed) {
		if (elapsed < 0.001f)
			elapsed = 0.001f;
		if (model.execute(elapsed)) {
			if (model.showAnimations == ViewModel.ANIMATIONS_OFF)
				requestRender();
			else {
				if (lastRender > 0.0f)
					setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
				lastRender = 0.0f;
			}
		} else {
			if (lastRender < 0.3f) {
				lastRender += elapsed;
				if (lastRender >= 0.29f)
					setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
			}

		}
	}

	AnimateThread thread = null;

	@Override
	public void onPause() {
		super.onPause();
		thread.goDown = true;
		try {
			thread.interrupt();
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
		model.clearEffects();
		if (thread == null) {
			thread = new AnimateThread();
			thread.start();
		}
	}

	public void setScale(float scale) {
		this.scale = scale;
		renderer.zoom = scale;
		renderer.updateModelViewMatrix = true;
	}

	public final float getScale() {
		return scale;
	}
}
