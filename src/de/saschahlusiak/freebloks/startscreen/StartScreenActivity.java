package de.saschahlusiak.freebloks.startscreen;

import de.saschahlusiak.freebloks.AboutActivity;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.game.FreebloksActivity;
import de.saschahlusiak.freebloks.preferences.FreebloksPreferences;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

public class StartScreenActivity extends Activity {
	private static final int DIALOG_JOIN = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.start_screen);
		
		PackageInfo pinfo;
		try {
			pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			((TextView)findViewById(R.id.version)).setText("version: " + pinfo.versionName);
		} catch (NameNotFoundException e) {
			((TextView)findViewById(R.id.version)).setVisibility(View.GONE);
			e.printStackTrace();
		}
				
		findViewById(R.id.new_game).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(StartScreenActivity.this, FreebloksActivity.class);
				/* request_player:
				 *   false for AUTO PLAY
				 *   true for requesting 1 player
				 */
				intent.putExtra("request_player", true);
				startActivity(intent);
			}
		});
		findViewById(R.id.join_game).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_JOIN);
			}
		});
		findViewById(R.id.preferences).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(StartScreenActivity.this, FreebloksPreferences.class);
				startActivity(intent);
			}
		});
	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_JOIN:
			final Dialog addDialog = new Dialog(this);
			addDialog.setContentView(R.layout.join_game_dialog);
			addDialog.getWindow().setLayout(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);
			addDialog.setTitle(R.string.join_game);
			((EditText) addDialog.findViewById(R.id.server))
					.setText("blokus.mooo.com");
			Button okAdd = (Button) addDialog
					.findViewById(android.R.id.button1);
			okAdd.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String n = ((EditText) addDialog.findViewById(R.id.server))
							.getText().toString();
					boolean request_player = ((CheckBox) addDialog
							.findViewById(R.id.request_player)).isChecked();
					addDialog.dismiss();
					
					Intent intent = new Intent(StartScreenActivity.this, FreebloksActivity.class);
					intent.putExtra("server", n);
					intent.putExtra("request_player", request_player);
					startActivity(intent);
				}
			});
			((Button) addDialog.findViewById(android.R.id.button2))
					.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							addDialog.dismiss();
							// if (view.getSpiel() == null)
							// finish();
						}
					});
			return addDialog;
		}
		return super.onCreateDialog(id);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.startscreen_optionsmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.about:
			intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
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
