package de.saschahlusiak.freebloks.preferences.sections

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.res.stringResource
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.preferences.heading
import de.saschahlusiak.freebloks.ui.preferences.Preference

internal fun LazyListScope.statisticsSection(
    onStatistics: () -> Unit
) {
    heading(R.string.prefs_statistics)

    item {
        Preference(
            title = stringResource(R.string.prefs_statistics),
            summary = stringResource(R.string.prefs_statistics_long),
            onClick = onStatistics
        )
    }
}
