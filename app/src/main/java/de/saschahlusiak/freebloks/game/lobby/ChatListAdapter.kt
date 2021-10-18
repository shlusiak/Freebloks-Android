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
import de.saschahlusiak.freebloks.databinding.ChatListItemGenericBinding
import de.saschahlusiak.freebloks.databinding.ChatListItemLocalBinding
import de.saschahlusiak.freebloks.databinding.ChatListItemRemoteBinding
import de.saschahlusiak.freebloks.model.Game
import de.saschahlusiak.freebloks.model.colorOf

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class ChatListAdapter(context: Context, val game: Game) : RecyclerView.Adapter<ViewHolder>() {
    private val inflater = LayoutInflater.from(context)

    sealed class CellType(@LayoutRes val layoutResId: Int) {
        class Generic(val text: String) : CellType(R.layout.chat_list_item_generic)
        class Server(val text: String) : CellType(R.layout.chat_list_item_generic)
        class LocalMessage(val text: String, @ColorRes val colorRes: Int) :
            CellType(R.layout.chat_list_item_local)

        class RemoteMessage(val name: String, val text: String, @ColorRes val colorRes: Int) :
            CellType(R.layout.chat_list_item_remote)
    }

    private var items: List<CellType> = emptyList()

    fun setData(newItems: List<ChatItem>): Int {
        val old = this.items

        items = newItems.map {
            when (it) {
                is ChatItem.Generic -> CellType.Generic(it.text)
                is ChatItem.Server -> CellType.Server(it.text)

                is ChatItem.Message -> {
                    val color = if (it.player == null)
                        R.color.player_foreground_servermessage
                    else
                        game.gameMode.colorOf(it.player).backgroundColorId

                    if (it.isLocal)
                        CellType.LocalMessage(it.text, color)
                    else
                        CellType.RemoteMessage(it.name, it.text, color)
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
                is CellType.Generic -> with(ChatListItemGenericBinding.bind(holder.itemView)) {
                    textView.text = item.text
                }
                is CellType.Server -> with(ChatListItemGenericBinding.bind(holder.itemView)) {
                    textView.text = item.text
                }

                is CellType.LocalMessage -> with(ChatListItemLocalBinding.bind(holder.itemView)) {
                    bubble.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, item.colorRes))
                    textView.text = item.text
                }

                is CellType.RemoteMessage -> with(ChatListItemRemoteBinding.bind(holder.itemView)) {
                    bubble.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, item.colorRes))
                    textView.text = item.text
                    name.text = item.name
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}