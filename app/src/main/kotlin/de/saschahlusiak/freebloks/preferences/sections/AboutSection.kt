package de.saschahlusiak.freebloks.preferences.sections

import android.content.Intent
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.saschahlusiak.freebloks.BuildConfig
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.preferences.heading
import de.saschahlusiak.freebloks.rules.RulesActivity
import de.saschahlusiak.freebloks.ui.preferences.Preference

internal fun LazyListScope.aboutSection(
    onRate: () -> Unit,
    onSupport: () -> Unit,
    onAbout: () -> Unit,
) {
    heading(R.string.about)

    item {
        val context = LocalContext.current
        Preference(
            title = stringResource(R.string.prefs_rules),
            summary = stringResource(R.string.prefs_rules_long),
        ) {
            val intent = Intent(context, RulesActivity::class.java)
            context.startActivity(intent)
        }
    }

    item {
        Preference(
            title = stringResource(R.string.prefs_rate_review, BuildConfig.APP_STORE_NAME),
            summary = stringResource(R.string.prefs_rate_review_long),
            onClick = onRate
        )
    }

    item {
        Preference(
            title = stringResource(R.string.prefs_support),
            summary = stringResource(R.string.prefs_support_long),
            onClick = onSupport
        )
    }

    item {
        Preference(
            title = stringResource(R.string.about_freebloks),
            summary = stringResource(R.string.copyright_string),
            onClick = onAbout
        )
    }
}
