package de.saschahlusiak.freebloks.rules

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.analytics
import kotlinx.android.synthetic.main.rules_activity.*

class RulesActivity : AppCompatActivity() {
    private val youtubeLink = "https://www.youtube.com/watch?v=pc8nmWpcQWs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rules_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        youtube.setOnClickListener(this::onYoutubeButtonClick)

        analytics.logEvent("show_rules", null)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun onYoutubeButtonClick(v: View) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeLink))
        startActivity(intent)
        analytics.logEvent("show_rules_video", null)
    }
}