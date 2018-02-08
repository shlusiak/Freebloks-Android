package de.saschahlusiak.freebloks.game;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import de.saschahlusiak.freebloks.R;

public class ConfigureStonesDialog extends Dialog implements View.OnClickListener {

	public ConfigureStonesDialog(Context context) {
		super(context, R.style.Theme_Freebloks_Light_Dialog);

		setTitle(R.string.configure_stones_dialog_title);
		setContentView(R.layout.configure_stones_dialog);

		findViewById(R.id.ok).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId())
		{
			case R.id.ok:
				dismiss();
				break;

			case R.id.cancel:
				dismiss();
				break;
		}
	}
}
