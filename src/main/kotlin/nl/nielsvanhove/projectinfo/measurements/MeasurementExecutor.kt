package nl.nielsvanhove.projectinfo.measurements

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import nl.nielsvanhove.projectinfo.core.CommandExecutor
import nl.nielsvanhove.projectinfo.core.GitWrapper
import nl.nielsvanhove.projectinfo.measurements.MeasurementConfig.*
import nl.nielsvanhove.projectinfo.project.ProjectConfig
import java.lang.RuntimeException

class MeasurementExecutor(
    val projectConfig: ProjectConfig,
    val commandExecutor: CommandExecutor,
    val gitWrapper: GitWrapper
) {
    fun executeIfNeeded(commit: JsonObject, runAllMeasurements: Boolean): JsonObject? {

        val previousMeasurements = commit["measurements"] as JsonObject

        if (!shouldCheckout(previousMeasurements) && !runAllMeasurements) {
            return null
        }

        println("updating " + commit["committerDate"])
        gitWrapper.checkout(commit["commitHash"]!!.jsonPrimitive.content)

        val measurementsToRun = mutableListOf<MeasurementConfig>()
        for (measurement in projectConfig.measurements) {
            if (!(previousMeasurements).containsKey(measurement.key) || runAllMeasurements) {
                measurementsToRun.add(measurement)
            }
        }

        val result = executeMeasurements(measurementsToRun)
        val measurements =
            JsonObject(mapOf("measurements" to JsonObject((commit["measurements"] as JsonObject) + result)))
        return JsonObject(content = JsonObject(commit + measurements))
    }

    private fun executeMeasurements(toRun: MutableList<MeasurementConfig>): JsonObject {
        val map = mutableMapOf<String, JsonElement>()
        for (item in toRun) {
            map[item.key] = JsonPrimitive(executeMeasurement(item))
        }

        return JsonObject(content = map)
    }


    private fun executeMeasurement(measurement: MeasurementConfig): Int {
        return when (measurement) {
            is GrepMeasurementConfig -> {
                val executor = GrepExecutor(projectConfig, commandExecutor, measurement)
                executor()
            }
            is FilesInFolderMeasurementConfig -> {
                val executor = FilesInFolderExecutor(projectConfig, commandExecutor, measurement)
                executor()
            }
            is LinesInFileMeasurementConfig -> {
                val executor = LinesInFileExecutor(projectConfig, commandExecutor, measurement)
                executor()
            }
            else -> throw RuntimeException("Unknown measurementConfig")
        }
    }


    private fun shouldCheckout(previousMeasurements: JsonObject): Boolean {
        for (measurement in projectConfig.measurements) {
            if (!previousMeasurements.containsKey(measurement.key)) {
                return true
            }
        }

        return false
    }
}