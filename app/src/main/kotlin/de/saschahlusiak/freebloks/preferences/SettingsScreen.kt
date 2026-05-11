package de.saschahlusiak.freebloks.preferences

import androidx.annotation.StringRes
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.ui.preferences.PreferenceHeading
import de.saschahlusiak.freebloks.utils.Previews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsActivityViewModel,
    onBack: () -> Unit
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
            heading(R.string.prefs_interface)
            interfaceItems(viewModel)
            item { HorizontalDivider() }

            heading(R.string.prefs_display)
            displayItems(viewModel)
            item { HorizontalDivider() }

            heading(R.string.prefs_misc)
            miscItems(viewModel)
            item { HorizontalDivider() }

            heading(R.string.prefs_statistics)
            statisticsItems(viewModel)
            item { HorizontalDivider() }

            heading(R.string.google_play_games)
            googlePlayGamesItems(viewModel)
            item { HorizontalDivider() }

            heading(R.string.about)
            aboutItems()
        }
    }
}

internal fun LazyListScope.heading(
    @StringRes title: Int
) { item { PreferenceHeading(stringResource(title)) } }

@Previews
@Composable
private fun Preview() {
    AppTheme {
//        SettingsScreen(
//            onBack = {}
//        )
    }
}