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

	public GameMenu(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);		
		setContentView(R.layout.game_menu_dialog);
		
		try {
			PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			((TextView)findViewById(R.id.version)).setText("Freebloks v" + pinfo.versionName);
		} catch (NameNotFoundException e) {
			((TextView)findViewById(R.id.version)).setVisibility(View.GONE);
			e.printStackTrace();
		}
		
		setCanceledOnTouchOutside(true);
	}
	
	@Override
	public void onBackPressed() {
		getOwnerActivity().finish();
	}
}
