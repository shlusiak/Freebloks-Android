package de.saschahlusiak.freebloks.view.scene;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL11;

import android.graphics.PointF;
import android.os.Handler;

import androidx.annotation.NonNull;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.model.Orientation;
import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.Shape;
import de.saschahlusiak.freebloks.view.BoardRenderer;
import de.saschahlusiak.freebloks.view.FreebloksRenderer;

public class Wheel implements SceneElement {
	private final static String tag = Wheel.class.getSimpleName();

	private final float MAX_FLING_SPEED = 100.0f;
	private final float MIN_FLING_SPEED = 2.0f;
	private final float MAX_STONE_DRAG_DISTANCE = 3.5f;

	private enum Status {
		IDLE, SPINNING, FLINGING
	}

	private Stone highlightStone;
	private float currentOffset;	/* the current offset */
	private float lastOffset;		/* the offset on last touch down; rotate back to here when idle */
	private float maxOffset;		/* the maximum offset till reaching the right end */
	private float originalX, originalY;

	private float flingSpeed, lastFlingOffset;

	private Status status = Status.IDLE;
	private ArrayList<Stone> stones;
	private int currentPlayer; /* the currently shown player */
	private Scene scene;
	private boolean moves_left;

	private PointF lastPointerLocation = new PointF();
	private PointF tmp = new PointF();
	private Handler handler = new Handler();

	private static final float stone_spacing = 5.5f * BoardRenderer.stoneSize * 2.0f;

	private Runnable hapticTimerRunnable = new Runnable() {
		@Override
		public void run() {
			if (status != Status.SPINNING)
				return;
			if (highlightStone == null)
				return;
			if (Math.abs(currentOffset - lastOffset) > 3.0f)
				return;
			if (!scene.game.isLocalPlayer())
				return;

			tmp.x = lastPointerLocation.x;
			tmp.y = lastPointerLocation.y;
			scene.boardObject.modelToBoard(tmp);

			if (!scene.soundPool.play(scene.soundPool.SOUND_CLICK2, 1.0f, 1))
				scene.vibrate(Global.VIBRATE_START_DRAGGING);
			showStone(highlightStone.getShape().getNumber());
			scene.currentStone.startDragging(tmp, highlightStone, Orientation.Default, scene.getPlayerColor(currentPlayer));
			scene.currentStone.hasMoved = true;
			scene.boardObject.resetRotation();
			status = Status.IDLE;

			scene.requestRender();
		}
	};

	public Wheel(Scene scene) {
		this.scene = scene;
		stones = new ArrayList<>();
		currentPlayer = -1;
	}

	public synchronized void update(int player) {
		stones.clear();
		if (player < 0)
			return;
		if (scene.game == null)
			return;
		Player p = scene.board.getPlayer(player);
		moves_left = p.getNumberOfPossibleTurns() > 0;
		for (int i = 0; i < Shape.COUNT; i++) {
			Stone s = p.getStone(i);
			if (s != null && s.isAvailable())
				stones.add(s);
		}
		this.highlightStone = null;
		maxOffset = (float)((stones.size() - 1)/ 2) * stone_spacing;

		if (stones.size() > 0)
			rotateTo((stones.size() + 1) / 2 - 2);

		this.currentPlayer = player;
	}

	private void rotateTo(int column) {
		lastOffset = (float)column * stone_spacing;
		if (lastOffset < 0.0f)
			lastOffset = 0.0f;
		if (lastOffset > maxOffset)
			lastOffset = maxOffset;
		if (!scene.hasAnimations())
			currentOffset = lastOffset;
	}

