package de.saschahlusiak.freebloks.view.effects;

import de.saschahlusiak.freebloks.model.Stone;

public abstract class AbsStoneEffect extends AbsEffect {
	Stone stone;
	int player, x, y;
	AbsStoneEffect(Stone stone, int player, int x, int y) {
		this.stone = stone;
		this.player = player;
		this.x = x;
		this.y = y;
	}
	
	@Override
	public boolean isEffected(int x, int y) {
		x = x - this.x;
		y = y - this.y;

		if (x < 0 || y < 0)
			return false;
		if (x >= stone.get_stone_size())
			return false;
		if (y >= stone.get_stone_size())
			return false;
		
		if (stone.get_stone_field(y, x) == Stone.STONE_FIELD_FREE)
			return false;
		
		return true;			
	}
}