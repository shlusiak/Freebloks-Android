package de.saschahlusiak.freebloks.theme

import android.content.Context
import android.util.Log

/**
 * Manages available [Theme] definitions. Get the singleton instance using [.get].
 *
 * @see [getTheme]
 */
class ThemeManager private constructor(context: Context) {
    private val tag = ThemeManager::class.java.simpleName

    val backgroundThemes: List<Theme>
    val boardThemes: List<Theme>

    init {
        backgroundThemes = loadBackgroundThemes(context)
        boardThemes = loadBoardThemes(context)
    }

    /**
     * Discovers and initialises all background themes
     * @param context Context
     */
    private fun loadBackgroundThemes(context: Context): List<Theme> {
        val themes: MutableList<Theme> = mutableListOf(
            ColorThemes.Black,
            ColorThemes.Blue
        )

        if (BuildConfig.DEBUG) {
            themes.add(ColorThemes.Green)
            themes.add(ColorThemes.White)
        }

        themes.addAll(AssetThemes().getAllThemes(context))
        return themes
    }

    /**
     * Discovers and initialises all board themes
     * @param context Context
     */
    private fun loadBoardThemes(context: Context): List<Theme> {
        return BoardThemes().getAllThemes(context).toList()
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
            val provider = c.getDeclaredConstructor().newInstance()

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
     * Get a specified Theme by name, or the default theme if not found.
     *
     * @param name name, as retrieved via [Theme.name]
     * @param defaultTheme fall back theme, if not found
     * @return Theme instance
     */
    fun getTheme(name: String?, defaultTheme: Theme): Theme {
        return (backgroundThemes + boardThemes).firstOrNull { it.name == name } ?: defaultTheme
    }

    companion object {
        private var singleton: ThemeManager? = null

        /**
         * Return the singleton instance of the [ThemeManager]
         *
         * @param context Context
         * @return the singleton instance
         */
        fun get(context: Context): ThemeManager {
            return singleton ?: ThemeManager(context).also { singleton = it }
        }

        /**
         * Release all resources
         */
        fun release() {
            singleton = null
        }
    }
}
