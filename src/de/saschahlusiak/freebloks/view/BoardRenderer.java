package de.saschahlusiak.freebloks.view;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;


public class BoardRenderer {
	final static float bevel_size = 0.18f;
	final public static float bevel_height = 0.25f;
	final static float border_bottom = -1.1f;
	final static float border_top = 0.4f;
	final static float border_width = 0.5f;
	final public static float stone_size = 0.45f;

	final public static float DEFAULT_ALPHA = 0.75f;

	SimpleModel field;
	SimpleModel border;
	public SimpleModel stone;
	SimpleModel shadow;

    final float r1 = stone_size;
    final float r2 = stone_size - bevel_size;
    final float y1 = -bevel_height;
    final float y2 = 0.0f;

    final float texture_scale = 0.12f;
    final float texture_rotation = 1.0f;

    int texture[];


	final float board_diffuse_normal[] = {0.6f,0.6f,0.6f,1.0f};
	final float board_diffuse_available[] = {0.50f,0.8f,0.60f,1.0f};
	final float board_specular[] = {0.25f,0.24f,0.24f,1.0f};
	final float board_shininess[] = {35.0f};

	public final float stone_specular[]={0.3f, 0.3f, 0.3f, 1.0f};
	public final float stone_shininess[]={ 30.0f };

	final float no_mat[] = {0,0,0,1};

	BoardRenderer(int field_size) {
		initField();
		initBorder(field_size);
		initStone();
		initShadow();
	}

	private void initField() {
		field = new SimpleModel(8, 10, true);
		/* bottom, inner */
		field.addVertex(-r2, y1, +r2,  0, 1, 0,  -r2, r2);
		field.addVertex(+r2, y1, +r2,  0, 1, 0,  r2, r2);
		field.addVertex(+r2, y1, -r2,  0, 1, 0,  r2, -r2);
		field.addVertex(-r2, y1, -r2,  0, 1, 0,  -r2, -r2);

		/* top, outer */
		field.addVertex(-r1, y2, +r1, +1, 1, -1,  -r1, r1);
		field.addVertex(+r1, y2, +r1, -1, 1, -1,  r1, r1);
		field.addVertex(+r1, y2, -r1, -1, 1, +1,  r1, -r1);
		field.addVertex(-r1, y2, -r1, +1, 1, +1,  -r1, -r1);

		field.addIndex(0, 1, 2);
		field.addIndex(0, 2, 3);
		field.addIndex(0, 5, 1);
		field.addIndex(0, 4, 5);
		field.addIndex(1, 5, 6);
		field.addIndex(1, 6, 2);
		field.addIndex(2, 6, 7);
		field.addIndex(2, 7, 3);
		field.addIndex(3, 7, 4);
		field.addIndex(3, 4, 0);

		field.commit();
	}

	void initShadow() {
		shadow = new SimpleModel(4, 2, false);

		shadow.addVertex(-r1, 0, +r1, 0, 1, 0, 0, 0);
		shadow.addVertex(+r1, 0, +r1, 0, 1, 0, 1, 0);
		shadow.addVertex(+r1, 0, -r1, 0, 1, 0, 1, 1);
		shadow.addVertex(-r1, 0, -r1, 0, 1, 0, 0, 1);

		shadow.addIndex(0, 1, 2);
		shadow.addIndex(0, 2, 3);

		shadow.commit();
	}

	private void initStone() {
		stone = new SimpleModel(12, 20, true);
		/* top, inner */
		stone.addVertex(-r2, -y1, +r2,  0, 1, 0,  0, 0);
		stone.addVertex(+r2, -y1, +r2,  0, 1, 0,  0, 0);
		stone.addVertex(+r2, -y1, -r2,  0, 1, 0,  0, 0);
		stone.addVertex(-r2, -y1, -r2,  0, 1, 0,  0, 0);

		/* middle, outer */
		stone.addVertex(-r1, y2, +r1, -1, 0, +1,  0, 0);
		stone.addVertex(+r1, y2, +r1, +1, 0, +1,  0, 0);
		stone.addVertex(+r1, y2, -r1, +1, 0, -1,  0, 0);
		stone.addVertex(-r1, y2, -r1, -1, 0, -1,  0, 0);

		/* bottom, inner */
		stone.addVertex(-r2, y1, +r2,  0, -1, 0,  0, 0);
		stone.addVertex(+r2, y1, +r2,  0, -1, 0,  0, 0);
		stone.addVertex(+r2, y1, -r2,  0, -1, 0,  0, 0);
		stone.addVertex(-r2, y1, -r2,  0, -1, 0,  0, 0);

		/* top */
		stone.addIndex(0, 1, 2);
		stone.addIndex(0, 2, 3);
		stone.addIndex(0, 5, 1);
		stone.addIndex(0, 4, 5);
		stone.addIndex(1, 5, 6);
		stone.addIndex(1, 6, 2);
		stone.addIndex(2, 6, 7);
		stone.addIndex(2, 7, 3);
		stone.addIndex(3, 7, 4);
		stone.addIndex(3, 4, 0);

		/* bottom */
		stone.addIndex(8, 10, 9);
		stone.addIndex(8, 11, 10);
		stone.addIndex(8, 9, 5);
		stone.addIndex(8, 5, 4);
		stone.addIndex(9, 6, 5);
		stone.addIndex(9, 10, 6);
		stone.addIndex(10, 7, 6);
		stone.addIndex(10, 11, 7);
		stone.addIndex(11, 4, 7);
		stone.addIndex(11, 8, 4);

		stone.commit();
	}

