package de.saschahlusiak.freebloks.donate;

import java.util.ArrayList;
import java.util.List;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.billing.IabException;
import de.saschahlusiak.freebloks.billing.IabHelper;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class DonateActivity extends Activity implements OnItemClickListener, OnIabSetupFinishedListener, OnIabPurchaseFinishedListener {
	ListView list;
	DonateAdapter adapter;
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


	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		SkuDetails detail = adapter.getItem(position);
		mHelper.launchPurchaseFlow(this, detail.getSku(), RC_REQUEST, 
                this, "abcdef");
	}

	@Override
	public void onIabSetupFinished(IabResult result) {
		if (!result.isSuccess()) {
			Log.d(tag, "Problem setting up In-app Billing: " + result);
		} else {
			List<String> more = new ArrayList<String>();
			more.add("donation_001");
			more.add("donation_002");
			more.add("donation_003");
			Inventory inventory;
			try {
				inventory = mHelper.queryInventory(true, more);
			} catch (IabException e) {
				e.printStackTrace();
				return;
			}
			ArrayList<SkuDetails> items = new ArrayList<SkuDetails>();
			for (int i = 0; i < more.size(); i++) {
				SkuDetails details = inventory.getSkuDetails(more.get(i));
				if (details != null)
					items.add(details);
			}
			
			list = (ListView)findViewById(R.id.list);
			adapter = new DonateAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, items);
			list.setAdapter(adapter);
			list.setOnItemClickListener(this);			
		}
	}

	@Override
	public void onIabPurchaseFinished(IabResult result, Purchase info) {
		Log.d(tag, "Purchase finished: " + result + ", purchase: " + info);
	}
}
