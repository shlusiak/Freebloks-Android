package de.saschahlusiak.freebloks.view.effects;

import de.saschahlusiak.freebloks.model.Stone;

public abstract class AbsStoneEffect extends AbsEffect {
	Stone stone;
	int player, x, y;
	AbsStoneEffect(Stone stone, int player, int x, int y) {
		this.stone = stone;
		this.player = player;
		/* x = 19 - y;
		 * y = x;
		 */
		this.y = 19 - y;
		this.x = x;
	}
	
	@Override
	public boolean isEffected(int x, int y) {
		/* TODO: fix this nonesense once and for all! */
		y = 19 - y;
		x = x - this.x;
		y = this.y - y;

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