package de.saschahlusiak.freebloks.theme

import android.content.Context
import android.util.Log
import de.saschahlusiak.freebloks.BuildConfig

/**
 * Manages available [Theme] definitions. Get the singleton instance using [.get].
 *
 * @see [getAllThemes]
 * @see [getTheme]
 */
class ThemeManager private constructor(context: Context) {
    private val tag = ThemeManager::class.java.simpleName

    private val allThemes: MutableList<Theme> = mutableListOf()

    init {
        initThemes(context)
    }

    /**
     * Discovers and initialises all themes
     * @param context Context
     */
    private fun initThemes(context: Context) {
        allThemes.clear()

        allThemes.add(ColorThemes.Black)
        allThemes.add(ColorThemes.Blue)

        if (BuildConfig.DEBUG) {
            allThemes.add(ColorThemes.Green)
            allThemes.add(ColorThemes.White)
        }

        allThemes.addAll(AssetThemes().getAllThemes(context))
    }

    /**
     * For a given package name (a [ThemeProvider] return all themes. On error, an empty collection is returned.
     *
     * @param context Context
     * @param className fully qualified class name of theme provider to use
     */
    private fun loadThemesFromPackage(context: Context, className: String): Collection<Theme> {
        try {
            val c = Class.forName(className)
            val provider = c.newInstance()

            provider as? ThemeProvider ?: throw IllegalArgumentException("ThemeProvider expected, ${provider.javaClass.name} found")

            val themesFromProvider = provider.getAllThemes(context)
            Log.i(tag, "Got " + themesFromProvider.size + " themes from " + className)

            return themesFromProvider
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        }

        return ArrayList()
    }

    /**
     * @return all known themes, including [.RANDOM] and [ColorThemes]
     */
    fun getAllThemes(): List<Theme> {
        return allThemes
    }

    /**
     * Get a specified Theme by name, or the default theme if not found
     *
     * @param name name, as retrieved via [Theme.name]
     * @param defaultTheme fall back theme, if not found
     * @return Theme instance
     */
    fun getTheme(name: String?, defaultTheme: Theme?): Theme? {
        for (theme in allThemes) {
            if (theme.name == name) {
                return theme
            }
        }

        return defaultTheme
    }

    companion object {
        private var singleton: ThemeManager? = null

        /**
         * Return the singleton instance of the [ThemeManager]
         *
         * @param context Context
         * @return the singleton instance
         */
        @JvmStatic
        fun get(context: Context): ThemeManager {
            return singleton ?: ThemeManager(context).also { singleton = it }
        }

        /**
         * Release all resources
         */
        @JvmStatic
        fun release() {
            singleton = null
        }
    }
}
