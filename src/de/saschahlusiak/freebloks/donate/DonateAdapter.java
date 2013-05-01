package de.saschahlusiak.freebloks.donate;

import java.util.List;

import de.saschahlusiak.freebloks.billing.SkuDetails;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DonateAdapter extends ArrayAdapter<SkuDetails> {

	public DonateAdapter(Context context, int resource, int textViewResourceId,
			List<SkuDetails> objects) {
		super(context, resource, textViewResourceId, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		SkuDetails item = getItem(position);
		((TextView)v.findViewById(android.R.id.text1)).setText(item.getPrice());
		return v;
	}
}
