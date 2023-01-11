package de.saschahlusiak.freebloks.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData

interface GooglePlayGamesHelper {
    val signedIn: MutableLiveData<Boolean>
    val playerName: MutableLiveData<String?>

    val isAvailable: Boolean

    val isSignedIn: Boolean

    fun setWindowForPopups(window: Window) { }

    fun beginUserInitiatedSignIn(activity: Activity, requestCode: Int) { }

    fun beginUserInitiatedSignIn(fragment: Fragment, requestCode: Int) { }

    fun startSignOut() { }

    fun unlock(achievement: String) { }

    fun increment(achievement: String, increment: Int) { }

    fun submitScore(leaderboard: String, score: Long) { }

    fun startAchievementsIntent(activity: Activity, requestCode: Int) { }

    fun startLeaderboardIntent(activity: Activity, leaderboard: String, requestCode: Int) { }

    fun startAchievementsIntent(fragment: Fragment, requestCode: Int) { }

    fun startLeaderboardIntent(fragment: Fragment, leaderboard: String, requestCode: Int) { }

    fun onActivityResult(responseCode: Int, data: Intent?, onError: (String?) -> Unit) { }

    fun newSignInButton(context: Context): View? { return null }
}

/**
 * This is the public facade of the Google Play interface, which is also the dummy implementation that does nothing.
 */
class EmptyGooglePlayGamesHelper: GooglePlayGamesHelper {
    override val signedIn = MutableLiveData(false)
    override val playerName = MutableLiveData<String?>(null)

    override val isAvailable: Boolean
        get() = false

    override val isSignedIn: Boolean
        get() = (signedIn.value == true)

    override fun setWindowForPopups(window: Window) { }

    override fun beginUserInitiatedSignIn(activity: Activity, requestCode: Int) { }

    override fun beginUserInitiatedSignIn(fragment: Fragment, requestCode: Int) { }

    override fun startSignOut() { }

    override fun unlock(achievement: String) { }

    override fun increment(achievement: String, increment: Int) { }

    override fun submitScore(leaderboard: String, score: Long) { }

    override fun startAchievementsIntent(activity: Activity, requestCode: Int) { }

    override fun startLeaderboardIntent(activity: Activity, leaderboard: String, requestCode: Int) { }

    override fun startAchievementsIntent(fragment: Fragment, requestCode: Int) { }

    override fun startLeaderboardIntent(fragment: Fragment, leaderboard: String, requestCode: Int) { }

    override fun onActivityResult(responseCode: Int, data: Intent?, onError: (String?) -> Unit) { }

    override fun newSignInButton(context: Context): View? { return null }
}