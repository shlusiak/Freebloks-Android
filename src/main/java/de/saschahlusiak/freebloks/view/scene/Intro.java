package de.saschahlusiak.freebloks.view.scene;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.model.Board;
import de.saschahlusiak.freebloks.model.GameMode;
import de.saschahlusiak.freebloks.model.OrientedShape;
import de.saschahlusiak.freebloks.model.Rotation;
import de.saschahlusiak.freebloks.model.Shape;
import de.saschahlusiak.freebloks.theme.ThemeManager;
import de.saschahlusiak.freebloks.view.BackgroundRenderer;
import de.saschahlusiak.freebloks.view.BoardRenderer;
import de.saschahlusiak.freebloks.view.FreebloksRenderer;
import de.saschahlusiak.freebloks.view.effects.PhysicalShapeEffect;
import android.graphics.PointF;
import android.opengl.GLU;

public class Intro {
	public interface IntroCompleteListener {
		void onIntroCompleted();
	}

	private final static float INTRO_SPEED = 1.0f;

	private final static float WIPE_SPEED = 14.0f;
	private final static float WIPE_ANGLE = 28.0f;
	private final static float MATRIX_START = 1.56f;
	private final static float MATRIX_STOP = 6.0f;
	private final static float MATRIX_DURATION_START = 0.25f;
	private final static float MATRIX_DURATION_STOP = 0.25f;

	private Scene model;
	private IntroCompleteListener listener;

	private float anim = 0.0f;
	private final ArrayList<PhysicalShapeEffect> effects = new ArrayList<>();
	private int phase = 0;
	private boolean field_up = false;
	private float field_anim = 0.0f;
	private OrientedShape[] stones = new OrientedShape[14];
	private BackgroundRenderer backgroundRenderer;


	public Intro(Context context, Scene model, IntroCompleteListener listener) {
		backgroundRenderer = new BackgroundRenderer(context.getResources());
		backgroundRenderer.setTheme(ThemeManager.get(context).getTheme("blue", null));
		setModel(model, listener);
		init();
	}

	public void setModel(Scene model, IntroCompleteListener listener) {
		this.model = model;
		this.listener = listener;
	}

	private void init() {
		stones[0] = new OrientedShape(5, false, Rotation.Left);
		// XXX
		//   X

		stones[1] = new OrientedShape(8);
		// X
		// X
		// X
		// X

		stones[2] = new OrientedShape(10);
		// XX
		//  X
		// XX

		stones[3] = new OrientedShape(12);
		// X
		// X
		// XXX

		stones[4] = new OrientedShape(1);
		// X
		// X

		stones[5] = new OrientedShape(20);
		// X
		// X
		// X
		// X
		// X

		stones[6] = new OrientedShape(5);
		//  X
		//  X
		// XX

		stones[7] = new OrientedShape(2);
		// XX
		//  X
		stones[7].rotateLeft();
		stones[7].rotateLeft();

		stones[8] = new OrientedShape(0);
		// X

		stones[9] = new OrientedShape(3);
		// X
		// X
		// X

		stones[10] = new OrientedShape(10);
		// X X
		// XXX
		stones[10].rotateRight();

		stones[11] = new OrientedShape(1);
		// XX
		stones[11].rotateRight();

		stones[12] = new OrientedShape(5);
		//   X
		// XXX
		stones[12].rotateLeft();
		stones[12].mirrorVertically();

		stones[13] = new OrientedShape(5);
		// XXX
		// X
		stones[13].rotateRight();
		stones[13].mirrorVertically();

		addChar('f',3,4,5);
		addChar('r',2,7,6);
		addChar('e',1,10,5);
		addChar('e',0,13,6);

		addChar('b',0,2,12);
		addChar('l',1,5,11);
		addChar('o',2,8,12);
		addChar('k',3,11,11);
		addChar('s',2,14,13);
	}

	public boolean handlePointerDown(PointF m) {
		cancel();
		return true;
	}

	public boolean handlePointerMove(PointF m) {
		return false;
	}

	public boolean handlePointerUp(PointF m) {
		return false;
	}

	public void cancel() {
		model.view.post(new Runnable() {
			@Override
			public void run() {
				listener.onIntroCompleted();
				model.view.requestRender();
			}
		});
	}

