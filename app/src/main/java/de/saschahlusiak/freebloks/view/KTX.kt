package de.saschahlusiak.freebloks.view

import android.content.res.AssetManager
import de.saschahlusiak.freebloks.crashReporter
import javax.microedition.khronos.opengles.GL10

object KTX {
    private external fun loadKTXTexture(assetManager: AssetManager, file: String, target: Int, skipMipmaps: Int): Int

    fun loadKTXTexture(assetManager: AssetManager, file: String) {
        crashReporter.log("Loading texture $file")
        val ret = loadKTXTexture(assetManager, file, GL10.GL_TEXTURE_2D, 0)
        if (ret != 0) throw RuntimeException("loadKTXTexture returned $ret for texture $file")
    }

    init {
        System.loadLibrary("ktx")
    }
}