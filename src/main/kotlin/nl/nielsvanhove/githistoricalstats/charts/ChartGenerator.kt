package nl.nielsvanhove.githistoricalstats.charts

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
import org.jetbrains.letsPlot.export.ggsave
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import nl.nielsvanhove.githistoricalstats.model.Granularity
import nl.nielsvanhove.githistoricalstats.model.Granularity.MONTH
import nl.nielsvanhove.githistoricalstats.model.Granularity.MONTH12
import nl.nielsvanhove.githistoricalstats.model.Granularity.QUARTER
import nl.nielsvanhove.githistoricalstats.model.Granularity.QUARTER12
import nl.nielsvanhove.githistoricalstats.model.Granularity.YEAR
import nl.nielsvanhove.githistoricalstats.project.ProjectConfig
import nl.nielsvanhove.githistoricalstats.project.ProjectData

class ChartGenerator(private val projectConfig: ProjectConfig, private val projectData: ProjectData) {

    /**
     * Get all the content, in a parsable way, trimming the data on the front
     */
    private fun getContent(list: List<String>, granularity: Granularity): List<ChartRowData> {
        println("getting content for $list")
        val returnValues = mutableListOf<ChartRowData>()
        for (commit in projectData.commits) {
            val commitMeasurementValues = mutableMapOf<String, Int>()
            val measurements = (commit as JsonObject)["measurements"] as JsonObject
            for (s in list) {
                if (!measurements.containsKey(s)) throw IllegalArgumentException("Measurement `$s` does not exist.")
                commitMeasurementValues[s] = (measurements[s]!!.jsonPrimitive.content).toInt()
            }

            if (commitMeasurementValues.values.sum() > 0 || returnValues.size > 0) {
                val isLastOfYear = commit["isLastOfYear"]!!.jsonPrimitive.toString().toBoolean()
                val isLastOfQuarter = commit["isLastOfQuarter"]!!.jsonPrimitive.toString().toBoolean()
                val isLastOfMonth = commit["isLastOfMonth"]!!.jsonPrimitive.toString().toBoolean()
                val isFirstCommit = commit["isFirstCommit"]!!.jsonPrimitive.toString().toBoolean()
                val isLastCommit = commit["isLastCommit"]!!.jsonPrimitive.toString().toBoolean()

                if (isFirstCommit || isLastCommit || (granularity == YEAR && isLastOfYear) || (granularity == QUARTER && isLastOfQuarter) || (granularity == QUARTER12 && isLastOfQuarter) || (granularity == MONTH && isLastOfMonth) || (granularity == MONTH12 && isLastOfMonth)) {
                    val date = OffsetDateTime.parse(commit["committerDate"]!!.jsonPrimitive.content)
                    returnValues.add(ChartRowData(date = date, data = commitMeasurementValues))
                }
            }
        }

        if (granularity == MONTH12 || granularity == QUARTER12) {
            return returnValues.takeLast(12).toMutableList()
        }

        return returnValues
    }

    fun generate(chart: Chart) {
        generate(chart, YEAR, 1)
        //generate(chart, QUARTER, 2)
        //generate(chart, QUARTER12, 3)
        //generate(chart, MONTH, 4)
        //generate(chart, MONTH12, 5)
    }

    private fun generate(chart: Chart, granularity: Granularity, offset: Int) {
        val contentItems = (chart.barItems.flatMap { it.items } + chart.lineItems).distinct()
        val content = getContent(contentItems, granularity)

        val barData = chartBarData(chart, content)
        val lineData = chartLineData(chart, content)

        val largestValue = lineData.maxOfOrNull { it.items.max() } ?: 0
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dates = content.map { formatter.format(it.date) }

        val renderer = ChartRenderer(
            chart = chart,
            barData = barData,
            lines = lineData,
            dates = dates,
            barColors = getBarColorsFor(barItems = chart.barItems),
            lineColors = getLineColorsFor(lineItems = chart.lineItems.size),
            largestValue = largestValue
        )
        val plot = renderer.render()

        val id = chart.id + "_" + offset + "_" + granularity.toString().lowercase(Locale.getDefault())

        ggsave(plot, "$id.png", path = "output/" + projectConfig.name)
    }

    private fun chartLineData(
        chart: Chart,
        content: List<ChartRowData>
    ): List<ChartLineData> {
        val lineData = mutableListOf<ChartLineData>()
        for (lineItem in chart.lineItems) {
            val allData = mutableListOf<Int>()
            for (chartRowData in content) {
                val data = chartRowData.data[lineItem]!!
                allData.add(data)
            }
            lineData.add(ChartLineData(items = allData))
        }
        return lineData
    }

    private fun chartBarData(
        chart: Chart,
        content: List<ChartRowData>
    ): List<ChartBarData> {
        val barData = mutableListOf<ChartBarData>()
        for (itemX in chart.barItems) {
            val allData = mutableListOf<Int>()
            for (chartRowData in content) {
                for (key in itemX.items) {
                    allData.add(chartRowData.data[key]!!)
                }
            }

            barData.add(
                ChartBarData(
                    offsets = generateOffsets(itemX.items.size, allData.size),
                    allData = allData,
                    labels = repeatlabels(itemX.items, allData.size)
                )
            )
        }
        return barData
    }

    private fun getLineColorsFor(lineItems: Int): List<String> {
        val colors = mutableListOf<String>()

        if (lineItems <= baseColors.size) {
            colors.addAll(baseColors.subList(0, lineItems))
        }

        return colors
    }

    private fun getBarColorsFor(barItems: List<BarChartStack>): List<String> {
        val colors = mutableListOf<String>()

        val totalColors = barItems.flatMap { it.items }.size
        if (totalColors <= baseColors.size) {
            colors.addAll(baseColors.subList(0, totalColors))
        } else {
            barItems.forEachIndexed { index, chartStack ->
                val source = if (index == 0) {
                    if (chartStack.items.size <= 3) {
                        redChartColorsSmall
                    } else {
                        redChartColorsLarge
                    }
                } else {
                    if (chartStack.items.size <= 3) {
                        blueChartColorsSmall
                    } else {
                        blueChartColorsLarge
                    }
                }
                colors.addAll(source.subList(0, chartStack.items.size))
            }
        }

        return colors
    }

    private fun generateOffsets(itemCount: Int, dataSize: Int): List<Int> {
        val list = mutableListOf<Int>()

        var i = 0
        while (list.size < dataSize) {
            i++
            for (j in (1..itemCount)) {
                list.add(i)
            }
        }

        return list
    }

    private fun repeatlabels(initialList: List<String>, upTo: Int): List<String> {
        val result = generateSequence { initialList }
            .flatten()
            .take(upTo)
            .toList()

        return result
    }
}