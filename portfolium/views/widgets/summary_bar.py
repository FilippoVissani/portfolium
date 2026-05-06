from PySide6.QtWidgets import QWidget, QHBoxLayout, QLabel, QFrame
from PySide6.QtCore import Qt
from PySide6.QtGui import QFont


class SummaryBar(QFrame):
    """
    MVC View – top banner showing portfolio name, total value and overall G/L.
    """

    def __init__(self, parent: QWidget | None = None) -> None:
        super().__init__(parent)
        self.setFrameShape(QFrame.StyledPanel)
        self.setStyleSheet(
            "SummaryBar { background-color: #181825; border-radius: 8px; }"
        )

        layout = QHBoxLayout(self)
        layout.setContentsMargins(20, 10, 20, 10)
        layout.setSpacing(32)

        # App title
        title_font = QFont()
        title_font.setPointSize(14)
        title_font.setBold(True)
        title = QLabel("Portfolium")
        title.setFont(title_font)
        title.setStyleSheet("color: #89b4fa;")

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

        layout.addWidget(title)
        layout.addStretch()
        layout.addWidget(self._total_label)
        layout.addSpacing(16)
        layout.addWidget(self._gain_label)

    def update_values(self, total: float, gain: float, gain_pct: float) -> None:
        self._total_label.setText(f"Portfolio Value:  €{total:,.2f}")

        sign = "+" if gain >= 0 else ""
        color = "#a6e3a1" if gain >= 0 else "#f38ba8"
        self._gain_label.setText(
            f"<span style='color:{color}; font-weight:bold;'>"
            f"{sign}€{gain:,.2f} &nbsp; ({sign}{gain_pct:.2f}%)"
            f"</span>"
        )
        self._gain_label.setTextFormat(Qt.RichText)
