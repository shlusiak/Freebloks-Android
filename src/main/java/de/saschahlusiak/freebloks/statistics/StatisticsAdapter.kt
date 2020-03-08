package de.saschahlusiak.freebloks.statistics

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import de.saschahlusiak.freebloks.R
import kotlinx.android.synthetic.main.statistics_item.view.*

class StatisticsAdapter(private val context: Context, private val labels: Array<String>, private val values1: Array<String?>) : BaseAdapter() {
    override fun getCount() = labels.size

    override fun getItem(position: Int) = null

    override fun isEnabled(position: Int) = (values1[position] != null)

    override fun areAllItemsEnabled() = true

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val v = convertView ?: LayoutInflater.from(context).inflate(R.layout.statistics_item, parent, false)
        v.text1.apply {
            isEnabled = isEnabled(position)
            text = labels[position]
        }
        v.text2.apply {
            isEnabled = isEnabled(position)
            text = values1[position] ?: "--"
        }

        return v
    }
}