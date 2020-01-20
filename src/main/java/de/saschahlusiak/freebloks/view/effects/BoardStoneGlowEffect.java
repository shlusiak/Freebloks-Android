package de.saschahlusiak.freebloks.view.effects;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.view.BoardRenderer;
import de.saschahlusiak.freebloks.view.model.ViewModel;

public class BoardStoneGlowEffect extends AbsEffect implements Effect {
	int x, y;
	ViewModel model;

	private static final float BLEND_END = 0.55f;

	float color[] = new float[4];
	final float[] color1;
	final float[] color2;

	public BoardStoneGlowEffect(ViewModel model, int color, int x, int y, float distance) {
		this.model = model;
		this.x = x;
		this.y = y;
		this.time = -0.6f - distance * 0.025f;

	    color1 = Global.stone_color_a[color];
	    color2 = Global.stone_color_a[0];

	    this.color[3] = BoardRenderer.DEFAULT_ALPHA;
	    this.color[0] = color1[0] * this.color[3];
	    this.color[1] = color1[1] * this.color[3];
	    this.color[2] = color1[2] * this.color[3];
	}

	@Override
	public boolean isEffected(int x, int y) {
		return (x == this.x && y == this.y);
	}

	@Override
	public boolean isDone() {
		return time > BLEND_END;
	}

	@Override
	public boolean execute(float elapsed) {
	    float blend = (float)Math.sin(time / BLEND_END * Math.PI) * 0.9f;
	    if (time < 0.0f || time > BLEND_END)
	    	blend = 0.0f;

		color[3] = BoardRenderer.DEFAULT_ALPHA;
	    color[0] = (color1[0] * (1.0f - blend) + color2[0] * blend) * color[3];
	    color[1] = (color1[1] * (1.0f - blend) + color2[1] * blend) * color[3];
	    color[2] = (color1[2] * (1.0f - blend) + color2[2] * blend) * color[3];

		return super.execute(elapsed);
	}

	@Override
	public void render(GL11 gl, BoardRenderer renderer) {
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);

		gl.glPushMatrix();

	    gl.glTranslatef(
	    		-BoardRenderer.stone_size * (float)(model.spiel.width - 1) + BoardRenderer.stone_size * 2.0f * (float)x,
	    		0,
	    		-BoardRenderer.stone_size * (float)(model.spiel.height - 1) + BoardRenderer.stone_size * 2.0f * (float)y);

		renderer.renderStone(gl, color);
		gl.glPopMatrix();

		gl.glDisable(GL10.GL_BLEND);
	}

	@Override
	public void renderShadow(GL11 gl, BoardRenderer renderer) {

	}
}