package de.saschahlusiak.freebloks.view.effects;

import javax.microedition.khronos.opengles.GL11;

import de.saschahlusiak.freebloks.model.Orientation;
import de.saschahlusiak.freebloks.model.Shape;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.view.BoardRenderer;
import de.saschahlusiak.freebloks.view.model.ViewModel;

public abstract class AbsStoneEffect extends AbsEffect implements Effect {
	Shape stone;
	int color, x, y;
	ViewModel model;
	final Orientation orientation;

	AbsStoneEffect(ViewModel model, Shape shape, int color, int x, int y, Orientation orientation) {
		this.model = model;
		this.stone = shape;
		this.color = color;
		this.x = x;
		this.y = y;
		this.orientation = orientation;
	}
	
	AbsStoneEffect(ViewModel model, Turn turn) {
		this(model, turn.getShape(), model.getPlayerColor(turn.getPlayer()), turn.getX(), turn.getY(), turn.getOrientation());
	}

	@Override
	public boolean isEffected(int x, int y) {
		x = x - this.x;
		y = y - this.y;

		if (x < 0 || y < 0)
			return false;
		if (x >= stone.getSize())
			return false;
		if (y >= stone.getSize())
			return false;

		return stone.isStone(x, y, orientation);
	}

	@Override
	public void renderShadow(GL11 gl, BoardRenderer renderer) {

	}
}