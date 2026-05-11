package de.saschahlusiak.freebloks.preferences

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.saschahlusiak.freebloks.BuildConfig
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.ui.preferences.CheckboxPreference
import de.saschahlusiak.freebloks.ui.preferences.Preference

internal fun LazyListScope.aboutItems() {
    item {
        Preference(
            stringResource(R.string.prefs_rules),
            summary = stringResource(R.string.prefs_rules_long),
        ) {
            // TODO
        }
    }

    item {
        Preference(
            stringResource(R.string.prefs_rate_review, BuildConfig.APP_STORE_NAME),
            summary = stringResource(R.string.prefs_rate_review_long),
        ) {
            // TODO
        }
    }

    item {
        Preference(
            stringResource(R.string.prefs_support),
            summary = stringResource(R.string.prefs_support_long),
        ) {
            // TODO
        }
    }

    item {
        Preference(
            stringResource(R.string.about_freebloks),
            summary = stringResource(R.string.copyright_string),
        ) {
            // TODO
        }
    }
}
