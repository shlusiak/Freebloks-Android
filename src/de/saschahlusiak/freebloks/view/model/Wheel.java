package de.saschahlusiak.freebloks.view.model;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.PointF;
import android.os.Handler;
import android.util.Log;
import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.view.BoardRenderer;
import de.saschahlusiak.freebloks.view.FreebloksRenderer;

public class Wheel implements ViewElement {
	private final static String tag = Wheel.class.getSimpleName();
	
	private Stone highlightStone;
	private float currentOffset;
	private float lastOffset;
	private float originalX, originalY;
	private boolean spinning = false;
	private ArrayList<Stone> stones;
	private int currentPlayer; /* the currently shown player */
	private ViewModel model;
	private boolean moves_left;
	
	private PointF tmp = new PointF();
	private Handler handler = new Handler();
	private Timer timer = new Timer();
	private TimerTask task;
	
	public Wheel(ViewModel model) {
		this.model = model;
		stones = new ArrayList<Stone>();
		currentPlayer = -1;
	}
	
	public synchronized void update(int player) {
		stones.clear();
		if (player < 0)
			return;
		if (model.spiel == null)
			return;
		Player p = model.spiel.get_player(player);
		moves_left = p.m_number_of_possible_turns > 0;
		for (int i = 0; i < Stone.STONE_COUNT_ALL_SHAPES; i++) {
			Stone s = p.get_stone(i);
			if (s != null && s.get_available() > 0)
				stones.add(s);
		}
		this.highlightStone = null;

		if (stones.size() > 0)
			rotateTo((stones.size() + 1) / 2 - 2);
		
		this.currentPlayer = player;
	}
	
	private void rotateTo(int column) {
		lastOffset = (((float) model.spiel.m_field_size_x / 2.0f) / 5.5f - 5.3f + (float)column - 1.5f) * 17.0f;
		while (lastOffset > 180)
			lastOffset -= 360.0f;
		while (lastOffset < -180)
			lastOffset += 360.0f;
	}
	
	private int getStonePositionInWheel(int stone) {
		for (int i = 0; i < stones.size(); i++)
			if (stones.get(i).get_number() == stone)
				return i;
		return 0;
	}
	
	/* makes sure the given stone is visible in the wheel */
	public void showStone(int stone) {
		rotateTo(getStonePositionInWheel(stone) / 2);
	}

	/* returns the player number currently shown in the wheel (aka. last call of update) */
	public final int getCurrentPlayer() {
		return this.currentPlayer;
	}
	
	/* returns the currently highlighted stone in the wheel */
	public Stone getCurrentStone() {
		return this.highlightStone;
	}

	public void setCurrentStone(Stone stone) {
		this.highlightStone = stone;
	}

