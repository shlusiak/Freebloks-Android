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
import de.saschahlusiak.freebloks.view.effects.EffectSet;
import de.saschahlusiak.freebloks.view.effects.StoneFadeEffect;
import de.saschahlusiak.freebloks.view.effects.StoneRollEffect;

public class CurrentStone implements ViewElement {
	private static final String tag = CurrentStone.class.getSimpleName();
	
	enum Status {
		IDLE, DRAGGING, ROTATING, FLIPPING_HORIZONTAL, FLIPPING_VERTICAL
	}
	
	Stone stone;
	PointF pos = new PointF();
	boolean hasMoved, isValid;
	float stone_rel_x, stone_rel_y;
	float rotate_angle;
	int texture[];
	SimpleModel overlay;
	Status status;
	ViewModel model;
	
	final float hover_height_low = 0.45f;
	final float hover_height_high = 0.45f;
	
	CurrentStone(ViewModel model) {
		this.model = model;
		
		status = Status.IDLE;
		
		float s = 5.5f;
		texture = null;
		
		overlay = new SimpleModel(4, 2);
		overlay.addVertex(-s, 0, s, 0, -1, 0, 0, 0);
		overlay.addVertex(+s, 0, s, 0, -1, 0, 1, 0);
		overlay.addVertex(+s, 0, -s, 0, -1, 0, 1, 1);
		overlay.addVertex(-s, 0, -s, 0, -1, 0, 0, 1);
		overlay.addIndex(0, 1, 2);
		overlay.addIndex(0, 2, 3);
		
		overlay.commit();
	}
	
	public void updateTexture(Context context, GL10 gl) {
		if (texture == null)
			texture = new int[2];

		gl.glGenTextures(2, texture, 0);
		
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
	}
	
//	final float diffuse_red[] = { 1.0f, 0.5f, 0.5f, 1.0f };
//	final float diffuse_green[] = { 0.5f, 1.0f, 0.5f, 1.0f };
//	final float diffuse_white[] = { 1.0f, 1.0f, 1.0f, 0.6f };
	
	public synchronized void render(FreebloksRenderer renderer, GL10 gl) {
		if (stone == null)
			return;
		
		final float hover_height = (status == Status.IDLE) ? hover_height_low : hover_height_high;
		
		float offset = (float)(stone.get_stone_size()) - 1.0f;
		
		gl.glPushMatrix();
		
		gl.glDisable(GL10.GL_DEPTH_TEST);

	    gl.glPushMatrix();
	    /* TODO: optimize the following 3 groups of glTranslatef */
	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * pos.x,
	    		0,
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * pos.y);
		gl.glTranslatef(
				BoardRenderer.stone_size * offset,
				0,
				BoardRenderer.stone_size * offset);
		
	    gl.glTranslatef(-2.5f * hover_height * 0.11f, 0, 2.0f * hover_height * 0.11f);
		
		if (status == Status.ROTATING)
			gl.glRotatef(rotate_angle, 0, 1, 0);
		if (status == Status.FLIPPING_HORIZONTAL)
			gl.glRotatef(rotate_angle, 0, 0, 1);
		if (status == Status.FLIPPING_VERTICAL)
			gl.glRotatef(rotate_angle, 1, 0, 0);
		gl.glScalef(1.09f, 0, 1.09f);

	    gl.glTranslatef(
				-BoardRenderer.stone_size * offset,
				0,
				-BoardRenderer.stone_size * offset);

	    gl.glDisable(GL10.GL_CULL_FACE);
		renderer.board.renderStoneShadow(gl, model.spiel.current_player(), stone, 0.40f);
	    gl.glEnable(GL10.GL_CULL_FACE);
		gl.glPopMatrix();
		
		
		
