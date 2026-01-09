package io.github.filippovissani.portfolium.view.components

import kotlinx.html.*

/**
 * Reusable card component for displaying metrics
 */
object CardComponent {
    fun DIV.metricCard(
        icon: String,
        iconColor: String,
        title: String,
        value: String,
        valueClass: String = ""
    ) {
        div(classes = "card") {
            div(classes = "card-header") {
                div(classes = "card-icon $iconColor") {
                    unsafe { raw("""<i class="$icon"></i>""") }
                }
                h3 { +title }
            }
            div(classes = "value${if (valueClass.isNotEmpty()) " $valueClass" else ""}") { +value }
        }
    }
}