	public boolean execute(float elapsed) {
		elapsed *= 1.2f * INTRO_SPEED;
		anim += elapsed;

		if (field_up || field_anim > 0.000001f)
		{
			if (field_up)
			{
				field_anim += elapsed*WIPE_SPEED;
				if (field_anim > 1.0f)	{
					field_anim = 1.0f;
					field_up = false;
				}
			} else {
				field_anim -= elapsed*2.5f;
				if (field_anim < 0.0)
					field_anim = 0.0f;
			}
		}
		if (phase==0)
		{
			/* In phase 0 kommt ein Matrix-Mode zwischen die fliegenden Steine */
			if (anim < MATRIX_START + MATRIX_DURATION_START)
			{
				if (anim<MATRIX_START)executeEffects(elapsed);
				else executeEffects(elapsed*(MATRIX_DURATION_START-anim+MATRIX_START)/MATRIX_DURATION_START);
			}
			if (anim>MATRIX_STOP)
			{
				if (anim>MATRIX_STOP+MATRIX_DURATION_STOP)executeEffects(elapsed);
				else executeEffects(elapsed*(anim-MATRIX_STOP)/MATRIX_DURATION_STOP);
			}
			if (anim > 10.5f)
			{
				/* Nach 10.5 Zeiteinheiten Feld leeren und Phase auf 1 */
				phase=1;
				wipe();
			}
		} else synchronized(effects) {
			/* Effekte animieren */
			executeEffects(elapsed);
			/* Bei den Phasen fallen Steine vom Himmel, diese sollen zuegig fallen */
			if (phase==2 || phase==4 || phase==5)
				executeEffects(elapsed*0.7f);
			/* Jede Phase dauert 12 Zeiteinheiten */
			if (anim>12.0)
			{
				/* Neue Phase und entweder Feld leeren */
				phase++;
				if (phase==3)
				{
					anim=10.8f;
					wipe();
				}
				if (phase==6)
				{
					anim=9.5f;
					wipe();
				}
				/* Oder neue Steine regnen lassen. */
				if (phase==2)
				{
					anim=9.1f;
					/* Alle Steine entfernen */
					effects.clear();
					addChar('b',-1,5,9);
					addChar('y',-1,9,9);
				}
				if (phase==4)
				{
					effects.clear();
					anim=10.2f;
					addChar('s',0,1,5);
					addChar('a',2,4,5);
					addChar('s',3,7,5);
					addChar('c',2,10,5);
					addChar('h',1,13,5);
					addChar('a',0,16,5);
				}
				if (phase==5)
				{
					anim=8.5f;
					addChar('h',3,0,11);
					addChar('l',2,3,11);
					addChar('u',0,6,11);
					addChar('s',1,9,11);
					addChar('i',2,11,11);
					addChar('a',0,13,11);
					addChar('k',3,16,11);
				}
				/* Nach der 7. Phase ist das Intro vorrueber */
				if (phase==7)cancel();
			}
		}
		return true;
	}

