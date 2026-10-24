package de.saschahlusiak.freebloks.view.gles2

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils

internal object Gles2Textures {
    /**
     * Uploads the bitmap into a new texture, mipmapped where ES 2.0 allows it (power of two sizes), and returns the id.
     * Non power of two textures cannot be mipmapped or repeated in core ES 2.0, so those are clamped and use plain linear filtering.
     */
    fun fromBitmap(bitmap: Bitmap): Int {
        val ids = IntArray(1)
        GLES20.glGenTextures(1, ids, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ids[0])

        val pot = isPowerOfTwo(bitmap.width) && isPowerOfTwo(bitmap.height)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, if (pot) GLES20.GL_LINEAR_MIPMAP_NEAREST else GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        if (pot) GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        return ids[0]
    }

    fun delete(id: Int) {
        if (id != 0) GLES20.glDeleteTextures(1, intArrayOf(id), 0)
    }

    private fun isPowerOfTwo(v: Int) = v > 0 && (v and (v - 1)) == 0
}
