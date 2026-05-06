from typing import Dict

from PySide6.QtWidgets import QWidget, QVBoxLayout, QLabel, QSizePolicy
from PySide6.QtCore import Qt
from matplotlib.figure import Figure
from matplotlib.backends.backend_qtagg import FigureCanvasQTAgg as FigureCanvas

from ..theme import ThemeManager


class AllocationChartWidget(QWidget):
    """
    MVC View – matplotlib pie chart showing portfolio asset allocation.
    """

    def __init__(self, parent: QWidget | None = None) -> None:
        super().__init__(parent)
        self._last_allocation: Dict[str, float] = {}

        layout = QVBoxLayout(self)
        layout.setContentsMargins(4, 4, 4, 4)
        layout.setSpacing(0)

        self._title_label = QLabel("Allocation")
        self._title_label.setAlignment(Qt.AlignCenter)
        self._title_label.setStyleSheet(
            "font-size: 11pt; font-weight: bold; margin: 4px 0;"
        )
        layout.addWidget(self._title_label)

        c = ThemeManager().colors()
        self._fig = Figure(facecolor=c["bg"])
        self._canvas = FigureCanvas(self._fig)
        self._canvas.setSizePolicy(QSizePolicy.Expanding, QSizePolicy.Expanding)
        self._canvas.setStyleSheet("background-color: transparent;")
        layout.addWidget(self._canvas)

        ThemeManager().changed.connect(self._on_theme_changed)

    def _on_theme_changed(self, _theme: str) -> None:
        self.update_data(self._last_allocation)

    def update_data(self, allocation: Dict[str, float]) -> None:
        self._last_allocation = allocation
        c = ThemeManager().colors()
        self._fig.clear()
        self._fig.set_facecolor(c["bg"])

        if not allocation:
            self._canvas.draw()
            return

        ax = self._fig.add_subplot(111)
        ax.set_facecolor(c["bg"])

        labels = list(allocation.keys())
        values = list(allocation.values())
        palette = c["palette"]
        colors = (palette * ((len(labels) // len(palette)) + 1))[: len(labels)]

        wedges, _, autotexts = ax.pie(
            values,
            autopct="%1.1f%%",
            colors=colors,
            startangle=90,
            pctdistance=0.75,
            wedgeprops={"edgecolor": c["bg"], "linewidth": 2},
        )

        for at in autotexts:
            at.set_color(c["text"])
            at.set_fontsize(8)
            at.set_fontweight("bold")

        ax.legend(
            wedges,
            labels,
            loc="lower center",
            bbox_to_anchor=(0.5, -0.08),
            ncol=min(len(labels), 3),
            frameon=False,
            labelcolor=c["text"],
            fontsize=9,
        )

        self._fig.tight_layout(pad=1.2)
        self._canvas.draw()
