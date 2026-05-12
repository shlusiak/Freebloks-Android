package de.saschahlusiak.freebloks.preferences

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.preferences.sections.aboutSection
import de.saschahlusiak.freebloks.preferences.sections.displaySection
import de.saschahlusiak.freebloks.preferences.sections.googlePlaySection
import de.saschahlusiak.freebloks.preferences.sections.interfaceSection
import de.saschahlusiak.freebloks.preferences.sections.miscSection
import de.saschahlusiak.freebloks.preferences.sections.statisticsSection
import de.saschahlusiak.freebloks.ui.preferences.PreferenceHeading
import de.saschahlusiak.freebloks.utils.Previews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsActivityViewModel,
    onBack: () -> Unit,
    onSignIn: () -> Unit,
    onAchievements: () -> Unit,
    onLeaderboard: () -> Unit,
    onAbout: () -> Unit,
    onSupport: () -> Unit,
    onRate: () -> Unit,
    onStatistics: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding()),
            contentPadding = PaddingValues(
                bottom = contentPadding.calculateBottomPadding()
            )
        ) {
            interfaceSection(viewModel)
            divider()

            displaySection(viewModel)
            divider()

            miscSection(viewModel)
            divider()

            statisticsSection(onStatistics = onStatistics)
            divider()

            googlePlaySection(
                bridge = viewModel.googleHelper,
                onSignIn = onSignIn,
                onAchievements = onAchievements,
                onLeaderboard = onLeaderboard
            )
            divider()

            aboutSection(
                onRate = onRate,
                onAbout = onAbout,
                onSupport = onSupport
            )
        }
    }
}

internal fun LazyListScope.divider() {
    item { HorizontalDivider(Modifier.padding(bottom = 16.dp)) }
}

internal fun LazyListScope.heading(
    @StringRes title: Int
) {
    stickyHeader {
        PreferenceHeading(
            title = stringResource(title),
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}

@Previews
@Composable
private fun Preview() {
    AppTheme {
//        SettingsScreen(
//            onBack = {}
//        )
    }
}