	private int getStonePositionInWheel(int stone) {
		for (int i = 0; i < stones.size(); i++)
			if (stones.get(i).getShape().getNumber() == stone)
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
		status = Status.IDLE;
		if (scene.game == null)
			return false;

		lastOffset = currentOffset;
		lastFlingOffset = currentOffset;
		flingSpeed = 0.0f;

		lastPointerLocation.x = m.x;
		lastPointerLocation.y = m.y;

		tmp.x = m.x;
		tmp.y = m.y;
		scene.boardObject.modelToBoard(tmp);
		scene.boardObject.boardToUnified(tmp);
		if (!scene.verticalLayout) {
			float t = tmp.x;
			tmp.x = tmp.y;
			tmp.y = scene.board.width - t - 1;
		}

		originalX = tmp.x;
		originalY = tmp.y;

		if (tmp.y > 0)
			return false;

		int row = (int) (-(tmp.y + 2.0f) / 6.7f);
		int col = (int) ((tmp.x - (float) scene.board.width / 2.0f + lastOffset) / stone_spacing + 0.5f);

		if (!scene.game.isLocalPlayer() || scene.game.getCurrentPlayer() != currentPlayer) {
			status = Status.SPINNING;
			return true;
		}

		int nr = col * 2 + row;
		if (nr < 0 || nr >= stones.size() || row > 1)
			highlightStone = null;
		else
			highlightStone = stones.get(nr);
		if (highlightStone != null && !highlightStone.isAvailable())
			highlightStone = null;
		else if (highlightStone != null) {
			/* we tapped on a stone; start timer */
			handler.removeCallbacks(hapticTimerRunnable);
			if (scene.currentStone.stone != null && scene.currentStone.stone != highlightStone) {
				scene.soundPool.play(scene.soundPool.SOUND_CLICK2, 1.0f, 1);
				status = Status.SPINNING;
			} else {
				status = Status.SPINNING;
				handler.postDelayed(hapticTimerRunnable, 500);
			}
		} else
			status = Status.SPINNING;

		return true;
	}

	@Override
	synchronized public boolean handlePointerMove(@NonNull PointF m) {
		if (status != Status.SPINNING)
			return false;

		tmp.x = m.x;
		tmp.y = m.y;
		scene.boardObject.modelToBoard(tmp);
		scene.boardObject.boardToUnified(tmp);

		if (!scene.verticalLayout) {
			float t = tmp.x;
			tmp.x = tmp.y;
			tmp.y = scene.board.width - t - 1;
		}

		/* everything underneath row 0 spins the wheel */
		float offset = (originalX - tmp.x) * 1.7f;
		offset *= 1.0f / (1.0f + Math.abs(originalY - tmp.y) / 2.3f);
		currentOffset += offset;
		if (currentOffset < 0.0f)
			currentOffset = 0.0f;
		if (currentOffset > maxOffset)
			currentOffset = maxOffset;

		originalX = tmp.x;

		if (!scene.game.isLocalPlayer() || scene.game.getCurrentPlayer() != currentPlayer) {
			scene.invalidate();
			return true;
		}

		/* if the wheel is moved too far, deselect highlighted stone */
		if (Math.abs(currentOffset - lastOffset) >= MAX_STONE_DRAG_DISTANCE) {
			highlightStone = null;
		}

		if (highlightStone != null && (tmp.y >= 0.0f || Math.abs(tmp.y - originalY) >= 3.5f)) {
			tmp.x = m.x;
			tmp.y = m.y;
			scene.boardObject.modelToBoard(tmp);
			showStone(highlightStone.getShape().getNumber());
			if (scene.currentStone.stone != highlightStone)
				scene.soundPool.play(scene.soundPool.SOUND_CLICK2, 1.0f, 1);
			scene.currentStone.startDragging(tmp, highlightStone, Orientation.Default, scene.getPlayerColor(currentPlayer));
			status = Status.IDLE;
			scene.boardObject.resetRotation();
		}
		scene.invalidate();
		return true;
	}

	@Override
	public boolean handlePointerUp(@NonNull PointF m) {
		handler.removeCallbacks(hapticTimerRunnable);
		if (status == Status.SPINNING) {
			if (highlightStone != null && scene.currentStone.stone != highlightStone && (Math.abs(lastOffset - currentOffset) < 0.5f)) {
				if (scene.currentStone.stone != null)
					scene.currentStone.startDragging(null, highlightStone, Orientation.Default, scene.getPlayerColor(currentPlayer));
				scene.currentStone.status = CurrentStone.Status.IDLE;
				showStone(highlightStone.getShape().getNumber());
				status = Status.IDLE;
			} else {
				lastOffset = currentOffset;
				if (scene.hasAnimations())
					status = Status.FLINGING;
				else
					status = Status.IDLE;
			}
			return true;
		}
		return false;
	}

