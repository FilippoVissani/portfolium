"""Tests for view theming behavior."""

from typing import cast

from PySide6.QtWidgets import QApplication

from portfolium.views.theme import ThemeManager, apply_theme


class _DummyApp:
    pass


def test_apply_default_dark_theme_even_when_already_selected(monkeypatch):
    """Startup path should still apply dark palette/QSS to the app."""
    ThemeManager._instance = None
    manager = ThemeManager()

    calls = []

    def fake_apply(app, theme):
        calls.append((app, theme))

    monkeypatch.setattr("portfolium.views.theme._apply_to_app", fake_apply)

    app = _DummyApp()
    apply_theme(cast(QApplication, app), "dark")

    assert manager.current == "dark"
    assert calls == [(app, "dark")]


def test_changed_signal_emitted_only_on_theme_switch(monkeypatch):
    ThemeManager._instance = None
    manager = ThemeManager()

    monkeypatch.setattr("portfolium.views.theme._apply_to_app", lambda app, theme: None)

    emitted = []
    manager.changed.connect(emitted.append)

    apply_theme(cast(QApplication, _DummyApp()), "dark")
    apply_theme(cast(QApplication, _DummyApp()), "light")

    assert emitted == ["light"]


