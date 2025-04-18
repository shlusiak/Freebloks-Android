package de.saschahlusiak.freebloks.rules

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.app.theme.AppTheme
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
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                RulesScreen(
                    onBack = { finish() },
                    onWatchVideo = ::onYoutubeButtonClick
                )
            }
        }

        analytics.logEvent("rules_show")
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