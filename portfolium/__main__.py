"""
Entry point – run with:
    python -m portfolium [data_dir]
"""
import sys
import argparse
from pathlib import Path

# Configure the matplotlib backend before any view imports pull it in
import matplotlib
matplotlib.use("QtAgg")

from PySide6.QtWidgets import QApplication, QMessageBox

from .services.yaml_loader import load_accounts_from_directory
from .models.portfolio import Portfolio
from .services.market_data import MarketDataService
from .controllers.portfolio_controller import PortfolioController
from .views.main_window import MainWindow
from .views.theme import apply_dark_theme


def main() -> None:
    parser = argparse.ArgumentParser(
        prog="portfolium",
        description="Portfolium — Personal Finance Manager",
    )
    parser.add_argument(
        "data_dir",
        nargs="?",
        default="example_data",
        help="Directory containing YAML account files (default: example_data)",
    )
    args = parser.parse_args()

    app = QApplication(sys.argv)
    app.setApplicationName("Portfolium")
    apply_dark_theme(app)

    data_dir = Path(args.data_dir)
    if not data_dir.exists():
        QMessageBox.critical(
            None,
            "Portfolium – Error",
            f"Data directory not found:\n{data_dir.resolve()}",
        )
        sys.exit(1)

    try:
        accounts = load_accounts_from_directory(data_dir)
    except Exception as exc:
        QMessageBox.critical(None, "Portfolium – Error", f"Failed to load accounts:\n{exc}")
        sys.exit(1)

    if not accounts:
        QMessageBox.warning(
            None,
            "Portfolium – Warning",
            f"No YAML account files found in:\n{data_dir.resolve()}",
        )

    portfolio = Portfolio(accounts)
    market_data = MarketDataService()
    controller = PortfolioController(portfolio, market_data)

    window = MainWindow(controller)
    window.show()

    sys.exit(app.exec())


if __name__ == "__main__":
    main()
