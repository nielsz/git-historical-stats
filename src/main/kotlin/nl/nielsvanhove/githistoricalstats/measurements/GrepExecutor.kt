package nl.nielsvanhove.githistoricalstats.measurements

import nl.nielsvanhove.githistoricalstats.core.CommandExecutor
import nl.nielsvanhove.githistoricalstats.measurements.MeasurementConfig.GrepMeasurementConfig
import nl.nielsvanhove.githistoricalstats.project.ProjectConfig

class GrepExecutor(
    private val projectConfig: ProjectConfig,
    private val commandExecutor: CommandExecutor,
    private val measurement: GrepMeasurementConfig
) {
    operator fun invoke(): Int {
        val fileTypeIncludes = projectConfig.filetypes.joinToString(" ") { "--include=*.$it" }
        val c = "grep --recursive " + fileTypeIncludes + " -i -e '" + measurement.pattern + "' . | wc -l"
        val command = listOf("bash", "-c") + listOf(c)
        val output = commandExecutor.execute(command).trim()
        return output.toInt()
    }
}