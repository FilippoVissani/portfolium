from typing import Dict

from PySide6.QtWidgets import QWidget, QVBoxLayout, QLabel, QSizePolicy
from PySide6.QtCore import Qt
from matplotlib.figure import Figure
from matplotlib.backends.backend_qtagg import FigureCanvasQTAgg as FigureCanvas

_BG = "#1e1e2e"
_TEXT = "#cdd6f4"
_SUBTEXT = "#6c7086"

# Catppuccin Mocha accent colours
_PALETTE = [
    "#89b4fa",  # blue
    "#a6e3a1",  # green
    "#fab387",  # peach
    "#f38ba8",  # red
    "#cba6f7",  # mauve
    "#94e2d5",  # teal
    "#f9e2af",  # yellow
    "#89dceb",  # sky
    "#b4befe",  # lavender
    "#eba0ac",  # maroon
]


class AllocationChartWidget(QWidget):
    """
    MVC View – matplotlib pie chart showing portfolio asset allocation.
    Includes assets (by market value) and remaining cash liquidity.
    """

    def __init__(self, parent: QWidget | None = None) -> None:
        super().__init__(parent)

        layout = QVBoxLayout(self)
        layout.setContentsMargins(4, 4, 4, 4)
        layout.setSpacing(0)

        title = QLabel("Allocation")
        title.setAlignment(Qt.AlignCenter)
        title.setStyleSheet(
            "font-size: 11pt; font-weight: bold; color: #cdd6f4; margin: 4px 0;"
        )
        layout.addWidget(title)

        self._fig = Figure(facecolor=_BG)
        self._canvas = FigureCanvas(self._fig)
        self._canvas.setSizePolicy(QSizePolicy.Expanding, QSizePolicy.Expanding)
        self._canvas.setStyleSheet("background-color: transparent;")
        layout.addWidget(self._canvas)

    def update_data(self, allocation: Dict[str, float]) -> None:
        self._fig.clear()

        if not allocation:
            self._canvas.draw()
            return

        ax = self._fig.add_subplot(111)
        ax.set_facecolor(_BG)

        labels = list(allocation.keys())
        values = list(allocation.values())
        colors = (_PALETTE * ((len(labels) // len(_PALETTE)) + 1))[: len(labels)]

        wedges, _, autotexts = ax.pie(
            values,
            autopct="%1.1f%%",
            colors=colors,
            startangle=90,
            pctdistance=0.75,
            wedgeprops={"edgecolor": _BG, "linewidth": 2},
        )

        for at in autotexts:
            at.set_color(_BG)
            at.set_fontsize(8)
            at.set_fontweight("bold")

        ax.legend(
            wedges,
            labels,
            loc="lower center",
            bbox_to_anchor=(0.5, -0.08),
            ncol=min(len(labels), 3),
            frameon=False,
            labelcolor=_TEXT,
            fontsize=9,
        )

        self._fig.tight_layout(pad=1.2)
        self._canvas.draw()
