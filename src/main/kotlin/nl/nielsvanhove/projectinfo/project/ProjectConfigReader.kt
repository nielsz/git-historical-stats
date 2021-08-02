package nl.nielsvanhove.projectinfo.project

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
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
            if (type == "grep") {
                patterns.add(
                    GrepMeasurementConfig(
                        key = measurementJson["key"]!!.jsonPrimitive.content,
                        pattern = measurementJson["pattern"]!!.jsonPrimitive.content,
                    )
                )
            } else if (type == "filesInFolder") {
                patterns.add(
                    FilesInFolderMeasurementConfig(
                        key = measurementJson["key"]!!.jsonPrimitive.content,
                        folderPattern = measurementJson["folder_pattern"]!!.jsonPrimitive.content,
                        filePattern = measurementJson["file_pattern"]!!.jsonPrimitive.content,
                    )
                )
            }else if (type == "linesInFile") {
                patterns.add(
                    LinesInFileMeasurementConfig(
                        key = measurementJson["key"]!!.jsonPrimitive.content,
                        filePattern = measurementJson["file_pattern"]!!.jsonPrimitive.content,
                        linePattern = measurementJson["line_pattern"]!!.jsonPrimitive.content,
                    )
                )
            }


        }

        val filetypes = mutableListOf<String>()
        for (jsonElement in rootObject["filetypes"]!!.jsonArray) {
            filetypes.add(jsonElement.jsonPrimitive.content)
        }

        return ProjectConfig(name = projectName, repo = File(repo), branch = branch, filetypes, measurements = patterns)
    }
}