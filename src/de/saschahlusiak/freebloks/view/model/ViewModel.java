package de.saschahlusiak.freebloks.view.model;

import java.util.ArrayList;

import android.graphics.PointF;

import de.saschahlusiak.freebloks.controller.Spielleiter;
import de.saschahlusiak.freebloks.game.ActivityInterface;
import de.saschahlusiak.freebloks.view.Freebloks3DView;
import de.saschahlusiak.freebloks.view.effects.AbsEffect;

@SuppressWarnings("serial")
public class ViewModel extends ArrayList<ViewElement> {
	public Wheel wheel;
	public CurrentStone currentStone;
	public Spielleiter spiel;
	public Board board;
	public ActivityInterface activity;
	public Freebloks3DView view;
	public ArrayList<AbsEffect> effects;

	public boolean showSeeds, showOpponents, showAnimations, snapAid;
	public int showPlayer;
	public boolean vertical_layout = true;
	boolean redraw;

	public ViewModel(Freebloks3DView view) {
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

	public void setSpiel(Spielleiter spiel) {
		this.spiel = spiel;
	}

	public boolean handlePointerDown(PointF m) {
		redraw = false;
		for (ViewElement e: this)
			if (e.handlePointerDown(m))
				return redraw;
		return redraw;
	}

	public boolean handlePointerMove(PointF m) {
		redraw = false;
		for (ViewElement e: this)
			if (e.handlePointerMove(m))
				return redraw;
		return redraw;
	}

	public boolean handlePointerUp(PointF m) {
		redraw = false;
		for (ViewElement e: this)
			if (e.handlePointerUp(m))
				return redraw;
		return redraw;
	}
	
	public void addEffect(AbsEffect effect) {
		synchronized (effects) {
			effects.add(effect);
		}
	}
}
