package io.github.filippovissani.portfolium.view

import io.github.filippovissani.portfolium.model.Portfolio

interface IView {
    /**
     * Starts the web server for the portfolio dashboard.
     *
     * @param portfolio The portfolio data to display
     * @param port The port on which to run the web server (default is 8080)
     */
    fun render(portfolio: Portfolio, port: Int = 8080)
}