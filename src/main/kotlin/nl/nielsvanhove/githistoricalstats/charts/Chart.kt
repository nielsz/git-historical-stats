package nl.nielsvanhove.githistoricalstats.charts

/**
 * A complete chart
 */
data class Chart(
    val id: String,
    val barItems: List<BarChartStack>,
    val lineItems: List<String>,
    val title: String,
    val subtitle: String? = null,
    val caption: String? = null,
    val legend: ChartLegend? = null
)

/**
 * One bar of a chart, which can contains multiple stacked items, but normally just one item.
 */
data class BarChartStack(val items: List<String>)

/**
 * One line of a chart.
 */
data class ChartLineData(val items: List<Int>)

data class ChartLegend(val title: String?, val items: List<String>)


val redChartColorsSmall = listOf("#D00000", "#E85D04", "#FAA307")
val redChartColorsLarge =
    listOf("#1B4332", "#FF0A54", "#FFBA08", "#FF6000", "#74C69D", "#FF85A1", "#B7E4C7", "#FF9100", "#D8F3DC", "#FFAA00")
val blueChartColorsSmall = listOf("#103E8F", "#16B9F5", "#C2E7FF")
val blueChartColorsLarge = listOf("#103E8F", "#16B9F5", "#C2E7FF", "#B95B13", "#944910", "#6E370C")

val baseColors = listOf("#264653", "#2A9D8F", "#E9C46A", "#F4A261", "#E76F51", "#6D6875", "#A5A58D")
