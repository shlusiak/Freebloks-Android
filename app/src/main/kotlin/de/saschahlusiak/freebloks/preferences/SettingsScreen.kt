package de.saschahlusiak.freebloks.preferences

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.VideogameAsset
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.preferences.sections.aboutSection
import de.saschahlusiak.freebloks.preferences.sections.displaySection
import de.saschahlusiak.freebloks.preferences.sections.googlePlaySection
import de.saschahlusiak.freebloks.preferences.sections.interfaceSection
import de.saschahlusiak.freebloks.preferences.sections.miscSection
import de.saschahlusiak.freebloks.preferences.sections.statisticsSection
import de.saschahlusiak.freebloks.ui.preferences.PreferenceHeading

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
        if (currentWindowAdaptiveInfo().windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)) {
            var selection by rememberSaveable { mutableIntStateOf(0) }

            Row(modifier = Modifier.padding(contentPadding)) {
                Surface(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                        .padding(start = 12.dp, end = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column {
                        SectionItem(
                            label = stringResource(R.string.prefs_interface),
                            icon = Icons.Default.Mouse,
                            isSelected = selection == 0
                        ) { selection = 0 }
                        SectionItem(
                            label = stringResource(R.string.prefs_display),
                            icon = Icons.Default.BrightnessMedium,
                            isSelected = selection == 1
                        ) { selection = 1 }
                        SectionItem(
                            label = stringResource(R.string.prefs_misc),
                            icon = Icons.AutoMirrored.Filled.Label,
                            isSelected = selection == 2
                        ) { selection = 2 }
                        SectionItem(
                            label = stringResource(R.string.prefs_statistics),
                            icon = Icons.Default.StackedLineChart,
                            isSelected = selection == 3
                        ) { selection = 3 }
                        SectionItem(
                            label = stringResource(R.string.google_play_games),
                            icon = Icons.Outlined.VideogameAsset,
                            isSelected = selection == 4
                        ) { selection = 4 }
                        SectionItem(
                            label = stringResource(R.string.about),
                            icon = Icons.Outlined.Info,
                            isSelected = selection == 5
                        ) { selection = 5 }
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(0.6f)
                        .padding(end = 12.dp, start = 4.dp)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    LazyColumn {
                        when (selection) {
                            0 -> interfaceSection(viewModel)
                            1 -> displaySection(viewModel)
                            2 -> miscSection(viewModel)
                            3 -> statisticsSection(onStatistics)
                            4 -> googlePlaySection(viewModel.googleHelper, onSignIn, onAchievements, onLeaderboard)
                            5 -> aboutSection(onRate, onAbout, onSupport)
                        }
                    }
                }
            }
        } else {
            LazyColumn(contentPadding = contentPadding) {
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
}

@Composable
internal fun SectionItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    val colors = ListItemDefaults.colors(
        containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        headlineColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
        leadingIconColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
    )

    ListItem(
        headlineContent = { Text(label) },
        colors = colors,
        leadingContent = {
            Icon(icon, "")
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
    )
}

internal fun LazyListScope.divider() {
    item { HorizontalDivider(Modifier.padding(bottom = 16.dp)) }
}

internal fun LazyListScope.heading(
    @StringRes title: Int
) {
    item { PreferenceHeading(title = stringResource(title)) }
}
