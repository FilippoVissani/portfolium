from PySide6.QtWidgets import QApplication
from PySide6.QtGui import QColor, QPalette
from PySide6.QtCore import Qt

# Catppuccin Mocha palette
_BG = QColor(30, 30, 46)  # base
_BG_ALT = QColor(24, 24, 37)  # mantle
_SURFACE = QColor(49, 50, 68)  # surface0
_OVERLAY = QColor(69, 71, 90)  # overlay0
_TEXT = QColor(205, 214, 244)  # text
_SUBTEXT = QColor(108, 112, 134)  # subtext0
_ACCENT = QColor(137, 180, 250)  # blue
_ACCENT_FG = QColor(30, 30, 46)

_QSS = """
QMainWindow, QWidget {
    background-color: #1e1e2e;
    color: #cdd6f4;
}
QTableWidget {
    gridline-color: #313244;
    background-color: #181825;
    border: 1px solid #313244;
    border-radius: 6px;
    font-size: 9pt;
}
QTableWidget::item { padding: 2px 6px; }
QTableWidget::item:selected {
    background-color: #45475a;
    color: #cdd6f4;
}
QHeaderView::section {
    background-color: #313244;
    color: #cdd6f4;
    border: none;
    padding: 5px 8px;
    font-weight: bold;
    font-size: 9pt;
}
QPushButton {
    background-color: #313244;
    color: #cdd6f4;
    border: 1px solid #45475a;
    border-radius: 4px;
    padding: 3px 10px;
    font-size: 9pt;
}
QPushButton:checked {
    background-color: #89b4fa;
    color: #1e1e2e;
    border-color: #89b4fa;
    font-weight: bold;
}
QPushButton:hover:!checked { background-color: #45475a; }
QPushButton:disabled { color: #6c7086; }
QSplitter::handle { background-color: #313244; width: 2px; height: 2px; }
QStatusBar { background-color: #181825; color: #6c7086; font-size: 8pt; }
QScrollBar:vertical {
    background: #1e1e2e;
    width: 8px;
    border-radius: 4px;
}
QScrollBar::handle:vertical {
    background: #45475a;
    border-radius: 4px;
    min-height: 20px;
}
QScrollBar::add-line:vertical, QScrollBar::sub-line:vertical { height: 0; }
"""


def apply_dark_theme(app: QApplication) -> None:
    app.setStyle("Fusion")

    palette = QPalette()
    palette.setColor(QPalette.Window, _BG)
    palette.setColor(QPalette.WindowText, _TEXT)
    palette.setColor(QPalette.Base, _BG_ALT)
    palette.setColor(QPalette.AlternateBase, _SURFACE)
    palette.setColor(QPalette.ToolTipBase, _SURFACE)
    palette.setColor(QPalette.ToolTipText, _TEXT)
    palette.setColor(QPalette.Text, _TEXT)
    palette.setColor(QPalette.Button, _SURFACE)
    palette.setColor(QPalette.ButtonText, _TEXT)
    palette.setColor(QPalette.BrightText, QColor(255, 85, 85))
    palette.setColor(QPalette.Link, _ACCENT)
    palette.setColor(QPalette.Highlight, _ACCENT)
    palette.setColor(QPalette.HighlightedText, _ACCENT_FG)
    palette.setColor(QPalette.Disabled, QPalette.Text, _SUBTEXT)
    palette.setColor(QPalette.Disabled, QPalette.ButtonText, _SUBTEXT)
    app.setPalette(palette)
    app.setStyleSheet(_QSS)
