package nl.nielsvanhove.projectinfo.measurements

import nl.nielsvanhove.projectinfo.core.CommandExecutor
import nl.nielsvanhove.projectinfo.measurements.MeasurementConfig.BashMeasurementConfig
import nl.nielsvanhove.projectinfo.project.ProjectConfig

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