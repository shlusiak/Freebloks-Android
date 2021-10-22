package de.saschahlusiak.freebloks.statistics

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.database.HighScoreDB
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.GameMode.Companion.from
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.DependencyProvider
import de.saschahlusiak.freebloks.databinding.StatisticsActivityBinding
import de.saschahlusiak.freebloks.utils.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatisticsActivity : AppCompatActivity() {
    private val db = HighScoreDB(this)
    private var adapter: StatisticsAdapter? = null
    private var gameMode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS
    private val values: Array<String?> = arrayOfNulls(9)
    private var menu: Menu? = null

    private lateinit var gameHelper: de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper

    private val binding by viewBinding(StatisticsActivityBinding::inflate)
    private var googleSignInButton: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        db.open()

        with(binding) {

            val labels = resources.getStringArray(R.array.statistics_labels)
            adapter = StatisticsAdapter(this@StatisticsActivity, labels, values)
            listView.adapter = adapter
            ok.setOnClickListener { finish() }

            DependencyProvider.initialise(this@StatisticsActivity)
            gameHelper = DependencyProvider.googlePlayGamesHelper()

            val prefs = PreferenceManager.getDefaultSharedPreferences(this@StatisticsActivity)
            this@StatisticsActivity.gameMode = from(prefs.getInt("gamemode", GameMode.GAMEMODE_4_COLORS_4_PLAYERS.ordinal))

            refreshData()

            val actionBar = supportActionBar
            if (actionBar == null) {
                gameMode.setSelection(this@StatisticsActivity.gameMode.ordinal)
                gameMode.onItemSelectedListener =
                    object : OnItemSelectedListener {
                        override fun onItemSelected(adapter: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            this@StatisticsActivity.gameMode = from(position)
                            refreshData()
                        }

                        override fun onNothingSelected(adapterView: AdapterView<*>?) {}
                    }
            } else {
                gameMode.visibility = View.GONE
                val mSpinnerAdapter: SpinnerAdapter = ArrayAdapter.createFromResource(
                    this@StatisticsActivity, R.array.game_modes,
                    android.R.layout.simple_spinner_dropdown_item
                )
                actionBar.navigationMode = androidx.appcompat.app.ActionBar.NAVIGATION_MODE_LIST
                actionBar.setListNavigationCallbacks(mSpinnerAdapter) { itemPosition, _ ->
                    this@StatisticsActivity.gameMode = from(itemPosition)
                    refreshData()
                    true
                }
                actionBar.setSelectedNavigationItem(this@StatisticsActivity.gameMode.ordinal)
                actionBar.setDisplayShowTitleEnabled(false)
                actionBar.setDisplayHomeAsUpEnabled(true)
            }

            if (gameHelper.isAvailable) {
                googleSignInButton = gameHelper.newSignInButton(this@StatisticsActivity)
                googleSignInButton?.let {
                    signinStub.addView(
                        it,
                        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    )
                    signinStub.isVisible = true
                }
                googleSignInButton?.setOnClickListener {
                    gameHelper.beginUserInitiatedSignIn(this@StatisticsActivity, REQUEST_SIGN_IN)
                }
                gameHelper.signedIn.observe(this@StatisticsActivity) { onGoogleAccountChanged(it) }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.stats_optionsmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val isSignedIn = gameHelper.isSignedIn
        this.menu = menu
        menu.findItem(R.id.signout).isVisible = isSignedIn
        menu.findItem(R.id.achievements).isVisible = isSignedIn
        menu.findItem(R.id.leaderboard).isVisible = isSignedIn
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.clear -> {
                db.clearHighScores()
                refreshData()
                return true
            }
            R.id.signout -> {
                gameHelper.startSignOut()
                invalidateOptionsMenu()
                return true
            }
            R.id.achievements -> {
                if (gameHelper.isSignedIn) gameHelper.startAchievementsIntent(this, REQUEST_ACHIEVEMENTS)
                return true
            }
            R.id.leaderboard -> {
                if (gameHelper.isSignedIn) gameHelper.startLeaderboardIntent(this, getString(R.string.leaderboard_points_total), REQUEST_LEADERBOARD)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SIGN_IN -> gameHelper.onActivityResult(resultCode, data) { error ->
                MaterialAlertDialogBuilder(this).apply {
                    setMessage(error ?: getString(R.string.google_play_games_signin_failed))
                    setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss()}
                    show()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun refreshData() = lifecycleScope.launch {
        withContext(Dispatchers.IO) {
            var games = db.getTotalNumberOfGames(gameMode)
            val points = db.getTotalNumberOfPoints(gameMode)
            val perfect = db.getNumberOfPerfectGames(gameMode)
            var good = db.getNumberOfGoodGames(gameMode)
            val stonesLeft = db.getTotalNumberOfStonesLeft(gameMode)

            var stonesUsed = games * Shape.COUNT - stonesLeft

            var i = 0
            while (i < values.size) {
                values[i] = ""
                i++
            }
            values[0] = String.format("%d", games)
            values[8] = String.format("%d", points)
            if (games == 0) /* avoid divide by zero */ {
                games = 1
                stonesUsed = 0
            }
            good -= perfect
            values[1] = String.format("%.1f%%", 100.0f * good.toFloat() / games.toFloat())
            values[2] = String.format("%.1f%%", 100.0f * perfect.toFloat() / games.toFloat())
            i = 0
            while (i < 4) {
                val n = db.getNumberOfPlace(gameMode, i + 1)
                values[3 + i] = String.format("%.1f%%", 100.0f * n.toFloat() / games.toFloat())
                i++
            }
            when (gameMode) {
                GameMode.GAMEMODE_2_COLORS_2_PLAYERS,
                GameMode.GAMEMODE_DUO,
                GameMode.GAMEMODE_JUNIOR -> {
                    values[6] = null
                    values[5] = values[6]
                }

                else -> {
                }
            }
            values[7] = String.format("%.1f%%", 100.0f * stonesUsed.toFloat() / games.toFloat() / Shape.COUNT.toFloat())
        }

        adapter?.notifyDataSetChanged()
    }

    private fun onGoogleAccountChanged(signedIn: Boolean) {
        if (signedIn) {
            googleSignInButton?.visibility = View.GONE
            invalidateOptionsMenu()
            gameHelper.submitScore(
                getString(R.string.leaderboard_games_won),
                db.getNumberOfPlace(null, 1).toLong())
            gameHelper.submitScore(
                getString(R.string.leaderboard_points_total),
                db.getTotalNumberOfPoints(null).toLong())
        } else {
            googleSignInButton?.visibility = View.VISIBLE
            invalidateOptionsMenu()
        }
    }

    companion object {
        private const val REQUEST_LEADERBOARD = 1
        private const val REQUEST_ACHIEVEMENTS = 2
        private const val REQUEST_SIGN_IN = 3
    }
}