	public void initBorder(int field_size) {
		border = new SimpleModel(12, 6, true);
		float w1, w2;
		w1 = stone_size * field_size;
		w2 = w1 + border_width;

		/* front */
		border.addVertex(w2, border_top, w2, 0, 0, 1, w2, border_top);
		border.addVertex(-w2, border_top, w2, 0, 0, 1, -w2, border_top);
		border.addVertex(-w2, border_bottom, w2, 0, 0, 1, -w2, border_bottom);
		border.addVertex(w2, border_bottom, w2, 0, 0, 1, w2, border_bottom);

		/* top */
		border.addVertex(w2, border_top, w2, 0, 1, 0, w2, w2);
		border.addVertex(-w2, border_top, w2, 0, 1, 0, -w2, w2);
		border.addVertex(-w1, border_top, w1, 0, 1, 0, -w1, w1);
		border.addVertex(w1, border_top, w1, 0, 1, 0, w1, w1);

		/* inner */
		border.addVertex(w1, border_top, w1, 0, 0, -1, w1, border_top);
		border.addVertex(-w1, border_top, w1, 0, 0, -1, -w1, border_top);
		border.addVertex(-w1, 0, w1, 0, 0, -1, -w1, w1);
		border.addVertex(w1, 0, w1, 0, 0, -1, w1, w1);

		border.addIndex(0, 1, 2);
		border.addIndex(0, 2, 3);

		border.addIndex(7, 6, 4);
		border.addIndex(4, 6, 5);

		border.addIndex(9, 8, 10);
		border.addIndex(8, 11, 10);

		border.commit();
	}

