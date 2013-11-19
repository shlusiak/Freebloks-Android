package de.saschahlusiak.freebloks.view.model;

import java.util.ArrayList;

import android.graphics.PointF;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.controller.Spielleiter;
import de.saschahlusiak.freebloks.game.ActivityInterface;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.view.Freebloks3DView;
import de.saschahlusiak.freebloks.view.effects.Effect;

@SuppressWarnings("serial")
public class ViewModel extends ArrayList<ViewElement> implements ViewElement {
	public Wheel wheel;
	public CurrentStone currentStone;
	public Spielleiter spiel;
	public Board board;
	public ActivityInterface activity;
	public Freebloks3DView view;
	public ArrayList<Effect> effects;
	public Intro intro;
	public Sounds soundPool;

	public boolean showSeeds, showOpponents, showAnimations, snapAid;
	public boolean vertical_layout = true;
	boolean redraw;

	public ViewModel(Freebloks3DView view) {
		this.view = view;
		
		this.spiel = new Spielleiter(Spiel.DEFAULT_FIELD_SIZE_Y, Spiel.DEFAULT_FIELD_SIZE_X);
		
		currentStone = new CurrentStone(this);
		wheel = new Wheel(this);
		board = new Board(this, Spiel.DEFAULT_FIELD_SIZE_X);
		
		effects = new ArrayList<Effect>();
		
		add(currentStone);
		add(wheel);
		add(board);
	}
	
	public void reset() {
		currentStone.startDragging(null, null, 0);
		board.resetRotation();
	}

	public void setSpiel(Spielleiter spiel) {
		this.spiel = spiel;
	}

	public boolean handlePointerDown(PointF m) {
		redraw = false;
		if (intro != null) {
			redraw = intro.handlePointerDown(m);
			return redraw;
		}
		for (int i = 0; i < size(); i++)
			if (get(i).handlePointerDown(m))
				return redraw;
		return redraw;
	}

	public boolean handlePointerMove(PointF m) {
		redraw = false;
		for (int i = 0; i < size(); i++)
			if (get(i).handlePointerMove(m))
				return redraw;
		return redraw;
	}

	public boolean handlePointerUp(PointF m) {
		redraw = false;
		for (int i = 0; i < size(); i++)
			if (get(i).handlePointerUp(m))
				return redraw;
		return redraw;
	}
	
	@Override
	public boolean execute(float elapsed) {
		boolean redraw = false;
		if (intro != null) {
			redraw |= intro.execute(elapsed);
			return redraw;
		}
		
		synchronized (effects) {
			int i = 0;
			while (i < effects.size()) {
				redraw |= effects.get(i).execute(elapsed);
				if (effects.get(i).isDone()) {
					effects.remove(i);
					redraw = true;
				} else
					i++;
			}
		}
		
		for (int i = 0; i < size(); i++)
			redraw |= get(i).execute(elapsed);
		
		return redraw;
	}
	
	public void addEffect(Effect effect) {
		synchronized (effects) {
			effects.add(effect);
		}
	}
	
	public void clearEffects() {
		synchronized (effects) {
			effects.clear();
		}
	}

	public int getPlayerColor(int player) {
		if (spiel == null)
			return Global.getPlayerColor(player, Spielleiter.GAMEMODE_4_COLORS_4_PLAYERS);
		return Global.getPlayerColor(player, spiel.m_gamemode);
	}
}
