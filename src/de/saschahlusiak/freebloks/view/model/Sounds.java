package de.saschahlusiak.freebloks.view.model;

import de.saschahlusiak.freebloks.R;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class Sounds extends SoundPool {
	boolean enabled;
	
	public int SOUND_CLICK1;
	public int SOUND_CLICK2;

	public Sounds(Context context) {
		super(5, AudioManager.STREAM_MUSIC, 0);
		enabled = true;
		loadSounds(context);
	}
	
	void loadSounds(Context context) {
		SOUND_CLICK1 = load(context, R.raw.click1, 1);
		SOUND_CLICK2 = load(context, R.raw.click2, 1);
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean play(int id, float volume, float rate) {
		if (!enabled)
			return false;
		play(id, volume, volume, 1, 0, rate);
		return true;
	}
}
