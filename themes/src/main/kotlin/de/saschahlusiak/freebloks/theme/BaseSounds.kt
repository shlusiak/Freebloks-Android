package de.saschahlusiak.freebloks.theme

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RawRes
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

abstract class BaseSounds(context: Context) : FeedbackProvider {
    var soundsEnabled = true
    var vibrationEnabled = true

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    private val scope = CoroutineScope(Dispatchers.Default)

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(15)
        .setAudioAttributes(AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build())
        .build()

    /**
     * List of sound resources to preload and play
     */
    protected abstract val soundResources: Collection<Int>

    /**
     * When loaded, the map of sound resource to id
     */
    private val soundIds = mutableMapOf<Int, Int>()

    init {
        scope.launch {
            loadSounds(context)
        }
    }

    override fun shutdown() {
        scope.cancel()
        soundPool.release()
    }

    private fun loadSounds(context: Context) {
        Log.d(tag, "loading sounds")

        val time = measureTimeMillis {
            with(soundPool) {
                soundResources.forEach {
                    soundIds[it] = load(context, it, 1)
                }
            }
        }

        Log.d(tag, "loaded sounds in ${time}ms")
    }

    protected fun play(@RawRes resId: Int, volume: Float, rate: Float) {
        if (!soundsEnabled) return
        val soundId = soundIds[resId] ?: return
        if (soundId == 0) return

        scope.launch {
            soundPool.play(soundId, volume * GLOBAL_VOLUME, volume * GLOBAL_VOLUME, 1, 0, rate)
        }
    }

    protected fun vibrate(length: Long) {
        if (vibrationEnabled) vibrator?.vibrate(length)
    }

    companion object {
        private val tag = BaseSounds::class.java.simpleName
        private const val GLOBAL_VOLUME = 0.5f
    }
}