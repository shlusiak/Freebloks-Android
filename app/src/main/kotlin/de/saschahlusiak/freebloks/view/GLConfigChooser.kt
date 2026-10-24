package de.saschahlusiak.freebloks.view

import android.opengl.GLSurfaceView.EGLConfigChooser
import android.util.Log
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay

internal class GLConfigChooser(private val msaa: Int, private val glesVersion: Int = 1) : EGLConfigChooser {
    private val tag = GLConfigChooser::class.java.simpleName
    private val mValue = IntArray(1)

    /** Attributes selecting the renderable type; empty for GLES 1.x, which is the EGL default. */
    private val renderableSpec: IntArray = if (glesVersion >= 2)
        intArrayOf(EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT)
    else
        intArrayOf()

    override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig {
        var numConfigs = 0
        var configSpec: IntArray? = null

        if (msaa >= 2) {
            /* try multi-sampling first, if requested */
            configSpec = intArrayOf(
                EGL10.EGL_RED_SIZE, 5,
                EGL10.EGL_GREEN_SIZE, 6,
                EGL10.EGL_BLUE_SIZE, 5,
                EGL10.EGL_DEPTH_SIZE, 16,
                EGL10.EGL_SAMPLE_BUFFERS, 1 /* true */,
                EGL10.EGL_SAMPLES, msaa,
                *renderableSpec,
                EGL10.EGL_NONE
            )
            if (egl.eglChooseConfig(display, configSpec, null, 0, mValue)) {
                numConfigs = mValue[0]
            }
        }

        if (numConfigs <= 0) {
            // then try without multi-sampling.
            configSpec = intArrayOf(
                EGL10.EGL_RED_SIZE, 5,
                EGL10.EGL_GREEN_SIZE, 6,
                EGL10.EGL_BLUE_SIZE, 5,
                EGL10.EGL_DEPTH_SIZE, 16,
                *renderableSpec,
                EGL10.EGL_NONE
            )
            require(egl.eglChooseConfig(display, configSpec, null, 0, mValue))
                { "2nd eglChooseConfig failed" }

            numConfigs = mValue[0]
            require(numConfigs > 0)
                { "No EGL configs found" }
        }

        // Get all matching configurations.
        val configs = arrayOfNulls<EGLConfig>(numConfigs)

        require(egl.eglChooseConfig(display, configSpec, configs, numConfigs, mValue))
            { "data eglChooseConfig failed" }

        if (configs.isEmpty()) throw IllegalArgumentException("No config chosen")
        val config = configs[0] ?: throw IllegalArgumentException("Config is null")

        Log.w(tag, String.format("found config: %d/%d/%d, depth %d, msaa %d",
            findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE),
            findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE),
            findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE),
            findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE),
            findConfigAttrib(egl, display, config, EGL10.EGL_SAMPLES))
        )
        return config
    }

    private fun findConfigAttrib(egl: EGL10, display: EGLDisplay, config: EGLConfig, attribute: Int, defaultValue: Int = 0): Int {
        return if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
            mValue[0]
        } else
            defaultValue
    }

    private companion object {
        const val EGL_RENDERABLE_TYPE = 0x3040
        const val EGL_OPENGL_ES2_BIT = 0x0004
    }
}
