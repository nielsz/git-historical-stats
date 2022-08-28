package nl.nielsvanhove.githistoricalstats.charts

import org.jetbrains.letsPlot.Stat
import org.jetbrains.letsPlot.geom.geomBar
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.geom.geomText
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.intern.Scale
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.pos.positionStack
import org.jetbrains.letsPlot.sampling.samplingNone
import org.jetbrains.letsPlot.scale.scaleFillDiscrete
import org.jetbrains.letsPlot.scale.scaleFillManual
import org.jetbrains.letsPlot.scale.scaleXDiscrete

class ChartRenderer(
    val chart: Chart,
    val barData: List<ChartBarData>,
    val barColors: List<String>,
    val lines: List<ChartLineData>,
    val lineColors: List<String>,
    val dates: List<String>,
    val largestValue: Int,
) {
    private val totalBarWidth = 0.9
    private val barWidth = totalBarWidth / barData.size

    fun render(): Plot {
        println(chart)
        var plot = letsPlot() +
                labs(x = "", y = "", title = chart.title, subtitle = chart.subtitle, caption = chart.caption)

        barData.forEachIndexed { index, chartBarData ->
            plot += geomBar(stat = Stat.identity, position = positionStack, width = barWidth, sampling = samplingNone) {
                x = chartBarData.offsets.map { it + offsetForIndex(index, barData.size) }
                y = chartBarData.allData
                fill = chartBarData.labels
            }
        }

        lines.forEach { line ->
            plot += geomLine(size = 1.0, color = lineColors[0]) {
                x = (1..line.items.size)
                y = line.items
            }

            plot += geomPoint(size = lineDotSize(largestValue), shape = 16, color = lineColors[0], stroke = 0.0) {
                x = (1..line.items.size)
                y = line.items
            }

            val texts = line.items.map { it.formatAmount() }
            plot += geomText(data = mapOf("texts" to texts), size = 3, vjust = 2, color = "white", fontface = "bold") {
                x = (1..line.items.size)
                y = line.items
                label = "texts"
            }
        }

        return plot +
                scaleFillManual(values = barColors) +
                addLegend() +
                scaleXDiscrete(breaks = (1..dates.size).toList(), labels = dates)
    }

    private fun addLegend(): Scale {

        val legend = chart.legend
        val legendLabels = legend?.items ?: chart.barItems.flatMap { it.items }

        if (legendLabels.size <= 1) {
            // no legend if there's only one item. just write a good enough title.
            return scaleFillDiscrete(name = "")
        }

        val legendName = legend?.title ?: " "
        return scaleFillDiscrete(name = legendName, labels = legendLabels)
    }

    private fun offsetForIndex(index: Int, size: Int): Double {
        if (size == 1) return 0.0

        val shiftLeftABit = if (size % 2 == 0) (barWidth / 2) else barWidth
        return (index * barWidth) - shiftLeftABit
    }

    private fun Int.formatAmount() = when {
        this >= 100_000 -> ((this / 1000)).toString() + "k" // 193k
        this >= 1000 -> ((this / 100) / 10.0).toString() + "k" // 14.0k
        else -> this.toString() // 83
    }

    /**
     * If the largest value in the dataset is very small, we don't have to display large dots
     **/
    private fun lineDotSize(largestValue: Int): Number {
        return when {
            largestValue >= 10_000 -> 7.5
            largestValue >= 1_000 -> 7.0
            largestValue >= 100 -> 6.0
            else -> 5.0
        }
    }
}
