package de.saschahlusiak.freebloks

object Feature {
    // if fast, no delay in server between CPU moves
    val FAST_GAME = BuildConfig.DEBUG

    val DYNAMIC_COLORS = BuildConfig.DEBUG

    // use the shader based OpenGL ES 2.0 renderer instead of the fixed function OpenGL ES 1.x one
    val GLES2_RENDERER = BuildConfig.DEBUG

    const val FORCE_TABLET_DIMENSIONS = false
}