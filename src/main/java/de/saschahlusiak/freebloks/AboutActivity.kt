package de.saschahlusiak.freebloks

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_activity)
        if (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK != Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            val params = window.attributes
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            window.attributes = params
        }

        findViewById<View>(R.id.ok).setOnClickListener { finish() }
        @SuppressLint("SetTextI18n")
        findViewById<TextView>(R.id.version).text = "v${BuildConfig.VERSION_NAME}"
        findViewById<TextView>(R.id.url1).text = Global.getMarketURLString(packageName)
    }
}