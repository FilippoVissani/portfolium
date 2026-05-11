from typing import TypedDict


class PieChartColors(TypedDict):
    bg: str
    text: str
    palette: list[str]


def render_pie_chart(
    ax,
    data: dict[str, float],
    colors: PieChartColors,
    *,
    title: str | None = None,
    empty_text: str = "No data",
) -> None:
    """Render a themed pie chart with a shared style across all pages."""

    ax.clear()
    ax.set_facecolor(colors["bg"])

    if not data:
        ax.text(0.5, 0.5, empty_text, color=colors["text"], ha="center", va="center")
        ax.axis("off")
        return

    labels = list(data)
    values = [float(v) for v in data.values()]  # type: ignore[reportGeneralTypeIssues]
    palette = colors["palette"]  # type: ignore[reportGeneralTypeIssues]
    pie_colors = (palette * ((len(labels) // len(palette)) + 1))[: len(labels)]  # type: ignore[reportGeneralTypeIssues]
    total = sum(values)

    def _autopct(pct: float) -> str:
        if pct <= 0:
            return ""
        absolute = total * (pct / 100.0)
        return f"{pct:.1f}%\n€{absolute:,.0f}"

    wedges, _, autotexts = ax.pie(
        values,
        labels=None,
        autopct=_autopct,
        colors=pie_colors,
        startangle=90,
        pctdistance=0.72,
        wedgeprops={"edgecolor": colors["bg"], "linewidth": 1.8},
        textprops={"color": "#111111", "fontsize": 8, "fontweight": "bold"},
    )

    for at in autotexts:
        at.set_linespacing(0.9)

    ax.legend(
        wedges,
        labels,
        loc="center left",
        bbox_to_anchor=(1.02, 0.5),
        ncol=1,
        frameon=False,
        labelcolor=colors["text"],
        fontsize=8,
        borderaxespad=0.0,
        handlelength=1.1,
        handletextpad=0.6,
        labelspacing=0.5,
    )
    ax.axis("equal")

    if title:
        ax.set_title(title, color=colors["text"], fontsize=10)

    ax.figure.tight_layout(rect=(0.0, 0.0, 0.78, 1.0), pad=1.2)

