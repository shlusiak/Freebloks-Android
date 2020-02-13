package de.saschahlusiak.freebloks.view.model;

import java.util.ArrayList;

import android.graphics.PointF;
import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.model.Board;
import de.saschahlusiak.freebloks.model.GameMode;
import de.saschahlusiak.freebloks.model.Game;
import de.saschahlusiak.freebloks.game.ActivityInterface;
import de.saschahlusiak.freebloks.view.Freebloks3DView;
import de.saschahlusiak.freebloks.view.effects.Effect;

@SuppressWarnings("serial")
public class ViewModel extends ArrayList<ViewElement> implements ViewElement {
	public final Wheel wheel;
	public final CurrentStone currentStone;
	public Game game;
	public Board board;
	public final BoardObject boardObject;
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

		this.board = new Board(Board.DEFAULT_BOARD_SIZE);
		this.game = new Game(board);

		currentStone = new CurrentStone(this);
		wheel = new Wheel(this);
		boardObject = new BoardObject(this, Board.DEFAULT_BOARD_SIZE);

		effects = new ArrayList<>();

		add(currentStone);
		add(wheel);
		add(boardObject);
	}
	
	public boolean hasAnimations() {
		return showAnimations != ANIMATIONS_OFF;
	}

	public void reset() {
		currentStone.stopDragging();
		boardObject.resetRotation();
	}

	public void setGame(Game game) {
		this.game = game;
		if (game != null)
			this.board = game.getBoard();
		else
			this.board = null;
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
		if (game == null)
			return Global.getPlayerColor(player, GameMode.GAMEMODE_4_COLORS_4_PLAYERS);
		return Global.getPlayerColor(player, game.getGameMode());
	}
}
