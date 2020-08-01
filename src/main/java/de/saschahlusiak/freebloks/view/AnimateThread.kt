package de.saschahlusiak.freebloks.view

import android.util.Log
import de.saschahlusiak.freebloks.view.scene.AnimationType
import de.saschahlusiak.freebloks.view.scene.Scene

class AnimateThread(private val scene: Scene, private val execute: (Float, Float) -> Boolean) : Thread("AnimateThread") {
    private val tag = AnimateThread::class.java.simpleName

    @Volatile var goDown = false

    override fun run() {
        Log.d(tag, "AnimateThread starting up")

        try {
            sleep(100)
        } catch (e1: InterruptedException) {
            e1.printStackTrace()
            return
        }
        var delay: Long
        var time: Long
        var lastExecTime: Long
        // how long ago did we last render the scene?
        var lastRendered = 1.0f
        time = System.currentTimeMillis()
        lastExecTime = 0

        while (!goDown) {
            delay = when (scene.showAnimations) {
                AnimationType.Full -> if (lastRendered < 0.2f) 1000 / 60 else 1000 / 15
                AnimationType.Half -> if (lastRendered < 0.2f) 1000 / 30 else 1000 / 15
                AnimationType.Off -> if (scene.intro != null) 1000 / 30 else 1000 / 3
                else -> if (scene.intro != null) 1000 / 30 else 1000 / 3
            }.toLong()

            try {
                if (delay - lastExecTime > 0) sleep(delay - lastExecTime)
            } catch (e: InterruptedException) {
                break
            }

            val tmp = System.currentTimeMillis()
            val elapsed = (tmp - time + 1).toFloat() / 1000.0f
            lastExecTime = tmp
            lastRendered += elapsed
            if (execute(elapsed, lastRendered)) {
                lastRendered = 0.0f
            }
            lastExecTime = System.currentTimeMillis() - lastExecTime

            time = tmp
        }
        Log.d(tag, "AnimateThread going down")
    }
}