	@Override
	synchronized public boolean handlePointerDown(final PointF m) {
		spinning = false;
		if (model.spiel == null)
			return false;
		
		lastOffset = currentOffset;
		
		tmp.x = m.x;
		tmp.y = m.y;
		model.board.modelToBoard(tmp);
		model.board.boardToUnified(tmp);
		if (!model.vertical_layout) {
			float t = tmp.x;
			tmp.x = tmp.y;
			tmp.y = model.spiel.m_field_size_x - t - 1;
		}
		
		originalX = tmp.x;
		originalY = tmp.y;

		if (tmp.y > 0)
			return false;
		
		/* TODO: remove or understand magic numbers */
		int row = (int) (-(tmp.y + 2.0f) / 6.7f);
		int col = (int) ((tmp.x - (float) model.spiel.m_field_size_x / 2.0f) / 5.5f + 5.3f + lastOffset / 17.0f);

//		Log.d(tag, "currentWheelAngle = " + originalAngle);
//		Log.d(tag, "unified coordinates (" + tmp.x + ", " + tmp.y + ")");
//		Log.d(tag, "row " + row + ", col " + col);
		
		if (!model.spiel.is_local_player() || model.spiel.current_player() != currentPlayer) {
			spinning = true;
			return true;
		}
		
		int nr = col * 2 + row;
		if (nr < 0 || nr >= stones.size() || row > 1)
			highlightStone = null;
		else
			highlightStone = stones.get(nr);
		if (highlightStone != null && highlightStone.get_available() <= 0)
			highlightStone = null;
		else if (highlightStone != null) {
			/* we tapped on a stone; start timer */
			if (task != null)
				task.cancel();
			if (model.currentStone.stone != null && model.currentStone.stone != highlightStone) {
				model.currentStone.startDragging(null, highlightStone);
				model.soundPool.play(model.soundPool.SOUND_CLICK2, 1.0f, 1);
			} else timer.schedule(task = new TimerTask() {
				
				@Override
				public void run() {
					if (!spinning)
						return;
					if (highlightStone == null)
						return;
					if (Math.abs(currentOffset - lastOffset) > 10.0f)
						return;
					if (!model.spiel.is_local_player())
						return;
					
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							if (highlightStone == null)
								return;
							tmp.x = m.x;
							tmp.y = m.y;
							model.board.modelToBoard(tmp);
							
							Log.d(tag, "timer expire, start moving stone");
							if (model.soundPool != null && !model.soundPool.play(model.soundPool.SOUND_CLICK2, 1.0f, 1))
								model.activity.vibrate(Global.VIBRATE_START_DRAGGING);
							if (!model.showAnimations)
								currentOffset = lastOffset;
							model.currentStone.startDragging(tmp, highlightStone);
							model.board.resetRotation();
							spinning = false;
							model.view.requestRender();
						}
					});
					spinning = false;
				}
			}, 500);
		}

		spinning = true;
		return true;
	}

	@Override
	synchronized public boolean handlePointerMove(PointF m) {
		if (!spinning)
			return false;
		
		tmp.x = m.x;
		tmp.y = m.y;
		model.board.modelToBoard(tmp);
		model.board.boardToUnified(tmp);
		
		if (!model.vertical_layout) {
			float t = tmp.x;
			tmp.x = tmp.y;
			tmp.y = model.spiel.m_field_size_x - t - 1;
		}
		
		/* everything underneath row 0 spins the wheel */
		float offset = 8.0f * (originalX - tmp.x);
		offset *= 1.0f / (1.0f + Math.abs(originalY - tmp.y) / 3.0f);
		currentOffset += offset;
		while (currentOffset > 180)
			currentOffset -= 360.0f;
		while (currentOffset < -180)
			currentOffset += 360.0f;

		originalX = tmp.x;

		model.redraw = true;
		if (!model.spiel.is_local_player() || model.spiel.current_player() != currentPlayer) {
			return true;
		}

		if (Math.abs(currentOffset - lastOffset) >= 90.0f) {
			highlightStone = null;
		}

		if (highlightStone != null && (tmp.y >= 0.0f || Math.abs(tmp.y - originalY) >= 3.5f)) {
			if (Math.abs(currentOffset - lastOffset) < 90.0f) {
//				model.activity.vibrate_on_move(Global.VIBRATE_START_DRAGGING);
				tmp.x = m.x;
				tmp.y = m.y;
				model.board.modelToBoard(tmp);
				if (!model.showAnimations)
					currentOffset = lastOffset;
				if (model.currentStone.stone != highlightStone)
					model.soundPool.play(model.soundPool.SOUND_CLICK2, 1.0f, 1);
				model.currentStone.startDragging(tmp, highlightStone);
				spinning = false;
				model.board.resetRotation();
			}
		}
		return true;
	}
	
	@Override
	public boolean handlePointerUp(PointF m) {
		if (task != null)
			task.cancel();
		task = null;
		if (spinning) {
			lastOffset = currentOffset;
			spinning = false;
			return true;
		}
		return false;
	}	

	public synchronized void render(FreebloksRenderer renderer, GL10 gl) {
		final float da = 17.0f;
		float angle = currentOffset + 9.5f * 0.5f * da;
		
		if (model.spiel == null)
			return;		

		gl.glTranslatef(0, -BoardRenderer.stone_size * 33.0f, 0);
		gl.glRotatef(currentOffset, 0, 0, 1);
		gl.glTranslatef(-BoardRenderer.stone_size * 5.1f * 6.5f, 0, BoardRenderer.stone_size * (model.spiel.m_field_size_x + 10));
		gl.glRotatef(9.5f * 0.5f * da, 0, 0, 1);
		gl.glScalef(1.1f, 1.1f, 1.1f);
		gl.glPushMatrix();
		for (int i = 0; i < stones.size(); i++) {
			Stone s = stones.get(i);
			
			float alpha = 1.0f;
			while (angle < -180f)
				angle += 360.0f;
			while (angle > 180.0f)
				angle -= 360.0f;

			alpha = 0.95f / (1.0f + Math.abs(angle) / 47.0f);
			if (model.currentStone.stone != null)
				alpha *= 0.7f;
			if (!moves_left && !model.spiel.is_finished())
				alpha *= 0.65f;

			if (s.get_available() - ((s == model.currentStone.stone) ? 1 : 0) > 0) {
				gl.glPushMatrix();
				gl.glRotatef(90 * model.board.centerPlayer, 0, 1, 0);
				if (!model.vertical_layout)
					gl.glRotatef(-90.0f, 0, 1, 0);
				gl.glTranslatef(-s.get_stone_size() * BoardRenderer.stone_size, 0, -s.get_stone_size() * BoardRenderer.stone_size);
				renderer.board.renderPlayerStone(gl, (s == highlightStone) ? -1 : currentPlayer, s, alpha);
				gl.glPopMatrix();
			}
			
			if (i % 2 == 0) {
				gl.glTranslatef(0, 0, BoardRenderer.stone_size * 2.0f * 5.1f);
			} else {
				gl.glPopMatrix();
				gl.glTranslatef(BoardRenderer.stone_size * 2.0f * 5.1f, 0, 0);
				gl.glRotatef(-da, 0, 0, 1);
				angle -= da;
				gl.glPushMatrix();
			}
		}
		
		gl.glPopMatrix();
	}

	@Override
	public boolean execute(float elapsed) {
		final float EPSILON = 1.0f;
		if (spinning == false && (Math.abs(currentOffset - lastOffset) > EPSILON)) {
			final float ROTSPEED = 10.0f + (float)Math.pow(Math.abs(currentOffset - lastOffset), 0.65f) * 15.0f;

			if (!model.showAnimations) {
				currentOffset = lastOffset;
				return true;
			}
			if (currentOffset < lastOffset) {
				currentOffset += elapsed * ROTSPEED;
				if (currentOffset > lastOffset)
					currentOffset = lastOffset;
			} else {
				currentOffset -= elapsed * ROTSPEED;
				if (currentOffset < lastOffset)
					currentOffset = lastOffset;				
			}
			return true;
		}
		return false;
	}
}
