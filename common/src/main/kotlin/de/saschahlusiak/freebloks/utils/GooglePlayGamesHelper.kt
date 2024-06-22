package de.saschahlusiak.freebloks.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.Window
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import kotlinx.coroutines.flow.MutableStateFlow

data class LeaderboardEntry(
    val rank: Long,
    val icon: Drawable?,
    val name: String,
    val points: Int,
    val isLocal: Boolean
)

interface GooglePlayGamesHelper {
    val signedIn: MutableStateFlow<Boolean>
    val playerName: MutableStateFlow<String?>

    val isAvailable: Boolean

    val isSignedIn: Boolean

    fun setWindowForPopups(window: Window) { }

    fun beginUserInitiatedSignIn(activity: Activity, requestCode: Int) { }

    fun beginUserInitiatedSignIn(fragment: Fragment, requestCode: Int) { }

    fun startSignOut() { }

    fun unlock(@StringRes achievement: Int) { }

    fun increment(@StringRes achievement: Int, increment: Int) { }

    fun submitScore(@StringRes leaderboard: Int, score: Long) { }

    fun startAchievementsIntent(activity: Activity, requestCode: Int) { }

    fun startLeaderboardIntent(activity: Activity, leaderboard: String, requestCode: Int) { }

    fun startAchievementsIntent(fragment: Fragment, requestCode: Int) { }

    fun startLeaderboardIntent(fragment: Fragment, leaderboard: String, requestCode: Int) { }

    fun onActivityResult(responseCode: Int, data: Intent?, onError: (String?) -> Unit) { }

    fun newSignInButton(context: Context): View? { return null }

    suspend fun getLeaderboard(): List<LeaderboardEntry>
}

/**
 * This is the public facade of the Google Play interface, which is also the dummy implementation that does nothing.
 */
class EmptyGooglePlayGamesHelper: GooglePlayGamesHelper {
    override val signedIn = MutableStateFlow(false)
    override val playerName = MutableStateFlow<String?>(null)

    override val isAvailable: Boolean
        get() = false

    override val isSignedIn: Boolean
        get() = (signedIn.value == true)

    override fun setWindowForPopups(window: Window) { }

    override fun beginUserInitiatedSignIn(activity: Activity, requestCode: Int) { }

    override fun beginUserInitiatedSignIn(fragment: Fragment, requestCode: Int) { }

    override fun startSignOut() { }

    override fun unlock(achievement: Int) { }

    override fun increment(achievement: Int, increment: Int) { }

    override fun submitScore(leaderboard: Int, score: Long) { }

    override fun startAchievementsIntent(activity: Activity, requestCode: Int) { }

    override fun startLeaderboardIntent(activity: Activity, leaderboard: String, requestCode: Int) { }

    override fun startAchievementsIntent(fragment: Fragment, requestCode: Int) { }

    override fun startLeaderboardIntent(fragment: Fragment, leaderboard: String, requestCode: Int) { }

    override fun onActivityResult(responseCode: Int, data: Intent?, onError: (String?) -> Unit) { }

    override fun newSignInButton(context: Context): View? { return null }

    override suspend fun getLeaderboard() = emptyList<LeaderboardEntry>()
}