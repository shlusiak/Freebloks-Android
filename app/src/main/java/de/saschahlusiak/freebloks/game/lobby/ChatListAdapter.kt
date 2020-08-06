package de.saschahlusiak.freebloks.game.lobby

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.model.GameMode
import kotlinx.android.synthetic.main.chat_list_item.view.*

class ChatListAdapter(context: Context, var gameMode: GameMode) : ArrayAdapter<ChatEntry>(context, R.layout.chat_list_item, R.id.textView) {

    fun setData(entries: List<ChatEntry>) {
        clear()
        addAll(entries)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val c = getItem(position) ?: return view

        view.textView.apply {
            if (c.isServerMessage()) {
                gravity = Gravity.RIGHT
                setTextColor(Color.LTGRAY)
            } else {
                gravity = Gravity.LEFT
                setTextColor(c.getColor(context, gameMode))
            }
        }
        return view
    }
}