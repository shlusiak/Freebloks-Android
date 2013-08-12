package de.saschahlusiak.freebloks.donate;

import java.util.ArrayList;
import java.util.List;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.billing.IabException;
import de.saschahlusiak.freebloks.billing.IabHelper;
import de.saschahlusiak.freebloks.billing.IabHelper.OnConsumeFinishedListener;
import de.saschahlusiak.freebloks.billing.IabHelper.OnIabPurchaseFinishedListener;
import de.saschahlusiak.freebloks.billing.IabHelper.OnIabSetupFinishedListener;
import de.saschahlusiak.freebloks.billing.IabResult;
import de.saschahlusiak.freebloks.billing.Inventory;
import de.saschahlusiak.freebloks.billing.Purchase;
import de.saschahlusiak.freebloks.billing.SkuDetails;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class DonateActivity extends Activity implements OnIabSetupFinishedListener, OnIabPurchaseFinishedListener, OnConsumeFinishedListener {
	IabHelper mHelper;
	
	private static final String tag = DonateActivity.class.getSimpleName();

    static final int RC_REQUEST = 10001;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.donate_activity);
		
		mHelper = new IabHelper(this, Global.base64EncodedPublicKey);
		mHelper.enableDebugLogging(true);
		
		mHelper.startSetup(this);		
	}
	
	@Override
	protected void onDestroy() {
		if (mHelper != null) mHelper.dispose();
		mHelper = null;
		super.onDestroy();
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

	void setupButton(int id, SkuDetails details) {
		final RadioButton b = (RadioButton)findViewById(id);
		
		if (details == null) {
			b.setText(android.R.string.unknownName);
			b.setEnabled(false);
			return;
		}
		
		b.setText(details.getPrice());
		b.setTag(details);
		b.setEnabled(true);
	}

	@Override
	public void onIabSetupFinished(final IabResult result) {
		findViewById(R.id.next).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RadioGroup group = (RadioGroup)findViewById(R.id.radioGroup);
				RadioButton button = (RadioButton)findViewById(group.getCheckedRadioButtonId());
				if (button == null)
					return;
				if (!button.isEnabled())
					return;
				
				SkuDetails detail = (SkuDetails)button.getTag();
				if (detail == null) {
					finish();
					return;
				}
				mHelper.launchPurchaseFlow(DonateActivity.this, detail.getSku(), RC_REQUEST, 
						DonateActivity.this, "abcdef");
			}
		});

		if (!result.isSuccess()) {
			Log.d(tag, "Problem setting up In-app Billing: " + result);
			Toast.makeText(this, "no internet connection", Toast.LENGTH_LONG).show();
		} else {
			List<String> more = new ArrayList<String>();
			more.add("donation_001");
			more.add("donation_002");
			more.add("donation_003");
			more.add("donation_004");
			Inventory inventory;
			try {
				inventory = mHelper.queryInventory(true, more);
			} catch (IabException e) {
				e.printStackTrace();
				Toast.makeText(this, "error querying inventory", Toast.LENGTH_LONG).show();
				((Button)findViewById(R.id.next)).setText(R.string.donation_retry);
				findViewById(R.id.next).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						onIabSetupFinished(result);
					}
				});
				return;
			}
			((Button)findViewById(R.id.next)).setText(R.string.donation_next);
			setupButton(R.id.donation_001, inventory.getSkuDetails("donation_001"));
			setupButton(R.id.donation_002, inventory.getSkuDetails("donation_002"));
			setupButton(R.id.donation_003, inventory.getSkuDetails("donation_003"));
			setupButton(R.id.donation_004, inventory.getSkuDetails("donation_004"));
			
			for (String sku: more) {
				if (inventory.hasPurchase(sku))
	                mHelper.consumeAsync(inventory.getPurchase(sku), this);
			}
		}
	}

	@Override
	public void onIabPurchaseFinished(IabResult result, Purchase info) {
		Log.d(tag, "Purchase finished: " + result + ", purchase: " + info);
		if (result.isFailure()) {
            Log.e(tag, "Error purchasing: " + result);
            return;
        }
		/* consume purchase */
        mHelper.consumeAsync(info, this);
        finish();
	}

	@Override
	public void onConsumeFinished(Purchase purchase, IabResult result) {
		Log.d(tag, "Consumption finished. Purchase: " + purchase + ", result: " + result);		
	}
}
