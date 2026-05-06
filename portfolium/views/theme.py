from PySide6.QtWidgets import QApplication
from PySide6.QtGui import QColor, QPalette
from PySide6.QtCore import QObject, Signal

# ── Dark theme: Catppuccin Mocha ──────────────────────────────────────────── #
_DARK_COLORS = {
    "bg":       "#1e1e2e",
    "bg_alt":   "#181825",
    "surface":  "#313244",
    "overlay":  "#45475a",
    "text":     "#cdd6f4",
    "subtext":  "#6c7086",
    "accent":   "#89b4fa",
    "accent_fg":"#1e1e2e",
    "green":    "#a6e3a1",
    "red":      "#f38ba8",
    "yellow":   "#f9e2af",
    "palette": ["#89b4fa","#a6e3a1","#fab387","#f38ba8","#cba6f7","#94e2d5","#f9e2af","#89dceb","#b4befe","#eba0ac"],
}

_LIGHT_COLORS = {
    "bg":       "#eff1f5",
    "bg_alt":   "#e6e9ef",
    "surface":  "#ccd0da",
    "overlay":  "#bcc0cc",
    "text":     "#4c4f69",
    "subtext":  "#7c7f93",
    "accent":   "#1e66f5",
    "accent_fg":"#eff1f5",
    "green":    "#40a02b",
    "red":      "#d20f39",
    "yellow":   "#df8e1d",
    "palette": ["#1e66f5","#40a02b","#fe640b","#d20f39","#8839ef","#179299","#df8e1d","#04a5e5","#7287fd","#e64553"],
}


def _make_qss(c: dict) -> str:
    return f"""
QMainWindow, QWidget {{
    background-color: {c['bg']};
    color: {c['text']};
}}
QTabWidget::pane {{ border: 1px solid {c['surface']}; }}
QTabBar::tab {{
    background-color: {c['bg_alt']};
    color: {c['text']};
    padding: 5px 14px;
    border: 1px solid {c['surface']};
    border-bottom: none;
    border-top-left-radius: 4px;
    border-top-right-radius: 4px;
}}
QTabBar::tab:selected {{ background-color: {c['bg']}; font-weight: bold; }}
QTabBar::tab:hover:!selected {{ background-color: {c['surface']}; }}
QTableWidget {{
    gridline-color: {c['surface']};
    background-color: {c['bg_alt']};
    border: 1px solid {c['surface']};
    border-radius: 6px;
    font-size: 9pt;
}}
QTableWidget::item {{ padding: 2px 6px; }}
QTableWidget::item:selected {{
    background-color: {c['overlay']};
    color: {c['text']};
}}
QHeaderView::section {{
    background-color: {c['surface']};
    color: {c['text']};
    border: none;
    padding: 5px 8px;
    font-weight: bold;
    font-size: 9pt;
}}
QPushButton {{
    background-color: {c['surface']};
    color: {c['text']};
    border: 1px solid {c['overlay']};
    border-radius: 4px;
    padding: 3px 10px;
    font-size: 9pt;
}}
QPushButton:checked {{
    background-color: {c['accent']};
    color: {c['accent_fg']};
    border-color: {c['accent']};
    font-weight: bold;
}}
QPushButton:hover:!checked {{ background-color: {c['overlay']}; }}
QPushButton:disabled {{ color: {c['subtext']}; }}
QSplitter::handle {{ background-color: {c['surface']}; width: 2px; height: 2px; }}
QStatusBar {{ background-color: {c['bg_alt']}; color: {c['subtext']}; font-size: 8pt; }}
QScrollBar:vertical {{
    background: {c['bg']};
    width: 8px;
    border-radius: 4px;
}}
QScrollBar::handle:vertical {{
    background: {c['overlay']};
    border-radius: 4px;
    min-height: 20px;
}}
QScrollBar::add-line:vertical, QScrollBar::sub-line:vertical {{ height: 0; }}
QScrollArea {{ background: {c['bg']}; border: none; }}
QMenuBar {{
    background-color: {c['bg_alt']};
    color: {c['text']};
}}
QMenuBar::item:selected {{
    background-color: {c['surface']};
}}
QMenu {{
    background-color: {c['bg_alt']};
    color: {c['text']};
    border: 1px solid {c['surface']};
}}
QMenu::item:selected {{
    background-color: {c['overlay']};
}}
"""


def _build_palette(c: dict) -> QPalette:
    bg      = QColor(c["bg"])
    bg_alt  = QColor(c["bg_alt"])
    surface = QColor(c["surface"])
    text    = QColor(c["text"])
    subtext = QColor(c["subtext"])
    accent  = QColor(c["accent"])
    acc_fg  = QColor(c["accent_fg"])

    palette = QPalette()
    palette.setColor(QPalette.Window, bg)
    palette.setColor(QPalette.WindowText, text)
    palette.setColor(QPalette.Base, bg_alt)
    palette.setColor(QPalette.AlternateBase, surface)
    palette.setColor(QPalette.ToolTipBase, surface)
    palette.setColor(QPalette.ToolTipText, text)
    palette.setColor(QPalette.Text, text)
    palette.setColor(QPalette.Button, surface)
    palette.setColor(QPalette.ButtonText, text)
    palette.setColor(QPalette.BrightText, QColor(255, 85, 85))
    palette.setColor(QPalette.Link, accent)
    palette.setColor(QPalette.Highlight, accent)
    palette.setColor(QPalette.HighlightedText, acc_fg)
    palette.setColor(QPalette.Disabled, QPalette.Text, subtext)
    palette.setColor(QPalette.Disabled, QPalette.ButtonText, subtext)
    return palette


# ── ThemeManager singleton ────────────────────────────────────────────────── #

class ThemeManager(QObject):
    """Singleton that holds the current theme and emits *changed* when it switches."""

    changed = Signal(str)  # emits the new theme name ("dark" / "light")

    _instance: "ThemeManager | None" = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance

    def __init__(self):
        if hasattr(self, "_initialised"):
            return
        super().__init__()
        self._theme = "dark"
        self._initialised = True

    @property
    def current(self) -> str:
        return self._theme

    def colors(self) -> dict:
        return _DARK_COLORS if self._theme == "dark" else _LIGHT_COLORS

    def set_theme(self, theme: str, app: QApplication | None = None) -> None:
        if theme == self._theme:
            return
        self._theme = theme
        if app is None:
            app = QApplication.instance()
        if app:
            _apply_to_app(app, theme)
        self.changed.emit(theme)


def _apply_to_app(app: QApplication, theme: str) -> None:
    app.setStyle("Fusion")
    c = _DARK_COLORS if theme == "dark" else _LIGHT_COLORS
    app.setPalette(_build_palette(c))
    app.setStyleSheet(_make_qss(c))


def apply_theme(app: QApplication, theme: str = "dark") -> None:
    """Apply the chosen color theme to *app* and notify the ThemeManager."""
    ThemeManager().set_theme(theme, app)


# Backwards-compatible alias
def apply_dark_theme(app: QApplication) -> None:
    apply_theme(app, "dark")
