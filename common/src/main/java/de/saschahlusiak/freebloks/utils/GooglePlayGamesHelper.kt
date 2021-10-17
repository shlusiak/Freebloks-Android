package de.saschahlusiak.freebloks.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData

/**
 * This is the public facade of the Google Play interface, which is also the dummy implementation that does nothing.
 */
open class GooglePlayGamesHelper {
    val signedIn = MutableLiveData(false)
    val playerName = MutableLiveData<String?>(null)

    open val isAvailable: Boolean
        get() = false

    val isSignedIn: Boolean
        get() = (signedIn.value == true)

    open fun setWindowForPopups(window: Window) { }

    open fun beginUserInitiatedSignIn(activity: Activity, requestCode: Int) { }

    open fun beginUserInitiatedSignIn(fragment: Fragment, requestCode: Int) { }

    open fun startSignOut() { }

    open fun unlock(achievement: String) { }

    open fun increment(achievement: String, increment: Int) { }

    open fun submitScore(leaderboard: String, score: Long) { }

    open fun startAchievementsIntent(activity: Activity, requestCode: Int) { }

    open fun startLeaderboardIntent(activity: Activity, leaderboard: String, requestCode: Int) { }

    open fun startAchievementsIntent(fragment: Fragment, requestCode: Int) { }

    open fun startLeaderboardIntent(fragment: Fragment, leaderboard: String, requestCode: Int) { }

    open fun onActivityResult(responseCode: Int, data: Intent?, onError: (String?) -> Unit) { }

    open fun newSignInButton(context: Context): View? { return null }
}