package nl.nielsvanhove.githistoricalstats.measurements

import nl.nielsvanhove.githistoricalstats.core.CommandExecutor
import nl.nielsvanhove.githistoricalstats.measurements.MeasurementConfig.BashMeasurementConfig
import nl.nielsvanhove.githistoricalstats.project.ProjectConfig

class BashExecutor(
    private val projectConfig: ProjectConfig,
    private val commandExecutor: CommandExecutor,
    private val measurement: BashMeasurementConfig
) {
    operator fun invoke(): Int {
        val command = listOf("bash", "-c") + listOf(measurement.command)
        val output = commandExecutor.execute(command).trim()
        return output.toInt()
    }
}