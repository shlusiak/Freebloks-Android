package de.saschahlusiak.freebloks.view.model;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.util.Log;
import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.view.BoardRenderer;
import de.saschahlusiak.freebloks.view.FreebloksRenderer;
import de.saschahlusiak.freebloks.view.SimpleModel;

public class CurrentStone implements ViewElement {
	private static final String tag = CurrentStone.class.getSimpleName();

	enum Status {
		IDLE, DRAGGING, ROTATING, FLIPPING_HORIZONTAL, FLIPPING_VERTICAL
	}

	Stone stone;
	int current_color;
	PointF pos = new PointF();
	boolean hasMoved; /* has the stone been moved since it was touched? */
	boolean canCommit; /* is the stone commitable if it has not been moved? */
	boolean isValid;
	float stone_rel_x, stone_rel_y;
	float rotate_angle;
	int texture[];
	SimpleModel overlay;
	Status status;
	ViewModel model;

	public final float hover_height_low = 0.55f;
	public final float hover_height_high = 0.55f;
	private static final float overlay_radius = 6.0f;

	CurrentStone(ViewModel model) {
		this.model = model;

		status = Status.IDLE;

		texture = null;

		overlay = new SimpleModel(4, 2);
		overlay.addVertex(-overlay_radius, 0, overlay_radius, 0, -1, 0, 0, 0);
		overlay.addVertex(+overlay_radius, 0, overlay_radius, 0, -1, 0, 1, 0);
		overlay.addVertex(+overlay_radius, 0, -overlay_radius, 0, -1, 0, 1, 1);
		overlay.addVertex(-overlay_radius, 0, -overlay_radius, 0, -1, 0, 0, 1);
		overlay.addIndex(0, 1, 2);
		overlay.addIndex(0, 2, 3);

		overlay.commit();
	}

	public void updateTexture(Context context, GL10 gl) {
		if (texture == null)
			texture = new int[3];

		gl.glGenTextures(3, texture, 0);

		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.stone_overlay_green);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);
		if (gl instanceof GL11) {
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR_MIPMAP_NEAREST);
			gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
		} else {
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		}
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		BoardRenderer.myTexImage2D(gl, bitmap);
		bitmap.recycle();

		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.stone_overlay_red);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[1]);
		if (gl instanceof GL11) {
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR_MIPMAP_NEAREST);
			gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
		} else {
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		}
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		BoardRenderer.myTexImage2D(gl, bitmap);
		bitmap.recycle();

		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.stone_overlay_shadow);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[2]);
		if (gl instanceof GL11) {
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR_MIPMAP_NEAREST);
			gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
		} else {
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		}
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		BoardRenderer.myTexImage2D(gl, bitmap);
		bitmap.recycle();
	}