	void add(int stone, int player, int dx, int dy) {
		float x,y,z;
		/* Eine Rotationsachse berechnen */
		float angx=(float)(Math.random() * 2.0 *Math.PI);
		float angy=(float)(Math.random() * 2.0 *Math.PI);
		float axe_x=(float)(Math.sin(angx)*Math.cos(angy));
		float axe_y=(float)(Math.sin(angy));
		float axe_z=(float)(Math.cos(angx)*Math.cos(angy));

		/* CPhysicalStone erstellen, aus stones[stone] */
		OrientedShape st = stones[stone];
		Shape shape = st.getShape();
		PhysicalShapeEffect s = new PhysicalShapeEffect(model, shape, Global.getPlayerColor(player, GameMode.GAMEMODE_4_COLORS_4_PLAYERS), st.getOrientation());

		/* Lokale dx/dy des Feldes in globale Welt-Koordinaten umrechnen. */
		x=(float)(-(Board.DEFAULT_BOARD_SIZE - 1)*BoardRenderer.stone_size+((double)dx+(double)shape.getSize()/2.0)*BoardRenderer.stone_size*2.0-BoardRenderer.stone_size);
		z=(float)(-(Board.DEFAULT_BOARD_SIZE - 1)*BoardRenderer.stone_size+((double)dy+(double)shape.getSize()/2.0)*BoardRenderer.stone_size*2.0-BoardRenderer.stone_size);
		/* Zufaellige Hoehe geben. */
		y=22.0f+(float)(Math.random() * 18.0f);

		/* Der Stein wird in <time> sek den Boden erreichen. */
		float time=(float)Math.sqrt(2.0f*y/ PhysicalShapeEffect.gravity);
		/* x/z Koordinaten zufaellig verschieben */
		float xoffs=(float)Math.random()*60.0f - 30.0f;
		float zoffs=(float)Math.random()*60.0f - 30.0f;
		/* Position setzen */
		s.setPos(x+xoffs,y,z+zoffs);
		/* x/z Geschwindigkeit setzen, y Geschw. ist 0 */
		s.setSpeed(-xoffs/time,0,-zoffs/time);
		/* Gewuenschtes Ziel in Stein speichern */
		s.setTarget(x,0,z);
		/* Stein dreht sich exakt um 360 Grad in <time> sek. */
		s.setRotationSpeed(360.0f/time, axe_x, axe_y, axe_z);
		/* Effekt der verketteten Liste hinzufuegen. */
		effects.add(s);
	}

	void addChar(char c, int color, int x, int y) {
		switch (c)
		{
		case 'a':
			add(5,color,x-2,y);
			add(2,color,x+1,y);
			add(4,color,x+1,y+3);
			break;
		case 'b':
			add(0,color,x,  y-1);
			add(1,color,x-2,y+1);
			add(2,color,x+1,y+2);
			break;
		case 'c':
			add(5,color,x-2,y);
			add(11,color,x+1,y-1);
			add(11,color,x+1,y+3);
			break;
		case 'e':
			add(11,color,x+1,y-1);
			add(4,color,x-1,y);
			add(3,color,x,y+2);
			add(8,color,x+1,y+2);
			break;
		case 'f':
			add(13,color,x,y-1);
			add(9,color,x-1,y+2);
			add(8,color,x+1,y+2);
			break;
		case 'l':
			add(4,color,x-1,y);
			add(3,color,x,y+2);
			break;

		case 'o':
			add(5,color,x-2,y);
			add(6,color,x+1,y+2);
			add(7,color,x+1,y);
			break;
		case 'h':
			add(6,color,x+1,y);
			add(5,color,x-2,y);
			add(4,color,x+1,y+3);
			break;
		case 'k':
			add(5,color,x-2,y);
			add(8,color,x+1,y+2);
			add(4,color,x+1,y);
			add(4,color,x+1,y+3);
			break;
		case 'n':
			add(5,color,x-2,y);
			add(5,color,x,y);
			add(4,color,x,y+2);
			break;

		case 'u':
			add(9,color, x-1,y);
			add(9,color, x+1,y);
			add(10,color,x  ,y+3);
			break;

		case 'i':
			add(4, color, x, y);
			add(9, color, x, y+2);
			break;

		case 'r':
			add(0,color,x,y-1);
			add(1,color,x-2,y+1);
			add(8,color,x+1,y+2);
			add(4,color,x+1,y+3);
			break;
		case 's':
			add(3,color, x,  y);
			add(11,color,x+1,y-1);
			add(12,color,x,  y+3);
			break;
		case 'x':
			add(4,color,x-1,y);
			add(4,color,x+1,y);
			add(4,color,x-1,y+3);
			add(4,color,x+1,y+3);
			add(8,color,x+1,y+2);
			break;
		case 'y':
			add(4,color,x-1,y);
			add(4,color,x+1,y);
			add(9,color,x,y+2);
			break;

	 	default: throw new IllegalStateException("Falscher char uebergeben: " + c);
		}
	}

