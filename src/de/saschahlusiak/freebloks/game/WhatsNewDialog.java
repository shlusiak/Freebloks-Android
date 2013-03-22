package de.saschahlusiak.freebloks.game;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import de.saschahlusiak.freebloks.R;

public class WhatsNewDialog {
	private static final String WHATS_NEW = 
			"* more natural stone placement\n" +
			"* finer vibration control\n" +
			"* sound effect for placing stone\n" +
			"* support for Blokus Duo\n" +
			"* custom field sizes\n" +
			"* layout updates\n" +
			"* German translation\n" +
			"* minor improvements\n" +
			"* some bugs fixed, others added";


	
	static Dialog create(Context context) {
		PackageInfo pinfo;
		try {
			pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
			
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		View view;
		view = LayoutInflater.from(context).inflate(R.layout.development_warning, null, false);
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
}
