from PySide6.QtWidgets import QWidget, QHBoxLayout, QLabel, QFrame
from PySide6.QtCore import Qt
from PySide6.QtGui import QFont

from ..theme import ThemeManager


class SummaryBar(QFrame):
    """
    MVC View – top banner showing portfolio name, total value and overall G/L.
    """

    def __init__(self, parent: QWidget | None = None) -> None:
        super().__init__(parent)
        self.setFrameShape(QFrame.StyledPanel)

        layout = QHBoxLayout(self)
        layout.setContentsMargins(20, 10, 20, 10)
        layout.setSpacing(32)

        # App title
        title_font = QFont()
        title_font.setPointSize(14)
        title_font.setBold(True)
        self._title = QLabel("Portfolium")
        self._title.setFont(title_font)

        # Total value
        val_font = QFont()
        val_font.setPointSize(12)
        self._total_label = QLabel("—")
        self._total_label.setFont(val_font)
        self._total_label.setAlignment(Qt.AlignCenter)

        # Gain / loss
        self._gain_label = QLabel("—")
        self._gain_label.setFont(val_font)
        self._gain_label.setAlignment(Qt.AlignRight | Qt.AlignVCenter)

        layout.addWidget(self._title)
        layout.addStretch()
        layout.addWidget(self._total_label)
        layout.addSpacing(16)
        layout.addWidget(self._gain_label)

        self._apply_theme(ThemeManager().current)
        ThemeManager().changed.connect(self._apply_theme)

    def _apply_theme(self, theme: str) -> None:
        c = ThemeManager().colors()
        self.setStyleSheet(
            f"SummaryBar {{ background-color: {c['bg_alt']}; border-radius: 8px; }}"
        )
        self._title.setStyleSheet(f"color: {c['accent']};")

    def update_values(self, total: float, gain: float, gain_pct: float) -> None:
        c = ThemeManager().colors()
        self._total_label.setText(f"Portfolio Value:  €{total:,.2f}")

        sign = "+" if gain >= 0 else ""
        color = c["green"] if gain >= 0 else c["red"]
        self._gain_label.setText(
            f"<span style='color:{color}; font-weight:bold;'>"
            f"{sign}€{gain:,.2f} &nbsp; ({sign}{gain_pct:.2f}%)"
            f"</span>"
        )
        self._gain_label.setTextFormat(Qt.RichText)
