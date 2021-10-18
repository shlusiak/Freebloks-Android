package de.saschahlusiak.freebloks.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtilLight
import com.google.android.gms.common.SignInButton
import com.google.android.gms.games.*
import com.google.android.gms.tasks.OnCompleteListener
import java.lang.IllegalStateException

/**
 * This is the actual implementation of Google Play provider. The [GooglePlayGamesHelper]
 * implementation is just a dummy that does not require any dependencies.
 */
class DefaultGooglePlayGamesHelper(private val context: Context) : GooglePlayGamesHelper() {
    private val tag = DefaultGooglePlayGamesHelper::class.java.simpleName

    private var googleSignInClient: GoogleSignInClient
    private var gamesClient: GamesClient? = null
    private var leaderboardsClient: LeaderboardsClient? = null
    private var achievementsClient: AchievementsClient? = null
    private var playersClient: PlayersClient? = null

    var currentGoogleAccount: GoogleSignInAccount? = null

    override val isAvailable: Boolean
        get() = (GooglePlayServicesUtilLight.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS)

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

    override fun beginUserInitiatedSignIn(activity: Activity, requestCode: Int) {
        Log.d(tag, "Starting sign in to Google Play Games")
        val intent = googleSignInClient.signInIntent
        activity.startActivityForResult(intent, requestCode)
    }

    override fun beginUserInitiatedSignIn(fragment: Fragment, requestCode: Int) {
        Log.d(tag, "Starting sign in to Google Play Games")
        val intent = googleSignInClient.signInIntent
        fragment.startActivityForResult(intent, requestCode)
    }

    private fun setGoogleAccount(account: GoogleSignInAccount?) {
        if (account === currentGoogleAccount) return

        if (account != null) {
            leaderboardsClient = Games.getLeaderboardsClient(context, account)
            achievementsClient = Games.getAchievementsClient(context, account)
            gamesClient = Games.getGamesClient(context, account)
            playersClient = Games.getPlayersClient(context, account).also {
                it.currentPlayer.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val player = task.result ?: return@addOnCompleteListener
                        this.playerName.value = player.displayName
                    } else {
                        startSignOut()
                    }
                }
            }

            currentGoogleAccount = account
            signedIn.value = true
        } else {
            leaderboardsClient = null
            achievementsClient = null
            gamesClient = null
            signedIn.value = false
            playerName.value = null
            currentGoogleAccount = null
        }
    }

    override fun setWindowForPopups(window: Window) {
        gamesClient?.setViewForPopups(window.decorView)
    }

    override fun startSignOut() {
        val onSignOutCompleteListener: OnCompleteListener<Void> = OnCompleteListener { setGoogleAccount(null) }
        googleSignInClient.signOut().addOnCompleteListener(onSignOutCompleteListener)
    }

    override fun unlock(achievement: String) {
        achievementsClient?.unlock(achievement)
    }

    override fun increment(achievement: String, increment: Int) {
        achievementsClient?.increment(achievement, increment)
    }

    override fun submitScore(leaderboard: String, score: Long) {
        leaderboardsClient?.submitScore(leaderboard, score)
    }

    override fun startAchievementsIntent(activity: Activity, requestCode: Int) {
        achievementsClient?.achievementsIntent?.addOnSuccessListener { intent ->
            activity.startActivityForResult(intent, requestCode)
        }
    }

    override fun startLeaderboardIntent(activity: Activity, leaderboard: String, requestCode: Int) {
        leaderboardsClient?.getLeaderboardIntent(leaderboard)?.addOnSuccessListener { intent ->
            activity.startActivityForResult(intent, requestCode)
        }
    }

    override fun startAchievementsIntent(fragment: Fragment, requestCode: Int) {
        achievementsClient?.achievementsIntent?.addOnSuccessListener { intent ->
            fragment.startActivityForResult(intent, requestCode)
        }
    }

    override fun startLeaderboardIntent(fragment: Fragment, leaderboard: String, requestCode: Int) {
        leaderboardsClient?.getLeaderboardIntent(leaderboard)?.addOnSuccessListener { intent ->
            fragment.startActivityForResult(intent, requestCode)
        }
    }

    /**
     * Handle activity result. Call this method from your Activity's
     * onActivityResult callback . If the activity result pertains to the sign-in
     * process, processes it appropriately.
     *
     * @return an optional error message, in case of error
     */
    override fun onActivityResult(responseCode: Int, data: Intent?, onError: (String?) -> Unit) {
        data ?: return
        val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data) ?: return
        if (result.status.isSuccess) {
            val account = result.signInAccount ?: throw IllegalStateException("account is null")
            setGoogleAccount(account)
        } else if (result.status.isCanceled) {
            // user aborted is no error
        } else {
            val message = result.status.statusMessage
            Log.e(tag, "Sign in result: ${result.status}")
            onError.invoke(message)
        }
    }

    override fun newSignInButton(context: Context) = SignInButton(context)
}