package de.saschahlusiak.freebloks.view.model;

import de.saschahlusiak.freebloks.view.BoardRenderer;
import android.graphics.PointF;

public class Board extends ViewElement {
	public Board(ViewModel model) {
		super(model);
	}
	
	
	/**
	 * Converts a point from model coordinates to (non-uniformed) board coordinates
	 * @param point
	 * @return point
	 */
	public PointF modelToBoard(PointF point) {
		point.x = point.x + BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1);
		point.y = BoardRenderer.stone_size * (float)(model.spiel.m_field_size_x - 1) - point.y;
		
		point.x = point.x / (BoardRenderer.stone_size * 2.0f);
		point.y = point.y / (BoardRenderer.stone_size * 2.0f);
		
		return point;
	}
	
	/**
	 * converts p from relative board coordinates, to rotated board coordinates
	 * @param p
	 */
	void boardToUnified(PointF p) {
		float tmp;
		
		switch (model.showPlayer) {
		default:
		case 0: /* nothing */
			break;
		case 1:
			tmp = p.x;
			p.x = model.spiel.m_field_size_x - p.y;
			p.y = tmp;
			break;
		case 2: /* 180 degree */
			p.x = model.spiel.m_field_size_x - p.x;
			p.y = model.spiel.m_field_size_y - p.y;
			break;
		case 3:
			tmp = p.y;
			p.y = model.spiel.m_field_size_y - p.x;
			p.x = tmp;
			break;
		}
	}
}
