package nl.nielsvanhove.githistoricalstats.charts

import org.jetbrains.letsPlot.Pos
import org.jetbrains.letsPlot.Stat
import org.jetbrains.letsPlot.geom.geomBar
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.intern.Scale
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.pos.positionStack
import org.jetbrains.letsPlot.sampling.samplingNone
import org.jetbrains.letsPlot.scale.scaleFillDiscrete
import org.jetbrains.letsPlot.scale.scaleFillManual
import org.jetbrains.letsPlot.scale.scaleXDiscrete

class ChartRenderer(val chart: Chart, val data: List<ChartBarData>, val dates: List<String>, val colors: List<String>) {

    val totalBarWidth = 0.9
    val barWidth = totalBarWidth / data.size

    fun render(): Plot {

        var plot = letsPlot() +
                labs(x = "", y = "", title = chart.title, subtitle = chart.subtitle, caption = chart.caption)

        data.forEachIndexed { index, chartBarData ->
            plot += geomBar(stat = Stat.identity, position = positionStack, width = barWidth, sampling = samplingNone) {
                x = chartBarData.offsets.map { it + offsetForIndex(index, data.size) }
                y = chartBarData.allData
                fill = chartBarData.labels
            }
        }

        return plot +
                scaleFillManual(values = colors) +
                addLegend() +
                scaleXDiscrete(breaks = (1..dates.size).toList(), labels = dates)
    }

    private fun addLegend(): Scale {

        val legend = chart.legend
        val legendLabels = legend?.items ?: chart.items.flatMap { it.items }

        if (legendLabels.size <= 1) {
            // no legend if there's only one items. just write a good enough title.
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
}
