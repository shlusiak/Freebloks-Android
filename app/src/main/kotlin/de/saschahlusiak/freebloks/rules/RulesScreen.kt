package de.saschahlusiak.freebloks.rules

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.app.theme.dimensions
import de.saschahlusiak.freebloks.utils.Previews

private val paragraphPadding
    @Composable get() = MaterialTheme.dimensions.innerPaddingSmall

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(onBack: () -> Unit, onWatchVideo: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.rules_title)) },
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
                .padding(horizontal = MaterialTheme.dimensions.activityPadding)
                .padding(top = contentPadding.calculateTopPadding()),
            verticalArrangement = spacedBy(MaterialTheme.dimensions.innerPaddingMedium),
            contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding())
        ) {
            item { Introduction(onWatchVideo) }

            stickyHeader { Heading(R.string.rules_how_to_play) }
            item { HowToPlay() }

            stickyHeader { Heading(R.string.rules_blokus_classic) }
            item { BlokusClassic() }

            stickyHeader { Heading(R.string.rules_variants, style = MaterialTheme.typography.headlineSmall) }
            item { Variants() }

            stickyHeader { Heading(R.string.rules_scoring, style = MaterialTheme.typography.headlineSmall) }
            item { Scoring() }

            stickyHeader { Heading(R.string.blokus_duo) }
            item { BlokusDuo() }

            stickyHeader { Heading(R.string.blokus_junior) }
            item { BlokusJunior() }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Introduction(onWatchVideo: () -> Unit = {}) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(
            onClick = onWatchVideo,
            modifier = Modifier.heightIn(min = MaterialTheme.dimensions.buttonSize)
        ) {
            Text(text = stringResource(id = R.string.rules_youtube))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.rules_classic_1),
                Modifier
                    .weight(1f)
                    .padding(vertical = paragraphPadding)
            )

            Image(
                painter = painterResource(id = R.drawable.appicon_big), contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
        }
    }
}

@Composable
private fun Heading(
    @StringRes text: Int,
    style: TextStyle = MaterialTheme.typography.headlineSmall
) {
    Text(
        text = stringResource(text),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        style = style
    )
}

@Composable
@Preview(showBackground = true)
private fun HowToPlay() {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.rules_2), contentDescription = null,
            )

            Text(
                text = stringResource(id = R.string.rules_how_to_play_1),
                Modifier.weight(1f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = MaterialTheme.dimensions.innerPaddingMedium)
        ) {
            Text(
                text = stringResource(id = R.string.rules_how_to_play_2),
                Modifier.weight(1f)
            )

            Image(
                painter = painterResource(id = R.drawable.rules_1), contentDescription = null
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun BlokusClassic() {
    Column {
        Text(
            text = stringResource(id = R.string.rules_classic_2),
            Modifier.padding(vertical = paragraphPadding)
        )

        Text(
            text = stringResource(id = R.string.rules_classic_3),
            Modifier.padding(vertical = paragraphPadding)
        )

        Image(
            painter = painterResource(id = R.drawable.rules_3), contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        Text(
            text = stringResource(id = R.string.rules_classic_4),
            Modifier.padding(vertical = paragraphPadding)
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun Variants() {
    Text(
        stringResource(id = R.string.rules_variants_1),
        modifier = Modifier.padding(vertical = paragraphPadding)
    )
}

@Composable
@Preview(showBackground = true)
private fun Scoring() {
    Column {
        Text(
            stringResource(id = R.string.rules_scoring_1),
            modifier = Modifier.padding(vertical = paragraphPadding)
        )

        Text(
            stringResource(id = R.string.rules_scoring_2),
            modifier = Modifier.padding(vertical = paragraphPadding)
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun BlokusDuo() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = MaterialTheme.dimensions.innerPaddingMedium)
    ) {
        Image(
            painter = painterResource(id = R.drawable.rules_4), contentDescription = null,
        )

        Text(
            text = stringResource(id = R.string.rules_duo_1),
            Modifier
                .weight(1f)
                .padding(vertical = paragraphPadding)
        )
    }

}

@Composable
@Preview(showBackground = true)
private fun BlokusJunior() {
    Text(
        stringResource(id = R.string.rules_junior_1),
        modifier = Modifier.padding(vertical = paragraphPadding)
    )
}

@Previews
@Composable
private fun Preview() {
    AppTheme {
        Surface {
            RulesScreen({}, {})
        }
    }
}