		gl.glPushMatrix();
	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * (float)(pos.x + stone.get_stone_size() / 2),
	    		0,
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * (float)(pos.y + stone.get_stone_size() / 2));
	    
		if (status == Status.ROTATING)
			gl.glRotatef(rotate_angle, 0, 1, 0);
		if (status == Status.FLIPPING_HORIZONTAL)
			gl.glRotatef(rotate_angle, 0, 0, 1);
		if (status == Status.FLIPPING_VERTICAL)
			gl.glRotatef(rotate_angle, 1, 0, 0);
		
	    gl.glBindTexture(GL10.GL_TEXTURE_2D, isValid ? texture[0] : texture[1]);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnable(GL10.GL_BLEND);
		gl.glDisable(GL10.GL_LIGHTING);
		
	    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, overlay.getVertexBuffer());
	    gl.glNormalPointer(GL10.GL_FLOAT, 0, overlay.getNormalBuffer());
	    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, overlay.getTextureBuffer());
	    
	    overlay.drawElements(gl);
	    gl.glRotatef(180.0f, 1, 0, 0);
	    overlay.drawElements(gl);
	    gl.glRotatef(180.0f, 1, 0, 0);

		gl.glEnable(GL10.GL_LIGHTING);
	    
	    
	    gl.glPopMatrix();
	    
		gl.glDisable(GL10.GL_TEXTURE_2D);
		
	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * pos.x,
	    		0,
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * pos.y);

		gl.glTranslatef(
				BoardRenderer.stone_size * offset,
				hover_height,
				BoardRenderer.stone_size * offset);

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
	    
		renderer.board.renderPlayerStone(gl, model.spiel.current_player(), stone, 
				(status != Status.IDLE || isValid) ? 1.0f : BoardRenderer.DEFAULT_ALPHA);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		
		gl.glPopMatrix();
	}

	boolean moveTo(float x, float y) {
		if (stone == null)
			return false;
		
		/* provide a weird but nice pseudo snapping feeling */
		if (model.showAnimations) {
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

		/*
		for (int i = 0; i < stone.get_stone_size(); i++)
			for (int j = 0; j < stone.get_stone_size(); j++) {
				if (stone.get_stone_field(j, i) == Stone.STONE_FIELD_ALLOWED) {
					if (x + i < 0)
						x = -i;
					if (y + j < 0)
						y = -j;
					
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
		if (stone != null) {
			fieldPoint.x = m.x;
			fieldPoint.y = m.y;
			model.board.modelToBoard(fieldPoint);
			screenPoint.x = fieldPoint.x;
			screenPoint.y = fieldPoint.y;
			
			stone_rel_x = (pos.x - fieldPoint.x) + stone.get_stone_size() / 2;
			stone_rel_y = (pos.y - fieldPoint.y) + stone.get_stone_size() / 2;
			
//			Log.d(tag, "rel = (" + stone_rel_x + " / " + stone_rel_y+ ")");
			if ((Math.abs(stone_rel_x) <= 8) && (Math.abs(stone_rel_y) <= 8)) {
				if ((Math.abs(stone_rel_x) > 4.0f) && (Math.abs(stone_rel_y) < 3.0f) ||
					(Math.abs(stone_rel_x) < 3.0f) && (Math.abs(stone_rel_y) > 4.0f)) {
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
			model.redraw |= model.showAnimations;
		}
		if (status == Status.ROTATING) {
			float rx = (pos.x - fieldPoint.x) + stone.get_stone_size() / 2;
			float ry = (pos.y - fieldPoint.y) + stone.get_stone_size() / 2;
			float a1 = (float)Math.atan2(stone_rel_y, stone_rel_x);
			float a2 = (float)Math.atan2(ry, rx);
			rotate_angle = (a1 - a2) * 180.0f / (float)Math.PI;
			if (Math.abs(rx) + Math.abs(ry) < 5 && Math.abs(rotate_angle) < 25.0f) {
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
			
			if (fieldPoint.y < -2.0f) {
				model.wheel.highlightStone = stone;
				status = Status.IDLE;
				stone = null;
			} else	if (!hasMoved) {
				int player = model.spiel.current_player();
				if (model.activity.commitCurrentStone(stone, (int)Math.floor(pos.x + 0.5f), (int)Math.floor(pos.y + 0.5f))) {
					if (model.showAnimations) {
						Stone st = new Stone();
						st.copyFrom(stone);
						StoneRollEffect e = new StoneRollEffect(model, st, player, (int)Math.floor(pos.x + 0.5f), (int)Math.floor(pos.y + 0.5f), hover_height_high, -15.0f);
				
						EffectSet set = new EffectSet();
						set.add(e);
						set.add(new StoneFadeEffect(model, st, player, (int)Math.floor(pos.x + 0.5f), (int)Math.floor(pos.y + 0.5f), 4.0f));
						model.addEffect(set);
						
					}
					status = Status.IDLE;
					stone = null;
					model.wheel.highlightStone = null;
				}
			} else {
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
	
	synchronized public void startDragging(PointF fieldPoint, Stone stone) {
		this.stone = stone;
		if (stone == null) {
			status = Status.IDLE;
			return;
		}

		status = Status.DRAGGING;
		hasMoved = true;
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
