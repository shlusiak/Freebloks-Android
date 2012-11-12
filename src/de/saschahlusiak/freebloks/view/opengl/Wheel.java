package de.saschahlusiak.freebloks.view.opengl;

import java.util.Timer;
import java.util.TimerTask;

import android.graphics.PointF;
import android.util.Log;
import de.saschahlusiak.freebloks.model.Stone;

public class Wheel extends ViewElement {
	private final static String tag = Wheel.class.getSimpleName();
	
	int highlightStone = -1;
	float currentAngle = 0.0f;
	float originalAngle;
	Timer timer;
	
	public Wheel(ViewModel model) {
		super(model);
	}

	TimerTask moveStoneTask = new TimerTask() {
		@Override
		public void run() {
			timer = null;
			cancel();

			if (highlightStone == -1)
				return;

			if (Math.abs(currentAngle - originalAngle) < 10.0f) {

			}
		}
	};
	
	PointF tmp = new PointF();
	float originalX;
	boolean spinning = false;

	@Override
	boolean handlePointerDown(PointF m) {
		spinning = false;
		if (!model.spiel.is_local_player())
			return false;
		
		originalAngle = currentAngle;
		
		tmp.x = m.x;
		tmp.y = m.y;
		model.board.modelToBoard(tmp);
		model.board.boardToUnified(tmp);
		
		originalX = tmp.x;

		if (tmp.y > 0)
			return false;
		
		int row = (int) (-tmp.y / 5.5f);
		int col = (int) ((tmp.x - (float) model.spiel.m_field_size_x / 2.0f) / 7.0f + 5.5f + originalAngle / 17.0f);

		Log.d(tag, "currentWheelAngle = " + originalAngle);
		Log.d(tag, "unified coordinates (" + tmp.x + ", " + tmp.y + ")");
		Log.d(tag, "row " + row + ", col " + col);

		highlightStone = row * 11 + col;
		if (col > 11 || row > 1 || col < 0 || row < 0 || highlightStone >= 21) {
			highlightStone = -1;
			return false;
		}
		Stone s = model.spiel.get_current_player().get_stone(highlightStone);
		if (s != null && s.get_available() <= 0)
			highlightStone = -1;
		else if (s != null) {
			/* we tapped on a stone; start timer */
			if (timer != null)
				timer.cancel();
			timer = new Timer();
//			timer.schedule(moveStoneTask, 300);
		}

		if (model.currentStone.stone != null) {
			model.currentStone.stone = s;
		}
		model.activity.selectCurrentStone(model.spiel, s);
		spinning = true;
		return true;
	}

	@Override
	boolean handlePointerMove(PointF m) {
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
			highlightStone = -1;
			model.activity.selectCurrentStone(model.spiel, null);
		}

		if (highlightStone >= 0 && tmp.y >= 0) {
			if (Math.abs(currentAngle - originalAngle) < 90.0f) {
				// renderer.currentWheelAngle = originalWheelAngle;
				Stone stone = model.spiel.get_current_player().get_stone(
						highlightStone);
				model.activity.selectCurrentStone(model.spiel, stone);
				highlightStone = -1;
				tmp.x = m.x;
				tmp.y = m.y;
				model.board.modelToBoard(tmp);
				model.currentStone.startDragging(stone, (int)tmp.x, (int)tmp.y);
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

}
