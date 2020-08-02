package de.saschahlusiak.freebloks.theme

enum class FeedbackType {
    StoneBounce,
    UndoStone,
    OutOfMoves,
    StoneHasBeenSet,
    Hint,
    StartDragging,
    Snap
}

interface FeedbackProvider {
    var soundsEnabled: Boolean
    var vibrationEnabled: Boolean

    fun vibrate(length: Long)
    fun play(sound: FeedbackType, volume: Float = 1.0f, speed: Float = 1.0f): Boolean

    fun playOrVibrate(sound: FeedbackType, vibration: Long, volume: Float = 1.0f, speed: Float = 1.0f) {
        if (!play(sound, volume, speed))
            vibrate(vibration)
    }

    fun shutdown() {}
}
