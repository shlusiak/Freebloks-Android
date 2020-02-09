package de.saschahlusiak.freebloks.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

import de.saschahlusiak.freebloks.controller.GameStateException;

public class Stone implements Serializable, Cloneable {
	private static final long serialVersionUID = -4949247356899826370L;

	public static final int FIELD_FREE    =  240;
	public static final int FIELD_ALLOWED =  241;
	public static final int FIELD_DENIED  =  255;

	public static final int STONE_FIELD_FREE = 0;
	public static final int STONE_FIELD_ALLOWED = 1;

	private int m_available;
	private int m_shape;

	private StoneType type;

	public Stone() {
		m_available = m_shape = 0;
		init(0);
	}
	
	public Stone(int shape) {
		init(shape);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public final void copyFrom(@NonNull Stone from) {
		this.m_available = from.m_available;
		this.m_shape = from.m_shape;
		type = from.type;
	}

	public final void setAvailable(int value){
		this.m_available = value;
	}

	public final void availableIncrement(){
		m_available++;
	}

	public final void availableDecrement() throws GameStateException {
		if (m_available <= 0)
			throw new GameStateException("stone " + m_shape + " not available");
		m_available--;
	}

	public final int getAvailableCount() {
		return m_available;
	}

	public final Mirrorable getMirrorable() {
		return type.getMirrorable();
	}

	public final Rotateable getRotateable() {
		return type.getRotateable();
	}

	public final int getPositionPoints() {
		return type.getPositionPoints();
	}

	public final int getShape() {
		return m_shape;
	}

	public final int getSize() {
		return type.getSize();
	}

	public final int getPoints() {
		return type.getPoints();
	}

	public final void init(int shape) {
		m_shape = shape;
		type = StoneType.get(m_shape);
	}

	public final int getStoneField(int y, int x, int mirror, int rotate) {
		return type.getStoneField(x, y, mirror == 1, Rotation.values()[rotate]);
	}
}
