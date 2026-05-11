package de.saschahlusiak.freebloks.preferences.sections

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.preferences.SettingsActivityViewModel
import de.saschahlusiak.freebloks.preferences.dialogs.ThemePreferenceDialog
import de.saschahlusiak.freebloks.ui.preferences.CheckboxPreference
import de.saschahlusiak.freebloks.ui.preferences.Preference

internal fun LazyListScope.displaySection(
    viewModel: SettingsActivityViewModel
) {
    item {
        val seeds by viewModel.seeds.collectAsStateWithLifecycle()
        CheckboxPreference(
            title = stringResource(R.string.prefs_show_corners),
            summary = stringResource(R.string.prefs_show_corners_long),
            checked = seeds,
            onCheckedChange = viewModel::setSeeds
        )
    }

    item {
        val opponents by viewModel.opponents.collectAsStateWithLifecycle()

        CheckboxPreference(
            title = stringResource(R.string.prefs_show_opponents),
            summary = stringResource(R.string.prefs_show_opponents_long),
            checked = opponents,
            onCheckedChange = viewModel::setOpponents
        )
    }

    item {
        val animations by viewModel.animations.collectAsStateWithLifecycle()
        Preference(
            title = stringResource(R.string.prefs_show_animations),
            summary = stringArrayResource(R.array.prefs_animations_labels)[animations.ordinal]
        ) {
            // TODO
        }
    }

    item {
        val theme by viewModel.theme.collectAsStateWithLifecycle()
        var visible by remember { mutableStateOf(false) }
        Preference(
            title = stringResource(R.string.prefs_board_theme),
            summary = theme.getLabel(LocalContext.current),
            onClick = { visible = true }
        )
        if (visible) {
            ThemePreferenceDialog(
                title = stringResource(R.string.prefs_background_theme),
                initialValue = theme,
                themes = viewModel.themes,
                onChange = { viewModel.setTheme(it) },
                onDismiss = { visible = false }
            )
        }
    }

    item {
        val theme by viewModel.boardTheme.collectAsStateWithLifecycle()
        var visible by remember { mutableStateOf(false) }
        Preference(
            title = stringResource(R.string.prefs_board_theme),
            summary = theme.getLabel(LocalContext.current),
            onClick = { visible = true }
        )
        if (visible) {
            ThemePreferenceDialog(
                title = stringResource(R.string.prefs_board_theme),
                initialValue = theme,
                themes = viewModel.boardThemes,
                onChange = { viewModel.setBoardTheme(it) },
                onDismiss = { visible = false }
            )
        }
    }
}
