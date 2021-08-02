package nl.nielsvanhove.projectinfo.measurements

import nl.nielsvanhove.projectinfo.core.CommandExecutor
import nl.nielsvanhove.projectinfo.measurements.MeasurementConfig.LinesInFileMeasurementConfig
import nl.nielsvanhove.projectinfo.project.ProjectConfig
import kotlin.system.exitProcess

class LinesInFileExecutor(
    private val projectConfig: ProjectConfig,
    private val commandExecutor: CommandExecutor,
    private val measurement: LinesInFileMeasurementConfig
) {
    operator fun invoke(): Int {
        val c = "find . -name '" + measurement.filePattern + "' -exec grep '" + measurement.linePattern + "' {} + | wc -l"
        val command = listOf("bash", "-c") + listOf(c)
        val output = commandExecutor.execute(command).trim()
        return output.toInt()
    }
}