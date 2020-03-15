package de.saschahlusiak.freebloks.view.scene.intro

internal enum class Phase(val duration: Float) {
    Freebloks(9.5f),
    FreebloksWipe(1.7f),
    By(3.0f),
    ByWipe(1.6f),
    Sascha(1.6f),
    Hlusiak(3.5f),
    HlusiakWipe(2.5f);

    fun next(): Phase? = if (ordinal >= values().size - 1) null else values()[ordinal + 1]

    fun enter(intro: Intro) = with(intro.effects) {
        when (this@Phase) {
            Freebloks -> {
                addChar('f', 3, 4, 5)
                addChar('r', 2, 7, 6)
                addChar('e', 1, 10, 5)
                addChar('e', 0, 13, 6)
                addChar('b', 0, 2, 12)
                addChar('l', 1, 5, 11)
                addChar('o', 2, 8, 12)
                addChar('k', 3, 11, 11)
                addChar('s', 2, 14, 13)
            }
            FreebloksWipe -> intro.flipBoard()
            By -> {
                clear()
                addChar('b', -1, 5, 9)
                addChar('y', -1, 9, 9)
            }
            ByWipe -> intro.flipBoard()
            Sascha -> {
                clear()
                addChar('s', 0, 1, 5)
                addChar('a', 2, 4, 5)
                addChar('s', 3, 7, 5)
                addChar('c', 2, 10, 5)
                addChar('h', 1, 13, 5)
                addChar('a', 0, 16, 5)
            }
            Hlusiak -> {
                addChar('h', 3, 0, 11)
                addChar('l', 2, 3, 11)
                addChar('u', 0, 6, 11)
                addChar('s', 1, 9, 11)
                addChar('i', 2, 11, 11)
                addChar('a', 0, 13, 11)
                addChar('k', 3, 16, 11)
            }
            HlusiakWipe -> intro.flipBoard()
        }
    }
}
