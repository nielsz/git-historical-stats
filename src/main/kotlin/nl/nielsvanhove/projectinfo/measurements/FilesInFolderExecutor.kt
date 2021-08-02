package nl.nielsvanhove.projectinfo.measurements

import nl.nielsvanhove.projectinfo.core.CommandExecutor
import nl.nielsvanhove.projectinfo.measurements.MeasurementConfig.FilesInFolderMeasurementConfig
import nl.nielsvanhove.projectinfo.project.ProjectConfig

class FilesInFolderExecutor(
    private val projectConfig: ProjectConfig,
    private val commandExecutor: CommandExecutor,
    private val measurement: FilesInFolderMeasurementConfig
) {
    operator fun invoke(): Int {
        val c = "find . -type d -name '" + measurement.folderPattern + "' -exec ls -l {} \\; | grep '" + measurement.filePattern+ "' | wc -l"
        val command = listOf("bash", "-c") + listOf(c)
        val output = commandExecutor.execute(command).trim()
        return output.toInt()
    }
}