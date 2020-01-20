package de.saschahlusiak.freebloks;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.about_activity);

		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) !=
			        Configuration.SCREENLAYOUT_SIZE_XLARGE)
		{
			WindowManager.LayoutParams params = getWindow().getAttributes();
			params.width = LayoutParams.MATCH_PARENT;
			getWindow().setAttributes(params);
		}

		findViewById(R.id.ok).setOnClickListener(v -> finish());
		((TextView)findViewById(R.id.version)).setText("v" + BuildConfig.VERSION_NAME);
		((TextView)findViewById(R.id.url1)).setText(Global.getMarketURLString(getPackageName()));
	}
}
