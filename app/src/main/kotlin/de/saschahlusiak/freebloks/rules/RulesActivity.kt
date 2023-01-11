package de.saschahlusiak.freebloks.rules

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.databinding.RulesActivityBinding
import de.saschahlusiak.freebloks.utils.AnalyticsProvider
import de.saschahlusiak.freebloks.utils.viewBinding
import javax.inject.Inject

@AndroidEntryPoint
class RulesActivity : AppCompatActivity() {
    private val youtubeLink = "https://www.youtube.com/watch?v=pc8nmWpcQWs"

    private val binding by viewBinding(RulesActivityBinding::inflate)

    @Inject
    lateinit var analytics: AnalyticsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.youtube.setOnClickListener { onYoutubeButtonClick() }

        analytics.logEvent("rules_show", null)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onYoutubeButtonClick() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeLink))
        startActivity(intent)
        analytics.logEvent("rules_video_click")
    }
}