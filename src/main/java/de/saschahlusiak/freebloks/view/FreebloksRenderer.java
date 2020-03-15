package de.saschahlusiak.freebloks.view;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.res.AssetManager;
import android.util.Log;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.model.Board;
import de.saschahlusiak.freebloks.model.Game;
import de.saschahlusiak.freebloks.model.GameMode;
import de.saschahlusiak.freebloks.theme.ColorThemes;
import de.saschahlusiak.freebloks.view.scene.Scene;
import nl.weeaboo.jktx.KTXFile;
import nl.weeaboo.jktx.KTXFormatException;
import nl.weeaboo.jktx.KTXHeader;
import nl.weeaboo.jktx.KTXTextureData;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import de.saschahlusiak.freebloks.view.scene.intro.Intro;

public class FreebloksRenderer implements GLSurfaceView.Renderer {
	private static final String tag = FreebloksRenderer.class.getSimpleName();

	private final float[] light0_ambient = {0.35f, 0.35f, 0.35f, 1.0f};
	private final float[] light0_diffuse = {0.8f, 0.8f, 0.8f, 1.0f};
	private final float[] light0_specular = {1.0f, 1.0f, 1.0f, 1.0f};
	public final float[] light0_pos = {2.5f, 5f, -2.0f, 0.0f};
	public float width = 1, height = 1;
	private Scene model;
	private Context context;
	public float fixed_zoom;
	private float mAngleX;
	float zoom;

	private int[] viewport = new int[4];
	private float[] projectionMatrix = new float[16];
	private float[] modelViewMatrix = new float[16];
	public static boolean isSoftwareRenderer, isEmulator;

	public BoardRenderer boardRenderer;
	BackgroundRenderer backgroundRenderer;

	public FreebloksRenderer(Context context, Scene model) {
		this.context = context;
		this.model = model;
		mAngleX = 70.0f;
		boardRenderer = new BoardRenderer();
		backgroundRenderer = new BackgroundRenderer(context.getResources(), ColorThemes.Blue);
	}

	public void init(int field_size) {
		boardRenderer.setFieldSize(field_size);
	}

	private final float[] outputfar = new float[4];
	private float[] outputnear = new float[4];

	public PointF windowToModel(PointF point) {
		float x1, y1, z1, x2, y2, z2, u;

		synchronized (outputfar) {
			GLU.gluUnProject(point.x, viewport[3] - point.y, 0.0f, modelViewMatrix, 0, projectionMatrix, 0, viewport, 0, outputnear, 0);
			GLU.gluUnProject(point.x, viewport[3] - point.y, 1.0f, modelViewMatrix, 0, projectionMatrix, 0, viewport, 0, outputfar, 0);
		}
//		Log.d("windowToModel", "(" + point.x + "/" + point.y + ")  => far  (" + outputfar[0] + "/" + outputfar[1] + "/" + outputfar[2] + "/" + outputfar[3] + ")");
//		Log.d("windowToModel", "(" + point.x + "/" + point.y + ")  => near (" + outputnear[0] + "/" + outputnear[1] + "/" + outputnear[2] + "/" + outputnear[3] + ")");

		x1 = (outputfar[0] / outputfar[3]);
		y1 = (outputfar[1] / outputfar[3]);
		z1 = (outputfar[2] / outputfar[3]);
		x2 = (outputnear[0] / outputnear[3]);
		y2 = (outputnear[1] / outputnear[3]);
		z2 = (outputnear[2] / outputnear[3]);
		u = (0.0f - y1) / (y2 - y1);

		point.x = x1 + u * (x2 - x1);
		point.y = z1 + u * (z2 - z1);
		return point;
	}

	boolean updateModelViewMatrix = true;

