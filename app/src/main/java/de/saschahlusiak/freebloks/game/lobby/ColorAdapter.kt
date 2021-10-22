package de.saschahlusiak.freebloks.game.lobby

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.databinding.ColorGridItemBinding
import de.saschahlusiak.freebloks.model.Game
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.colorOf
import de.saschahlusiak.freebloks.network.message.MessageServerStatus
import java.lang.IllegalStateException

class ColorAdapter(
    private val listener: EditPlayerNameListener,
    private val context: Context,
    private val game: Game,
    private var lastStatus: MessageServerStatus?
) : BaseAdapter() {

    interface EditPlayerNameListener {
        fun onEditPlayerName(player: Int)
    }

    fun setCurrentStatus(status: MessageServerStatus?) {
        lastStatus = status
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        val gameMode  = lastStatus?.gameMode ?: return 4

        return when(gameMode) {
            GameMode.GAMEMODE_2_COLORS_2_PLAYERS,
            GameMode.GAMEMODE_DUO,
            GameMode.GAMEMODE_JUNIOR -> 2

            else -> 4
        }
    }

    override fun getItem(position: Int) = null

    private fun playerForPosition(position: Int) = when(lastStatus?.gameMode) {
        // if in two player mode, we have only 2 positions, make player 1 (yellow) the player 2 (red)
        GameMode.GAMEMODE_2_COLORS_2_PLAYERS,
        GameMode.GAMEMODE_DUO,
        GameMode.GAMEMODE_JUNIOR -> if (position == 1) 2 else 0

        else -> position
    }

    override fun getItemId(position: Int): Long {
        return playerForPosition(position).toLong()
    }

    override fun isEnabled(position: Int): Boolean {
        val lastStatus = lastStatus ?: return false
        val player = playerForPosition(position)

        if (game.isStarted) return false

        return if (lastStatus.isClient(position)) {
            // enabled if it is a local player
            game.isLocalPlayer(player)
        } else true // available
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.color_grid_item, parent, false)
        val binding = ColorGridItemBinding.bind(view)

        val lastStatus = lastStatus

        val layerDrawable = context.resources.getDrawable(R.drawable.bg_card_1, context.theme).mutate() as LayerDrawable
        val background = layerDrawable.findDrawableByLayerId(R.id.color1) as GradientDrawable

        view.background = layerDrawable

        binding.text.apply {
            setTextColor(Color.WHITE)
            visibility = View.VISIBLE
        }

        if (lastStatus == null) {
            /* unknown game state */
            background.setColor(Color.BLACK)
            background.alpha = 96

            binding.text.text = "---"
            binding.text.clearAnimation()

            binding.progressBar.visibility = View.INVISIBLE
            binding.editButton.visibility = View.INVISIBLE

            binding.checkBox.isChecked = false
            return view
        }

        val player = playerForPosition(position)

        setupView(binding, background, player)

        return view
    }

    private fun setupView(binding: ColorGridItemBinding, background: GradientDrawable, player: Int) = with(binding) {
        val lastStatus = lastStatus ?: throw IllegalStateException("lastStatus is null")
        val playerColor = game.gameMode.colorOf(player)

        background.setColor(ContextCompat.getColor(context, playerColor.backgroundColorId))

        editButton.setOnClickListener { listener.onEditPlayerName(player) }

        if (lastStatus.isClient(player)) {
            /* it is a human player */
            val client = lastStatus.clientForPlayer[player] ?: throw IllegalStateException("Player has no client")
            val name = lastStatus.getClientName(client) ?: context.getString(R.string.client_d, client + 1)

            text.text = name
            progressBar.visibility = View.INVISIBLE

            if (game.isLocalPlayer(player)) {
                text.typeface = Typeface.DEFAULT_BOLD
                editButton.visibility = View.VISIBLE

                val a: Animation = TranslateAnimation(
                    TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                    TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                    TranslateAnimation.RELATIVE_TO_SELF, 0.15f,
                    TranslateAnimation.RELATIVE_TO_SELF, -0.15f
                ).apply {
                    duration = 400
                    interpolator = DecelerateInterpolator()
                    repeatMode = Animation.REVERSE
                    repeatCount = Animation.INFINITE
                }
                text.startAnimation(a)

                checkBox.isChecked = true
                checkBox.isEnabled = true
                checkBox.visibility = View.VISIBLE
            } else {
                text.clearAnimation()
                editButton.visibility = View.INVISIBLE

                checkBox.isChecked = false
                checkBox.isEnabled = false
                checkBox.visibility = View.INVISIBLE
            }
        } else {
            /* computer player */
            background.alpha = 96

            text.text = "---"
            text.clearAnimation()
            if (game.isStarted) {
                text.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
            } else {
                text.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE
            }

            editButton.visibility = View.INVISIBLE
            checkBox.isChecked = false
            checkBox.isEnabled = false
            checkBox.visibility = View.VISIBLE
        }
    }
}