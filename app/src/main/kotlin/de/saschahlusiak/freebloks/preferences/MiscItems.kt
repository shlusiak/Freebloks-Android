package de.saschahlusiak.freebloks.preferences

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.ui.preferences.CheckboxPreference

internal fun LazyListScope.miscItems(
    viewModel: SettingsActivityViewModel
) {
    item {
        val sounds by viewModel.sounds.collectAsStateWithLifecycle()
        CheckboxPreference(
            stringResource(R.string.prefs_sounds),
            summary = stringResource(R.string.prefs_sounds_long),
            checked = sounds,
            onCheckedChange = viewModel::setSounds
        )
    }

    item {
        val vibrate by viewModel.vibrate.collectAsStateWithLifecycle()

        CheckboxPreference(
            stringResource(R.string.prefs_vibrate),
            summary = stringResource(R.string.prefs_vibrate_long),
            checked = vibrate,
            onCheckedChange = viewModel::setVibrate
        )
    }

    item {
        val snap by viewModel.snap.collectAsStateWithLifecycle()
        CheckboxPreference(
            stringResource(R.string.prefs_snap_to_corners),
            summary = stringResource(R.string.prefs_snap_to_corners_long),
            checked = snap,
            onCheckedChange = viewModel::setSnap
        )
    }
}
