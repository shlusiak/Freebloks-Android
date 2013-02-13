package de.saschahlusiak.freebloks.view.model;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;
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
	Point pos = new Point();
	boolean hasMoved;
	boolean startedFromBelow;
	float stone_rel_x, stone_rel_y;
	float rotate_angle;
	int texture[];
	SimpleModel overlay;
	Status status;
	ViewModel model;
	
	
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
			texture = new int[1];

		gl.glGenTextures(1, texture, 0);
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.stone_overlay);

		
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
	}
	
	final float diffuse_red[] = { 1.0f, 0.5f, 0.5f, 1.0f };
	final float diffuse_green[] = { 0.5f, 1.0f, 0.5f, 1.0f };
	
	public synchronized void render(FreebloksRenderer renderer, GL10 gl) {
		if (stone == null)
			return;
		
		gl.glPushMatrix();
		gl.glTranslatef(0, 0.3f, 0.0f);
		
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

	    gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnable(GL10.GL_BLEND);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		
		/* TODO: cache result of is_valid_turn when moved */
		boolean isvalid = model.spiel.is_valid_turn(stone, model.showPlayer, pos.y, pos.x) == Stone.FIELD_ALLOWED;
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, isvalid ? diffuse_green : diffuse_red, 0);

		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

	    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, overlay.getVertexBuffer());
	    gl.glNormalPointer(GL10.GL_FLOAT, 0, overlay.getNormalBuffer());
	    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, overlay.getTextureBuffer());
	    
	    overlay.drawElements(gl);
	    gl.glRotatef(180.0f, 1, 0, 0);
	    overlay.drawElements(gl);
	    gl.glRotatef(180.0f, 1, 0, 0);

		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	    
	    
	    gl.glPopMatrix();
	    
		gl.glDisable(GL10.GL_BLEND);
		gl.glDisable(GL10.GL_TEXTURE_2D);
		
	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * pos.x,
	    		0,
	    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * pos.y);
		
		
		float offset = (float)(stone.get_stone_size())/ 2.0f;
		offset -= 0.5f;
	    	
		gl.glTranslatef(
				BoardRenderer.stone_size * 2.0f * offset,
				0,
				BoardRenderer.stone_size * 2.0f * offset);

		if (status == Status.ROTATING)
			gl.glRotatef(rotate_angle, 0, 1, 0);
		if (status == Status.FLIPPING_HORIZONTAL)
			gl.glRotatef(rotate_angle, 0, 0, 1);
		if (status == Status.FLIPPING_VERTICAL)
			gl.glRotatef(rotate_angle, 1, 0, 0);
	    	
	    gl.glTranslatef(
				-BoardRenderer.stone_size * 2.0f * offset,
				0,
				-BoardRenderer.stone_size * 2.0f * offset);
	    
		renderer.board.renderPlayerStone(gl, model.spiel.current_player(), stone, BoardRenderer.DEFAULT_ALPHA);
		
		gl.glEnable(GL10.GL_DEPTH_TEST);
		
		gl.glPopMatrix();
	}

	boolean moveTo(int x, int y) {
		if (stone == null)
			return false;
		
		for (int i = 0; i < stone.get_stone_size(); i++)
			for (int j = 0; j < stone.get_stone_size(); j++) {
				if (stone.get_stone_field(j, i) == Stone.STONE_FIELD_ALLOWED) {
					if (x + i < 0)
						x = -i;
					if (y + j < 0)
						y = -j;
					
					if (x + i >= model.spiel.m_field_size_x)
						x = model.spiel.m_field_size_x - i - 1;
					if (y + j >= model.spiel.m_field_size_y)
						y = model.spiel.m_field_size_y - j - 1;
				}
			}
		
		if (pos.x != x || pos.y != y) {
			pos.x = x;
			pos.y = y;
			return true;
		}
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
					startedFromBelow = false;
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
			int x = (int)(0.5f + fieldPoint.x + stone_rel_x - stone.get_stone_size() / 2);
			int y = (int)(0.5f + fieldPoint.y + stone_rel_y - stone.get_stone_size() / 2);
			if (!hasMoved && (Math.abs(screenPoint.x - fieldPoint.x) < THRESHOLD) && Math.abs(screenPoint.y - fieldPoint.y) < THRESHOLD)
				return true;
			
			if (model.snapAid && model.spiel.is_valid_turn(stone, model.showPlayer, y, x) != Stone.FIELD_ALLOWED)
				for (int i = -1; i <= 1; i++)
					for (int j = -1; j <= 1; j++)
				{
					if (model.spiel.is_valid_turn(stone, model.showPlayer, y + j, x + i) == Stone.FIELD_ALLOWED)
					{
						boolean mv = moveTo(x + i, y + j);
						model.redraw |= mv;
						hasMoved |= mv;
						
						return true;
					}
				}

			boolean mv = moveTo(x, y);
			model.redraw |= mv;
			hasMoved |= mv;
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
	
	@Override
	synchronized public boolean handlePointerUp(PointF m) {
		if (status == Status.DRAGGING) {
			fieldPoint.x = m.x;
			fieldPoint.y = m.y;
			model.board.modelToBoard(fieldPoint);

			int x = (int)(0.5f + fieldPoint.x + stone_rel_x - stone.get_stone_size() / 2);
			int y = (int)(0.5f + fieldPoint.y + stone_rel_y + stone.get_stone_size() / 2);
			fieldPoint.x = x;
			fieldPoint.y = y;
			model.board.boardToUnified(fieldPoint);
			if (!model.vertical_layout)
				fieldPoint.y = fieldPoint.x;
			
			if (!startedFromBelow && fieldPoint.y < -2.0f) {
				model.wheel.highlightStone = stone;
				status = Status.IDLE;
				stone = null;
			} else	if (!hasMoved) {
				int player = model.spiel.current_player();
				if (model.activity.commitCurrentStone(stone, pos.x, pos.y)) {
					if (model.showAnimations) {
						Stone st = new Stone();
						st.copyFrom(stone);
						StoneRollEffect e = new StoneRollEffect(st, player, pos.x, pos.y, 0.5f, -13.0f);
				
						EffectSet set = new EffectSet();
						set.add(e);
						set.add(new StoneFadeEffect(st, player, pos.x, pos.y, 4.0f));
						model.addEffect(set);
						
					}
					status = Status.IDLE;
					stone = null;
					model.wheel.highlightStone = null;
				}
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
			/* TODO: generalize snap code */
			if (model.snapAid && model.spiel.is_valid_turn(stone, model.showPlayer, pos.y, pos.x) != Stone.FIELD_ALLOWED)
				for (int i = -1; i <= 1; i++)
					for (int j = -1; j <= 1; j++)
				{
					if (model.spiel.is_valid_turn(stone, model.showPlayer, pos.y + j, pos.x + i) == Stone.FIELD_ALLOWED)
					{
						boolean mv = moveTo(pos.x + i, pos.y + j);
						model.redraw |= mv;
						status = Status.IDLE;						
						return true;
					}
				}
		}
		if (status == Status.FLIPPING_HORIZONTAL) {
			if (Math.abs(rotate_angle) > 90.0f)
				stone.mirror_over_y();
			
			rotate_angle = 0.0f;
			if (model.snapAid && model.spiel.is_valid_turn(stone, model.showPlayer, pos.y, pos.x) != Stone.FIELD_ALLOWED)
				for (int i = -1; i <= 1; i++)
					for (int j = -1; j <= 1; j++)
				{
					if (model.spiel.is_valid_turn(stone, model.showPlayer, pos.y + j, pos.x + i) == Stone.FIELD_ALLOWED)
					{
						boolean mv = moveTo(pos.x + i, pos.y + j);
						model.redraw |= mv;
						status = Status.IDLE;						
						return true;
					}
				}
		}
		if (status == Status.FLIPPING_VERTICAL) {
			if (Math.abs(rotate_angle) > 90.0f)
				stone.mirror_over_x();
			
			rotate_angle = 0.0f;
			if (model.snapAid && model.spiel.is_valid_turn(stone, model.showPlayer, pos.y, pos.x) != Stone.FIELD_ALLOWED)
				for (int i = -1; i <= 1; i++)
					for (int j = -1; j <= 1; j++)
				{
					if (model.spiel.is_valid_turn(stone, model.showPlayer, pos.y + j, pos.x + i) == Stone.FIELD_ALLOWED)
					{
						boolean mv = moveTo(pos.x + i, pos.y + j);
						model.redraw |= mv;
						status = Status.IDLE;						
						return true;
					}
				}
		}
		status = Status.IDLE;
		return false;
	}
	
	synchronized public void startDragging(PointF fieldPoint, Stone stone, boolean startedFromBelow) {
		this.stone = stone;
		if (stone == null) {
			status = Status.IDLE;
			return;
		}

		status = Status.DRAGGING;
		this.startedFromBelow = startedFromBelow;
		hasMoved = false;
		stone_rel_x = 0;
		stone_rel_y = 0;
		
		int x = (int)(0.5f + fieldPoint.x + stone_rel_x - stone.get_stone_size() / 2);
		int y = (int)(0.5f + fieldPoint.y + stone_rel_y - stone.get_stone_size() / 2);

		moveTo(x, y);
	}

	@Override
	public boolean execute(float elapsed) {
		// TODO Auto-generated method stub
		return false;
	}	
}
