from typing import List

from PySide6.QtWidgets import QTableWidget, QTableWidgetItem, QHeaderView, QAbstractItemView
from PySide6.QtCore import Qt
from PySide6.QtGui import QBrush, QColor, QFont

from ...controllers.portfolio_controller import AssetInfo

_COLUMNS = [
    "Name",
    "Ticker",
    "Price (€)",
    "Quantity",
    "G/L (€)",
    "G/L (%)",
    "Intraday G/L (€)",
]

_GREEN = QColor("#a6e3a1")
_RED = QColor("#f38ba8")


class AssetsTableWidget(QTableWidget):
    """
    MVC View – table of open positions with colour-coded gain/loss columns.

    Columns
    -------
    Name | Ticker | Price (€) | Quantity | G/L (€) | G/L (%) | Intraday G/L (€)
    """

    def __init__(self, parent=None) -> None:
        super().__init__(0, len(_COLUMNS), parent)

        self.setHorizontalHeaderLabels(_COLUMNS)

        hh = self.horizontalHeader()
        hh.setSectionResizeMode(0, QHeaderView.Stretch)          # Name stretches
        for i in range(1, len(_COLUMNS)):
            hh.setSectionResizeMode(i, QHeaderView.ResizeToContents)

        self.setEditTriggers(QAbstractItemView.NoEditTriggers)
        self.setSelectionBehavior(QAbstractItemView.SelectRows)
        self.setSelectionMode(QAbstractItemView.SingleSelection)
        self.setAlternatingRowColors(True)
        self.verticalHeader().setVisible(False)
        self.setShowGrid(True)
        self.setSortingEnabled(True)

        mono = QFont("Monospace")
        mono.setStyleHint(QFont.TypeWriter)
        mono.setPointSize(9)
        self._mono_font = mono

    def update_data(self, assets: List[AssetInfo]) -> None:
        self.setSortingEnabled(False)
        self.setRowCount(len(assets))

        for row, asset in enumerate(assets):
            # (text, alignment, optional_value_for_colouring)
            cells = [
                (asset.name,                              Qt.AlignLeft | Qt.AlignVCenter,  None),
                (asset.symbol,                            Qt.AlignCenter,                  None),
                (f"{asset.current_price:,.2f}",           Qt.AlignRight | Qt.AlignVCenter, None),
                (f"{asset.quantity:,.4f}",                Qt.AlignRight | Qt.AlignVCenter, None),
                (f"{asset.gain_loss_eur:+,.2f}",          Qt.AlignRight | Qt.AlignVCenter, asset.gain_loss_eur),
                (f"{asset.gain_loss_pct:+.2f}%",          Qt.AlignRight | Qt.AlignVCenter, asset.gain_loss_pct),
                (f"{asset.intraday_gain_loss_eur:+,.2f}", Qt.AlignRight | Qt.AlignVCenter, asset.intraday_gain_loss_eur),
            ]

            for col, (text, align, colour_val) in enumerate(cells):
                item = QTableWidgetItem(text)
                item.setTextAlignment(align)
                if col >= 2:  # numeric columns use monospace
                    item.setFont(self._mono_font)
                if colour_val is not None:
                    item.setForeground(QBrush(_GREEN if colour_val >= 0 else _RED))
                self.setItem(row, col, item)

        self.resizeRowsToContents()
        self.setSortingEnabled(True)
