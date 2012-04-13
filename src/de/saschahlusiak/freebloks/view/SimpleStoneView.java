package de.saschahlusiak.freebloks.view;

import de.saschahlusiak.freebloks.model.Stone;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class SimpleStoneView extends View {
	Stone stone;
	int player;
	Paint blockPaint;
	
	static final int block_size = 20;
	
	public SimpleStoneView(Context context, int player, Stone stone) {
		super(context);
		this.stone = stone;
		this.player = player;
		
		int alpha;
		if (stone.get_available() <= 0)
			alpha = 30;
		else
			alpha = 200;
		blockPaint = new Paint();
		switch (player) {
		case 0:	blockPaint.setColor(Color.argb(alpha, 0, 30, 190));		break;
		case 1:	blockPaint.setColor(Color.argb(alpha, 190, 190, 0));	break;
		case 2:	blockPaint.setColor(Color.argb(alpha, 190, 0, 0));		break;
		case 3:	blockPaint.setColor(Color.argb(alpha, 0, 190, 0));		break;
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(block_size * 5, block_size * 5);
		// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//canvas.drawColor(Color.RED);
		for (int y = 0; y < stone.get_stone_size(); y++)
			for (int x = 0; x < stone.get_stone_size(); x++) {
				if (stone.get_stone_field(y, x) != Stone.STONE_FIELD_FREE) {
					canvas.drawRect(block_size * x, block_size * y, (block_size) * (x + 1) - 1, (block_size) * (y + 1) - 1, blockPaint);
				}
			}
		
		super.onDraw(canvas);
	}
}
