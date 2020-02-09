package de.saschahlusiak.freebloks.view.effects;

import javax.microedition.khronos.opengles.GL11;

import de.saschahlusiak.freebloks.model.Orientation;
import de.saschahlusiak.freebloks.model.Shape;
import de.saschahlusiak.freebloks.view.BoardRenderer;
import de.saschahlusiak.freebloks.view.model.ViewModel;

public class PhysicalStoneEffect extends AbsStoneEffect {
	public static final float GRAVITY = 17.0f;
	/* Aktuelle Position des Steins in Welt-Koordinaten. */
	float x,y,z;
	/* Aktuellen Winkel */
	float ang;
	/* Rotationsgeschwindigkeit, sowie Rotationsachsen. */
	float angspeed,ax,ay,az;
	/* Geschwindigkeit des Steins in Einheiten/sek */
	float speedx,speedy,speedz;
	/* Angestrebte Position auf dem Feld. Bei Landung wird die Position daran ausgerichtet,
	   um kleine Rechenungenauigkeiten waehrend der Animation auszugleichen. */
	float dx,dy,dz;

	public PhysicalStoneEffect(ViewModel model, Shape stone, int color, Orientation orientation) {
		super(model, stone, color, 0, 0, orientation.getMirrored() ? 1 : 0, orientation.getRotation().getValue());
		this.x = this.y = this.z = 0.0f;
		ang = 0.0f;
		angspeed = 0.0f;
		ax = az = speedx = speedy = speedz = 0.0f;
		ay = 1.0f;
	}

	@Override
	public void renderShadow(GL11 gl, BoardRenderer renderer) {
		gl.glPushMatrix();
		gl.glScalef(1, 0.01f, 1);
		gl.glTranslatef(x, 0, z);

		float m_alpha = 0.60f - y / 20.0f;
	    gl.glTranslatef(-2.5f * y * 0.11f, 0, 2.0f * y * 0.11f);

		gl.glRotatef(ang, ax, ay, az);
		gl.glScalef(1.0f + y / 18.0f, 1, 1.0f + y / 18.0f);


	    gl.glTranslatef(
 				+BoardRenderer.stone_size-(float)stone.getSize()/2.0f*BoardRenderer.stone_size*2.0f,
 				0.0f,
			    +BoardRenderer.stone_size-(float)stone.getSize()/2.0f*BoardRenderer.stone_size*2.0f);
		renderer.renderStoneShadow(gl, color, stone, mirror, rotate, m_alpha);
		gl.glPopMatrix();
	}

	@Override
	public void render(GL11 gl, BoardRenderer renderer) {
		gl.glPushMatrix();
		gl.glTranslatef(x, y, z);
		gl.glRotatef(ang, ax, ay, az);

		gl.glTranslatef(
 				+BoardRenderer.stone_size-(float)stone.getSize()/2.0f*BoardRenderer.stone_size*2.0f,
 				0.0f,
			    +BoardRenderer.stone_size-(float)stone.getSize()/2.0f*BoardRenderer.stone_size*2.0f);

		renderer.renderPlayerStone(gl, color, stone, mirror, rotate, BoardRenderer.DEFAULT_ALPHA);
		gl.glPopMatrix();
	}

	@Override
	public boolean isDone() {
		return false;
	}

	public void setPos(float sx, float sy, float sz) {
		this.x=sx;
		this.y=sy;
		this.z=sz;
	}

	public void setRotationSpeed(float angs,float ax,float ay,float az) {
		angspeed=angs;
		this.ax=ax;
		this.ay=ay;
		this.az=az;
	}

	public void setSpeed(float sx,float sy,float sz) {
		speedx=sx;
		speedy=sy;
		speedz=sz;
	}

	public void setDestination(float destx,float desty,float destz) {
		dx=destx;
		dy=desty;
		dz=destz;
	}

	@Override
	public boolean execute(float elapsed) {
		super.execute(elapsed);

		/* Gradlinige Bewegung, einfach zu animieren. */
		x+=speedx*elapsed;
		y-=speedy*elapsed;
		z+=speedz*elapsed;
		/* Konstante Winkelgeschwindigkeit bei Rotation. Linear animieren. */
		ang+=elapsed*angspeed;
		/* wenn y unterhalb von dy gefallen ist, Stein auf dx/dy/dz setzen. */
		if (y<dy && dy>-100.0)
		{
			/* Stein soll wieder nach oben huepfen, aber mit gedaempfter vertikaler Geschw. */
			if (speedy>0.5)speedy=-speedy*0.32f;else speedy=0.0f;
			/* Soll nicht mehr rotieren, und soll genau parallel zur Erde sein. */
			angspeed=0.0f;
			ang=0.0f;
			/* x und y Geschw. auf 0 setzen. */
			speedx=0.0f;
			speedz=0.0f;
			/* Position auf Ziel setzen. */
			x=dx;
			y=dy;
			z=dz;
		}
		/* Stein vertikal beschleunigen (Gravitation). */
		speedy+=elapsed*GRAVITY;
		/* Der Stein will nie entfernt werden, verschwindet nie automatisch. */
		return false;
	}

	public final float getX() { return x;}
	public final float getY() { return y;}
	public final float getZ() { return z;}
	public final void unsetDestination() { dy=-200.0f; }
}
