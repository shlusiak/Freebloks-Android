package de.saschahlusiak.freebloks.theme

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.VibrationEffect.DEFAULT_AMPLITUDE
import de.saschahlusiak.freebloks.theme.FeedbackType.*

class DefaultSounds(context: Context) : BaseSounds(context) {
    override val soundResources: Collection<Int>
        get() = listOf(
            R.raw.click1,
            R.raw.click2,
            R.raw.click3,
            R.raw.hint,
            R.raw.drip1,
            R.raw.playerout,
            R.raw.chat,
            R.raw.bell
        )

    override fun play(type: FeedbackType, volume: Float, speed: Float) {
        when (type) {
            StoneHasBeenSet -> {
                play(R.raw.click1, volume, speed)

                vibrate(vibrationClick)
            }

            StoneBounce -> play(R.raw.click1, volume, speed)
            UndoStone -> play(R.raw.drip1, volume, 1.0f)
            OutOfMoves -> play(R.raw.playerout, volume, 1.0f)
            Hint -> play(R.raw.hint, volume, 1.0f)
            StartDragging -> play(R.raw.click2, volume, 1.0f)
            GameOver -> play(R.raw.bell, volume, 1.0f)

            Snap -> {
                play(R.raw.click3, volume, 1.0f)

                // only vibrate if sounds are off, otherwise it is too much
                if (!soundsEnabled)
                    vibrate(vibrationTick)
            }
        }
    }

    companion object {
        private val vibrationTick = if (Build.VERSION.SDK_INT >= 29) {
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
        } else {
            VibrationEffect.createOneShot(40L, DEFAULT_AMPLITUDE)
        }

        private val vibrationClick = if (Build.VERSION.SDK_INT >= 29) {
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
        } else {
            VibrationEffect.createOneShot(65L, DEFAULT_AMPLITUDE)
        }
    }
}