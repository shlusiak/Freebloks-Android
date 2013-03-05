package de.saschahlusiak.freebloks.view.model;

import de.saschahlusiak.freebloks.R;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class Sounds extends SoundPool {
	boolean enabled;
	
	public int SOUND_CLICK;

	public Sounds(Context context) {
		super(5, AudioManager.STREAM_MUSIC, 0);
		enabled = true;
		loadSounds(context);
	}
	
	void loadSounds(Context context) {
		SOUND_CLICK = load(context, R.raw.click1, 1);
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void play(int id, float volume, float rate) {
		if (!enabled)
			return;
		play(id, volume, volume, 1, 0, rate);
	}
}
