package de.saschahlusiak.freebloks.donate;

import com.google.firebase.analytics.FirebaseAnalytics;
import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class DonateActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	    setContentView(R.layout.donate_activity);
	    
		if (getActionBar() != null)
			getActionBar().setDisplayHomeAsUpEnabled(true);

		((RadioButton)findViewById(R.id.donation_freebloksvip)).setChecked(true);

		findViewById(R.id.next).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNextButtonPress();
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	void onNextButtonPress() {
		RadioGroup group = (RadioGroup)findViewById(R.id.radioGroup);
	//	RadioButton button = (RadioButton)findViewById(group.getCheckedRadioButtonId());

		if (group.getCheckedRadioButtonId() == R.id.donation_freebloksvip) {
			FirebaseAnalytics.getInstance(this).logEvent("view_donate", null);

			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Global.getMarketURLString("de.saschahlusiak.freebloksvip")));
			startActivity(intent);
			finish();
			return;
		}
		finish();
	}
}
