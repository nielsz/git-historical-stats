package nl.nielsvanhove.projectinfo

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.system.exitProcess

class MeasurementExecutor(
    val projectConfig: ProjectConfig,
    val commandExecutor: CommandExecutor,
    val gitWrapper: GitWrapper
) {
    fun executeIfNeeded(commit: JsonElement) {

        val hash = (commit as JsonObject)["commitHash"]!!.jsonPrimitive.content
        val measurements = commit["measurements"]
        println("executeIfNeeded! $hash on $commit")

        var shouldCheckout = false
        if (measurements == null) {
            println("Measurements == null, should check out!")
            shouldCheckout = true
        } else {
            for (measurement in projectConfig.measurements) {
                if (!(measurements!! as JsonObject).containsKey(measurement.key)) {
                    shouldCheckout = true
                    break
                }
            }
        }

        if (shouldCheckout) {
            gitWrapper.checkout(hash)
            for (measurement in projectConfig.measurements) {
                if (!(measurements!! as JsonObject).containsKey(measurement.key)) {
                    val amount = executeMeasurement(measurement)
                    println("got amount: $amount")
                }
            }
            exitProcess(3)
        }
    }

    private fun executeMeasurement(measurement: MeasurementConfig): Int {
        println("executing $measurement")
        if (measurement.type == "grep") {
            val fileTypeIncludes = projectConfig.filetypes.map {
                "--include *.$it"
            }.joinToString(" ")

            val c = "grep --recursive " + fileTypeIncludes + " -i -e '" + measurement.pattern + "' ."
            val command = listOf("bash", "-c") + listOf(c)
            return commandExecutor.execute(command).lines().size
        }

        return -1
    }
}