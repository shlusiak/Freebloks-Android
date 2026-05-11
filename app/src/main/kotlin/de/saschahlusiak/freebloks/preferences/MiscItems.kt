package de.saschahlusiak.freebloks.preferences

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.ui.preferences.CheckboxPreference
import de.saschahlusiak.freebloks.ui.preferences.Preference

internal fun LazyListScope.miscItems(
    viewModel: SettingsActivityViewModel
) {
    item {
        Preference(
            stringResource(R.string.prefs_player_name),
            summary = stringResource(R.string.prefs_player_name_default)
        ) {
            // TODO
        }
    }

    item {
        val skipIntro by viewModel.skipIntro.collectAsStateWithLifecycle()

        CheckboxPreference(
            stringResource(R.string.prefs_skip_intro),
            summary = stringResource(R.string.prefs_skip_intro_long),
            checked = skipIntro,
            onCheckedChange = viewModel::setSkipIntro
        )
    }

    item {
        val autoResume by viewModel.autoResume.collectAsStateWithLifecycle()
        CheckboxPreference(
            stringResource(R.string.prefs_auto_resume),
            summary = stringResource(R.string.prefs_auto_resume_long),
            checked = autoResume,
            onCheckedChange = viewModel::setAutoResume
        )
    }
}
