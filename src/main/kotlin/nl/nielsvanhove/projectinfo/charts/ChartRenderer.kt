package nl.nielsvanhove.projectinfo.charts

import jetbrains.letsPlot.Pos
import jetbrains.letsPlot.Stat
import jetbrains.letsPlot.geom.geomBar
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.label.labs
import jetbrains.letsPlot.letsPlot
import jetbrains.letsPlot.sampling.samplingNone
import jetbrains.letsPlot.scale.scaleFillDiscrete
import jetbrains.letsPlot.scale.scaleFillManual
import jetbrains.letsPlot.scale.scaleXDiscrete
import jetbrains.letsPlot.theme

class ChartRenderer(val chart: Chart, val data: List<ChartBarData>, val dates: List<String>, val colors: List<String>) {

    val totalBarWidth = 0.9
    val barWidth = totalBarWidth / data.size

    fun render(): Plot {

        var plot = letsPlot() +
                labs(x = "", y = "", title = chart.title)

        data.forEachIndexed { index, chartBarData ->
            plot += geomBar(stat = Stat.identity, position = Pos.stack, width = barWidth, sampling = samplingNone) {
                x = chartBarData.offsets.map { it + offsetForIndex(index, data.size) }
                y = chartBarData.allData
                fill = chartBarData.labels
            }
        }


        val legend = chart.legend
        val legendLabels = legend?.items ?: chart.items.flatMap { it.items }

        val legendName = legend?.title ?: " "

        return plot +
                scaleFillManual(values = colors) +
                scaleFillDiscrete(name= legendName, labels = legendLabels) +
                scaleXDiscrete(breaks = (1..dates.size).toList(), labels = dates) +
                theme()
    }

    private fun offsetForIndex(index: Int, size: Int): Double {
        if (size == 1) return 0.0

        val shiftLeftABit = if (size % 2 == 0) (barWidth / 2) else barWidth
        return (index * barWidth) - shiftLeftABit
    }
}
