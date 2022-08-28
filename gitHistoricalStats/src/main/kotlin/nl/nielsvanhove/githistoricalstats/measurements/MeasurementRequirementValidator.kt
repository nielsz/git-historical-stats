package nl.nielsvanhove.githistoricalstats.measurements

import nl.nielsvanhove.githistoricalstats.core.CommandExecutor
import nl.nielsvanhove.githistoricalstats.measurements.MeasurementConfig.ClocMeasurementConfig
import nl.nielsvanhove.githistoricalstats.project.ProjectConfig

class MeasurementRequirementValidator(
    private val projectConfig: ProjectConfig,
    private val commandExecutor: CommandExecutor
) {

    fun validate() {
        // If we're running cloc measurements, better make sure cloc is installed.
        val hasClocMeasurements = projectConfig.measurements.filterIsInstance<ClocMeasurementConfig>().isNotEmpty()
        if (hasClocMeasurements && commandExecutor.execute(listOf("which", "cloc")).isEmpty()) {
            throw RuntimeException("cloc isn't installed. See https://github.com/AlDanial/cloc#install-via-package-manager")
        }
    }
}