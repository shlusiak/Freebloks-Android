package de.saschahlusiak.freebloks.preferences.sections

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.preferences.SettingsActivityViewModel
import de.saschahlusiak.freebloks.preferences.dialogs.SimpleTextDialog
import de.saschahlusiak.freebloks.ui.preferences.CheckboxPreference
import de.saschahlusiak.freebloks.ui.preferences.Preference

internal fun LazyListScope.miscSection(
    viewModel: SettingsActivityViewModel
) {
    item {
        val name by viewModel.playerName.collectAsStateWithLifecycle()
        var nameVisible by remember { mutableStateOf(false) }
        Preference(
            title = stringResource(R.string.prefs_player_name),
            summary = name,
            onClick = { nameVisible = true }
        )
        if (nameVisible) {
            SimpleTextDialog(
                initialValue = name,
                title = stringResource(R.string.prefs_player_name),
                hint = stringResource(R.string.player_name_hint),
                onChange = { viewModel.setName(it) },
                onDismiss = { nameVisible = false }
            )
        }
    }

    item {
        val skipIntro by viewModel.skipIntro.collectAsStateWithLifecycle()

        CheckboxPreference(
            title = stringResource(R.string.prefs_skip_intro),
            summary = stringResource(R.string.prefs_skip_intro_long),
            checked = skipIntro,
            onCheckedChange = viewModel::setSkipIntro
        )
    }

    item {
        val autoResume by viewModel.autoResume.collectAsStateWithLifecycle()
        CheckboxPreference(
            title = stringResource(R.string.prefs_auto_resume),
            summary = stringResource(R.string.prefs_auto_resume_long),
            checked = autoResume,
            onCheckedChange = viewModel::setAutoResume
        )
    }
}
