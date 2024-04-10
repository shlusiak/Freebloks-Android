package de.saschahlusiak.freebloks.rules

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.utils.AnalyticsProvider
import de.saschahlusiak.freebloks.utils.CrashReporter
import javax.inject.Inject

@AndroidEntryPoint
class RulesActivity : AppCompatActivity() {
    private val youtubeLink = "https://www.youtube.com/watch?v=pc8nmWpcQWs"

    @Inject
    lateinit var analytics: AnalyticsProvider

    @Inject
    lateinit var crashReporter: CrashReporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                RulesScreen(
                    onBack = { finish() },
                    onWatchVideo = ::onYoutubeButtonClick
                )
            }
        }

        analytics.logEvent("rules_show", null)
    }

    private fun onYoutubeButtonClick() {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeLink))
            startActivity(intent)
            analytics.logEvent("rules_video_click")
        }.onFailure {
            crashReporter.logException(it)
        }
    }
}