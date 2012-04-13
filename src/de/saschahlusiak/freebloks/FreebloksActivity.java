package de.saschahlusiak.freebloks;

import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.controller.SpielClientInterface;
import de.saschahlusiak.freebloks.controller.Spielleiter;
import de.saschahlusiak.freebloks.model.Ki;
import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.NET_CHAT;
import de.saschahlusiak.freebloks.network.NET_SET_STONE;
import de.saschahlusiak.freebloks.network.Network;
import de.saschahlusiak.freebloks.view.SimpleFreebloksView;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

public class FreebloksActivity extends Activity  {
	static final String tag = FreebloksActivity.class.getSimpleName();


	SimpleFreebloksView view;

	class KIThread extends Thread implements SpielClientInterface{
		SpielClient spiel;
		Ki ki = new Ki();
		
		KIThread(String server) {
			try {
				spiel = new SpielClient(this);
				view.setSpiel(spiel);
				spiel.connect(server, Network.DEFAULT_PORT);
			} catch (Exception e) {
				Toast.makeText(FreebloksActivity.this, e.getMessage(),
						Toast.LENGTH_LONG);
			}
		}

		@Override
		public void run() {
//			spiel.request_player();
//			spiel.request_player();
//			spiel.request_player();
//			spiel.request_player();

			spiel.request_start();

			do {
				if (!spiel.poll(true))
					break;
			} while (spiel.isConnected());
			spiel.disconnect();
			Log.i("KIThread", "thread going down");
		}
		
		public void gameStarted() {
			int i;
			Log.d(tag, "Game started");
			for (i = 0; i < Spiel.PLAYER_MAX; i++)
				if (spiel.is_local_player(i))
					Log.d(tag, "Local player: " + i);
		}

		public void newCurrentPlayer(int player) {
			if (!spiel.is_local_player())
				return;

			/* Ermittle CTurn, den die KI jetzt setzen wuerde */
			Turn turn = ki.get_ki_turn(spiel, spiel.current_player(), 5);
			Stone stone;
			if (turn == null) {
				Log.e(tag, "Player " + player + ": Did not find a valid move");
				return;
			}
			stone = spiel.get_current_player().get_stone(turn.m_stone_number);
			stone.mirror_rotate_to(turn.m_mirror_count, turn.m_rotate_count);
			spiel.set_stone(stone, turn.m_stone_number, turn.m_y, turn.m_x);
		}

		public void chatReceived(final NET_CHAT c) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (c.client == -1)
						Toast.makeText(FreebloksActivity.this, "* " + c.text,
								Toast.LENGTH_LONG).show();
					else
						Toast.makeText(FreebloksActivity.this,
								"Client " + c.client + ": " + c.text,
								Toast.LENGTH_LONG).show();
				}
			});
		}

		public void gameFinished() {
			int i;
			Log.i(tag, "-- Game finished! --");
			for (i = 0; i < Spiel.PLAYER_MAX; i++) {
				Player player = spiel.get_player(i);
				Log.i(tag, (spiel.is_local_player(i) ? "*" : " ") + "Player " + i
						+ " has " + player.m_stone_count + " stones left and "
						+ -player.m_stone_points_left + " points.");
			}

			spiel.disconnect();
		}

		@Override
		public void stoneWasSet(NET_SET_STONE s) {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					view.invalidate();			
				}
			});
		}

		@Override
		public void hintReceived(NET_SET_STONE s) {
			// TODO Auto-generated method stub

		}

		@Override
		public void stoneUndone(Stone s, Turn t) {
			// TODO Auto-generated method stub

		}		
	}
	
	KIThread kithread = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		view = (SimpleFreebloksView)findViewById(R.id.board);
		view.setSpiel(new Spielleiter(Spiel.DEFAULT_FIELD_SIZE_Y, Spiel.DEFAULT_FIELD_SIZE_X));
	}

	@Override
	protected void onDestroy() {
		if (kithread != null && kithread.spiel != null)
			kithread.spiel.disconnect();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.optionsmenu, menu);
		return true;
	}
	
	
	void showJoinGameDialog() {
		final Dialog addDialog = new Dialog(this);
		addDialog.setContentView(R.layout.join_game_dialog);
		addDialog.getWindow().setLayout(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		addDialog.setTitle(R.string.menu_join_game);
		Button okAdd = (Button) addDialog.findViewById(android.R.id.button1);
		okAdd.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String n = ((EditText)addDialog.findViewById(R.id.server)).getText().toString();
				if (kithread != null && kithread.spiel != null)
					kithread.spiel.disconnect();
				kithread = new KIThread(n);
				kithread.start();
				addDialog.dismiss();
			}
		});
		((Button)addDialog.findViewById(android.R.id.button2)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				addDialog.dismiss();
			}
		});
		addDialog.show();
	}

	@Override
	/** Called when a menu item is selected. */
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.new_local_game:
			return true;
			
		case R.id.join_game:
			showJoinGameDialog();
			return true;

		case R.id.preferences:
			// intent = new Intent(this, WordMixPreferencesActivity.class);
			// startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}