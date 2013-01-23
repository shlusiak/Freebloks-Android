package de.saschahlusiak.freebloks.startscreen;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import de.saschahlusiak.freebloks.AboutActivity;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.game.FreebloksActivity;
import de.saschahlusiak.freebloks.preferences.FreebloksPreferences;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Parcel;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class StartScreenActivity extends Activity {
	private static final int DIALOG_JOIN = 1;
	private static final int DIALOG_DEV = 2;

	PackageInfo pinfo;
	SharedPreferences prefs;
	
	private static final String WHATS_NEW = 
			"* fixed several crashes on resume/rotate";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.start_screen);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		try {
			pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			((TextView)findViewById(R.id.version)).setText("" + pinfo.versionName);
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
		findViewById(R.id.resume_game).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				resumeGame();
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
		
		if (savedInstanceState == null) {
			if (prefs.getInt("lastVersion", 0) != pinfo.versionCode) {
				showDialog(DIALOG_DEV);
				
				Editor editor = prefs.edit();
				editor.putInt("lastVersion", pinfo.versionCode);
				editor.commit();
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		try {
			FileInputStream fis = null;;
			fis = openFileInput(FreebloksActivity.GAME_STATE_FILE);
			if (fis != null)
				fis.close();
			((Button)findViewById(R.id.resume_game)).setEnabled(true);
		} catch (Exception e) {
			((Button)findViewById(R.id.resume_game)).setEnabled(false);
		}
	}
	
	void resumeGame() {
		try {
			FileInputStream fis = openFileInput(FreebloksActivity.GAME_STATE_FILE);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Parcel p = Parcel.obtain();
			byte[] b = new byte[1024];
			int bytesRead;
			while ((bytesRead = fis.read(b)) != -1) {
			   bos.write(b, 0, bytesRead);
			}
			fis.close();
			fis = null;
			
			byte[] bytes = bos.toByteArray();
			bos.close();
			bos = null;
			
			Bundle bundle;
			p.unmarshall(bytes, 0, bytes.length);
			p.setDataPosition(0);
			bundle = p.readBundle(FreebloksActivity.class.getClassLoader());

			Intent intent = new Intent(StartScreenActivity.this, FreebloksActivity.class);
			intent.putExtra("gamestate", bundle);
			startActivity(intent);
			deleteFile(FreebloksActivity.GAME_STATE_FILE);
		} catch (Exception e) {
			e.printStackTrace();
			/* TODO: translate */
			Toast.makeText(this, "Could not restore game", Toast.LENGTH_LONG).show();
		}
	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_JOIN:
			final Dialog addDialog = new Dialog(this);
			addDialog.setContentView(R.layout.join_game_dialog);
			
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
		case DIALOG_DEV:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			View view;
			view = LayoutInflater.from(this).inflate(R.layout.development_warning, null, false);
			builder.setTitle("version " + pinfo.versionName);
			((TextView)(view.findViewById(R.id.whatsnew))).setText(WHATS_NEW);
			builder.setView(view);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();					
				}
			});
			return builder.create();
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
