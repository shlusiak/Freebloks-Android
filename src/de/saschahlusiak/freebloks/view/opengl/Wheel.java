package de.saschahlusiak.freebloks.view.opengl;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.PointF;
import android.os.Handler;
import android.util.Log;
import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Stone;

public class Wheel extends ViewElement {
	private final static String tag = Wheel.class.getSimpleName();
	
	Stone highlightStone;
	float currentAngle = 0.0f;
	float originalAngle;
	float originalX;
	boolean spinning = false;
	ArrayList<Stone> stones;
	int lastPlayer;
	
	public Wheel(ViewModel model) {
		super(model);
		stones = new ArrayList<Stone>();
	}
	
	PointF tmp = new PointF();
	Handler handler = new Handler();
	Timer timer = new Timer();
	TimerTask task;
	
	
	synchronized void update(int currentPlayer) {
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

	@Override
	synchronized boolean handlePointerDown(final PointF m) {
		spinning = false;
		
		originalAngle = currentAngle;
		
		tmp.x = m.x;
		tmp.y = m.y;
		model.board.modelToBoard(tmp);
		model.board.boardToUnified(tmp);
		
		originalX = tmp.x;

		if (tmp.y > 0)
			return false;
		
		int row = (int) (-(tmp.y + 2.0f) / 5.5f);
		int col = (int) ((tmp.x - (float) model.spiel.m_field_size_x / 2.0f) / 7.0f + 5.5f + originalAngle / 17.0f);

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
					if (Math.abs(currentAngle - originalAngle) > 10.0f)
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
							model.activity.selectCurrentStone(highlightStone);
							model.activity.vibrate(100);
							model.currentStone.startDragging(highlightStone);
							highlightStone = null;
							model.currentStone.moveTo((int)tmp.x, (int)tmp.y);
							spinning = false;
							model.view.updateView();
						}
					});
					spinning = false;
				}
			}, 500);
			if (model.currentStone.stone != null) {
				model.currentStone.stone = highlightStone;
			}
			model.activity.selectCurrentStone(highlightStone);
		}

		spinning = true;
		return true;
	}

	@Override
	synchronized boolean handlePointerMove(PointF m) {
		if (!spinning)
			return false;
		
		tmp.x = m.x;
		tmp.y = m.y;
		model.board.modelToBoard(tmp);
		model.board.boardToUnified(tmp);
		
		/* everything underneath row 0 spins the wheel */
		currentAngle += 8.0f * (originalX - tmp.x);
		while (currentAngle > 180)
			currentAngle -= 360.0f;
		while (currentAngle < -180)
			currentAngle += 360.0f;

		originalX = tmp.x;

		if (Math.abs(currentAngle - originalAngle) >= 90.0f) {
			highlightStone = null;
			model.activity.selectCurrentStone(null);
		}

		if (highlightStone != null && tmp.y >= 0) {
			if (Math.abs(currentAngle - originalAngle) < 90.0f) {
				// renderer.currentWheelAngle = originalWheelAngle;
				model.activity.selectCurrentStone(highlightStone);
				model.activity.vibrate(100);
				tmp.x = m.x;
				tmp.y = m.y;
				model.board.modelToBoard(tmp);
				model.currentStone.startDragging(highlightStone);
				highlightStone = null;
				spinning = false;
			}
		}
		return true;
	}
	
	@Override
	boolean handlePointerUp(PointF m) {
		spinning = false;
		return false;
	}

	public synchronized void render(FreebloksRenderer renderer, GL10 gl) {
		final float da = 17.0f;
		float angle = currentAngle + 9.5f * 0.5f * da;
		
		if (model.spiel == null)
			return;
		
		
		gl.glPushMatrix();
		gl.glTranslatef(0, -BoardRenderer.stone_size * 27.0f, 0);
		gl.glRotatef(currentAngle, 0, 0, 1);
		gl.glTranslatef(-BoardRenderer.stone_size * 5.1f * 6.5f, 0, BoardRenderer.stone_size * (model.spiel.m_field_size_x + 10));
		gl.glRotatef(9.5f * 0.5f * da, 0, 0, 1);
		gl.glPushMatrix();
//		gl.glScalef(0.6f, 0.6f, 0.6f);
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

			if (s.get_available() - ((s == model.currentStone.stone) ? 1 : 0) > 0) {
				gl.glRotatef(90 * lastPlayer, 0, 1, 0);
				gl.glTranslatef(-s.get_stone_size() * BoardRenderer.stone_size, 0, -s.get_stone_size() * BoardRenderer.stone_size);
				renderer.board.renderPlayerStone(gl, (s == highlightStone) ? -1 : lastPlayer, s, alpha);
				gl.glTranslatef(s.get_stone_size() * BoardRenderer.stone_size, 0, s.get_stone_size() * BoardRenderer.stone_size);
				gl.glRotatef(-90 * lastPlayer, 0, 1, 0);
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
		gl.glPopMatrix();
	}
}