	void updateTexture(Context context, GL11 gl) {
		stone.invalidate(gl);

		texture = new int[2];

		gl.glGenTextures(texture.length, texture, 0);

		gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR_MIPMAP_NEAREST);
		gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        FreebloksRenderer.loadKTXTexture(gl, context.getResources(), R.raw.field_wood);


		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.stone_shadow);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[1]);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR_MIPMAP_NEAREST);
		gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
	}

	public void renderBoard(GL11 gl, Spiel spiel, int currentPlayer) {
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, board_specular, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, board_shininess, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, board_diffuse_normal, 0);

		gl.glEnable(GL10.GL_TEXTURE_2D);
	    gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);

		field.bindBuffers(gl);

	    gl.glMatrixMode(GL10.GL_TEXTURE);
	    gl.glLoadIdentity();
	    gl.glScalef(texture_scale, texture_scale, 1);
	    gl.glRotatef(texture_rotation, 0, 0, 1);
	    gl.glPushMatrix();
	    gl.glMatrixMode(GL10.GL_MODELVIEW);

	    int w = 20, h = 20;
	    if (spiel != null) {
	    	w = spiel.m_field_size_x;
	    	h = spiel.m_field_size_y;
	    }

	    gl.glPushMatrix();
	    gl.glTranslatef(-stone_size * (float)(w - 1), 0, -stone_size * (float)(h - 1) );
	    for (int y = 0; y < h; y++) {
	    	int x;
	    	for (x = 0; x < w; x++) {
	    		if (currentPlayer >= 0) {
	    			if (spiel.get_game_field(currentPlayer, y, x) == Stone.FIELD_ALLOWED)
	    				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, board_diffuse_available, 0);
	    			else
	    				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, board_diffuse_normal, 0);
	    		}

	    		field.drawElements(gl);

	    	    gl.glMatrixMode(GL10.GL_TEXTURE);
	    		gl.glTranslatef(stone_size * 2.0f, 0, 0);
	    	    gl.glMatrixMode(GL10.GL_MODELVIEW);
	    	    gl.glTranslatef(stone_size * 2.0f, 0, 0);
	    	}
    	    gl.glMatrixMode(GL10.GL_TEXTURE);
	    	gl.glTranslatef(- x * stone_size * 2.0f, stone_size * 2.0f, 0);
    	    gl.glMatrixMode(GL10.GL_MODELVIEW);
	    	gl.glTranslatef(- x * stone_size * 2.0f, 0, stone_size * 2.0f);
	    }
	    gl.glPopMatrix();
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, board_diffuse_normal, 0);

		border.bindBuffers(gl);

	    gl.glMatrixMode(GL10.GL_TEXTURE);
    	gl.glPopMatrix();
	    gl.glMatrixMode(GL10.GL_MODELVIEW);

	    /* we want the border in the depth buffer so it can cover the stones rendered later */
		gl.glEnable(GL10.GL_DEPTH_TEST);

	    for (int i = 0; i < 4; i++) {
	    	border.drawElements(gl);
	    	gl.glRotatef(90, 0, 1, 0);
	    }

	    gl.glMatrixMode(GL10.GL_TEXTURE);
	    gl.glLoadIdentity();
	    gl.glMatrixMode(GL10.GL_MODELVIEW);

		gl.glDisable(GL10.GL_TEXTURE_2D);
	}

	public void renderStone(GL11 gl, int color, float alpha) {
		final float c[] = Global.stone_color_a[color];
		tmp[0] = c[0] * alpha;
		tmp[1] = c[1] * alpha;
		tmp[2] = c[2] * alpha;
		tmp[3] = alpha;

		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, tmp, 0);

	    stone.drawElements(gl);
	}

	public final void renderStone(GL11 gl, float[] color) {
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, color, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, stone_specular, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, stone_shininess, 0);

		stone.bindBuffers(gl);
	    stone.drawElements(gl);
	}

	final float tmp[] = new float[4];

	public void renderStoneShadow(GL11 gl, int color, Stone stone, int mirror, int rotate, float alpha) {
		final float c[] = Global.stone_shadow_color_a[color];
		tmp[0] = c[0] * alpha;
		tmp[1] = c[1] * alpha;
		tmp[2] = c[2] * alpha;
		tmp[3] = alpha;

		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, tmp, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, no_mat, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, no_mat, 0);

		shadow.bindBuffers(gl);

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);

		gl.glEnable(GL10.GL_TEXTURE_2D);
	    gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[1]);

		for (int i = 0; i < stone.get_stone_size(); i++) {
			int j;
			for (j = 0; j < stone.get_stone_size(); j++) {
				if (stone.get_stone_field(i, j, mirror, rotate) != Stone.STONE_FIELD_FREE)
					shadow.drawElements(gl);
				gl.glTranslatef(stone_size * 2.0f, 0, 0);
			}
			gl.glTranslatef(-j*stone_size * 2.0f, 0, stone_size * 2.0f);
		}

	    gl.glDisable(GL10.GL_TEXTURE_2D);
	    gl.glDisable(GL10.GL_BLEND);
	}

	public void renderShadow(GL11 gl,
			Stone stone, int color, int mirror, int rotate,
			float height,
			float ang, float ax, float ay, float az,
			float light_angle,
			float alpha, float scale) {


		float offset = (float)(stone.get_stone_size()) - 1.0f;
		float m_alpha = 0.80f - height / 16.0f;

		/* TODO: remove this and always show the board at the exact same angle,
		 * so we always have light coming from top left */
		gl.glRotatef(-light_angle, 0, 1, 0);
	    gl.glTranslatef(2.5f * height * 0.08f, 0, 2.0f * height * 0.08f);
		gl.glRotatef(light_angle, 0, 1, 0);

	    gl.glTranslatef(
	    		BoardRenderer.stone_size * offset,
	    		0,
	    		BoardRenderer.stone_size * offset);
	    gl.glScalef(scale, 0.01f, scale);

		gl.glRotatef(ang, ax, ay, az);

	    gl.glScalef(1.0f + height / 16.0f, 1, 1.0f + height / 16.0f);

	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * offset,
	    		0,
	    		-BoardRenderer.stone_size * offset);

		renderStoneShadow(gl, color, stone, mirror, rotate, m_alpha * alpha);
	}

	public void renderPlayerStone(GL11 gl, int color, Stone stone, int mirror, int rotate, float alpha) {
		int i;
		final float c[] = Global.stone_color_a[color];
		tmp[0] = c[0] * alpha;
		tmp[1] = c[1] * alpha;
		tmp[2] = c[2] * alpha;
		tmp[3] = alpha;

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);

		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, tmp, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, stone_specular, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, stone_shininess, 0);

		this.stone.bindBuffers(gl);

		for (i = 0; i < stone.get_stone_size(); i++) {
			int j;
			for (j = 0; j < stone.get_stone_size(); j++) {
				if (stone.get_stone_field(i, j, mirror, rotate) != Stone.STONE_FIELD_FREE)
					this.stone.drawElements(gl);
				gl.glTranslatef(stone_size * 2.0f, 0, 0);
			}
			gl.glTranslatef(-j*stone_size * 2.0f, 0, stone_size * 2.0f);
		}
    	gl.glDisable(GL10.GL_BLEND);
	}
}
