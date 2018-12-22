package de.saschahlusiak.freebloks.game;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;

public class JoinDialog extends Dialog implements RadioGroup.OnCheckedChangeListener, View.OnClickListener, TextWatcher {
	private EditText name, server;
	private OnStartCustomGameListener listener;
	private RadioGroup serverType;
	private Button hostGame;

	private SharedPreferences prefs;

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		updateOkButtonEnabled();
	}

	@Override
	public void afterTextChanged(Editable s) {

	}

	private void updateOkButtonEnabled() {
		boolean enabled = true;
		if (serverType.getCheckedRadioButtonId() == R.id.radioButton2 && getCustomServer().isEmpty())
			enabled = false;

		findViewById(android.R.id.button1).setEnabled(enabled);
	}

	public interface OnStartCustomGameListener {
		void onJoinGame(String name, String server);
		void onHostGame(String name);
	}

	public JoinDialog(Context context, final OnStartCustomGameListener listener) {
		super(context, R.style.Theme_Freebloks_Light_Dialog);

		prefs = PreferenceManager.getDefaultSharedPreferences(context);

		this.listener = listener;

		setContentView(R.layout.join_dialog);

		setTitle(R.string.join_multiplayer_game);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		findViewById(android.R.id.closeButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		findViewById(android.R.id.button1).setOnClickListener(this);
		hostGame = findViewById(R.id.host_game);
		hostGame.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveSettings();
				dismiss();
				listener.onHostGame(getName());
			}
		});

		name = findViewById(R.id.name);
		server = findViewById(R.id.server_address);
		server.setText(prefs.getString("custom_server", ""));
		server.addTextChangedListener(this);

		serverType = findViewById(R.id.server_type);
		serverType.setOnCheckedChangeListener(this);

		((RadioButton)findViewById(R.id.radioButton1)).setChecked(true);
	}

	@Override
	public void onClick(View view) {
		switch (serverType.getCheckedRadioButtonId())
		{
			case R.id.radioButton1:
				listener.onJoinGame(getName(), Global.DEFAULT_SERVER_ADDRESS);
				break;
			case R.id.radioButton2:
				if (getCustomServer().isEmpty())
					return;

				listener.onJoinGame(getName(), getCustomServer());
				break;
		}
		saveSettings();
		dismiss();
	}

	private void saveSettings() {
		Editor editor = prefs.edit();
		editor.putString("player_name", getName());
		editor.putString("custom_server", getCustomServer());
		editor.apply();
	}

	public void setName(String name) {
		this.name.setText(name);
	}

	private String getName() {
		return name.getText().toString().trim();
	}

	private String getCustomServer() {
		return server.getText().toString().trim();
	}

	@Override
	public void onCheckedChanged(RadioGroup radioGroup, int i) {
		server.setVisibility(i == R.id.radioButton2 ? View.VISIBLE : View.GONE);
		hostGame.setVisibility(i == R.id.radioButton2 ? View.VISIBLE : View.INVISIBLE);

		if (i == R.id.radioButton2) {
			server.requestFocus();
		}
		updateOkButtonEnabled();
	}
}
