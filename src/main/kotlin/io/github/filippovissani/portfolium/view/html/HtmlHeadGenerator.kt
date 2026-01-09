package io.github.filippovissani.portfolium.view.html

import kotlinx.html.*

/**
 * Generates HTML head section with all required resources
 */
object HtmlHeadGenerator {
    fun HEAD.generateHead() {
        title { +"Portfolium - Personal Finance Dashboard" }
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1.0")

        // External libraries
        script(src = "https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js") {}
        link(
            rel = "stylesheet",
            href = "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css",
        )

        // Fonts
        link(rel = "preconnect", href = "https://fonts.googleapis.com")
        link(rel = "preconnect", href = "https://fonts.gstatic.com") {
            attributes["crossorigin"] = ""
        }
        link(
            rel = "stylesheet",
            href = "https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap",
        )

        // Custom styles
        link(rel = "stylesheet", href = "/static/css/styles.css")
    }
}

