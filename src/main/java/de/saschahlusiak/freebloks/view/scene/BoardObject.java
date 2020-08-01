package de.saschahlusiak.freebloks.view.scene;

import de.saschahlusiak.freebloks.model.GameMode;
import de.saschahlusiak.freebloks.model.Game;
import de.saschahlusiak.freebloks.view.BoardRenderer;
import android.graphics.PointF;

import androidx.annotation.NonNull;

public class BoardObject implements SceneElement {
	private Scene scene;
	public int lastSize;
	public float mAngleY;
	public int centerPlayer; /* the "center" position of the board, usually the first local */
	private float oa;
	private PointF om = new PointF();
	private boolean rotating = false;
	private boolean auto_rotate = true;
	private int lastDetailsPlayer = -1;
	private float ta;

	public BoardObject(Scene scene, int size) {
		this.scene = scene;
		this.lastSize = size;
		this.centerPlayer = 0;
		mAngleY = 0.0f;
		lastDetailsPlayer = -1;
	}

	/**
	 * Converts a point from model coordinates to (non-uniformed) board coordinates.
	 * The top-left corner is 0/0, the blue starting point is 0/19
	 * @param point
	 * @return point
	 */
	public PointF modelToBoard(PointF point) {
		point.x = point.x / (BoardRenderer.stoneSize * 2.0f);
		point.y = point.y / (BoardRenderer.stoneSize * 2.0f);

		point.x = point.x + 0.5f * (float)(scene.board.width - 1);
		point.y = point.y + 0.5f * (float)(scene.board.height - 1);

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
			p.y = scene.board.height - p.y - 1;
			break;
		case 1:
			tmp = p.x;
			p.x = p.y;
			p.y = tmp;
			break;
		case 2: /* 180 degree */
			p.x = scene.board.width - p.x - 1;
			break;
		case 3:
			tmp = p.y;
			p.y = scene.board.width - p.x - 1;
			p.x = scene.board.height - tmp - 1;
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

	public void updateDetailsPlayer() {
		int p;
		if (mAngleY > 0)
			p = ((int)mAngleY + 45) / 90;
		else
			p = ((int)mAngleY - 45) / 90;
		if (mAngleY < 10.0f && mAngleY >= - 10.0f)
			lastDetailsPlayer = -1;
		else
			lastDetailsPlayer = (centerPlayer + p + 4) % 4;

		final Game game = scene.game;
		if (game != null) {
			if (game.getGameMode() == GameMode.GAMEMODE_2_COLORS_2_PLAYERS ||
				game.getGameMode() == GameMode.GAMEMODE_DUO ||
				game.getGameMode() == GameMode.GAMEMODE_JUNIOR) {
				if (lastDetailsPlayer == 1)
					lastDetailsPlayer = 0;
				if (lastDetailsPlayer == 3)
					lastDetailsPlayer = 2;
			}
		}

		scene.setShowPlayerOverride(getShowDetailsPlayer(), lastDetailsPlayer >= 0);
	}

	/**
	 * returns the number of the player whose seeds are to be shown
	 *
	 * @return -1 if seeds are disabled
	 *         detail player if board is rotated
	 *         current player, if local
	 *         -1 otherwise
	 */
	public int getShowSeedsPlayer() {
		if (!scene.showSeeds)
			return -1;
		if (lastDetailsPlayer >= 0)
			return lastDetailsPlayer;
		if (scene.game.isFinished())
			return centerPlayer;
		if (scene.game.isLocalPlayer())
			return scene.game.getCurrentPlayer();
		return -1;
	}

	/**
	 * Returns the player, whose details are to be shown.
	 *
	 * @return player, 0..3, never -1
	 */
	private int getShowDetailsPlayer() {
		if (lastDetailsPlayer >= 0)
			return lastDetailsPlayer;
		if (!scene.game.isStarted())
			return -1;
		if (scene.game.isFinished())
			return centerPlayer;

		if (scene.game.getCurrentPlayer() >= 0)
			return scene.game.getCurrentPlayer();

		return centerPlayer;
	}

	/**
	 * The player that should be shown on the wheel.
	 *
	 * @return number between 0 and 3
	 */
	public int getShowWheelPlayer() {
		if (lastDetailsPlayer >= 0)
			return lastDetailsPlayer;
		if (scene.game == null)
			return centerPlayer;
		if (scene.game.isFinished()) {
			return centerPlayer;
		}
		if (scene.game.isLocalPlayer() || scene.showOpponents)
			return scene.game.getCurrentPlayer();
		/* TODO: would be nice to show the last current local player instead of the center one
		 * needs caching of previous local player */
		return centerPlayer;
	}

	@Override
	public boolean handlePointerDown(PointF m) {
		oa = (float)Math.atan2(m.y, m.x);
		om.x = m.x;
		om.y = m.y;
		rotating = true;
		auto_rotate = false;
		return true;
	}

	@Override
	public boolean handlePointerMove(@NonNull PointF m) {
		if (!rotating)
			return false;

		scene.currentStone.stopDragging();

		float an = (float)Math.atan2(m.y, m.x);
		mAngleY += (oa - an) / (float)Math.PI * 180.0f;
		oa = an;

		while (mAngleY >= 180.0f)
			mAngleY -= 360.0f;
		while (mAngleY <= -180.0f)
			mAngleY += 360.0f;
		updateDetailsPlayer();

		final int s = getShowWheelPlayer();

		if (scene.wheel.getCurrentPlayer() != s) {
			scene.wheel.update(s);
		}

		scene.invalidate();
		return true;
	}

	@Override
	public boolean handlePointerUp(@NonNull PointF m) {
		if (!rotating)
			return false;
		if (Math.abs(m.x - om.x) < 1 && Math.abs(m.y - om.y) < 1)
			resetRotation();
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
		auto_rotate = true;
		lastDetailsPlayer = -1;
	}

	@Override
	public boolean execute(float elapsed) {
		if (!rotating && scene.game != null && scene.game.isFinished() && auto_rotate) {
			final float ROTSPEED = 25.0f;

			mAngleY += elapsed * ROTSPEED;

			while (mAngleY >= 180.0f)
				mAngleY -= 360.0f;
			while (mAngleY <= -180.0f)
				mAngleY += 360.0f;

			updateDetailsPlayer();
			int s = getShowWheelPlayer();
			if (scene.wheel.getCurrentPlayer() != s) {
				scene.wheel.update(s);
			}

			return true;
		} else if (!rotating && Math.abs(mAngleY - ta) > 0.05f) {
			final float SNAPSPEED = 10.0f + (float)Math.pow(Math.abs(mAngleY - ta), 0.65f) * 30.0f;

			int lp = scene.wheel.getCurrentPlayer();
			if (mAngleY - ta > 0.1f) {
				mAngleY -= elapsed * SNAPSPEED;
				if (mAngleY - ta <= 0.1f) {
					mAngleY = ta;
					lp = -1;
				}
			}
			if (mAngleY - ta < -0.1f) {
				mAngleY += elapsed * SNAPSPEED;
				if (mAngleY - ta >= -0.1f) {
					mAngleY = ta;
					lp = -1;
				}
			}
			updateDetailsPlayer();
			int s = getShowWheelPlayer();
			if (lp != s) {
				scene.wheel.update(s);
			}
			return true;
		}
		return false;
	}
}
