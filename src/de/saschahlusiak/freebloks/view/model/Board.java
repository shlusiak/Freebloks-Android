package de.saschahlusiak.freebloks.view.model;

import de.saschahlusiak.freebloks.view.BoardRenderer;
import android.graphics.PointF;

public class Board {
	ViewModel model;
	
	public Board(ViewModel model) {
		this.model = model;
	}	
	
	/**
	 * Converts a point from model coordinates to (non-uniformed) board coordinates.
	 * The top-left corner is 0/0, the blue starting point is 0/19
	 * @param point
	 * @return point
	 */
	public PointF modelToBoard(PointF point) {
		point.x = point.x / (BoardRenderer.stone_size * 2.0f);
		point.y = point.y / (BoardRenderer.stone_size * 2.0f);
		
		point.x = point.x + 0.5f * (float)(model.spiel.m_field_size_x - 1);
		point.y = point.y + 0.5f * (float)(model.spiel.m_field_size_y - 1);

		return point;
	}
	
	/**
	 * converts p from relative board coordinates, to rotated board coordinates
	 * relative board coordinates: yellow starting point is 0/0, blue starting point is 0/19
	 * unified coordinates: bottom left corner is always 0/0
	 * @param p
	 */
	void boardToUnified(PointF p) {
		float tmp;
		
		switch (model.showPlayer) {
		default:
		case 0: /* nothing */
			p.y = model.spiel.m_field_size_y - p.y - 1;
			break;
		case 1:
			tmp = p.x;
			p.x = p.y;
			p.y = tmp;
			break;
		case 2: /* 180 degree */
			p.x = model.spiel.m_field_size_x - p.x - 1;
			break;
		case 3:
			tmp = p.y;
			p.y = model.spiel.m_field_size_x - p.x - 1;
			p.x = model.spiel.m_field_size_y - tmp - 1;
			break;
		}
	}
}
