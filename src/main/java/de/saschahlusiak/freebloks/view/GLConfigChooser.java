package de.saschahlusiak.freebloks.view;

import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

class GLConfigChooser implements GLSurfaceView.EGLConfigChooser {
	static private final String tag = GLConfigChooser.class.getSimpleName();
	
	private int msaa;

	public GLConfigChooser(int msaa) {
		this.msaa = msaa;
	}

	@Override
	public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
		mValue = new int[1];
		int numConfigs;
		int configSpec[] = null;

		if (msaa >= 2) {
			/* try multisampling first, if requested */
			configSpec = new int[] {
					EGL10.EGL_RED_SIZE, 5,
					EGL10.EGL_GREEN_SIZE, 6,
					EGL10.EGL_BLUE_SIZE, 5,
					EGL10.EGL_DEPTH_SIZE, 16,
					// Requires that setEGLContextClientVersion(2) is called on the view.
//					EGL10.EGL_RENDERABLE_TYPE, 4 /* EGL_OPENGL_ES2_BIT */,
					EGL10.EGL_SAMPLE_BUFFERS, 1 /* true */,
					EGL10.EGL_SAMPLES, msaa,
					EGL10.EGL_NONE };
	
			if (!egl.eglChooseConfig(display, configSpec, null, 0, mValue)) {
				numConfigs = 0;
			} else {
				numConfigs = mValue[0];
			}
		} else
			numConfigs = 0;

		if (numConfigs <= 0) {
			// try without multisampling.
			configSpec = new int[] {
					EGL10.EGL_RED_SIZE, 5,
					EGL10.EGL_GREEN_SIZE, 6,
					EGL10.EGL_BLUE_SIZE, 5,
					EGL10.EGL_DEPTH_SIZE, 16,
//					EGL10.EGL_RENDERABLE_TYPE, 4 	/* EGL_OPENGL_ES2_BIT */,
					EGL10.EGL_NONE };

			if (!egl.eglChooseConfig(display, configSpec, null, 0, mValue)) {
				throw new IllegalArgumentException("2nd eglChooseConfig failed");
			}
			numConfigs = mValue[0];

			if (numConfigs <= 0) {
				throw new IllegalArgumentException("No EGL configs found");
			}
		}
		
		// Get all matching configurations.
		EGLConfig[] configs = new EGLConfig[numConfigs];
		if (!egl.eglChooseConfig(display, configSpec, configs, numConfigs, mValue)) {
			throw new IllegalArgumentException("data eglChooseConfig failed");
		}

		int index = 0;

		EGLConfig config = configs.length > 0 ? configs[index] : null;
		if (config == null) {
			throw new IllegalArgumentException("No config chosen");
		}
		Log.w(tag, String.format("found config: %d/%d/%d, depth %d, msaa %d", 
				findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0),
				findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0),
				findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0),
				findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0),
				findConfigAttrib(egl, display, config, EGL10.EGL_SAMPLES, 0)));
		return config;
	}

	private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
		if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
			return mValue[0];
		}
		return defaultValue;
	}

	private int[] mValue;
}
