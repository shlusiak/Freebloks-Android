package de.saschahlusiak.freebloks.view.model

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.util.Log
import de.saschahlusiak.freebloks.R
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

class Sounds(context: Context) : SoundPool(10, AudioManager.STREAM_MUSIC, 0) {
    var isEnabled = true

    @JvmField var SOUND_CLICK1 = 0
    @JvmField var SOUND_CLICK2 = 0
    @JvmField var SOUND_CLICK3 = 0
    @JvmField var SOUND_HINT = 0
    @JvmField var SOUND_UNDO = 0
    @JvmField var SOUND_PLAYER_OUT = 0
    @JvmField var SOUND_CHAT = 0

    init {
        thread(name = "SoundLoadThread") {
            loadSounds(context)
        }
    }

    private fun loadSounds(context: Context) {
        Log.d(tag, "loading sounds")

        val time = measureTimeMillis {
            SOUND_CLICK1 = load(context, R.raw.click1, 1)
            SOUND_CLICK2 = load(context, R.raw.click2, 1)
            SOUND_CLICK3 = load(context, R.raw.click3, 1)
            SOUND_HINT = load(context, R.raw.hint, 1)
            SOUND_UNDO = load(context, R.raw.drip1, 1)
            SOUND_PLAYER_OUT = load(context, R.raw.playerout, 1)
            SOUND_CHAT = load(context, R.raw.chat, 1)
        }

        Log.d(tag, "loaded sounds in ${time}ms")
    }

    fun play(id: Int, volume: Float, rate: Float): Boolean {
        if (!isEnabled) return false
        if (id == 0) return false

        play(id, volume * GLOBAL_VOLUME, volume * GLOBAL_VOLUME, 1, 0, rate)
        return true
    }

    companion object {
        private val tag = Sounds::class.java.simpleName
        private const val GLOBAL_VOLUME = 0.5f
    }
}