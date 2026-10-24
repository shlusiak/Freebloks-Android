package de.saschahlusiak.freebloks.ktx

import android.content.res.AssetManager
import android.opengl.GLES20

object KTX {
    private external fun loadKTXTexture(assetManager: AssetManager, file: String, target: Int, skipMipmaps: Int): Int

    fun loadKTXTexture(assetManager: AssetManager, file: String) {
        val ret = loadKTXTexture(assetManager, file, GLES20.GL_TEXTURE_2D, 0)
        if (ret != 0) throw RuntimeException("loadKTXTexture returned $ret for texture $file")
    }

    init {
        System.loadLibrary("ktx")
    }
}