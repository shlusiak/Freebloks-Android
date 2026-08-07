package de.saschahlusiak.freebloks.utils

import android.app.Activity
import android.app.Application
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import com.google.android.gms.common.images.ImageManager
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.leaderboard.LeaderboardVariant
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * This is the actual implementation of Google Play provider. The [GooglePlayGamesHelper]
 * implementation is just a dummy that does not require any dependencies.
 */
@Singleton
class DefaultGooglePlayGamesHelper @Inject constructor(
    private val context: Application,
    private val crashReporter: CrashReporter
) : GooglePlayGamesHelper {
    private val tag = DefaultGooglePlayGamesHelper::class.java.simpleName

    private val imageManager = ImageManager.create(context)

    override val signedIn = MutableStateFlow(false)
    override val playerName = MutableStateFlow<String?>(null)
    override val isAvailable: Boolean
        get() { return true }
    override val isSignedIn: Boolean
        get() = signedIn.value

    private val scope = MainScope()

    private var activity: Activity? = null

    override val leaderboardFlow = signedIn
        .map { getLeaderboard() }
        .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)

    init {
        PlayGamesSdk.initialize(context)
    }

    override fun initialise(activity: Activity) {
        this.activity = activity

        checkName(activity)
    }

    private fun checkName(activity: Activity) {
        scope.launch {
            signedIn.value = PlayGames.getGamesSignInClient(activity)
                .isAuthenticated
                .await()
                .isAuthenticated

            val name = runCatching {
                PlayGames
                    .getPlayersClient(activity)
                    .currentPlayer
                    .await()
                    .displayName
            }.getOrNull()
            Log.d(tag, "name = $name")

            playerName.value = name
        }
    }

    override fun beginUserInitiatedSignIn(activity: Activity) {
        Log.d(tag, "Starting sign in to Google Play Games")
        PlayGames.getGamesSignInClient(activity)
            .signIn()
            .addOnSuccessListener { checkName(activity) }
            .addOnFailureListener { e ->
                playerName.value = null
                e.printStackTrace()
            }
    }

    override fun unlock(achievement: Int) {
        val activity = activity ?: return
        PlayGames.getAchievementsClient(activity)
            .unlock(context.getString(achievement))
    }

    override fun increment(achievement: Int, increment: Int) {
        val activity = activity ?: return
        PlayGames
            .getAchievementsClient(activity)
            .increment(context.getString(achievement), increment)
    }

    override fun submitScore(leaderboard: Int, score: Long) {
        val activity = activity ?: return
        PlayGames
            .getLeaderboardsClient(activity)
            .submitScore(context.getString(leaderboard), score)
    }

    override fun startAchievementsIntent(activity: Activity, requestCode: Int) {
        PlayGames
            .getAchievementsClient(activity)
            .achievementsIntent
            .addOnSuccessListener { activity.startActivityForResult(it, requestCode) }
            .addOnFailureListener { crashReporter.logException(it) }
    }

    override fun startLeaderboardIntent(activity: Activity, leaderboard: String, requestCode: Int) {
        PlayGames
            .getLeaderboardsClient(activity)
            .getLeaderboardIntent(leaderboard)
            .addOnSuccessListener { activity.startActivityForResult(it, requestCode) }
            .addOnFailureListener { crashReporter.logException(it) }
    }

    private suspend fun ImageManager.loadImage(uri: Uri): Drawable? {
        return suspendCancellableCoroutine { cont ->
            loadImage({ _, drawable, _ ->
                cont.resume(drawable)
            }, uri)
        }
    }

    override suspend fun fetchPlayerImage(uri: Uri?) = uri?.let { imageManager.loadImage(uri) }

    private suspend fun getLeaderboard(): List<LeaderboardEntry> {
        val activity = activity ?: return emptyList()
        val client = PlayGames.getLeaderboardsClient(activity)
        val playersClient = PlayGames.getPlayersClient(activity)

        val scoresTask = client.loadPlayerCenteredScores(
            LEADERBOARD_POINTS_TOTAL,
            LeaderboardVariant.TIME_SPAN_WEEKLY,
            LeaderboardVariant.COLLECTION_PUBLIC,
            3,
            true
        )

        val scores = runCatching {
            scoresTask.await().get()
        }.getOrNull() ?: return emptyList()

        val playerIdTask = playersClient.currentPlayerId
        val playerId = playerIdTask.await()

        val result = coroutineScope {
            scores.scores.map { score ->
                LeaderboardEntry(
                    rank = score.rank,
                    iconUri = score.scoreHolderIconImageUri,
                    name = score.scoreHolderDisplayName,
                    points = score.rawScore.toInt(),
                    isLocal = score.scoreHolder?.playerId == playerId,
                    fetchImage = ::fetchPlayerImage
                )
            }
        }

        scores.release()
        Log.d(tag, "result = $result")

        return result
    }

    companion object {
        private const val LEADERBOARD_POINTS_TOTAL = "CgkIuJHVzfEWEAIQAg"
    }
}