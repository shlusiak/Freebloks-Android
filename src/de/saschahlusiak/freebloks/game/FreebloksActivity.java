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
import de.saschahlusiak.freebloks.view.ViewInterface;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

public class FreebloksActivity extends Activity implements ActivityInterface {
	static final String tag = FreebloksActivity.class.getSimpleName();

	static final int DIALOG_JOIN = 1;
	static final int DIALOG_LOBBY = 2;
	static final int DIALOG_STONE_SELECT = 3;

	ViewInterface view;
	SpielClient spiel = null;
	Stone currentStone = null;
	SpielClientThread spielthread = null;
	
	class ConnectTask extends AsyncTask<String,Void,String> {
		ProgressDialog progress;
		SpielClient mySpiel = null;
		
		ConnectTask(boolean request_player) {
			mySpiel = new SpielClient();
			spielthread = new SpielClientThread(mySpiel, request_player);
		}
		
		@Override
		protected void onPreExecute() {
			view.setSpiel(null);
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

		view = (ViewInterface)findViewById(R.id.board);
		view.setActivity(this);
		

		spielthread = (SpielClientThread)getLastNonConfigurationInstance();
		if (spielthread != null) {
			spielthread.setView(this,  view);
			spiel = spielthread.spiel;
		} else
			showDialog(DIALOG_JOIN);
		view.setSpiel(spiel);
		
		findViewById(R.id.rotateRight).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentStone == null)
					return;
				int r = currentStone.get_rotate_counter();
				r--;
				if (r < 0)
					r = currentStone.get_rotateable() - 1;
				currentStone.mirror_rotate_to(currentStone.get_mirror_counter(), r);
				view.updateView();

//				stoneGalleryAdapter.notifyDataSetChanged();
//				Animation a = new RotateAnimation(90, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//				a.setDuration(250);
//				a.setInterpolator(new OvershootInterpolator());
//				stoneGallery.getSelectedView().startAnimation(a);
			}
		});
		findViewById(R.id.rotateLeft).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentStone == null)
					return;
				int r = currentStone.get_rotate_counter();
				r++;
				if (r >= currentStone.get_rotateable())
					r = 0;
				currentStone.mirror_rotate_to(currentStone.get_mirror_counter(), r);
				view.updateView();
//				stoneGalleryAdapter.notifyDataSetChanged();
//				Animation a = new RotateAnimation(-90, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//				a.setDuration(250);
//				a.setInterpolator(new OvershootInterpolator());
//				stoneGallery.getSelectedView().startAnimation(a);
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
			((EditText)addDialog.findViewById(R.id.server)).setText("blokus.mooo.com");
			Button okAdd = (Button) addDialog.findViewById(android.R.id.button1);
			okAdd.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String n = ((EditText)addDialog.findViewById(R.id.server)).getText().toString();
					boolean request_player = ((CheckBox)addDialog.findViewById(R.id.request_player)).isChecked();
					if (spielthread != null && spielthread.spiel != null)
						spielthread.spiel.disconnect();

					new ConnectTask(request_player).execute(n);
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
		case DIALOG_STONE_SELECT:
			return new StoneSelectDialog(this, new StoneSelectDialog.OnStoneSelectListener() {
				@Override
				public void onClick(DialogInterface dialog, Stone stone) {
					currentStone = stone;
					view.setCurrentStone(stone);
					view.updateView();
				}
			}, spiel.get_current_player());
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

	@Override
	public void OnShowStoneSelect() {
		showDialog(DIALOG_STONE_SELECT);
	}

}