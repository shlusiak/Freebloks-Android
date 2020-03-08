package de.saschahlusiak.freebloks.preferences;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import de.saschahlusiak.freebloks.view.scene.LegacyTheme;

@Deprecated // use the ones in types instead
public class ThemePreference extends ListPreference {

	class ThemeAdapter extends ArrayAdapter<CharSequence> {
		public ThemeAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_single_choice);
			for (int i = 0; i < getEntries().length; i++) {
				this.add("");
			}
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			LegacyTheme t = LegacyTheme.getLegacy(getContext(), getEntryValues()[position].toString());
			if (t != null)
				t.apply(v);
			return v;
		}
	}

	public ThemePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onPrepareDialogBuilder(final Builder builder) {
		super.onPrepareDialogBuilder(builder);
		builder.setAdapter(new ThemeAdapter(getContext()), new OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, int which) {
				String value = getEntryValues()[which].toString();
				if (callChangeListener(value)) {
					setValue(value);
				}
				dialog.dismiss();
			}
		});
	}
}
