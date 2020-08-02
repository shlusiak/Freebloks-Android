package de.saschahlusiak.freebloks.theme

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Vibrator
import android.util.Log
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.theme.FeedbackType.*
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class DefaultSounds(context: Context) : FeedbackProvider {
    override var soundsEnabled = true
    override var vibrationEnabled = true

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    private val scope = CoroutineScope(Dispatchers.Default)

    private val soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 0)

    private var soundClick1 = 0
    private var soundClick2 = 0
    private var soundClick3 = 0
    private var soundHint = 0
    private var soundUndo = 0
    private var soundPlayerOut = 0
    private var soundChat = 0

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
                soundClick1 = load(context, R.raw.click1, 1)
                soundClick2 = load(context, R.raw.click2, 1)
                soundClick3 = load(context, R.raw.click3, 1)
                soundHint = load(context, R.raw.hint, 1)
                soundUndo = load(context, R.raw.drip1, 1)
                soundPlayerOut = load(context, R.raw.playerout, 1)
                soundChat = load(context, R.raw.chat, 1)
            }
        }

        Log.d(tag, "loaded sounds in ${time}ms")
    }

    fun play(id: Int, volume: Float, rate: Float): Boolean {
        if (!soundsEnabled) return false
        if (id == 0) return false

        scope.launch {
            soundPool.play(id, volume * GLOBAL_VOLUME, volume * GLOBAL_VOLUME, 1, 0, rate)
        }

        return true
    }

    override fun play(sound: FeedbackType, volume: Float, speed: Float): Boolean {
        return when(sound) {
            StoneHasBeenSet,
            StoneBounce -> play(soundClick1, volume, speed)
            UndoStone -> play(soundUndo, volume, speed)
            OutOfMoves -> play(soundPlayerOut, volume, speed)
            Hint -> play(soundHint, volume, speed)
            StartDragging -> play(soundClick2, volume, speed)
            Snap -> play(soundClick3, volume, speed)
        }
    }

    override fun vibrate(length: Long) {
        if (vibrationEnabled)
            vibrator?.vibrate(length)
    }


    companion object {
        private val tag = DefaultSounds::class.java.simpleName
        private const val GLOBAL_VOLUME = 0.5f
    }
}