	private void wipe() {
		/* Zu Beginn das Feld hoch klappen */
		field_up = true;
		field_anim = 0.0f;
		/* Komplette verkettete Liste durchgehen und fuer jeden enthaltenen CPhysicalStone...*/
		for (PhysicalShapeEffect e: effects) {
			/* ...Geschwindigkeit setzen, dass die Steine tangential zur Drehung des
			   Felds wegfliegen */
			/* Winkel, in dem die Steine beschleunigt werden */
			final float ANG = WIPE_ANGLE / 180.0f * (float)Math.PI;
			/* Radialgeschwindigkeit errechnen. */
			final float v = (ANG*WIPE_SPEED)*(e.getCurrentZ()-20*BoardRenderer.stone_size)-(float)(Math.random() * 10.0 - 8.0);
			/* Stein nur leicht rotieren lassen, und nicht ganz zufaellig */
			final float a1=0.95f;
			final float a2=(float)((Math.random() < 0.5 ? 1 : -1)*Math.sqrt((1.0-a1*a1)/2.0));
			final float a3=(float)((Math.random() < 0.5 ? 1 : -1)*Math.sqrt((1.0-a1*a1)/2.0));
			/* Stein hauptsaechlich in Richtung der Felddrehung rotieren lassen */
			e.setRotationSpeed(WIPE_ANGLE*WIPE_SPEED+(float)(Math.random() * 6.6),a1,a2,a3);
			/* Geschwindigkeit und Winkel in Kartesische Koordinaten umrechnen */
			e.setSpeed((float)(Math.random() * 5.0),(float)Math.cos(ANG)*v,(float)-Math.sin(ANG)*v);
			/* Stein soll kein Ziel mehr haben, d.h. er faellt unendlich tief */
			e.unsetDestination();
		}
	}

	private void executeEffects(float elapsed) {
		for (int i = 0; i < effects.size(); i++)
			effects.get(i).execute(elapsed);
	}

	public void render(GL11 gl, FreebloksRenderer renderer) {
		gl.glLoadIdentity();

		backgroundRenderer.render(gl);

		/* Kamera positionieren */
		if (model.verticalLayout) {
			gl.glTranslatef(0, 4.5f, 0);
		} else {
			gl.glTranslatef(0, 1.5f, 0);
		}
		GLU.gluLookAt(gl,
			0, (model.verticalLayout ? 4 : 1), renderer.fixed_zoom * 0.9f,
			0, 0, 0,
			0, 1, 0);

		/* Kamera drehen, evtl. durch Matrix move */
		gl.glRotatef(50, 1, 0, 0);

		final float winkel1 = 180.0f;
		final float winkel2 = -60.0f;
		final float matrix_anim=(float)(Math.sin((anim - MATRIX_START)/(MATRIX_STOP-MATRIX_START)*Math.PI-Math.PI/2.0)/2.0+0.5);
		if (anim < MATRIX_START)
		{
			gl.glRotatef(winkel2, 1, 0, 0);
			gl.glRotatef(winkel1, 0, 1, 0);
		}
		else if (anim<MATRIX_STOP)
		{
			gl.glRotatef(winkel2-(matrix_anim*matrix_anim)*winkel2,1,0,0);
			gl.glRotatef(winkel1-matrix_anim*winkel1,0,1,0);
		}

		if (anim < MATRIX_START)
			gl.glTranslatef(0.0f, -14.0f+(anim/MATRIX_START)*4.0f, 0.0f);
		else if (anim<MATRIX_STOP) {
	 		gl.glTranslatef(0, -10+10*(matrix_anim*matrix_anim), 0);
		}

		/* Licht setzen der neuen Kameraposition anpassen*/
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, renderer.light0_pos, 0);

		/* Umgebung und Feld rendern. */
		gl.glPushMatrix();
		if (field_anim > 0.0001f) {
			gl.glTranslatef(0,0,20*BoardRenderer.stone_size);
			gl.glRotatef(field_anim*WIPE_ANGLE,1,0,0);
			gl.glTranslatef(0,0,-20*BoardRenderer.stone_size);
		}
		gl.glDisable(GL10.GL_DEPTH_TEST);
		renderer.board.renderBoard(gl, null, -1);
		gl.glDisable(GL10.GL_DEPTH_TEST);

		gl.glPopMatrix();
		/* Alle Steine rendern. */
		synchronized(effects) {
			for (int i = 0; i < effects.size(); i++)
				effects.get(i).renderShadow(gl, renderer.board);
			gl.glEnable(GL10.GL_DEPTH_TEST);
			for (int i = 0; i < effects.size(); i++)
				effects.get(i).render(gl, renderer.board);
		}
	}
}
