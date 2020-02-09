package de.saschahlusiak.freebloks.view.effects;

import javax.microedition.khronos.opengles.GL11;

import de.saschahlusiak.freebloks.model.Rotation;
import de.saschahlusiak.freebloks.model.Shape;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.view.BoardRenderer;
import de.saschahlusiak.freebloks.view.model.ViewModel;

public abstract class AbsStoneEffect extends AbsEffect implements Effect {
	Shape stone;
	int color, x, y, mirror, rotate;
	ViewModel model;

	AbsStoneEffect(ViewModel model, Shape shape, int color, int x, int y, int mirror, int rotate) {
		this.model = model;
		this.stone = shape;
		this.color = color;
		this.x = x;
		this.y = y;
		this.mirror = mirror;
		this.rotate = rotate;
	}
	
	AbsStoneEffect(ViewModel model, Turn turn) {
		this(model, Shape.get(turn.m_stone_number), model.getPlayerColor(turn.m_playernumber), turn.m_x, turn.m_y, turn.m_mirror_count, turn.m_rotate_count);
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

		return stone.isStone(x, y, mirror == 1, Rotation.from(rotate));
	}

	@Override
	public void renderShadow(GL11 gl, BoardRenderer renderer) {

	}

}