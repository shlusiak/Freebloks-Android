package de.saschahlusiak.freebloks.preferences.types

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Keep
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceViewHolder
import de.saschahlusiak.freebloks.R

class SelectablePreference: CheckBoxPreference {
    @Keep constructor(context: Context) : this(context, null)
    @Keep constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, TypedArrayUtils.getAttr(context, androidx.preference.R.attr.checkBoxPreferenceStyle, android.R.attr.checkBoxPreferenceStyle))
    @Keep constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    @Keep constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView.isActivated = isChecked
        holder.itemView.setBackgroundResource(R.drawable.selectable_activatable_background)
    }
}