package de.saschahlusiak.freebloks;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.about_activity);
		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) !=
			        Configuration.SCREENLAYOUT_SIZE_XLARGE)
		{
			LayoutParams params = getWindow().getAttributes();
			params.width = LayoutParams.MATCH_PARENT;
			getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
		}

		findViewById(R.id.ok).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		PackageInfo pinfo;
		try {
			pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			((TextView)findViewById(R.id.version)).setText("v" + pinfo.versionName);
		} catch (NameNotFoundException e) {
			findViewById(R.id.version).setVisibility(View.GONE);
			e.printStackTrace();
		}
		((TextView)findViewById(R.id.url1)).setText(Global.getMarketURLString(getPackageName()));
	}
}
