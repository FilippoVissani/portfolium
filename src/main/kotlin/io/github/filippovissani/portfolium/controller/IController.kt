package io.github.filippovissani.portfolium.controller

import io.github.filippovissani.portfolium.controller.pdf.PdfReport
import io.github.filippovissani.portfolium.view.IView

interface IController {
    fun computePortfolioSummary()

    fun exportPortfolioReport(): PdfReport

    fun setViews(vararg views: IView)
}
