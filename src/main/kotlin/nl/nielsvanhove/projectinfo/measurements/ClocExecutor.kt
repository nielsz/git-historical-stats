package nl.nielsvanhove.projectinfo.measurements

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import nl.nielsvanhove.projectinfo.core.CommandExecutor
import nl.nielsvanhove.projectinfo.measurements.MeasurementConfig.ClocMeasurementConfig
import nl.nielsvanhove.projectinfo.project.ProjectConfig
import kotlin.system.exitProcess

class ClocExecutor(
    private val projectConfig: ProjectConfig,
    private val commandExecutor: CommandExecutor,
    private val measurement: ClocMeasurementConfig
) {
    operator fun invoke(): Int {
        val fileTypeIncludes = if (measurement.filetypes.isNotEmpty()) {
            "--include-ext=" + measurement.filetypes.joinToString(",")
        } else {
            ""
        }

        val c = "cloc $fileTypeIncludes --json ${measurement.folder}"
        val command = listOf("bash", "-c") + listOf(c)
        val output = commandExecutor.execute(command).trim()

        if (output.isNotEmpty()) {
            val x = Json.parseToJsonElement(output) as JsonObject
            return (x["SUM"]!! as JsonObject)["code"]!!.jsonPrimitive.content.toInt()

        }
        return 0
    }
}