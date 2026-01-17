package io.github.filippovissani.portfolium.view.util

import org.slf4j.LoggerFactory
import java.awt.Desktop
import java.net.URI

/**
 * Utility class for launching the default system browser.
 */
object BrowserLauncher {
    private val logger = LoggerFactory.getLogger(BrowserLauncher::class.java)

    /**
     * Opens the default system browser to the specified URL.
     *
     * @param url The URL to open in the browser
     * @return true if the browser was successfully launched, false otherwise
     */
    fun openBrowser(url: String): Boolean {
        return try {
            when {
                Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) -> {
                    // Use Java Desktop API (works on most platforms)
                    Desktop.getDesktop().browse(URI(url))
                    logger.info("Opened browser to: $url")
                    true
                }
                else -> {
                    // Fallback to platform-specific commands
                    val os = System.getProperty("os.name").lowercase()
                    val command = when {
                        os.contains("win") -> listOf("cmd", "/c", "start", url)
                        os.contains("mac") -> listOf("open", url)
                        else -> listOf("xdg-open", url) // Linux and other Unix-like systems
                    }
                    Runtime.getRuntime().exec(command.toTypedArray())
                    logger.info("Opened browser to: $url using system command")
                    true
                }
            }
        } catch (e: Exception) {
            logger.warn("Could not open browser automatically: ${e.message}")
            false
        }
    }
}
