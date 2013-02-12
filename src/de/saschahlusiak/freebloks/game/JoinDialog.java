package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.R;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class JoinDialog extends Dialog {
	
	public interface OnJoinListener {
		public boolean OnJoin(String server, boolean request_player);
	}

	public JoinDialog(Context context, final OnJoinListener listener) {
		super(context);
		setContentView(R.layout.join_game_dialog);
		
		setTitle(R.string.join_game);
		((EditText)findViewById(R.id.server))
				.setText("blokus.mooo.com");
		Button okAdd = (Button)findViewById(android.R.id.button1);
		okAdd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String n = ((EditText)findViewById(R.id.server))
						.getText().toString();
				boolean request_player = ((CheckBox)findViewById(R.id.request_player)).isChecked();
				
				if (listener.OnJoin(n, request_player))
					dismiss();
			}
		});
		((Button)findViewById(android.R.id.button2)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
		});
	}
}
