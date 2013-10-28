package de.saschahlusiak.freebloks.stats;

import de.saschahlusiak.freebloks.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StatisticsAdapter extends BaseAdapter {
	String labels[];
	String values[];
	Context context;
	
	StatisticsAdapter(Context context, String[] labels, String[] values) {
		this.context = context;
		this.labels = labels;
		this.values = values;
	}
	
	@Override
	public int getCount() {
		return labels.length;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = LayoutInflater.from(context).inflate(R.layout.statistics_item, parent, false);
		((TextView)v.findViewById(android.R.id.text1)).setText(labels[position]);
		((TextView)v.findViewById(android.R.id.text2)).setText(values[position]);
		return v;
	}

}
