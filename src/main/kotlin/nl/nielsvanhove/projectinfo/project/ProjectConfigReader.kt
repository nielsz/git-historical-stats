package nl.nielsvanhove.projectinfo.project

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import nl.nielsvanhove.projectinfo.charts.Chart
import nl.nielsvanhove.projectinfo.charts.ChartStack
import nl.nielsvanhove.projectinfo.measurements.MeasurementConfig
import nl.nielsvanhove.projectinfo.measurements.MeasurementConfig.*
import java.io.File

object ProjectConfigReader {

    fun read(projectName: String): ProjectConfig {
        val filename = "projects/$projectName.config.json"

        val rootObject = Json.decodeFromString<JsonObject>(File(filename).readText())

        val repo = rootObject["repo"]!!.jsonPrimitive.content
        val branch = rootObject["branch"]!!.jsonPrimitive.content

        val measurementsJson = rootObject["measurements"] as JsonArray
        val patterns = mutableListOf<MeasurementConfig>()
        for (measurementJson in measurementsJson) {
            val type = (measurementJson as JsonObject)["type"]!!.jsonPrimitive.content
            when (type) {
                "bash" -> {
                    patterns.add(
                        BashMeasurementConfig(
                            key = measurementJson["key"]!!.jsonPrimitive.content,
                            command = measurementJson["command"]!!.jsonPrimitive.content,
                        )
                    )
                }
                "grep" -> {
                    patterns.add(
                        GrepMeasurementConfig(
                            key = measurementJson["key"]!!.jsonPrimitive.content,
                            pattern = measurementJson["pattern"]!!.jsonPrimitive.content,
                        )
                    )
                }
                "cloc" -> {
                    val filetypes = measurementJson["filetypes"]!!.jsonArray.map { it.jsonPrimitive.content }

                    patterns.add(
                        ClocMeasurementConfig(
                            key = measurementJson["key"]!!.jsonPrimitive.content,
                            filetypes = filetypes,
                            folder = measurementJson["folder"]!!.jsonPrimitive.content,
                        )
                    )
                }
            }
        }

        val filetypes = mutableListOf<String>()
        for (jsonElement in rootObject["filetypes"]!!.jsonArray) {
            filetypes.add(jsonElement.jsonPrimitive.content)
        }

        val charts = mutableListOf<Chart>()
        for (jsonElement in rootObject["charts"]!!.jsonArray) {
            val item = jsonElement.jsonObject

            val chartStacks = mutableListOf<ChartStack>()
            for (jsonElement in item["items"]!!.jsonArray) {
                chartStacks.add(ChartStack((jsonElement as JsonArray).map { it.jsonPrimitive.content }))
            }

            val chart = Chart(
                id = item["id"]!!.jsonPrimitive.content,
                title = item["title"]!!.jsonPrimitive.content,
                items = chartStacks
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
}