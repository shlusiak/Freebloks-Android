package de.saschahlusiak.freebloks.preferences.sections

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.preferences.heading
import de.saschahlusiak.freebloks.ui.preferences.Preference
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper

internal fun LazyListScope.googlePlaySection(
    bridge: GooglePlayGamesHelper,
    onSignIn: () -> Unit,
    onAchievements: () -> Unit,
    onLeaderboard: () -> Unit
) {
    heading(R.string.google_play_games)

    item {
        val name by bridge.playerName.collectAsStateWithLifecycle()
        if (name != null) {
            Preference(
                title = stringResource(R.string.google_play_games_signout),
                summary = stringResource(R.string.google_play_games_signout_long, name ?: ""),
            ) {
                bridge.startSignOut()
            }
        } else {
            Preference(
                title = stringResource(R.string.google_play_games_signin),
                summary = stringResource(R.string.google_play_games_signin_long),
                onClick = onSignIn
            )
        }
    }

    item {
        val signedIn by bridge.signedIn.collectAsStateWithLifecycle()

        Preference(
            title = stringResource(R.string.google_play_games_achievements),
            summary = stringResource(R.string.google_play_games_achievements_long),
            enabled = signedIn,
            onClick = onAchievements
        )
    }

    item {
        val signedIn by bridge.signedIn.collectAsStateWithLifecycle()

        Preference(
            title = stringResource(R.string.google_play_games_leaderboard),
            summary = stringResource(R.string.google_play_games_leaderboard_long),
            enabled = signedIn,
            onClick = onLeaderboard
        )
    }
}
