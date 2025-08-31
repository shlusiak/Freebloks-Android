package de.saschahlusiak.freebloks.theme

import android.app.Application
import android.content.Context
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ColorThemeTest {
    @Test
    fun testColorWithValue() {
        val app = ApplicationProvider.getApplicationContext<Context>()

        val theme = ColorTheme("Name", R.string.theme_black, 128, 0, 0)

        assertEquals(0, theme.colorRes)
        assertEquals(Color.rgb(128, 0, 0), theme.color)
        assertEquals(Color.rgb(128, 0, 0), theme.getColor(app.resources))
        assertNull(theme.asset)
        assertEquals("Name", theme.name)
        assertEquals(R.string.theme_black, theme.label)
        assertFalse(theme.isResource)
    }

    @Test
    fun testColorWithResource() {
        val app = ApplicationProvider.getApplicationContext<Context>()

        val theme = ColorTheme("Name", R.string.theme_black, R.color.theme_background_blue)

        assertEquals(R.color.theme_background_blue, theme.colorRes)
        assertEquals(0, theme.color)
        assertEquals(0xFF0D1A40.toInt(), theme.getColor(app.resources))
        assertNull(theme.asset)
        assertEquals("Name", theme.name)
        assertEquals(R.string.theme_black, theme.label)
        assertFalse(theme.isResource)
    }
}