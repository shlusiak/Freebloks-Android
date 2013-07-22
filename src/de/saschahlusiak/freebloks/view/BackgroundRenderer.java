package de.saschahlusiak.freebloks.view;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import de.saschahlusiak.freebloks.R;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLUtils;
import android.view.View;

public class BackgroundRenderer extends SimpleModel {
	float rgba[] = new float[4];
	int texture[];
	boolean hasTexture;
	
    final static int num_vertices = 4;
    final static int num_triangles = 2;
    final static float size = 80.0f;
    final static float textures = 15.0f;
    
    Theme theme;
    
    public static class Theme {
    	private String name;
    	
    	int drawable, r, g, b;
    	boolean isPreview, isDrawable;    	
    	
    	private Theme(int drawable) {
    		this.isDrawable = true;
    		this.isPreview = false;
    		this.drawable = drawable;
    		this.r = 12;
    		this.g = 25;
    		this.b = 64;
    	}
    	
    	private Theme(int r, int g, int b) {
    		this.isDrawable = false;
    		this.isPreview = false;
    		this.r = r;
    		this.g = g;
    		this.b = b;
    	}
    	    	
    	String getName() {
    		return name;
    	}
    	
    	final boolean isDrawable() {
    		return isDrawable;
    	}
    	
    	BitmapDrawable getDrawable(Resources resources) {
    		BitmapDrawable background = (BitmapDrawable) resources.getDrawable(drawable);

    		background.setTileModeXY(TileMode.REPEAT, isPreview ? TileMode.MIRROR : TileMode.REPEAT);
    		background.setFilterBitmap(true);
    		return background;
    	}
    	
    	final int getColor() {
    		return Color.rgb(r, g, b);
    	}
    	
    	final void getRGB(float[] rgb) {
    		rgb[0] = (float)this.r / 255.0f;
    		rgb[1] = (float)this.g / 255.0f;
    		rgb[2] = (float)this.b / 255.0f;
    	}
    	
    	public void apply(View view) {
    		if (isDrawable)
    			view.setBackgroundDrawable(getDrawable(view.getResources()));
    		else
    			view.setBackgroundColor(getColor());
    	}

    	Bitmap getBitmap(Resources resources) {
    		return BitmapFactory.decodeResource(resources, drawable);
    	}
    	
    	public static Theme get(String theme, boolean preview) {
    		Theme t;
    		
    		if (theme.equals("black")) {
    			t = new Theme(0, 0, 0);
    		} else if (theme.equals("blue")) {
    			t = new Theme(12, 25, 64);
    		} else if (theme.equals("texture_metal")) {
    			t = new Theme(preview ? R.drawable.texture_metal : R.drawable.texture_metal);
    		} else
    			return null;
    		
    		t.isPreview = preview;
    		t.name = theme;

    		return t;
    	}    	
    }

    
	public BackgroundRenderer() {
		super(num_vertices, num_triangles);
		
		hasTexture = false;
		rgba[0] = 0.0f;
		rgba[1] = 0.0f;
		rgba[2] = 0.0f;
		rgba[3] = 1.0f;

	    addVertex(-size, 0, -size, 0, 1, 0, 0, 0);
	    addVertex( size, 0, -size, 0, 1, 0, textures, 0);
	    addVertex( size, 0,  size, 0, 1, 0, textures, textures);
	    addVertex(-size, 0,  size, 0, 1, 0, 0, textures);

	    addIndex(0, 2, 1);
	    addIndex(0, 3, 2);

	    commit();
	}
	
	boolean valid = false;
	
	public void applyTheme(Theme theme) {	
		this.theme = theme;
		valid = false;
	}
	
	public void updateTexture(Resources resources, GL10 gl) {
		valid = true;
		rgba[3] = 1.0f;	/* a */
		if (theme == null) {
			rgba[0] = 0.0f; // transparent
			rgba[1] = 0.0f;
			rgba[2] = 0.0f;
			rgba[3] = 0.0f;
			hasTexture = false;
		} else if (theme.isDrawable()) {
			Bitmap bitmap = theme.getBitmap(resources);
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
		  	GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		  	
		  	bitmap.recycle();
		  	bitmap = null;
		} else {
			hasTexture = false;
		}
		theme.getRGB(rgba);
	}

	final float diffuse[] = {0.6f, 0.6f, 0.6f, 1.0f};
	final float specular[] = {0, 0, 0, 1};

	public void render(Resources resources, GL10 gl) {
		if (!valid)
			updateTexture(resources, gl);
		
		gl.glClearColor(rgba[0], rgba[1], rgba[2], rgba[3]);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		
		if (hasTexture) {
//			float shininess[]={3.0f};
			
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, diffuse, 0);
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, specular, 0);
//			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, shininess, 0);
			
			gl.glEnable(GL10.GL_TEXTURE_2D);
			gl.glDepthMask(false);
		
			gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, getVertexBuffer());
			gl.glNormalPointer(GL10.GL_FLOAT, 0, getNormalBuffer());
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, getTextureBuffer());
			
			drawElements(gl);
			
			gl.glDepthMask(true);
			gl.glDisable(GL10.GL_TEXTURE_2D);
		}
	}
}
