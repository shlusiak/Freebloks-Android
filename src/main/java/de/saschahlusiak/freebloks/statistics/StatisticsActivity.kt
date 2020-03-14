package de.saschahlusiak.freebloks.statistics

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.database.HighscoreDB
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.GameMode.Companion.from
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper
import kotlinx.android.synthetic.main.statistics_activity.*

class StatisticsActivity : AppCompatActivity() {
    private val REQUEST_LEADERBOARD = 1
    private val REQUEST_ACHIEVEMENTS = 2
    private val REQUEST_SIGN_IN = 3

    private val db = HighscoreDB(this)
    private var adapter: StatisticsAdapter? = null
    private var gameMode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS
    private val values: Array<String?> = arrayOfNulls(9)
    private var menu: Menu? = null

    private lateinit var gameHelper: GooglePlayGamesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db.open()

        setContentView(R.layout.statistics_activity)
        val labels = resources.getStringArray(R.array.statistics_labels)
        adapter = StatisticsAdapter(this, labels, values)
        listView.adapter = adapter
        ok.setOnClickListener { finish() }

        gameHelper =  GooglePlayGamesHelper(this)
        signin.setOnClickListener { gameHelper.beginUserInitiatedSignIn(this@StatisticsActivity, REQUEST_SIGN_IN) }

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        gameMode = from(prefs.getInt("gamemode", GameMode.GAMEMODE_4_COLORS_4_PLAYERS.ordinal))

        refreshData()

        val actionBar = supportActionBar
        if (actionBar == null) {
            game_mode.setSelection(gameMode.ordinal)
            (findViewById<View>(R.id.game_mode) as Spinner).onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(adapter: AdapterView<*>?, view: View, position: Int, id: Long) {
                    gameMode = from(position)
                    refreshData()
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }
        } else {
            findViewById<View>(R.id.game_mode).visibility = View.GONE
            val mSpinnerAdapter: SpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.game_modes,
                android.R.layout.simple_spinner_dropdown_item)
            actionBar.navigationMode = androidx.appcompat.app.ActionBar.NAVIGATION_MODE_LIST
            actionBar.setListNavigationCallbacks(mSpinnerAdapter) { itemPosition, itemId ->
                gameMode = from(itemPosition)
                refreshData()
                true
            }
            actionBar.setSelectedNavigationItem(gameMode.ordinal)
            actionBar.setDisplayShowTitleEnabled(false)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        if (gameHelper.isAvailable) {
            gameHelper.googleAccount.observe(this, Observer { onGoogleAccountChanged(it) })
        } else {
            signin.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
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
                db.clearHighscores()
                refreshData()
                return true
            }
            R.id.signout -> {
                gameHelper.startSignOut()
                signin.visibility = View.VISIBLE
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
            REQUEST_SIGN_IN -> gameHelper.onActivityResult(resultCode, data)
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun refreshData() {
        var games = db.getTotalNumberOfGames(gameMode)
        val points = db.getTotalNumberOfPoints(gameMode)
        val perfect = db.getNumberOfPerfectGames(gameMode)
        var good = db.getNumberOfGoodGames(gameMode)
        val stonesLeft = db.getTotalNumberOfStonesLeft(gameMode)
        var stonesUsed = games * Shape.COUNT - stonesLeft
        var i: Int
        i = 0
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
        when(gameMode) {
            GameMode.GAMEMODE_2_COLORS_2_PLAYERS,
            GameMode.GAMEMODE_DUO,
            GameMode.GAMEMODE_JUNIOR -> {
                values[6] = null
                values[5] = values[6]
            }

            else -> {}
        }
        values[7] = String.format("%.1f%%", 100.0f * stonesUsed.toFloat() / games.toFloat() / Shape.COUNT.toFloat())

        adapter?.notifyDataSetChanged()
    }

    private fun onGoogleAccountChanged(account: GoogleSignInAccount?) {
        if (account != null) {
            findViewById<View>(R.id.signin).visibility = View.GONE
            invalidateOptionsMenu()
            gameHelper.submitScore(
                getString(R.string.leaderboard_games_won),
                db.getNumberOfPlace(null, 1).toLong())
            gameHelper.submitScore(
                getString(R.string.leaderboard_points_total),
                db.getTotalNumberOfPoints(null).toLong())
        } else {
            findViewById<View>(R.id.signin).visibility = View.VISIBLE
            invalidateOptionsMenu()
        }
    }
}