package nl.nielsvanhove.githistoricalstats.project

import nl.nielsvanhove.githistoricalstats.charts.Chart
import nl.nielsvanhove.githistoricalstats.measurements.MeasurementConfig
import java.io.File

data class ProjectConfig(
    val name: String,
    val repo: File,
    val branch: String,
    val filetypes: List<String>,
    val measurements: List<MeasurementConfig>,
    val charts: List<Chart>
)