package de.saschahlusiak.freebloks.view;

import de.saschahlusiak.freebloks.controller.Spielleiter;
import de.saschahlusiak.freebloks.model.Stone;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.View;

public class SimpleFreebloksView extends View {
	Spielleiter spiel;
	Drawable redStone, blueStone, greenStone, yellowStone, availableField, freeField;
	int tilesize;

	public SimpleFreebloksView(Context context, AttributeSet attrs) {
		super(context, attrs);
		ShapeDrawable d;
		
		redStone = d = new ShapeDrawable(new RectShape());
		d.getPaint().setColor(Color.RED);
		
		yellowStone = d = new ShapeDrawable(new RectShape());
		d.getPaint().setColor(Color.YELLOW);

		greenStone = d = new ShapeDrawable(new RectShape());
		d.getPaint().setColor(Color.GREEN);

		blueStone = d = new ShapeDrawable(new RectShape());
		d.getPaint().setColor(Color.BLUE);
		
		availableField = d = new ShapeDrawable(new RectShape());
		d.getPaint().setColor(Color.argb(128, 0, 192, 0));
		
		freeField = d = new ShapeDrawable(new RectShape());
		d.getPaint().setColor(Color.BLACK);
		
		
		tilesize = 1;
	}
	
	public void setSpiel(Spielleiter spiel) {
		this.spiel = spiel;
		invalidate();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		tilesize = w < h ? w : h;
		tilesize /= spiel.m_field_size_x;
		
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		int i, j, p;
		super.onDraw(canvas);
		
		
		p = spiel.current_player();
		for (i = 0; i < spiel.m_field_size_x; i++)
			for (j = 0; j < spiel.m_field_size_y; j++)
			{
				int v = spiel.get_game_field(j, i);
				Drawable d;
				switch (v) {
				case 0:
					d = blueStone; break;
				case 1:
					d = yellowStone; break;
				case 2:
					d = redStone; break;
				case 3:
					d = greenStone; break;
				
				case Stone.FIELD_FREE:
					if (p >= 0 && spiel.get_game_field(p, j, i) == Stone.FIELD_ALLOWED)
						d = availableField;
					else
						d = freeField;
					break;
				default:
					d = freeField;
					break;
				}				
				
				if (d != null) {
					d.setBounds(i * tilesize, j * tilesize, i * tilesize + tilesize, j * tilesize + tilesize);
					d.draw(canvas);
				}
				
				
			}
				
			
	}

}
