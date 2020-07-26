package de.saschahlusiak.freebloks.view.scene.intro

import androidx.annotation.UiThread

interface IntroDelegate {
    /**
     * The Intro has finished playing
     */
    @UiThread
    fun onIntroCompleted()
}