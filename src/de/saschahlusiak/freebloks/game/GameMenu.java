package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.R;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class GameMenu extends Dialog {

	public GameMenu(Context context, boolean canresume) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);		
		setContentView(R.layout.game_menu_dialog);
		
		try {
			PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			((TextView)findViewById(R.id.version)).setText("" + pinfo.versionName);
		} catch (NameNotFoundException e) {
			((TextView)findViewById(R.id.version)).setVisibility(View.GONE);
			e.printStackTrace();
		}
		
		if (canresume) {
			setCanceledOnTouchOutside(true);
			findViewById(R.id.resume_game).setEnabled(true);
		} else {
			findViewById(R.id.resume_game).setEnabled(false);
		}
	}
}
