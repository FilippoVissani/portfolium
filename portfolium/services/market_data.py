from datetime import date, timedelta
from typing import Dict, List, Optional

import pandas as pd
import yfinance as yf


class MarketDataService:
    """Fetches current and historical market data from Yahoo Finance."""

    def __init__(self) -> None:
        self._ticker_cache: Dict[str, yf.Ticker] = {}

    # ------------------------------------------------------------------ #
    # Internal helpers                                                     #
    # ------------------------------------------------------------------ #

    def _ticker(self, symbol: str) -> yf.Ticker:
        if symbol not in self._ticker_cache:
            self._ticker_cache[symbol] = yf.Ticker(symbol)
        return self._ticker_cache[symbol]

    # ------------------------------------------------------------------ #
    # Current prices                                                       #
    # ------------------------------------------------------------------ #

    def get_current_price(self, symbol: str) -> Optional[float]:
        try:
            price = self._ticker(symbol).fast_info.last_price
            return float(price) if price is not None else None
        except Exception:
            return None

    def get_previous_close(self, symbol: str) -> Optional[float]:
        try:
            close = self._ticker(symbol).fast_info.previous_close
            return float(close) if close is not None else None
        except Exception:
            return None

    def get_current_prices(self, symbols: List[str]) -> Dict[str, float]:
        return {s: p for s in symbols if (p := self.get_current_price(s)) is not None}

    def get_previous_closes(self, symbols: List[str]) -> Dict[str, float]:
        return {s: p for s in symbols if (p := self.get_previous_close(s)) is not None}

    # ------------------------------------------------------------------ #
    # Historical prices                                                    #
    # ------------------------------------------------------------------ #

    def get_historical_prices(
        self, symbols: List[str], start: date, end: date
    ) -> pd.DataFrame:
        """Return a DataFrame (DatetimeIndex rows × symbol columns) of adj. close prices."""
        if not symbols:
            return pd.DataFrame()

        start_str = start.isoformat()
        end_str = (end + timedelta(days=1)).isoformat()

        try:
            if len(symbols) == 1:
                hist = self._ticker(symbols[0]).history(
                    start=start_str, end=end_str, auto_adjust=True
                )
                if hist.empty:
                    return pd.DataFrame()
                df = hist[["Close"]].rename(columns={"Close": symbols[0]})
                return df.ffill()

            raw = yf.download(
                symbols,
                start=start_str,
                end=end_str,
                auto_adjust=True,
                progress=False,
                threads=True,
            )
            if raw.empty:
                return pd.DataFrame()

            # yfinance >= 0.2 returns a MultiIndex; extract "Close" level
            if isinstance(raw.columns, pd.MultiIndex):
                close = raw["Close"]
            else:
                close = raw[["Close"]]

            if isinstance(close, pd.Series):
                close = close.to_frame(name=symbols[0])

            return close.ffill()

        except Exception:
            return pd.DataFrame()
