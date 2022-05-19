package nl.nielsvanhove.githistoricalstats.project

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import nl.nielsvanhove.githistoricalstats.charts.Chart
import nl.nielsvanhove.githistoricalstats.charts.ChartLegend
import nl.nielsvanhove.githistoricalstats.charts.ChartStack
import nl.nielsvanhove.githistoricalstats.measurements.MeasurementConfig
import nl.nielsvanhove.githistoricalstats.measurements.MeasurementConfig.*
import java.io.File
import java.io.FileNotFoundException

object ProjectConfigReader {

    fun read(projectName: String): ProjectConfig {
        val filename = "projects/$projectName.config.json"

        val rootObject = try {
            Json.decodeFromString<JsonObject>(File(filename).readText())
        } catch (ex: FileNotFoundException) {
            val absolutePath = File("").absolutePath
            System.err.println("Can't find project. Create $absolutePath/$filename in the projects folder.")
            System.err.println(
                "{\n" +
                        "  \"repo\": \"My local path to the repository\",\n" +
                        "  \"branch\": \"develop\",\n" +
                        "  \"filetypes\":[\"kt\",\"java\"],\n" +
                        "  \"charts\":[],\n" +
                        "  \"measurements\":[]\n" +
                        "}"
            )
            throw ex
        }

        val repo = rootObject["repo"]!!.jsonPrimitive.content
        val branch = rootObject["branch"]!!.jsonPrimitive.content

        val measurementsJson = rootObject["measurements"] as JsonArray?
        val patterns = mutableListOf<MeasurementConfig>()
        if (measurementsJson != null) {
            for (measurementJson in measurementsJson) {
                assert((measurementJson as JsonObject)["type"] != null, "Missing `type` string field in measurement.")
                assert(measurementJson["key"] != null, "Missing `key` string field in measurement.")
                val type = measurementJson["type"]!!.jsonPrimitive.content
                when (type) {
                    "bash" -> {
                        assert(measurementJson["command"] != null, "Bash measurement needs a `command` string field.")
                        patterns.add(
                            BashMeasurementConfig(
                                key = measurementJson["key"]!!.jsonPrimitive.content,
                                command = measurementJson["command"]!!.jsonPrimitive.content,
                            )
                        )
                    }
                    "grep" -> {
                        assert(measurementJson["pattern"] != null, "Grep measurement needs a `pattern` string field.")
                        val types = if (measurementJson.containsKey("filetypes")) {
                            measurementJson["filetypes"]!!.jsonArray.map { it.jsonPrimitive.content }
                        } else {
                            emptyList()
                        }

                        patterns.add(
                            GrepMeasurementConfig(
                                key = measurementJson["key"]!!.jsonPrimitive.content,
                                filetypes = types,
                                pattern = measurementJson["pattern"]!!.jsonPrimitive.content,
                            )
                        )
                    }
                    "cloc" -> {
                        assert(measurementJson["filetypes"] != null, "Cloc measurement needs a `filetypes` array field.")
                        val filetypes = measurementJson["filetypes"]!!.jsonArray.map { it.jsonPrimitive.content }
                        val folder = if (measurementJson.containsKey("folder")) {
                            measurementJson["folder"]!!.jsonPrimitive.content
                        } else "."

                        patterns.add(
                            ClocMeasurementConfig(
                                key = measurementJson["key"]!!.jsonPrimitive.content,
                                filetypes = filetypes,
                                folder = folder
                            )
                        )
                    }
                }
            }
        }

        val filetypes = mutableListOf<String>()
        for (jsonElement in rootObject["filetypes"]!!.jsonArray) {
            filetypes.add(jsonElement.jsonPrimitive.content)
        }

        val charts = mutableListOf<Chart>()
        val chartsArray = rootObject["charts"]?.jsonArray ?: emptyList()
        for (jsonElement in chartsArray) {
            val item = jsonElement.jsonObject
            assert(item["items"] != null, "An `items` array is needed to plot a chart.")
            assert(item["items"] is JsonArray, "The `items` element needs to be an array. One item represents one bar.")

            val chartStacks = mutableListOf<ChartStack>()
            for (jsonElement in item["items"]!!.jsonArray) {
                assert(jsonElement is JsonArray, "The `items` sub-element needs to be an array. One item represents one item in the stacked bar.")
                chartStacks.add(ChartStack((jsonElement as JsonArray).map { it.jsonPrimitive.content }))
            }

            val legend = item["legend"] as JsonObject?
            val chartLegend = if (legend != null) {
                val legendTitle = if (legend.containsKey("title")) {
                    (legend["title"] as JsonPrimitive).content
                } else {
                    null
                }
                val legendItems = (legend["items"] as JsonArray).map { it.jsonPrimitive.content }
                ChartLegend(title = legendTitle, items = legendItems)
            } else {
                null
            }

            val chart = Chart(
                id = item["id"]!!.jsonPrimitive.content,
                title = item["title"]!!.jsonPrimitive.content,
                items = chartStacks,
                legend = chartLegend
            )
            charts.add(chart)
        }

        return ProjectConfig(
            name = projectName,
            repo = File(repo),
            branch = branch,
            filetypes,
            measurements = patterns,
            charts = charts
        )
    }

    private fun assert(assertion: Boolean, error: String) {
        if (!assertion) throw IllegalArgumentException(error)
    }
}
