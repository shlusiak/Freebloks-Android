package de.saschahlusiak.freebloks;

import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.model.Ki;
import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.NET_CHAT;
import de.saschahlusiak.freebloks.network.Network;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class FreebloksActivity extends Activity {
	static final String tag = FreebloksActivity.class.getSimpleName();
	
	class KIClient extends SpielClient {
		Ki ki = new Ki();

		KIClient() {
			super();
		}
		
		public void gameStarted()
		{
			int i;
			Log.d(tag, "Game started");
			for (i=0;i<PLAYER_MAX;i++) if (is_local_player(i)) Log.d(tag, "Local player: " + i);
		}

		public void newCurrentPlayer(int player)
		{
			if (!is_local_player())return;

			/* Ermittle CTurn, den die KI jetzt setzen wuerde */
			Turn turn = ki.get_ki_turn(this, current_player(), 90);
			Stone stone;
			if (turn == null)
			{
				Log.e(tag, "Player " + player + ": Did not find a valid move");
				return;
			}
			stone = get_current_player().get_stone(turn.m_stone_number);
			stone.mirror_rotate_to(turn.m_mirror_count, turn.m_rotate_count);
			set_stone(stone, turn.m_stone_number, turn.m_y, turn.m_x);
		}

		public void chatReceived(final NET_CHAT c)
		{
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					if (c.client == -1)
						Toast.makeText(FreebloksActivity.this, "* " + c.text, Toast.LENGTH_LONG).show();
					else 
						Toast.makeText(FreebloksActivity.this, "Client " + c.client + ": " + c.text, Toast.LENGTH_LONG).show();					
				}
			});
		}

		public void gameFinished()
		{
			int i;
			Log.i(tag, "-- Game finished! --");
			for (i=0;i<PLAYER_MAX;i++)
			{
				Player player = get_player(i);
				Log.i(tag, (is_local_player(i) ? "*" : " ") + "Player " + i +" has " + player.m_stone_count + " stones left and " + -player.m_stone_points_left + " points.");
			}

			disconnect();
		}
	}
	
	class KIThread extends Thread {
		KIClient client;
		
		KIThread(KIClient client) {
			this.client = client;
			try {
				client.connect("192.168.1.123", Network.DEFAULT_PORT);
			} catch (Exception e) {
				Toast.makeText(FreebloksActivity.this, e.getMessage(), Toast.LENGTH_LONG);
			}
		}
		
		@Override
		public void run() {
			client.request_player();
			client.request_player();
			client.request_player();
			client.request_player();

			client.request_start();
			
			do {
				if (!client.poll())
					break;
			} while (client.isConnected());
			client.disconnect();
			Log.i("KIThread", "thread going down");
		}		
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        
        new KIThread(new KIClient()).start();
    }
}