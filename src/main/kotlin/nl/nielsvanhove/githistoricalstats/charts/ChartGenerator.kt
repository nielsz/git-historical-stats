package nl.nielsvanhove.githistoricalstats.charts

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import jetbrains.letsPlot.export.ggsave
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
        val returnValues = mutableListOf<ChartRowData>()
        for (commit in projectData.commits) {
            val commitMeasurementValues = mutableMapOf<String, Int>()
            val measurements = (commit as JsonObject)["measurements"] as JsonObject
            for (s in list) {
                if(!measurements.containsKey(s)) throw IllegalArgumentException("Measurement `$s` does not exist.")
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
        generate(chart, QUARTER, 2)
        generate(chart, QUARTER12, 3)
        generate(chart, MONTH, 4)
        generate(chart, MONTH12, 5)
    }


    private fun generate(chart: Chart, granularity: Granularity, offset: Int) {
        val id = chart.id + "_" + offset + "_" + granularity.toString().lowercase(Locale.getDefault())

        val content = getContent(chart.items.flatMap { it.items }, granularity)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dates = content.map { formatter.format(it.date) }

        val data = mutableListOf<ChartBarData>()

        for (itemX in chart.items) {
            val allData = mutableListOf<Int>()
            for (chartRowData in content) {
                for (key in itemX.items) {
                    allData.add(chartRowData.data[key]!!)
                }
            }

            data.add(
                ChartBarData(
                    offsets = generateOffsets(itemX.items.size, allData.size),
                    allData = allData,
                    labels = repeatlabels(itemX.items, allData.size)
                )
            )
        }

        val renderer = ChartRenderer(chart = chart, data = data, dates = dates, colors = getColorsFor(chart.items))
        val plot = renderer.render()
        ggsave(plot, "$id.png", path = "output/" + projectConfig.name)
    }

    private fun getColorsFor(items: List<ChartStack>): List<String> {
        val colors = mutableListOf<String>()

        val totalColors = items.flatMap { it.items }.size
        if (totalColors <= baseColors.size) {
            colors.addAll(baseColors.subList(0, totalColors))
        } else {
            items.forEachIndexed { index, chartStack ->
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