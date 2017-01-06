package de.saschahlusiak.freebloks.view.model;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import de.saschahlusiak.freebloks.R;

public class Theme {
	int drawable, texture;
	float rgba[] = { 1, 1, 1, 1 };
	boolean isPreview, isDrawable;

	private Theme() {
		this.isDrawable = false;
		this.isPreview = false;
	}

	private Theme(int drawable, int texture) {
		this.isDrawable = true;
		this.isPreview = false;
		this.drawable = drawable;
		this.texture = texture;
	}

	private Theme(float r, float g, float b) {
		this.isDrawable = false;
		this.isPreview = false;
		setRGB(r, g, b);
	}

	public final boolean isDrawable() {
		return isDrawable;
	}

	private void setDrawable(int drawable, int texture) {
		this.drawable = drawable;
		this.texture = texture;
		this.isDrawable = true;
	}

	private BitmapDrawable getDrawable(Resources resources) {
		BitmapDrawable background = (BitmapDrawable) resources.getDrawable(drawable);

		background.setTileModeXY(TileMode.REPEAT, isPreview ? TileMode.MIRROR : TileMode.REPEAT);
		background.setFilterBitmap(true);
		return background;
	}

	final int getColor() {
		return Color.rgb((int)(rgba[0] * 255.0f), (int)(rgba[1] * 255.0f), (int)(rgba[2] * 255.0f));
	}

	public final float[] getRGBA() {
		return rgba;
	}

	private void setRGB(float r, float g, float b) {
		this.rgba[0] = r;
		this.rgba[1] = g;
		this.rgba[2] = b;
	}

	private void setColor(int r, int g, int b) {
		this.rgba[0] = (float)r / 255.0f;
		this.rgba[1] = (float)g / 255.0f;
		this.rgba[2] = (float)b / 255.0f;
	}

	private void setColor(int color) {
		setColor(Color.red(color), Color.green(color), Color.blue(color));
	}

	public void apply(View view) {
		if (isDrawable)
			view.setBackgroundDrawable(getDrawable(view.getResources()));
		else
			view.setBackgroundColor(getColor());
	}

	public int getTexture() {
		return texture;
	}

	public static Theme get(Context context, String theme, boolean preview) {
		Theme t = new Theme();
		t.isPreview = preview;

		switch (theme) {
			default:
				Log.e("Theme", "theme not found: " + theme);
			case "blue":
				t.setColor(context.getResources().getColor(R.color.theme_background_blue));
				break;
			case "black":
				t.setColor(context.getResources().getColor(R.color.theme_background_black));
				break;
			case "texture_table_cloth_1":
				t.setDrawable(R.drawable.texture_table_1, R.raw.texture_table_1);
				t.setRGB(0.8f, 0.8f, 0.8f);
				break;
			case "texture_table_cloth_2":
				t.setDrawable(R.drawable.texture_table_2, R.raw.texture_table_2);
				t.setRGB(0.7f, 0.7f, 0.7f);
				break;
			case "texture_wood":
				t.setDrawable(R.drawable.texture_wood_fine, R.raw.texture_wood_fine);
				t.setRGB(0.7f, 0.7f, 0.7f);
				break;
			case "texture_metal":
				t.setDrawable(R.drawable.texture_metal, R.raw.texture_metal);
				t.setRGB(0.85f, 0.85f, 0.85f);
				break;
			case "texture_bricks":
				t.setDrawable(R.drawable.texture_bricks, R.raw.texture_bricks);
				t.setRGB(0.85f, 0.85f, 0.85f);
				break;
			case "texture_carpet_blue":
				t.setDrawable(R.drawable.texture_carpet_blue, R.raw.texture_carpet_blue);
				t.setRGB(0.85f, 0.85f, 0.85f);
				break;
			case "texture_velvet":
				t.setDrawable(R.drawable.texture_velvet, R.raw.texture_velvet);
				t.setRGB(0.85f, 0.85f, 0.85f);
				break;
			case "texture_grass":
				t.setDrawable(R.drawable.texture_grass, R.raw.texture_grass);
				t.setRGB(0.85f, 0.85f, 0.85f);
				break;
		}

		return t;
	}
}