//	final float diffuse_red[] = { 1.0f, 0.5f, 0.5f, 1.0f };
//	final float diffuse_green[] = { 0.5f, 1.0f, 0.5f, 1.0f };
//	final float diffuse_white[] = { 1.0f, 1.0f, 1.0f, 0.6f };

	public synchronized void render(FreebloksRenderer renderer, GL10 gl) {
		if (stone == null)
			return;

		float hover_height = (status == Status.IDLE) ? hover_height_low : hover_height_high;
		final float offset = (float)(stone.get_stone_size()) - 1.0f;

		if (status == Status.FLIPPING_HORIZONTAL ||
			status == Status.FLIPPING_VERTICAL)
			hover_height += Math.abs(Math.sin(rotate_angle / 180 * Math.PI) * overlay_radius * 0.4f);

		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glPushMatrix();
		gl.glTranslatef(
				BoardRenderer.stone_size * (-(float)(model.spiel.m_field_size_x - 1) + 2.0f * pos.x + offset),
				0,
				BoardRenderer.stone_size * (-(float)(model.spiel.m_field_size_x - 1) + 2.0f * pos.y + offset));

		/* STONE SHADOW */
	    gl.glPushMatrix();
		/* TODO: remove this and always show the board at the exact same angle,
		 * so we always have light coming from top left */
		/* TODO: merge with BoardRenderer.renderShadow() */
		gl.glRotatef(-model.board.centerPlayer * 90, 0, 1, 0);
	    gl.glTranslatef(2.5f * hover_height * 0.08f, 0, 2.0f * hover_height * 0.08f);
		gl.glRotatef(model.board.centerPlayer * 90, 0, 1, 0);

		if (status == Status.ROTATING)
			gl.glRotatef(rotate_angle, 0, 1, 0);
		if (status == Status.FLIPPING_HORIZONTAL)
			gl.glRotatef(rotate_angle, 0, 0, 1);
		if (status == Status.FLIPPING_VERTICAL)
			gl.glRotatef(rotate_angle, 1, 0, 0);
		gl.glScalef(1.09f, 0.01f, 1.09f);

	    gl.glTranslatef(
				-BoardRenderer.stone_size * offset,
				0,
				-BoardRenderer.stone_size * offset);

		renderer.board.renderStoneShadow(gl, model.spiel.current_player(), stone, 0.40f);
		gl.glPopMatrix();


		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnable(GL10.GL_BLEND);
		gl.glDisable(GL10.GL_LIGHTING);

	    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, overlay.getVertexBuffer());
	    gl.glNormalPointer(GL10.GL_FLOAT, 0, overlay.getNormalBuffer());
	    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, overlay.getTextureBuffer());


	    /* OVERLAY SHADOW */
	    gl.glPushMatrix();
	    gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[2]);
		gl.glRotatef(-model.board.centerPlayer * 90, 0, 1, 0);
	    gl.glTranslatef(2.5f * hover_height * 0.08f, 0, 2.0f * hover_height * 0.08f);
		gl.glRotatef(model.board.centerPlayer * 90, 0, 1, 0);
		if (status == Status.ROTATING)
			gl.glRotatef(rotate_angle, 0, 1, 0);
		if (status == Status.FLIPPING_HORIZONTAL)
			gl.glRotatef(rotate_angle, 0, 0, 1);
		if (status == Status.FLIPPING_VERTICAL)
			gl.glRotatef(rotate_angle, 1, 0, 0);
	    gl.glScalef(1.0f, 0.01f, 1.0f);
	    overlay.drawElements(gl);
	    gl.glPopMatrix();

		/* OVERLAY */
		gl.glPushMatrix();
		gl.glTranslatef(0, hover_height, 0);
		if (status == Status.ROTATING)
			gl.glRotatef(rotate_angle, 0, 1, 0);
		if (status == Status.FLIPPING_HORIZONTAL)
			gl.glRotatef(rotate_angle, 0, 0, 1);
		if (status == Status.FLIPPING_VERTICAL)
			gl.glRotatef(rotate_angle, 1, 0, 0);

	    gl.glBindTexture(GL10.GL_TEXTURE_2D, isValid ? texture[0] : texture[1]);
	    overlay.drawElements(gl);


	    gl.glEnable(GL10.GL_CULL_FACE);
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glDisable(GL10.GL_TEXTURE_2D);


		/* STONE */
	    gl.glPopMatrix();
		gl.glTranslatef(
				0,
				hover_height,
				0);
		if (status == Status.ROTATING)
			gl.glRotatef(rotate_angle, 0, 1, 0);
		if (status == Status.FLIPPING_HORIZONTAL)
			gl.glRotatef(rotate_angle, 0, 0, 1);
		if (status == Status.FLIPPING_VERTICAL)
			gl.glRotatef(rotate_angle, 1, 0, 0);

	    gl.glTranslatef(
				-BoardRenderer.stone_size * offset,
				0,
				-BoardRenderer.stone_size * offset);

	    gl.glEnable(GL10.GL_DEPTH_TEST);
		renderer.board.renderPlayerStone(gl, current_color, stone,
				(status != Status.IDLE || isValid) ? 1.0f : BoardRenderer.DEFAULT_ALPHA);

		gl.glPopMatrix();
	}

	boolean moveTo(float x, float y) {
		if (stone == null)
			return false;

		/* provide a weird but nice pseudo snapping feeling */
		if (model.hasAnimations()) {
			float r = x - (float)Math.floor(x);
			if (r < 0.5f) {
				r *= 2.0f;
				r = r * r * r * r;
				r /= 2.0f;
			} else {
				r = 1.0f - r;
				r *= 2.0f;
				r = r * r * r * r;
				r /= 2.0f;
				r = 1.0f - r;
			}
			x = (float)(Math.floor(x) + r);

			r = y - (float)Math.floor(y);
			if (r < 0.5f) {
				r *= 2.0f;
				r = r * r * r;
				r /= 2.0f;
			} else {
				r = 1.0f - r;
				r *= 2.0f;
				r = r * r * r;
				r /= 2.0f;
				r = 1.0f - r;
			}
			y = (float)(Math.floor(y) + r);
		} else {
			x = (float)Math.floor(x + 0.5f);
			y = (float)Math.floor(y + 0.5f);
		}

		/* FIXME: lock stone inside 3 top walls, when board is always in the same orientation */
		/*
		for (int i = 0; i < stone.get_stone_size(); i++)
			for (int j = 0; j < stone.get_stone_size(); j++) {
				if (stone.get_stone_field(j, i) == Stone.STONE_FIELD_ALLOWED) {
					if (x + i < 0)
						x = -i;
//					if (y + j < 0)
//						y = -j;

					if (x + i + 1 >= model.spiel.m_field_size_x)
						x = model.spiel.m_field_size_x - i - 1;
					if (y + j + 1 >= model.spiel.m_field_size_y)
						y = model.spiel.m_field_size_y - j - 1;
				}
			}
			*/


		if (Math.floor(0.5f + pos.x) != Math.floor(0.5f + x) || Math.floor(pos.y + 0.5f) != Math.floor(0.5f + y)) {
			pos.x = x;
			pos.y = y;
			return true;
		}
		pos.x = x;
		pos.y = y;
		return false;
	}

	PointF fieldPoint = new PointF();
	PointF screenPoint = new PointF();

	@Override
	synchronized public boolean handlePointerDown(PointF m) {
		status = Status.IDLE;
		hasMoved = false;
		canCommit = true;
		if (stone != null) {
			fieldPoint.x = m.x;
			fieldPoint.y = m.y;
			model.board.modelToBoard(fieldPoint);
			screenPoint.x = fieldPoint.x;
			screenPoint.y = fieldPoint.y;

			stone_rel_x = (pos.x - fieldPoint.x) + stone.get_stone_size() / 2;
			stone_rel_y = (pos.y - fieldPoint.y) + stone.get_stone_size() / 2;

//			Log.d(tag, "rel = (" + stone_rel_x + " / " + stone_rel_y+ ")");
			if ((Math.abs(stone_rel_x) <= overlay_radius + 3.0f) && (Math.abs(stone_rel_y) <= overlay_radius + 3.0f)) {
				if ((Math.abs(stone_rel_x) > overlay_radius - 1.5f) && (Math.abs(stone_rel_y) < 2.5f) ||
					(Math.abs(stone_rel_x) < 2.5f) && (Math.abs(stone_rel_y) > overlay_radius - 1.5f)) {
					status = Status.ROTATING;
					rotate_angle = 0.0f;
				} else {
					status = Status.DRAGGING;
				}
				return true;
			}
		}
		return false;
	}

	@Override
	synchronized public boolean handlePointerMove(PointF m) {
		if (status == Status.IDLE)
			return false;
		if (stone == null)
			return false;

		fieldPoint.x = m.x;
		fieldPoint.y = m.y;
		model.board.modelToBoard(fieldPoint);

		if (status == Status.DRAGGING) {
			final float THRESHOLD = 1.0f;
			float x = (fieldPoint.x + stone_rel_x - stone.get_stone_size() / 2);
			float y = (fieldPoint.y + stone_rel_y - stone.get_stone_size() / 2);
			if (!hasMoved && (Math.abs(screenPoint.x - fieldPoint.x) < THRESHOLD) && Math.abs(screenPoint.y - fieldPoint.y) < THRESHOLD)
				return true;

			boolean mv = snap(x, y, false);
			hasMoved |= mv;
			model.redraw |= mv;
			model.redraw |= model.hasAnimations();
		}
		if (status == Status.ROTATING) {
			float rx = (pos.x - fieldPoint.x) + stone.get_stone_size() / 2;
			float ry = (pos.y - fieldPoint.y) + stone.get_stone_size() / 2;
			float a1 = (float)Math.atan2(stone_rel_y, stone_rel_x);
			float a2 = (float)Math.atan2(ry, rx);
			rotate_angle = (a1 - a2) * 180.0f / (float)Math.PI;
			if (Math.abs(rx) + Math.abs(ry) < overlay_radius * 0.9f && Math.abs(rotate_angle) < 25.0f) {
				rotate_angle = 0.0f;
				status = Math.abs(stone_rel_y) < 3 ? Status.FLIPPING_HORIZONTAL : Status.FLIPPING_VERTICAL;
			}
			model.redraw = true;
		}
		if (status == Status.FLIPPING_HORIZONTAL) {
			float rx = (pos.x - fieldPoint.x) + stone.get_stone_size() / 2;
			float p = 0.0f;
			p = (stone_rel_x - rx) / (stone_rel_x * 2.0f);
			if (p < 0)
				p = 0;
			if (p > 1)
				p = 1;
			if (stone_rel_x > 0)
				p = -p;

			rotate_angle = p * 180;
			model.redraw = true;
		}
		if (status == Status.FLIPPING_VERTICAL) {
			float ry = (pos.y - fieldPoint.y) + stone.get_stone_size() / 2;
			float p = 0.0f;
			p = (stone_rel_y - ry) / (stone_rel_y * 2.0f);
			if (p < 0)
				p = 0;
			if (p > 1)
				p = 1;
			if (stone_rel_y < 0)
				p = -p;

			rotate_angle = p * 180;
			model.redraw = true;
		}
		return true;
	}

	public boolean is_valid_turn(float x, float y) {
		if (model.spiel.is_valid_turn(stone, model.spiel.current_player(), (int)Math.floor(y + 0.5f), (int)Math.floor(x + 0.5f)) == Stone.FIELD_ALLOWED)
			return true;
		return false;
	}

	boolean snap(float x, float y, boolean forceSound) {
		boolean hasMoved = false;
		if (!model.snapAid) {
			hasMoved = moveTo(x, y);
			isValid = is_valid_turn(x, y);
			if (isValid && (hasMoved || forceSound)) {
				if (!model.soundPool.play(model.soundPool.SOUND_CLICK3, 1.0f, 1.0f))
					model.activity.vibrate(Global.VIBRATE_STONE_SNAP);
			}
			return hasMoved;
		}
		if (is_valid_turn(x, y)) {
			isValid = true;
			hasMoved = moveTo((float)Math.floor(x + 0.5f), (float)Math.floor(y + 0.5f));
			if (hasMoved || forceSound) {
				if (!model.soundPool.play(model.soundPool.SOUND_CLICK3, 0.2f, 1.0f))
					model.activity.vibrate(Global.VIBRATE_STONE_SNAP);
			}
			return hasMoved;
		}
		for (int i = -1; i <= 1; i++)
			for (int j = -1; j <= 1; j++)
		{
			if (is_valid_turn(x + i, y + j))
			{
				isValid = true;
				hasMoved = moveTo((float)Math.floor(0.5f + x + i), (float)Math.floor(0.5f + y + j));
				if (hasMoved) {
					if (!model.soundPool.play(model.soundPool.SOUND_CLICK3, 0.2f, 1.0f))
						model.activity.vibrate(Global.VIBRATE_STONE_SNAP);
				}
				return hasMoved;
			}
		}
		isValid = false;
		return moveTo(x, y);
	}

	@Override
	synchronized public boolean handlePointerUp(PointF m) {
		if (status == Status.DRAGGING) {
			fieldPoint.x = m.x;
			fieldPoint.y = m.y;
			model.board.modelToBoard(fieldPoint);

			int x = (int)Math.floor(0.5f + fieldPoint.x + stone_rel_x - stone.get_stone_size() / 2);
			int y = (int)Math.floor(0.5f + fieldPoint.y + stone_rel_y - stone.get_stone_size() / 2);
			fieldPoint.x = x;
			fieldPoint.y = y;
			model.board.boardToUnified(fieldPoint);
			if (!model.vertical_layout)
				fieldPoint.y = model.spiel.m_field_size_x - fieldPoint.x - 1;

			if (fieldPoint.y < -2.0f && (hasMoved)) {
				model.wheel.setCurrentStone(stone);
				status = Status.IDLE;
				stone = null;
			} else	if (canCommit && !hasMoved) {
				if (model.activity.commitCurrentStone(stone, (int)Math.floor(pos.x + 0.5f), (int)Math.floor(pos.y + 0.5f))) {
					status = Status.IDLE;
					stone = null;
					model.wheel.setCurrentStone(null);
				}
			} else if (hasMoved) {
				snap(x, y, false);
			}
		}
		if (status == Status.ROTATING) {
			while (rotate_angle < -45.0f) {
				rotate_angle += 90.0f;
				stone.rotate_right();
			}
			while (rotate_angle > 45.0f) {
				rotate_angle -= 90.0f;
				stone.rotate_left();
			}
			rotate_angle = 0.0f;
			snap(pos.x, pos.y, true);
		}
		if (status == Status.FLIPPING_HORIZONTAL) {
			if (Math.abs(rotate_angle) > 90.0f)
				stone.mirror_over_y();

			rotate_angle = 0.0f;
			snap(pos.x, pos.y, true);
		}
		if (status == Status.FLIPPING_VERTICAL) {
			if (Math.abs(rotate_angle) > 90.0f)
				stone.mirror_over_x();

			rotate_angle = 0.0f;
			snap(pos.x, pos.y, true);
		}
		status = Status.IDLE;
		return false;
	}

	synchronized public void startDragging(PointF fieldPoint, Stone stone, int color) {
		this.stone = stone;
		this.current_color = color;
		if (stone == null) {
			status = Status.IDLE;
			return;
		}

		status = Status.DRAGGING;

		hasMoved = false;
		canCommit = false;
		/* TODO: set this to about 3 above the touch event to make stone visible
		 * when started dragging */
		stone_rel_x = 0;
		stone_rel_y = 0;

		if (fieldPoint != null) {
			int x = (int)Math.floor(0.5f + fieldPoint.x + stone_rel_x - stone.get_stone_size() / 2);
			int y = (int)Math.floor(0.5f + fieldPoint.y + stone_rel_y - stone.get_stone_size() / 2);

			moveTo(x, y);
		}
		isValid = is_valid_turn((int)pos.x, (int)pos.y);
	}

	@Override
	public boolean execute(float elapsed) {
		return false;
	}
}
