package de.saschahlusiak.freebloks.theme

import android.content.Context
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.theme.FeedbackType.*

class DefaultSounds(context: Context): BaseSounds(context) {
    override val soundResources: Collection<Int>
        get() = listOf(
            R.raw.click1,
            R.raw.click2,
            R.raw.click3,
            R.raw.hint,
            R.raw.drip1,
            R.raw.playerout,
            R.raw.chat
        )

    override fun play(type: FeedbackType, volume: Float, speed: Float) {
        when (type) {
            StoneHasBeenSet -> {
                play(R.raw.click1, volume, speed)

                vibrate(VIBRATE_SET_STONE)
            }

            StoneBounce -> play(R.raw.click1, volume, speed)
            UndoStone -> play(R.raw.drip1, volume, 1.0f)
            OutOfMoves -> play(R.raw.playerout, volume, 1.0f)
            Hint -> play(R.raw.hint, volume, 1.0f)
            StartDragging -> play(R.raw.click2, volume, 1.0f)

            Snap -> {
                play(R.raw.click3, volume, 1.0f)

                // only vibrate if sounds are off, otherwise it is too much
                if (!soundsEnabled)
                    vibrate(VIBRATE_STONE_SNAP)
            }
        }
    }

    companion object {
        private const val VIBRATE_SET_STONE = 65L
        private const val VIBRATE_STONE_SNAP = 40L
    }
}