package nl.nielsvanhove.projectinfo

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
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
            patterns.add(
                MeasurementConfig(
                    type = (measurementJson as JsonObject)["type"]!!.jsonPrimitive.content,
                    key = measurementJson["key"]!!.jsonPrimitive.content,
                    pattern = measurementJson["pattern"]!!.jsonPrimitive.content,
                )
            )
        }

        val filetypes = mutableListOf<String>()
        for (jsonElement in rootObject["filetypes"]!!.jsonArray) {
            filetypes.add(jsonElement.jsonPrimitive.content)
        }

        return ProjectConfig(name = projectName, repo = File(repo), branch = branch, filetypes, measurements = patterns)
    }
}