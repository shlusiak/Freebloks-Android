package de.saschahlusiak.freebloks.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.Window
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.games.*
import com.google.android.gms.tasks.OnCompleteListener
import de.saschahlusiak.freebloks.R
import java.lang.IllegalStateException

class GooglePlayGamesHelper(private val context: Context, private val listener: GameHelperListener) {
    private val tag = GooglePlayGamesHelper::class.java.simpleName

    /**
     *  Listener for sign-in success or failure events.
     **/
    interface GameHelperListener {
        /**
         * Called when the user is signed out
         */
        fun onGoogleAccountSignedOut()

        /**
         * Called when the user is signed in, even though we don't have the player yet
         */
        fun onGoogleAccountSignedIn(account: GoogleSignInAccount)
    }

    private var googleSignInClient: GoogleSignInClient
    private var gamesClient: GamesClient? = null
    private var leaderboardsClient: LeaderboardsClient? = null
    private var achievementsClient: AchievementsClient? = null
    private var playersClient: PlayersClient? = null
    private var googleAccount: GoogleSignInAccount? = null
    private var currentPlayer: Player? = null

    val isSignedIn: Boolean
        get() = googleAccount != null

    init {
        googleSignInClient = GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)

        val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (lastAccount == null) {
            googleSignInClient.silentSignIn().addOnSuccessListener { setGoogleAccount(it) }
        } else {
            // make sure we are not immediately calling the listener in the constructor
            Handler().post {
                setGoogleAccount(lastAccount)
            }
        }
    }

    fun beginUserInitiatedSignIn(activity: Activity, requestCode: Int) {
        Log.d(tag, "Starting sign in to Google Play Games")
        val intent = googleSignInClient.signInIntent
        activity.startActivityForResult(intent, requestCode)
    }

    fun beginUserInitiatedSignIn(fragment: Fragment, requestCode: Int) {
        Log.d(tag, "Starting sign in to Google Play Games")
        val intent = googleSignInClient.signInIntent
        fragment.startActivityForResult(intent, requestCode)
    }

    private fun setGoogleAccount(account: GoogleSignInAccount?) {
        if (account === googleAccount) return
        googleAccount = account

        if (account != null) {
            leaderboardsClient = Games.getLeaderboardsClient(context, account)
            achievementsClient = Games.getAchievementsClient(context, account)
            gamesClient = Games.getGamesClient(context, account)
            playersClient = Games.getPlayersClient(context, account)

            listener.onGoogleAccountSignedIn(account)
        } else {
            leaderboardsClient = null
            achievementsClient = null
            gamesClient = null
            currentPlayer = null
            listener.onGoogleAccountSignedOut()
        }
    }

    fun setWindowForPopups(window: Window) {
        gamesClient?.setViewForPopups(window.decorView)
    }

    fun getPlayer(callback: (Player) -> Unit) {
        val currentPlayer = currentPlayer
        if (currentPlayer != null) {
            callback.invoke(currentPlayer)
            return
        }

        playersClient?.currentPlayer?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                this.currentPlayer = task.result
            } else {
                // silent login failed, no message necessary
                listener.onGoogleAccountSignedOut()
                startSignOut()
            }
        }
    }

    fun startSignOut() {
        val onSignOutCompleteListener: OnCompleteListener<Void> = OnCompleteListener { setGoogleAccount(null) }
        googleSignInClient.signOut()?.addOnCompleteListener(onSignOutCompleteListener)
    }

    fun unlock(achievement: String) {
        if (!isSignedIn) return
        achievementsClient?.unlock(achievement)
    }

    fun increment(achievement: String, increment: Int) {
        if (!isSignedIn) return
        achievementsClient?.increment(achievement, increment)
    }

    fun submitScore(leaderboard: String, score: Long) {
        if (!isSignedIn) return
        leaderboardsClient?.submitScore(leaderboard, score)
    }

    fun startAchievementsIntent(activity: Activity, requestCode: Int) {
        achievementsClient?.achievementsIntent?.addOnSuccessListener { intent ->
            activity.startActivityForResult(intent, requestCode)
        }
    }

    fun startLeaderboardIntent(activity: Activity, leaderboard: String, requestCode: Int) {
        leaderboardsClient?.getLeaderboardIntent(leaderboard)?.addOnSuccessListener { intent ->
            activity.startActivityForResult(intent, requestCode)
        }
    }

    fun startAchievementsIntent(fragment: Fragment, requestCode: Int) {
        achievementsClient?.achievementsIntent?.addOnSuccessListener { intent ->
            fragment.startActivityForResult(intent, requestCode)
        }
    }

    fun startLeaderboardIntent(fragment: Fragment, leaderboard: String, requestCode: Int) {
        leaderboardsClient?.getLeaderboardIntent(leaderboard)?.addOnSuccessListener { intent ->
            fragment.startActivityForResult(intent, requestCode)
        }
    }

    /**
     * Handle activity result. Call this method from your Activity's
     * onActivityResult callback . If the activity result pertains to the sign-in
     * process, processes it appropriately.
     */
    fun onActivityResult(responseCode: Int, data: Intent?) {
        val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
        if (result.isSuccess) {
            val account = result.signInAccount ?: throw IllegalStateException("account is null")
            setGoogleAccount(account)
        } else {
            Log.e(tag, "Sign in result: ${result.status}")
            var message = result.status.statusMessage
            if (message == null) message = context.getString(R.string.playgames_sign_in_failed)
            listener.onGoogleAccountSignedOut()

            makeSimpleDialog(message).show()
        }
    }

    private fun makeSimpleDialog(text: String): Dialog {
        return makeSimpleDialog(context, text)
    }

    private fun makeSimpleDialog(title: String, text: String): Dialog {
        return makeSimpleDialog(context, title, text)
    }

    private fun makeSimpleDialog(context: Context, text: String): Dialog {
        return AlertDialog.Builder(context).setMessage(text)
            .setNeutralButton(android.R.string.ok, null)
            .create()
    }

    private fun makeSimpleDialog(context: Context, title: String, text: String): Dialog {
        return AlertDialog.Builder(context).setMessage(text)
            .setTitle(title).setNeutralButton(android.R.string.ok, null)
            .create()
    }
}