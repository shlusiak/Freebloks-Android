package de.saschahlusiak.freebloks.view.model;

import javax.microedition.khronos.opengles.GL10;

import de.saschahlusiak.freebloks.view.BoardRenderer;
import de.saschahlusiak.freebloks.view.FreebloksRenderer;
import android.graphics.PointF;

public class Board implements ViewElement {
	ViewModel model;
	public int last_size;
	public float mAngleY;
	public int centerPlayer; /* the "center" position of the board, usually the first local */
	
	public Board(ViewModel model, int size) {
		this.model = model;
		this.last_size = size;
		this.centerPlayer = 0;
		mAngleY = 0.0f;
	}

	/**
	 * Converts a point from model coordinates to (non-uniformed) board coordinates.
	 * The top-left corner is 0/0, the blue starting point is 0/19
	 * @param point
	 * @return point
	 */
	public PointF modelToBoard(PointF point) {
		point.x = point.x / (BoardRenderer.stone_size * 2.0f);
		point.y = point.y / (BoardRenderer.stone_size * 2.0f);
		
		point.x = point.x + 0.5f * (float)(model.spiel.m_field_size_x - 1);
		point.y = point.y + 0.5f * (float)(model.spiel.m_field_size_y - 1);

		return point;
	}
	
	/**
	 * converts p from relative board coordinates, to rotated board coordinates
	 * relative board coordinates: yellow starting point is 0/0, blue starting point is 0/19
	 * unified coordinates: bottom left corner is always 0/0
	 * @param p
	 */
	void boardToUnified(PointF p) {
		float tmp;
		
		switch (centerPlayer) {
		default:
		case 0: /* nothing */
			p.y = model.spiel.m_field_size_y - p.y - 1;
			break;
		case 1:
			tmp = p.x;
			p.x = p.y;
			p.y = tmp;
			break;
		case 2: /* 180 degree */
			p.x = model.spiel.m_field_size_x - p.x - 1;
			break;
		case 3:
			tmp = p.y;
			p.y = model.spiel.m_field_size_x - p.x - 1;
			p.x = model.spiel.m_field_size_y - tmp - 1;
			break;
		}
	}
	
	/**
	 * @return the base angle for the camera, to focus on the center player
	 */
	public float getCameraAngle() {
		if (centerPlayer < 0)
			return 0.0f;
		return -90.0f * (float)centerPlayer;
	}
	
	/**
	 * returns the player, whose details are to be shown, if board is rotated, -1 otherwise
	 * @return player, the board is rotated to
	 * @return -1, if board is not rotated
	 */
	public int getShowDetailsPlayer() {
		int p;
		if (model.spiel == null)
			return -1;
		if (mAngleY > 0)
			p = ((int)mAngleY + 45) / 90;
		else
			p = ((int)mAngleY - 45) / 90;
		if (mAngleY < 10.0f && mAngleY >= - 10.0f)
			return -1;
		return (centerPlayer + p + 4) % 4;
	}
	
	/**
	 * returns the number of the player whose seeds are to be shown
	 * 
	 * @return -1 if seeds are disabled
	 * @return detail player if board is rotated
	 * @return current player, if local
	 * @return -1 otherwise
	 */
	private int getShowSeedsPlayer() {
		if (!model.showSeeds)
			return -1;
		if (model.spiel == null)
			return -1;
		if (getShowDetailsPlayer() >= 0)
			return getShowDetailsPlayer();
		if (model.spiel.is_finished())
			return centerPlayer;
		if (model.spiel.is_local_player())
			return model.spiel.current_player();
		return -1;
	}
	
	/**
	 * the player that should be shown on the wheel
	 * @return
	 */
	public int getShowWheelPlayer() {
		if (getShowDetailsPlayer() >= 0)
			return getShowDetailsPlayer();
		if (model.spiel == null)
			return centerPlayer;
		if (model.spiel.is_finished()) {
			return centerPlayer;
		}
		if (model.spiel.is_local_player() || model.showOpponents)
			return model.spiel.current_player();
		/* TODO: would be nice to show the last current local player instead of the center one
		 * needs caching of previous local player */
		return centerPlayer;
	}
	
	public final void render(FreebloksRenderer renderer, GL10 gl) {
		renderer.board.renderBoard(gl, model.spiel, getShowSeedsPlayer());
	}

	float oa;
	PointF om = new PointF();
	boolean rotating = false;

	@Override
	public boolean handlePointerDown(PointF m) {
		oa = (float)Math.atan2(m.y, m.x);
		om.x = m.x;
		om.y = m.y;
		rotating = true;
		return true;
	}

	@Override
	public boolean handlePointerMove(PointF m) {
		if (!rotating)
			return false;
		
		model.currentStone.startDragging(null, null);
		
		float an = (float)Math.atan2(m.y, m.x);
		mAngleY += (oa - an) / (float)Math.PI * 180.0f;
		oa = an;
		
		while (mAngleY >= 180.0f)
			mAngleY -= 360.0f;
		while (mAngleY <= -180.0f)
			mAngleY += 360.0f;
		
		int s = getShowDetailsPlayer();
		if (s < 0)
			s = getShowWheelPlayer();
		if (model.wheel.getLastPlayer() != s) {
			model.wheel.update(s);
			model.activity.showPlayer(s);
		}

		model.redraw = true;
		return true;
	}
	
	float ta;

	@Override
	public boolean handlePointerUp(PointF m) {
		if (!rotating)
			return false;
		if (Math.abs(m.x - om.x) < 1 && Math.abs(m.y - om.y) < 1)
			ta = 0.0f;
		else {
			if (mAngleY > 0)
				ta = (float)(((int)mAngleY + 45) / 90 * 90);
			else
				ta = (float)(((int)mAngleY - 45) / 90 * 90);
		}
		rotating = false;
		return false;
	}
	
	public void resetRotation() {
		ta = 0.0f;
	}

	@Override
	public boolean execute(float elapsed) {
		if (!rotating && Math.abs(mAngleY - ta) > 0.05f) {
			final float SNAPSPEED = 10.0f + (float)Math.pow(Math.abs(mAngleY - ta), 0.65f) * 30.0f;
			
			if (mAngleY - ta > 0.1f) {
				mAngleY -= elapsed * SNAPSPEED;
				if (mAngleY - ta <= 0.1f)
					mAngleY = ta;
			}
			if (mAngleY - ta < -0.1f) {
				mAngleY += elapsed * SNAPSPEED;
				if (mAngleY - ta >= -0.1f)
					mAngleY = ta;
			}			
			int s = getShowWheelPlayer();
			if (model.wheel.getLastPlayer() != s) {
				model.wheel.update(s);
				model.activity.showPlayer(s);
			}
			return true;
		}
		return false;
	}
}
