package de.saschahlusiak.freebloks.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtilLight
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.images.ImageManager
import com.google.android.gms.games.*
import com.google.android.gms.games.LeaderboardsClient.LeaderboardScores
import com.google.android.gms.games.leaderboard.LeaderboardVariant
import com.google.android.gms.tasks.OnCompleteListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * This is the actual implementation of Google Play provider. The [GooglePlayGamesHelper]
 * implementation is just a dummy that does not require any dependencies.
 */
@Singleton
class DefaultGooglePlayGamesHelper @Inject constructor(
    private val context: Application
) : GooglePlayGamesHelper {
    private val tag = DefaultGooglePlayGamesHelper::class.java.simpleName

    private val googleSignInClient = GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)

    private var gamesClient: GamesClient? = null
    private var leaderboardsClient: LeaderboardsClient? = null
    private var achievementsClient: AchievementsClient? = null
    private var playersClient: PlayersClient? = null
    private val imageManager = ImageManager.create(context)

    private var currentGoogleAccount: GoogleSignInAccount? = null

    override val signedIn = MutableStateFlow(false)
    override val playerName = MutableStateFlow<String?>(null)
    override val isAvailable: Boolean
        get() = (GooglePlayServicesUtilLight.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS)
    override val isSignedIn: Boolean
        get() = signedIn.value

    init {
        val lastAccount = GoogleSignIn.getLastSignedInAccount(context)

        if (lastAccount == null) {
            googleSignInClient.silentSignIn().addOnSuccessListener { setGoogleAccount(it) }
        } else {
            // make sure we are not immediately calling the listener in the constructor
            Handler(Looper.getMainLooper()).post {
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

    override fun unlock(achievement: Int) {
        achievementsClient?.unlock(context.getString(achievement))
    }

    override fun increment(achievement: Int, increment: Int) {
        achievementsClient?.increment(context.getString(achievement), increment)
    }

    override fun submitScore(leaderboard: Int, score: Long) {
        leaderboardsClient?.submitScore(context.getString(leaderboard), score)
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

    private suspend fun ImageManager.loadImage(uri: Uri): Drawable? {
        return suspendCoroutine { cont ->
            loadImage({ _, drawable, _ ->
                cont.resume(drawable)
            }, uri)
        }
    }

    override suspend fun getLeaderboard(): List<LeaderboardEntry> {
        val client = leaderboardsClient ?: return emptyList()

        val scores: LeaderboardScores = client.loadPlayerCenteredScores(
            LEADERBOARD_POINTS_TOTAL,
            LeaderboardVariant.TIME_SPAN_WEEKLY,
            LeaderboardVariant.COLLECTION_PUBLIC,
            3,
            true
        ).await().get() ?: return emptyList()

        val playerId = playersClient?.currentPlayerId?.await()

        val result = scores.scores.map { score ->
            LeaderboardEntry(
                rank = score.rank,
                icon = imageManager.loadImage(score.scoreHolderIconImageUri),
                name = score.scoreHolderDisplayName,
                points = score.rawScore.toInt(),
                isLocal = score.scoreHolder?.playerId == playerId
            )
        }

        playersClient?.currentPlayer?.await()
        scores.release()
        Log.d(tag, "result = $result")

        return result
    }

    companion object {
        private const val LEADERBOARD_POINTS_TOTAL = "CgkIuJHVzfEWEAIQAg"
    }
}