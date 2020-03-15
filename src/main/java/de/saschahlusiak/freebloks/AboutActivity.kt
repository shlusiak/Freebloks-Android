package de.saschahlusiak.freebloks

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.saschahlusiak.freebloks.utils.applyMaterialBackground
import kotlinx.android.synthetic.main.about_activity.*

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.about_activity)

        ok.setOnClickListener { finish() }
        version.text = BuildConfig.VERSION_NAME
        url.text = Global.getMarketURLString(packageName)

        applyMaterialBackground()
    }
}