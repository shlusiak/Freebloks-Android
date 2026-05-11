package de.saschahlusiak.freebloks.preferences

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.ui.preferences.CheckboxPreference
import de.saschahlusiak.freebloks.ui.preferences.Preference

internal fun LazyListScope.statisticsItems(
    viewModel: SettingsActivityViewModel
) {
    item {
        Preference(
            stringResource(R.string.prefs_statistics),
            summary = stringResource(R.string.prefs_statistics_long)
        ) {
            // TODO
        }
    }
}
