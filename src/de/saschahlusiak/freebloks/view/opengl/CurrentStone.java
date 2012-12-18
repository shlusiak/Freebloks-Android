package de.saschahlusiak.freebloks.view.opengl;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Paint.Style;
import android.opengl.GLUtils;
import android.util.Log;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.view.opengl.AbsEffect.FadeEffect;
import de.saschahlusiak.freebloks.view.opengl.Freebloks3DView.MyRenderer;

public class CurrentStone extends ViewElement {
		Stone stone;
		Point pos = new Point();
		boolean dragging, hasMoved;
		float stone_rel_x, stone_rel_y;
		int texture[];
		SimpleModel overlay;
		
		
		CurrentStone(ViewModel model) {
			super(model);
			
			pos.x = -50;
			pos.y = -50;
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
		
		void updateTexture(GL10 gl) {
			if (texture == null)
				texture = new int[1];

			gl.glGenTextures(1, texture, 0);

			Bitmap bmp = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bmp);

			canvas.drawColor(Color.TRANSPARENT);
			
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setStyle(Style.FILL_AND_STROKE);
			paint.setColor(Color.argb(240, 255, 255, 255));
			canvas.drawCircle(64, 64, 64, paint);
			
			gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);		
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR); 
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0);

			bmp.recycle();
		}
		
		final float diffuse_red[] = { 1.0f, 0.5f, 0.5f, 0.50f };
		final float diffuse_green[] = { 0.5f, 1.0f, 0.5f, 0.50f };
		
		synchronized void render(MyRenderer renderer, GL10 gl) {
			if (stone == null)
				return;
			
			if (pos.x > -50 && pos.y > -50) {
				gl.glPushMatrix();
				gl.glTranslatef(0, 0.3f, 0.0f);
				
				gl.glPushMatrix();
			    gl.glTranslatef(
			    		-BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) + BoardRenderer.stone_size * 2.0f * (float)(pos.x + stone.get_stone_size() / 2),
			    		0,
			    		+BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) - BoardRenderer.stone_size * 2.0f * (float)(pos.y - stone.get_stone_size() / 2));

			    gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);
				gl.glEnable(GL10.GL_TEXTURE_2D);
				gl.glEnable(GL10.GL_BLEND);
				gl.glDisable(GL10.GL_DEPTH_TEST);
				
				/* TODO: cache result of is_valid_turn when moved */
				boolean isvalid = model.spiel.is_valid_turn(stone, model.showPlayer, 19 - pos.y, pos.x) == Stone.FIELD_ALLOWED;
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, isvalid ? diffuse_green : diffuse_red, 0);

				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

			    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, overlay.getVertexBuffer());
			    gl.glNormalPointer(GL10.GL_FLOAT, 0, overlay.getNormalBuffer());
			    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, overlay.getTextureBuffer());
			    
			    overlay.drawElements(gl);

				gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			    
			    
			    gl.glPopMatrix();
			    
				gl.glDisable(GL10.GL_BLEND);
				gl.glDisable(GL10.GL_TEXTURE_2D);
				
				renderer.board.renderPlayerStone(gl, model.spiel.current_player(), stone, 0.65f, pos.x, pos.y);
				
				gl.glEnable(GL10.GL_DEPTH_TEST);
				
				gl.glPopMatrix();
			}
		}

		boolean moveTo(int x, int y) {
			if (stone == null)
				return false;
			
			for (int i = 0; i < stone.get_stone_size(); i++)
				for (int j = 0; j < stone.get_stone_size(); j++) {
					if (stone.get_stone_field(j, i) == Stone.STONE_FIELD_ALLOWED) {
						if (i + x < 0)
							x = -i;
						if (y - j < 0)
							y = j;
						
						if (i + x >= model.spiel.m_field_size_x)
							x = model.spiel.m_field_size_x - i - 1;
						if (y - j >= model.spiel.m_field_size_y)
							y = model.spiel.m_field_size_y + j - 1;
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
		
		@Override
		synchronized public boolean handlePointerDown(PointF m) {
			dragging = false;
			hasMoved = false;
			if (stone != null) {
				fieldPoint.x = m.x;
				fieldPoint.y = m.y;
				model.board.modelToBoard(fieldPoint);
				
				stone_rel_x = (pos.x - fieldPoint.x);
				stone_rel_y = (pos.y - fieldPoint.y);
				
//				Log.d(tag, "rel = (" + stone_rel_x + " / " + stone_rel_y+ ")");
				if ((Math.abs(stone_rel_x) < 9) &&
						(Math.abs(stone_rel_y) < 9)) {
					dragging = true;
					return true;
				}
			}
			return false;
		}
		
		@Override
		synchronized public boolean handlePointerMove(PointF m) {
			if (!dragging)
				return false;
			
			fieldPoint.x = m.x;
			fieldPoint.y = m.y;
			model.board.modelToBoard(fieldPoint);
			
			int x = (int)(0.5f + fieldPoint.x + stone_rel_x);
			int y = (int)(0.5f + fieldPoint.y + stone_rel_y);
			hasMoved |= moveTo(x, y);
			return true;
		}
		
		@Override
		synchronized public boolean handlePointerUp(PointF m) {
			if (dragging) {
				if (!hasMoved) {
					int player = model.spiel.current_player();
					if (model.activity.commitCurrentStone(model.spiel, stone, pos.x, pos.y)) {
						Stone st = new Stone();
						st.copyFrom(stone);
						FadeEffect e = new FadeEffect(st, player, pos.x, 19 - pos.y);
					
						model.addEffect(e);
						
						stone = null;
						model.wheel.highlightStone = -1;
					}
				} else {
					fieldPoint.x = m.x;
					fieldPoint.y = m.y;
					model.board.modelToBoard(fieldPoint);

					int x = (int)(0.5f + fieldPoint.x + stone_rel_x);
					int y = (int)(0.5f + fieldPoint.y + stone_rel_y);
					fieldPoint.x = x;
					fieldPoint.y = y;
					model.board.boardToUnified(fieldPoint);
					if (fieldPoint.y < -2.0f) {
						model.wheel.highlightStone = stone.get_number();
						dragging = false;
						stone = null;
					}

				}
			}
			dragging = false;
			return false;
		}
		
		void startDragging(Stone stone) {
			this.stone = stone;
			if (stone == null)
				return;
			
			dragging = true;
			hasMoved = false;
			stone_rel_x = -(float)stone.get_stone_size() / 2.0f;
			stone_rel_y =  (float)stone.get_stone_size() / 2.0f;
		}
	
}
