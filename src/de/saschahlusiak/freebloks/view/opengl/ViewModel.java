package de.saschahlusiak.freebloks.view.opengl;

import java.util.ArrayList;

import android.graphics.PointF;

import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.game.ActivityInterface;
import de.saschahlusiak.freebloks.view.ViewInterface;

@SuppressWarnings("serial")
public class ViewModel extends ArrayList<ViewElement> {
	Wheel wheel;
	CurrentStone currentStone;
	SpielClient spiel;
	Board board;
	ActivityInterface activity;
	ViewInterface view;
	ArrayList<AbsEffect> effects;

	public boolean showSeeds, showOpponents, showAnimations;
	int showPlayer;

	public ViewModel(ViewInterface view) {
		this.view = view;
		
		currentStone = new CurrentStone(this);
		wheel = new Wheel(this);
		board = new Board(this);
		showPlayer = -1;
		
		effects = new ArrayList<AbsEffect>();
		
		add(currentStone);
		add(wheel);
		add(board);
	}

	public void setSpiel(SpielClient spiel) {
		this.spiel = spiel;
	}

	public boolean handlePointerDown(PointF m) {
		for (ViewElement e: this)
			if (e.handlePointerDown(m))
				return true;
		return false;
	}

	public boolean handlePointerMove(PointF m) {
		for (ViewElement e: this)
			if (e.handlePointerMove(m))
				return true;
		return false;
	}

	public boolean handlePointerUp(PointF m) {
		for (ViewElement e: this)
			if (e.handlePointerUp(m))
				return true;
		return false;
	}
	
	public void addEffect(AbsEffect effect) {
		synchronized (effects) {
			effects.add(effect);
		}
	}
}
