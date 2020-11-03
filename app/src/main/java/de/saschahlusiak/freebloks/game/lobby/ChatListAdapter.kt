package de.saschahlusiak.freebloks.game.lobby

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.model.Game
import de.saschahlusiak.freebloks.model.colorOf
import kotlinx.android.synthetic.main.chat_list_item_remote.view.*

class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

class ChatListAdapter(context: Context, val game: Game) : RecyclerView.Adapter<ViewHolder>() {
    private val inflater = LayoutInflater.from(context)

    sealed class CellType(@LayoutRes val layoutResId: Int) {
        class Generic(val text: String): CellType(R.layout.chat_list_item_generic)
        class Server(val text: String): CellType(R.layout.chat_list_item_generic)
        class Message(@LayoutRes layoutResId: Int, val name: String, val text: String, @ColorRes val colorRes: Int): CellType(layoutResId)
    }

    private var items: List<CellType> = emptyList()

    fun setData(newItems: List<ChatItem>): Int {
        val old = this.items

        items = newItems.map {
            when(it) {
                is ChatItem.Generic -> CellType.Generic(it.text)
                is ChatItem.Server -> CellType.Server(it.text)

                is ChatItem.Message -> {
                    val color = if (it.player == null)
                        R.color.player_foreground_servermessage
                    else
                        game.gameMode.colorOf(it.player).backgroundColorId

                    val layout = if (it.isLocal) R.layout.chat_list_item_local else R.layout.chat_list_item_remote

                    CellType.Message(layout, it.name, it.text, color)
                }
            }
        }

        // because the lists are append-only and all items are immutable, we only need to dispatch inserts
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = old.size
            override fun getNewListSize() = items.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItemPosition == newItemPosition
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItemPosition == newItemPosition
            }
        }).dispatchUpdatesTo(this)

        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(viewType, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].layoutResId
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            when (item) {
                is CellType.Generic -> textView.text = item.text
                is CellType.Server -> textView.text = item.text

                is CellType.Message -> {
                    bubble.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, item.colorRes))
                    textView.text = item.text
                    name?.text = item.name
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}