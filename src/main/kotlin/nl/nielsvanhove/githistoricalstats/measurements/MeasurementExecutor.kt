package nl.nielsvanhove.githistoricalstats.measurements

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import nl.nielsvanhove.githistoricalstats.core.CommandExecutor
import nl.nielsvanhove.githistoricalstats.core.GitWrapper
import nl.nielsvanhove.githistoricalstats.measurements.MeasurementConfig.*
import nl.nielsvanhove.githistoricalstats.project.ProjectConfig

class MeasurementExecutor(
    val projectConfig: ProjectConfig,
    val commandExecutor: CommandExecutor,
    val gitWrapper: GitWrapper
) {
    fun executeIfNeeded(commit: JsonObject, forceUpdateAll: Boolean, forceSpecificUpdate: String?): JsonObject? {

        val previousMeasurements = commit["measurements"] as JsonObject

        if (!shouldCheckout(previousMeasurements) && !forceUpdateAll && forceSpecificUpdate.isNullOrEmpty()) {
            return null
        }

        println("updating " + commit["committerDate"])
        gitWrapper.checkout(commit["commitHash"]!!.jsonPrimitive.content)

        val measurementsToRun = mutableListOf<MeasurementConfig>()
        for (measurement in projectConfig.measurements) {
            if (!(previousMeasurements).containsKey(measurement.key) || forceUpdateAll || forceSpecificUpdate == measurement.key) {
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
            is BashMeasurementConfig -> {
                val executor = BashExecutor(projectConfig, commandExecutor, measurement)
                executor()
            }
            is GrepMeasurementConfig -> {
                val executor = GrepExecutor(projectConfig, commandExecutor, measurement)
                executor()
            }
            is ClocMeasurementConfig -> {
                val executor = ClocExecutor(projectConfig, commandExecutor, measurement)
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