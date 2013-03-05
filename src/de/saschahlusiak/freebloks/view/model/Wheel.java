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
	
	Stone highlightStone;
	float currentAngle = 0.0f;
	float lastAngle;
	float originalX, originalY;
	boolean spinning = false;
	ArrayList<Stone> stones;
	int lastPlayer;
	ViewModel model;
	
	public Wheel(ViewModel model) {
		this.model = model;
		stones = new ArrayList<Stone>();
	}
	
	PointF tmp = new PointF();
	Handler handler = new Handler();
	Timer timer = new Timer();
	TimerTask task;
	
	
	public synchronized void update(int currentPlayer) {
		lastPlayer = currentPlayer;
		if (currentPlayer < 0)
			return;
		Player p = model.spiel.get_player(currentPlayer);
		stones.clear();
		for (int i = 0; i < Stone.STONE_COUNT_ALL_SHAPES; i++) {
			Stone s = p.get_stone(i);
			if (s != null && s.get_available() > 0)
				stones.add(s);
		}
	}
	
	public void update() {
		update(lastPlayer);
	}

	@Override
	synchronized public boolean handlePointerDown(final PointF m) {
		spinning = false;
		if (model.spiel == null)
			return false;
		if (lastPlayer < 0)
			return false;
		
		lastAngle = currentAngle;
		
		tmp.x = m.x;
		tmp.y = m.y;
		model.board.modelToBoard(tmp);
		model.board.boardToUnified(tmp);
		if (!model.vertical_layout) {
			float t = tmp.x;
			tmp.x = model.spiel.m_field_size_y - tmp.y - 1;
			tmp.y = t;
		}
		
		originalX = tmp.x;
		originalY = tmp.y;

		if (tmp.y > 0)
			return false;
		
		/* TODO: remove or understand magic numbers */
		int row = (int) (-(tmp.y + 2.0f) / 6.7f);
		int col = (int) ((tmp.x - (float) model.spiel.m_field_size_x / 2.0f) / 5.5f + 5.3f + lastAngle / 17.0f);

//		Log.d(tag, "currentWheelAngle = " + originalAngle);
//		Log.d(tag, "unified coordinates (" + tmp.x + ", " + tmp.y + ")");
//		Log.d(tag, "row " + row + ", col " + col);
		
		if (!model.spiel.is_local_player()) {
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
			timer.schedule(task = new TimerTask() {
				
				@Override
				public void run() {
					if (!spinning)
						return;
					if (highlightStone == null)
						return;
					if (Math.abs(currentAngle - lastAngle) > 10.0f)
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
							model.activity.vibrate_on_move(Global.VIBRATE_START_DRAGGING);
							if (!model.showAnimations)
								currentAngle = lastAngle;
							model.currentStone.startDragging(tmp, highlightStone, true);
							spinning = false;
							model.view.requestRender();
						}
					});
					spinning = false;
				}
			}, 500);
			if (model.currentStone.stone != null) {
				model.currentStone.stone = highlightStone;
			}
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
			tmp.x = model.spiel.m_field_size_y - tmp.y - 1;
			tmp.y = t;
		}
		
		/* everything underneath row 0 spins the wheel */
		float offset = 8.0f * (originalX - tmp.x);
		offset *= 1.0f / (1.0f + Math.abs(originalY - tmp.y) / 3.0f);
		currentAngle += offset;
		while (currentAngle > 180)
			currentAngle -= 360.0f;
		while (currentAngle < -180)
			currentAngle += 360.0f;

		originalX = tmp.x;

		model.redraw = true;
		if (!model.spiel.is_local_player())
			return true;

		if (Math.abs(currentAngle - lastAngle) >= 90.0f) {
			highlightStone = null;
		}

		if (highlightStone != null && (tmp.y >= 0.0f || Math.abs(tmp.y - originalY) >= 3.5f)) {
			if (Math.abs(currentAngle - lastAngle) < 90.0f) {
				model.activity.vibrate_on_move(Global.VIBRATE_START_DRAGGING);
				tmp.x = m.x;
				tmp.y = m.y;
				model.board.modelToBoard(tmp);
				if (!model.showAnimations)
					currentAngle = lastAngle;
				model.currentStone.startDragging(tmp, highlightStone, false);
				spinning = false;
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
			lastAngle = currentAngle;
			spinning = false;
			return true;
		}
		return false;
	}

	public synchronized void render(FreebloksRenderer renderer, GL10 gl) {
		final float da = 17.0f;
		float angle = currentAngle + 9.5f * 0.5f * da;
		
		if (model.spiel == null)
			return;
		if (lastPlayer < 0)
			return;
		
		
		gl.glTranslatef(0, -BoardRenderer.stone_size * 33.0f, 0);
		gl.glRotatef(currentAngle, 0, 0, 1);
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
			
			alpha = 0.8f / (1.0f + Math.abs(angle) / 47.0f);
			if (model.currentStone.stone != null)
				alpha *= 0.7f;

			if (s.get_available() - ((s == model.currentStone.stone) ? 1 : 0) > 0)
			/* always show selected stone, even when dragging */
			{
				gl.glPushMatrix();
				gl.glRotatef(90 * model.showPlayer, 0, 1, 0);
				if (!model.vertical_layout)
					gl.glRotatef(90.0f, 0, 1, 0);
				gl.glTranslatef(-s.get_stone_size() * BoardRenderer.stone_size, 0, -s.get_stone_size() * BoardRenderer.stone_size);
				renderer.board.renderPlayerStone(gl, (s == highlightStone) ? -1 : lastPlayer, s, alpha);
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
		final float ROTSPEED = 130.0f;
		if (spinning == false && (Math.abs(currentAngle - lastAngle) > EPSILON)) {
			if (currentAngle < lastAngle) {
				currentAngle += elapsed * ROTSPEED;
				if (currentAngle > lastAngle)
					currentAngle = lastAngle;
			} else {
				currentAngle -= elapsed * ROTSPEED;
				if (currentAngle < lastAngle)
					currentAngle = lastAngle;				
			}
			return true;
		}
		return false;
	}
}
