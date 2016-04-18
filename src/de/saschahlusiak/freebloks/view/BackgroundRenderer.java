package de.saschahlusiak.freebloks.view;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import de.saschahlusiak.freebloks.view.model.Theme;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLUtils;

public class BackgroundRenderer extends SimpleModel {
	float rgba[] = { 0, 0, 0, 1 };
	int texture[];
	boolean hasTexture;

    final static int num_vertices = 4;
    final static int num_triangles = 2;
    final static float size = 80.0f;
    final static float textures = 15.0f;

    Theme theme;
    Resources resources;

    public BackgroundRenderer(Resources resources) {
		super(num_vertices, num_triangles);

		this.resources = resources;
		hasTexture = false;

	    addVertex(-size, 0, -size, 0, 1, 0, 0, 0);
	    addVertex( size, 0, -size, 0, 1, 0, textures, 0);
	    addVertex( size, 0,  size, 0, 1, 0, textures, textures);
	    addVertex(-size, 0,  size, 0, 1, 0, 0, textures);

	    addIndex(0, 2, 1);
	    addIndex(0, 3, 2);

	    commit();
	}

	boolean valid = false;

	public void setTheme(Theme theme) {
		this.theme = theme;
		valid = false;
	}

	public void updateTexture(GL10 gl) {
		valid = true;
		if (theme == null) {
			rgba[0] = 0.0f; // transparent
			rgba[1] = 0.0f;
			rgba[2] = 0.0f;
			rgba[3] = 0.0f;
			hasTexture = false;
		} else if (theme.isDrawable()) {
			hasTexture = true;

			texture = new int[1];
			gl.glGenTextures(1, texture, 0);

			gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);
		  	if (gl instanceof GL11) {
				gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR_MIPMAP_LINEAR);
		  		gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
		  	} else {
				gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		  		gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_FALSE);
		  	}
		  	gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		  	FreebloksRenderer.loadKTXTexture(gl, resources, theme.getTexture());

			rgba = theme.getRGBA();
		} else {
			hasTexture = false;
			rgba = theme.getRGBA();
		}
	}

	final float specular[] = {0, 0, 0, 1};

	public void render(GL11 gl) {
		if (!valid)
			updateTexture(gl);

		if (hasTexture)
			gl.glClearColor(0.05f, 0.10f, 0.25f, 1.0f); /* the default background when textured */
		else
			gl.glClearColor(rgba[0], rgba[1], rgba[2], rgba[3]);

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		if (hasTexture) {
//			float shininess[]={3.0f};

			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, rgba, 0);
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, specular, 0);
//			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, shininess, 0);

			gl.glEnable(GL10.GL_TEXTURE_2D);

			gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);
			bindBuffers(gl);

			drawElements(gl);

			gl.glDisable(GL10.GL_TEXTURE_2D);
		}
	}
}
