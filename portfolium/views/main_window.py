from datetime import datetime

from PySide6.QtWidgets import (
    QMainWindow,
    QWidget,
    QVBoxLayout,
    QSplitter,
    QStatusBar,
    QApplication,
    QTabWidget,
)
from PySide6.QtCore import QThread, Signal, Qt

from ..controllers.portfolio_controller import PortfolioController
from .widgets.summary_bar import SummaryBar
from .widgets.assets_table import AssetsTableWidget
from .widgets.allocation_chart import AllocationChartWidget
from .widgets.historical_chart import HistoricalChartWidget
from .widgets.base_account_page import BaseAccountPage
from .widgets.planned_expenses_page import PlannedExpensesPage
from .widgets.emergency_fund_page import EmergencyFundPage


class _DataWorker(QThread):
    """Background worker that fetches market data without blocking the UI."""

    finished = Signal(object)  # emits a dict with computed results

    def __init__(self, controller: PortfolioController) -> None:
        super().__init__()
        self._ctrl = controller

    def run(self) -> None:
        self._ctrl.refresh_market_data()
        self.finished.emit(
            {
                "asset_infos": self._ctrl.get_investment_asset_infos(),
                "allocation": self._ctrl.get_investment_allocation_data(),
                "total": self._ctrl.get_total_investment_value(),
                "gain": self._ctrl.get_total_investment_gain_loss()[0],
                "gain_pct": self._ctrl.get_total_investment_gain_loss()[1],
            }
        )


class MainWindow(QMainWindow):
    """
    MVC View – the application's main window.
    Composes all sub-widgets and coordinates refreshes via the controller.
    """

    def __init__(self, controller: PortfolioController) -> None:
        super().__init__()
        self.controller = controller
        self._worker: _DataWorker | None = None

        self.setWindowTitle("Portfolium")
        self.setMinimumSize(1280, 800)
        self.resize(1440, 900)

        self._build_ui()

        status = QStatusBar()
        self.setStatusBar(status)
        self._status = status

        # Initial load
        self._refresh()

        # Auto-refresh every 60 s
        from PySide6.QtCore import QTimer
        self._timer = QTimer(self)
        self._timer.timeout.connect(self._refresh)
        self._timer.start(60_000)

    # ------------------------------------------------------------------ #
    # UI construction                                                      #
    # ------------------------------------------------------------------ #

    def _build_ui(self) -> None:
        root = QWidget()
        self.setCentralWidget(root)

        layout = QVBoxLayout(root)
        layout.setSpacing(8)
        layout.setContentsMargins(12, 12, 12, 12)

        tabs = QTabWidget()
        tabs.addTab(self._build_investment_page(), "Investments")
        tabs.addTab(self._build_base_account_page(), "Base Account")
        tabs.addTab(self._build_planned_expenses_page(), "Planned Expenses")
        tabs.addTab(self._build_emergency_fund_page(), "Emergency Fund")
        layout.addWidget(tabs)

    def _build_investment_page(self) -> QWidget:
        page = QWidget()
        layout = QVBoxLayout(page)
        layout.setSpacing(8)
        layout.setContentsMargins(0, 0, 0, 0)

        # ── Summary bar ──────────────────────────────────────────────── #
        self.summary_bar = SummaryBar()
        layout.addWidget(self.summary_bar)

        # ── Middle: asset table | pie chart ─────────────────────────── #
        middle = QSplitter(Qt.Horizontal)
        self.assets_table = AssetsTableWidget()
        self.allocation_chart = AllocationChartWidget()
        middle.addWidget(self.assets_table)
        middle.addWidget(self.allocation_chart)
        middle.setSizes([820, 380])
        layout.addWidget(middle, stretch=3)

        # ── Historical performance chart ─────────────────────────────── #
        self.historical_chart = HistoricalChartWidget(self.controller)
        layout.addWidget(self.historical_chart, stretch=2)

        return page

    def _build_base_account_page(self) -> QWidget:
        self.base_account_page = BaseAccountPage(self.controller)
        return self.base_account_page

    def _build_planned_expenses_page(self) -> QWidget:
        self.planned_expenses_page = PlannedExpensesPage(self.controller)
        return self.planned_expenses_page

    def _build_emergency_fund_page(self) -> QWidget:
        self.emergency_fund_page = EmergencyFundPage(self.controller)
        return self.emergency_fund_page

    # ------------------------------------------------------------------ #
    # Refresh logic                                                        #
    # ------------------------------------------------------------------ #

    def _refresh(self) -> None:
        if self._worker and self._worker.isRunning():
            return  # already fetching

        self._status.showMessage("Fetching market data…")
        self._worker = _DataWorker(self.controller)
        self._worker.finished.connect(self._on_data_ready)
        self._worker.start()

    def _on_data_ready(self, data: dict) -> None:
        self.summary_bar.update_values(data["total"], data["gain"], data["gain_pct"])
        self.assets_table.update_data(data["asset_infos"])
        self.allocation_chart.update_data(data["allocation"])
        self.historical_chart.refresh()
        self.base_account_page.refresh()
        self.planned_expenses_page.refresh()
        self.emergency_fund_page.refresh()

        now = datetime.now().strftime("%H:%M:%S")
        self._status.showMessage(f"Last updated: {now}")
