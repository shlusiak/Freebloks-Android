package de.saschahlusiak.freebloks.view.model;

import java.util.ArrayList;

import android.graphics.PointF;
import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.model.GameMode;
import de.saschahlusiak.freebloks.model.Spielleiter;
import de.saschahlusiak.freebloks.game.ActivityInterface;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.view.Freebloks3DView;
import de.saschahlusiak.freebloks.view.effects.Effect;

@SuppressWarnings("serial")
public class ViewModel extends ArrayList<ViewElement> implements ViewElement {
	public final Wheel wheel;
	public final CurrentStone currentStone;
	public Spielleiter spiel;
	public final Board board;
	public ActivityInterface activity;
	public final Freebloks3DView view;
	public final ArrayList<Effect> effects;
	public Intro intro;
	public Sounds soundPool;

	public boolean showSeeds, showOpponents, snapAid;
	public int showAnimations;
	public boolean immersiveMode = true;
	public boolean vertical_layout = true;
	boolean redraw;
	
	public final static int ANIMATIONS_FULL = 0;
	public final static int ANIMATIONS_HALF = 1;
	public final static int ANIMATIONS_OFF = 2;

	public ViewModel(Freebloks3DView view) {
		this.view = view;

		this.spiel = new Spielleiter(Spiel.DEFAULT_BOARD_SIZE);

		currentStone = new CurrentStone(this);
		wheel = new Wheel(this);
		board = new Board(this, Spiel.DEFAULT_BOARD_SIZE);

		effects = new ArrayList<>();

		add(currentStone);
		add(wheel);
		add(board);
	}
	
	public boolean hasAnimations() {
		return showAnimations != ANIMATIONS_OFF;
	}

	public void reset() {
		currentStone.stopDragging();
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
			redraw = intro.execute(elapsed);
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
			return Global.getPlayerColor(player, GameMode.GAMEMODE_4_COLORS_4_PLAYERS);
		return Global.getPlayerColor(player, spiel.getGameMode());
	}
}
