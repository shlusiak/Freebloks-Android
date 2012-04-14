package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.R.id;
import de.saschahlusiak.freebloks.R.layout;
import de.saschahlusiak.freebloks.R.menu;
import de.saschahlusiak.freebloks.R.string;
import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.controller.SpielClientInterface;
import de.saschahlusiak.freebloks.lobby.LobbyDialog;
import de.saschahlusiak.freebloks.model.Ki;
import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.NET_CHAT;
import de.saschahlusiak.freebloks.network.NET_SERVER_STATUS;
import de.saschahlusiak.freebloks.network.NET_SET_STONE;
import de.saschahlusiak.freebloks.network.Network;
import de.saschahlusiak.freebloks.preferences.FreebloksPreferences;
import de.saschahlusiak.freebloks.view.FreebloksViewInterface;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

public class FreebloksActivity extends Activity  {
	static final String tag = FreebloksActivity.class.getSimpleName();

	static final int DIALOG_JOIN = 1;
	static final int DIALOG_LOBBY = 2;

	FreebloksViewInterface view;
	Gallery stoneGallery;
	StoneGalleryAdapter stoneGalleryAdapter;
	SpielClient spiel = null;
	SpielClientThread spielthread = null;
	
	class ConnectTask extends AsyncTask<String,Void,String> {
		ProgressDialog progress;
		SpielClient mySpiel = null;
		
		@Override
		protected void onPreExecute() {
			view.setSpiel(null);
			mySpiel = new SpielClient();
			spielthread = new SpielClientThread(mySpiel);
			progress = new ProgressDialog(FreebloksActivity.this);
			progress.setMessage("Connecting...");
			progress.setIndeterminate(true);
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setCancelable(true);
			progress.show();
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(String... params) {
			try {
				mySpiel.connect(params[0], Network.DEFAULT_PORT);
			} catch (Exception e) {
				e.printStackTrace();
				return e.getMessage();
			}
			view.setSpiel(mySpiel);
			return null;
		}
		
		@Override
		protected void onCancelled() {
			progress.dismiss();
			super.onCancelled();
		}
		
		@Override
		protected void onPostExecute(String result) {
			spiel = mySpiel;
			progress.dismiss();
			if (result != null) {
				Toast.makeText(FreebloksActivity.this, result, Toast.LENGTH_LONG).show();
			} else {
				showDialog(DIALOG_LOBBY);
			}
			spielthread.setView(FreebloksActivity.this, view);
			spielthread.start();
			super.onPostExecute(result);
		}
		

	}
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		setContentView(prefs.getBoolean("view_opengl", true) ? R.layout.main_3d : R.layout.main);

		view = (FreebloksViewInterface)findViewById(R.id.board);
		stoneGallery = (Gallery)findViewById(R.id.stoneGallery);
		stoneGallery.setScrollbarFadingEnabled(false);
		stoneGalleryAdapter = new StoneGalleryAdapter(this, null);
		stoneGallery.setAdapter(stoneGalleryAdapter);
		spielthread = (SpielClientThread)getLastNonConfigurationInstance();
		if (spielthread != null) {
			spielthread.setView(this,  view);
			spiel = spielthread.spiel;
			stoneGalleryAdapter.setPlayer(spiel.get_current_player());
			stoneGalleryAdapter.notifyDataSetChanged();
			stoneGallery.setSelection(Stone.STONE_COUNT_ALL_SHAPES - 5);
		} else
			showDialog(DIALOG_JOIN);
		view.setSpiel(spiel);
		
		findViewById(R.id.rotateLeft).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Stone stone = (Stone)stoneGallery.getSelectedItem();
				int r = stone.get_rotate_counter();
				r--;
				if (r < 0)
					r = stone.get_rotateable() - 1;
				stone.mirror_rotate_to(stone.get_mirror_counter(), r);
//				stoneGalleryAdapter.notifyDataSetChanged();
				Animation a = new RotateAnimation(90, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				a.setDuration(250);
				a.setInterpolator(new OvershootInterpolator());
				stoneGallery.getSelectedView().startAnimation(a);
			}
		});
		findViewById(R.id.rotateRight).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Stone stone = (Stone)stoneGallery.getSelectedItem();
				int r = stone.get_rotate_counter();
				r++;
				if (r >= stone.get_rotateable())
					r = 0;
				stone.mirror_rotate_to(stone.get_mirror_counter(), r);
//				stoneGalleryAdapter.notifyDataSetChanged();
				Animation a = new RotateAnimation(-90, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				a.setDuration(250);
				a.setInterpolator(new OvershootInterpolator());
				stoneGallery.getSelectedView().startAnimation(a);
			}
		});
	}

	@Override
	protected void onDestroy() {
		if (spielthread != null) try {
			spielthread.spiel.disconnect();
			spielthread.goDown();
			spielthread.join();
			spielthread = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		SpielClientThread t = spielthread;
		if (t != null) {
			spielthread.setView(null, null);
			spielthread = null;
		}
		return t;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.optionsmenu, menu);
		return true;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_JOIN:
			final Dialog addDialog = new Dialog(this);
			addDialog.setContentView(R.layout.join_game_dialog);
			addDialog.getWindow().setLayout(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);
			addDialog.setTitle(R.string.menu_join_game);
			((EditText)addDialog.findViewById(R.id.server)).setText("192.168.2.159");
			Button okAdd = (Button) addDialog.findViewById(android.R.id.button1);
			okAdd.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String n = ((EditText)addDialog.findViewById(R.id.server)).getText().toString();
					if (spielthread != null && spielthread.spiel != null)
						spielthread.spiel.disconnect();

					new ConnectTask().execute(n);
					addDialog.dismiss();
				}
			});
			((Button)addDialog.findViewById(android.R.id.button2)).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					addDialog.dismiss();
//					if (view.getSpiel() == null)
//						finish();						
				}
			});
			return addDialog;
			
		case DIALOG_LOBBY:
			return new LobbyDialog(this, new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface arg0) {
					spiel.disconnect();
				}
			});
		default:
			return super.onCreateDialog(id);
		}
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		switch (id) {
		case DIALOG_LOBBY:
			((LobbyDialog)dialog).setSpiel(spiel);
			break;
		}
		super.onPrepareDialog(id, dialog, args);
	}

	@Override
	/** Called when a menu item is selected. */
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.new_local_game:
			return true;
			
		case R.id.join_game:
			showDialog(DIALOG_JOIN);
			return true;

		case R.id.preferences:
			intent = new Intent(this, FreebloksPreferences.class);
			startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}