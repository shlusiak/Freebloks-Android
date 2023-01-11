package de.saschahlusiak.freebloks.view.scene.intro

import de.saschahlusiak.freebloks.model.StoneColor.*

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
                addChar('f', Green, 4, 5)
                addChar('r', Red, 7, 6)
                addChar('e', Yellow, 10, 5)
                addChar('e', Blue, 13, 6)
                addChar('b', Blue, 2, 12)
                addChar('l', Yellow, 5, 11)
                addChar('o', Red, 8, 12)
                addChar('k', Green, 11, 11)
                addChar('s', Red, 14, 13)
            }
            FreebloksWipe -> intro.flipBoard()
            By -> {
                clear()
                addChar('b', White, 5, 9)
                addChar('y', White, 9, 9)
            }
            ByWipe -> intro.flipBoard()
            Sascha -> {
                clear()
                addChar('s', Blue, 1, 5)
                addChar('a', Red, 4, 5)
                addChar('s', Green, 7, 5)
                addChar('c', Red, 10, 5)
                addChar('h', Yellow, 13, 5)
                addChar('a', Blue, 16, 5)
            }
            Hlusiak -> {
                addChar('h', Green, 0, 11)
                addChar('l', Red, 3, 11)
                addChar('u', Blue, 6, 11)
                addChar('s', Yellow, 9, 11)
                addChar('i', Red, 11, 11)
                addChar('a', Blue, 13, 11)
                addChar('k', Green, 16, 11)
            }
            HlusiakWipe -> intro.flipBoard()
        }
    }
}
