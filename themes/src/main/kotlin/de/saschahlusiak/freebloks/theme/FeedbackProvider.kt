package de.saschahlusiak.freebloks.theme

enum class FeedbackType {
    StoneBounce,
    UndoStone,
    OutOfMoves,
    StoneHasBeenSet,
    Hint,
    StartDragging,
    Snap,
    GameOver
}

interface FeedbackProvider {
    fun play(type: FeedbackType, volume: Float = 1.0f, speed: Float = 1.0f)

    fun shutdown() {}
}
