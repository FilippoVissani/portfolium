package io.github.filippovissani.portfolium.view.components

import kotlinx.html.*

/**
 * Reusable chart component for rendering charts
 */
object ChartComponent {
    fun DIV.chartContainer(
        chartId: String,
        title: String,
        icon: String,
        badge: String? = null,
        badgeClass: String = "",
        fullWidth: Boolean = false
    ) {
        div(classes = "chart-container${if (fullWidth) " full-width" else ""}") {
            div(classes = "chart-title") {
                unsafe { raw("""<i class="$icon"></i>""") }
                +title
                if (badge != null) {
                    span(classes = "return-badge $badgeClass") {
                        +badge
                    }
                }
            }
            div {
                id = "${chartId}Container"
                canvas { id = chartId }
            }
        }
    }

    fun DIV.simpleChartContainer(
        chartId: String,
        title: String,
        icon: String
    ) {
        div(classes = "chart-container") {
            div(classes = "chart-title") {
                unsafe { raw("""<i class="$icon"></i>""") }
                +title
            }
            canvas { id = chartId }
        }
    }
}

