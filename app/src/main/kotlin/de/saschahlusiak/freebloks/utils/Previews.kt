package de.saschahlusiak.freebloks.utils

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "1 - Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "2 - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "DE")
@Preview(name = "3 - Table", uiMode = Configuration.UI_MODE_NIGHT_NO, widthDp = 800, heightDp = 600)
annotation class Previews