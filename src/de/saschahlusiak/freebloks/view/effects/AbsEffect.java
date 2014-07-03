package de.saschahlusiak.freebloks.view.effects;


public abstract class AbsEffect implements Effect {
	AbsEffect() {
		time = 0.0f;
	}

	float time;

	public boolean execute(float elapsed) {
		time += elapsed;
		return true;
	}

	public boolean isEffected(int x, int y) {
		return false;
	}
}
