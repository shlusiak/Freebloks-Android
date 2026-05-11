package de.saschahlusiak.freebloks.preferences.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.theme.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ThemePreferenceDialog(
    title: String,
    initialValue: Theme,
    themes: List<Theme>,
    onChange: (Theme) -> Unit,
    onDismiss: () -> Unit
) {
    var selection by rememberSaveable { mutableIntStateOf(themes.indexOf(initialValue)) }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier,
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                    items(themes.size) { index ->
                        ThemeItem(themes[index], index == selection) {
                            selection = index
                            themes.getOrNull(selection)?.let { value ->
                                onChange(value)
                            }
                            onDismiss()
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End).padding(8.dp)
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        }
    }
}

@Composable
private fun ThemeItem(
    theme: Theme,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .background(theme.brush())
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
//        Text(theme.name)

        RadioButton(
            selected = selected, onClick = onClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
        )
    }
}