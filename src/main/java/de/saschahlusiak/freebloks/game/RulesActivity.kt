package de.saschahlusiak.freebloks.game

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.google.firebase.analytics.FirebaseAnalytics
import de.saschahlusiak.freebloks.R

class RulesActivity : Activity(), View.OnClickListener {
    private val analytics by lazy { FirebaseAnalytics.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rules_activity)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        findViewById<View>(R.id.youtube).setOnClickListener(this)

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

    override fun onClick(v: View) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_LINK))
        startActivity(intent)
        analytics.logEvent("show_rules_video", null)
    }

    companion object {
        private const val YOUTUBE_LINK = "https://www.youtube.com/watch?v=pc8nmWpcQWs"
    }
}