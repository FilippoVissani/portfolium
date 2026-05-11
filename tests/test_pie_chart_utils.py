"""Tests for shared pie chart rendering helpers."""

from matplotlib.backends.backend_agg import FigureCanvasAgg
from matplotlib.figure import Figure

from portfolium.views.widgets.pie_chart_utils import PieChartColors, render_pie_chart


class TestPieChartRendering:
    """Regression tests for pie chart layout."""

    def test_legend_is_placed_outside_the_chart_area(self):
        fig = Figure(figsize=(4, 4))
        canvas = FigureCanvasAgg(fig)
        ax = fig.add_subplot(111)
        colors: PieChartColors = {
            "bg": "#1e1e2e",
            "text": "#cdd6f4",
            "palette": [
                "#89b4fa",
                "#a6e3a1",
                "#fab387",
                "#f38ba8",
                "#cba6f7",
                "#94e2d5",
            ],
        }

        render_pie_chart(
            ax,
            {
                "Very Long Category Name 1": 30.0,
                "Very Long Category Name 2": 25.0,
                "Very Long Category Name 3": 20.0,
                "Very Long Category Name 4": 15.0,
                "Very Long Category Name 5": 7.5,
                "Very Long Category Name 6": 2.5,
            },
            colors,
            title="Test Allocation",
        )

        canvas.draw()
        renderer = canvas.get_renderer()
        ax_bbox = ax.get_window_extent(renderer)
        legend = ax.get_legend()

        assert legend is not None
        legend_bbox = legend.get_window_extent(renderer)
        assert not ax_bbox.overlaps(legend_bbox)
        assert legend_bbox.x0 >= ax_bbox.x1
