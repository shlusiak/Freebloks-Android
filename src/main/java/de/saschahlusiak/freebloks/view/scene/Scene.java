package de.saschahlusiak.freebloks.view.scene;

import java.util.ArrayList;

import android.graphics.PointF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.client.GameClient;
import de.saschahlusiak.freebloks.game.FreebloksActivityViewModel;
import de.saschahlusiak.freebloks.model.Board;
import de.saschahlusiak.freebloks.model.GameMode;
import de.saschahlusiak.freebloks.model.Game;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.theme.Sounds;
import de.saschahlusiak.freebloks.view.Freebloks3DView;
import de.saschahlusiak.freebloks.view.effects.Effect;
import de.saschahlusiak.freebloks.view.effects.EffectSet;
import de.saschahlusiak.freebloks.view.effects.ShapeFadeEffect;
import de.saschahlusiak.freebloks.view.effects.ShapeRollEffect;

/**
 * This scene model is owned by the {@link Freebloks3DView} and
 * encapsulates 3D objects and renderable effects and sounds.
 *
 * This is a View class and is allowed to have references to the current View and Activity.
 */
public class Scene extends ArrayList<ViewElement> implements ViewElement {
	private final FreebloksActivityViewModel viewModel;
	public final Freebloks3DView view;

	public final Wheel wheel;
	public final CurrentStone currentStone;
	public GameClient client;
	public Game game;
	public Board board;
	public final BoardObject boardObject;
	public final ArrayList<Effect> effects;
	@Deprecated // delegate to viewModel
	public Sounds soundPool;

	public boolean showSeeds, showOpponents, snapAid;
	public int showAnimations;
	public boolean verticalLayout = true;
	boolean redraw;

	public final static int ANIMATIONS_FULL = 0;
	public final static int ANIMATIONS_HALF = 1;
	public final static int ANIMATIONS_OFF = 2;

	public Scene(Freebloks3DView view, FreebloksActivityViewModel viewModel) {
		this.view = view;
		this.viewModel = viewModel;

		this.board = new Board();
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

	/**
	 * The intro is part of the  scene but owned by the viewModel
	 *
	 * @return current intro
	 */
	public @Nullable Intro getIntro() {
		return viewModel.getIntro();
	}

	public void setGameClient(@Nullable GameClient client) {
		this.client = client;
		if (client != null) {
			this.game = client.game;
			this.board = game.getBoard();

			boardObject.resetRotation();
			wheel.update(boardObject.getShowWheelPlayer());
			boardObject.updateDetailsPlayer();
		} else {
			this.board = null;
			this.game = null;
		}

	}

	@Override
	public boolean handlePointerDown(@NonNull PointF m) {
		redraw = false;
		final Intro intro = getIntro();
		if (intro != null) {
			redraw = intro.handlePointerDown(m);
			return redraw;
		}
		for (int i = 0; i < size(); i++)
			if (get(i).handlePointerDown(m))
				return redraw;
		return redraw;
	}

	@Override
	public boolean handlePointerMove(@NonNull PointF m) {
		redraw = false;
		for (int i = 0; i < size(); i++)
			if (get(i).handlePointerMove(m))
				return redraw;
		return redraw;
	}

	@Override
	public boolean handlePointerUp(@NonNull PointF m) {
		redraw = false;
		for (int i = 0; i < size(); i++)
			if (get(i).handlePointerUp(m))
				return redraw;
		return redraw;
	}

	@Override
	public boolean execute(float elapsed) {
		boolean redraw = false;
		if (getIntro() != null) {
			redraw = getIntro().execute(elapsed);
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

	public boolean commitCurrentStone(@NonNull final Turn turn) {
		if (client == null)
			return false;

		if (!client.game.isLocalPlayer())
			return false;
		if (!client.game.getBoard().isValidTurn(turn))
			return false;

		if (hasAnimations()) {
			EffectSet set = new EffectSet();
			set.add(new ShapeRollEffect(this, turn, currentStone.hover_height_high, -15.0f));
			set.add(new ShapeFadeEffect(this, turn, 1.0f));
			addEffect(set);
		}

		soundPool.play(soundPool.SOUND_CLICK1, 1.0f, 0.9f + (float) Math.random() * 0.2f);
		viewModel.vibrate(Global.VIBRATE_SET_STONE);

		client.setStone(turn);
		return true;
	}

	// use Global.getPlayerColor instead
	@Deprecated
	public int getPlayerColor(int player) {
		if (game == null)
			return Global.getPlayerColor(player, GameMode.GAMEMODE_4_COLORS_4_PLAYERS);
		return Global.getPlayerColor(player, game.getGameMode());
	}

	public void vibrate(long milliseconds) {
		viewModel.vibrate(milliseconds);
	}

	public void setShowPlayerOverride(int player, boolean isRotated) {
		viewModel.setSheetPlayer(player, isRotated);
	}
}