	public synchronized void render(FreebloksRenderer renderer, GL11 gl) {
		if (scene.game == null)
			return;

		gl.glTranslatef(-currentOffset, 0, BoardRenderer.stoneSize * (scene.board.width + 10));
		for (int i = stones.size() - 1; i >= 0; i--) {
			Stone s = stones.get(i);

			if (s.getAvailable() - ((s == scene.currentStone.stone) ? 1 : 0) > 0) {
				final float col = i / 2;
				final float row = i % 2;
				final float offset = -((float)(s.getShape().getSize()) - 1.0f) * BoardRenderer.stoneSize;

				final float x = col * stone_spacing;
				final float effect = 12.5f / (12.5f + (float)Math.pow(Math.abs(x - currentOffset) * 0.5f, 2.5f));
				float y = 0.35f + effect * 0.75f;
				final float z = row * stone_spacing;
				final float scale = 0.9f + effect * 0.3f;
				float rotate = 90.0f * scene.boardObject.centerPlayer;
				if (!scene.verticalLayout)
					rotate -= 90.0f;

				float alpha = 1.0f;

				if (highlightStone == s && s != scene.currentStone.stone)
					y += 1.2f;

				if (!moves_left && !scene.game.isFinished())
					alpha *= 0.65f;
				alpha *= 0.75f + effect * 0.25f;

				gl.glPushMatrix();
				gl.glTranslatef(x, 0, z);
				
				gl.glScalef(scale, scale, scale);

				if (s.getAvailable() > 1 && s == highlightStone && s != scene.currentStone.stone) {
					gl.glPushMatrix();
					gl.glTranslatef(BoardRenderer.stoneSize, 0, BoardRenderer.stoneSize * 0.6f);
					gl.glRotatef(rotate, 0, 1, 0);
					gl.glScalef(0.85f, 0.85f, 0.85f);
					gl.glTranslatef(offset, 0, offset);

				//	gl.glTranslatef(BoardRenderer.stone_size * 0.5f, y - 1.2f, BoardRenderer.stone_size * 0.5f);
					renderer.boardRenderer.renderShape(gl, scene.getPlayerColor(currentPlayer), s.getShape(), Orientation.Default, alpha);
					gl.glPopMatrix();
				}
				
				gl.glRotatef(rotate, 0, 1, 0);
				gl.glTranslatef(offset, 0, offset);

				gl.glPushMatrix();
				renderer.boardRenderer.renderShapeShadow(gl, s.getShape(), scene.getPlayerColor(currentPlayer), Orientation.Default, y, 0, 0, 0, 0, 90 * scene.boardObject.centerPlayer, alpha, 1.0f);
				gl.glPopMatrix();

				gl.glTranslatef(0, y, 0);
				renderer.boardRenderer.renderShape(gl, (s == highlightStone && s != scene.currentStone.stone) ? 0 : scene.getPlayerColor(currentPlayer), s.getShape(), Orientation.Default, alpha);
				gl.glPopMatrix();
			}
		}
	}

	@Override
	public boolean execute(float elapsed) {
		final float EPSILON = 0.5f;
		if (status == Status.IDLE && (Math.abs(currentOffset - lastOffset) > EPSILON)) {
			final float ROTSPEED = 3.0f + (float)Math.pow(Math.abs(currentOffset - lastOffset), 0.65f) * 7.0f;

			if (!scene.hasAnimations()) {
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
		if (status == Status.SPINNING) {
			flingSpeed *= 0.2f;
			flingSpeed += 0.90f * (currentOffset - lastFlingOffset) / elapsed;
			if (flingSpeed > MAX_FLING_SPEED) {
				flingSpeed = MAX_FLING_SPEED;
			}
			if (flingSpeed < -MAX_FLING_SPEED) {
				flingSpeed = -MAX_FLING_SPEED;
			}
			lastFlingOffset = currentOffset;
		}
		if (status == Status.FLINGING) {
			if (Math.abs(flingSpeed) < MIN_FLING_SPEED) {
				status = Status.IDLE;
				lastOffset = currentOffset;
				return true;
			}

			currentOffset += flingSpeed * elapsed;
			if (Math.abs(currentOffset - lastOffset) >= MAX_STONE_DRAG_DISTANCE) {
				highlightStone = null;
			}
			flingSpeed *= (float)Math.pow(0.05f, elapsed);
			if (currentOffset < 0) {
				currentOffset = 0.0f;
				/* bounce */
				flingSpeed *= -0.4f;
			}
			if (currentOffset > maxOffset) {
				currentOffset = maxOffset;
				/* bounce */
				flingSpeed *= -0.4f;
			}
			return true;
		}
		return false;
	}
}