	public synchronized void onDrawFrame(GL10 gl) {
		GL11 gl11 = (GL11) gl;
		final float camera_distance = zoom;
//		long t = System.currentTimeMillis();
		float cameraAngle = model.boardObject.getCameraAngle();
		float boardAngle = model.boardObject.mAngleY;

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		final Intro intro = model.getIntro();
		if (intro != null) {
			intro.render(gl11, this);
			return;
		}

		gl.glLoadIdentity();
		if (model.verticalLayout) {
			gl.glTranslatef(0, 7.0f, 0);
		} else
			gl.glTranslatef(-5.0f, 0.6f, 0);

		GLU.gluLookAt(gl,
			(float) (fixed_zoom / camera_distance * Math.sin(cameraAngle * Math.PI / 180.0) * Math.cos(mAngleX * Math.PI / 180.0)),
			(float) (fixed_zoom / camera_distance * Math.sin(mAngleX * Math.PI / 180.0)),
			(float) (fixed_zoom / camera_distance * Math.cos(mAngleX * Math.PI / 180.0) * Math.cos(-cameraAngle * Math.PI / 180.0)),
			0.0f, 0.0f, 0.0f,
			0.0f, 1.0f, 0.0f);
		if (updateModelViewMatrix) synchronized (outputfar) {
//				Log.w("onDrawFrame", "updating modelViewMatrix");
			if (isSoftwareRenderer) {
				/* FIXME: add path for software renderer */
			} else {
				gl11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelViewMatrix, 0);
			}
			updateModelViewMatrix = false;
		}

		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, light0_pos, 0);

		/* render board */
		gl.glDisable(GL10.GL_DEPTH_TEST);

		gl.glRotatef(boardAngle, 0, 1, 0);
		backgroundRenderer.render(gl11);
		boardRenderer.renderBoard(gl11, model.board, model.boardObject.getShowSeedsPlayer());

		final Game game = model.game;
		if (game == null)
			return;

		final GameMode gameMode = game.getGameMode();
		final Board board = game.getBoard();

		/* render player stones on board, unless they are "effected" */
		gl.glPushMatrix();
		gl.glTranslatef(-BoardRenderer.stoneSize * (float) (model.board.width - 1), 0, -BoardRenderer.stoneSize * (float) (model.board.width - 1));
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);

		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, BoardRenderer.materialStoneSpecular, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS,BoardRenderer.materialStoneShininess, 0);

		boardRenderer.stone.bindBuffers(gl11);

		synchronized (model.effects) {
			for (int y = 0; y < board.height; y++) {
				int x;
				for (x = 0; x < board.width; x++) {
					int field = board.getFieldPlayer(y, x);
					if (field != Board.FIELD_FREE) {
						boolean effected = false;
						for (int i = 0; i < model.effects.size(); i++)
							if (model.effects.get(i).isEffected(x, y)) {
								effected = true;
								break;
							}
						if (!effected)
							boardRenderer.renderSingleStone(gl11, Global.getPlayerColor(field, gameMode), BoardRenderer.defaultStoneAlpha);
					}
					gl.glTranslatef(BoardRenderer.stoneSize * 2.0f, 0, 0);
				}
				gl.glTranslatef(-x * BoardRenderer.stoneSize * 2.0f, 0, BoardRenderer.stoneSize * 2.0f);
			}
		}

		gl.glDisable(GL10.GL_BLEND);
		gl.glPopMatrix();
		gl.glDisable(GL10.GL_DEPTH_TEST);

		/* render all effects */
		synchronized (model.effects) {
			for (int i = 0; i < model.effects.size(); i++) {
				model.effects.get(i).renderShadow(gl11, boardRenderer);
			}

			gl.glEnable(GL10.GL_DEPTH_TEST);

			for (int i = 0; i < model.effects.size(); i++) {
				model.effects.get(i).render(gl11, boardRenderer);
			}
		}
		gl.glDisable(GL10.GL_DEPTH_TEST);

		gl.glRotatef(-boardAngle, 0, 1, 0);

		gl.glPushMatrix();
		/* reverse the cameraAngle to always fix wheel in front of camera */
		gl.glRotatef(cameraAngle, 0, 1, 0);
		if (!model.verticalLayout)
			gl.glRotatef(90.0f, 0, 1, 0);
		model.wheel.render(this, gl11);
		gl.glPopMatrix();

		/* render current player stone on the field */
		if (game.isLocalPlayer())
			model.currentStone.render(this, gl11);

//		Log.d("Renderer", "render took " + (System.currentTimeMillis() - t) + " ms");
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GL11 gl11 = (GL11) gl;
		Log.d(tag, "onSurfaceChanged: " + width + ", " + height);

		gl.glViewport(0, 0, width, height);
		viewport[0] = 0;
		viewport[1] = 0;
		viewport[2] = width;
		viewport[3] = height;


		this.width = (float) width;
		this.height = (float) height;
		model.verticalLayout = (height >= width);

		float fovy;
		if (model.verticalLayout) {
			fovy = 35.0f;
		} else {
			fovy = 21.0f;
		}
		fixed_zoom = 55.0f;

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		GLU.gluPerspective(gl, fovy, this.width / this.height, 1.0f, 300.0f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);

		synchronized (outputfar) {
			if (isSoftwareRenderer) {
				/* FIXME: add path for software renderer */
			} else {
				gl11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, projectionMatrix, 0);
			}
		}
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		String renderer = gl.glGetString(GL10.GL_RENDERER);
		isEmulator = renderer.contains("Android Emulator OpenGL");
		isSoftwareRenderer = renderer.contains("PixelFlinger") || isEmulator;
		Log.i(tag, "Renderer: " + renderer);

		gl.glDisable(GL10.GL_DITHER);

		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_NORMALIZE);

		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, light0_pos, 0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, light0_ambient, 0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, light0_diffuse, 0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, light0_specular, 0);

		updateModelViewMatrix = true;
		model.currentStone.updateTexture(context, gl);
		boardRenderer.onSurfaceChanged(context, (GL11) gl);
		backgroundRenderer.updateTexture(gl);
	}

	private static void loadKTXTexture(GL10 gl, KTXFile file) {
		KTXHeader header = file.getHeader();
		KTXTextureData data = file.getTextureData();

		for (int level = 0; level < data.getNumberOfMipmapLevels(); level++) {
			gl.glCompressedTexImage2D(GL10.GL_TEXTURE_2D,
				level,
				header.getGLInternalFormat(),
				header.getPixelWidth(level),
				header.getPixelHeight(level),
				0,
				data.getBytesPerFace(level),
				data.getFace(level, 0));
		}
	}

	public static void loadKTXTexture(GL10 gl, Resources resources, String asset) {
		if (isEmulator)
			return;

		try {
			final InputStream input = resources.getAssets().open(asset, AssetManager.ACCESS_STREAMING);
			KTXFile file = new KTXFile();
			file.read(input);
			input.close();

			loadKTXTexture(gl, file);
		} catch (IOException | KTXFormatException e) {
			throw new RuntimeException(e);
		}
	}
}