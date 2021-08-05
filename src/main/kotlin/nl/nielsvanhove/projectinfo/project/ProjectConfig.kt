package nl.nielsvanhove.projectinfo.project

import nl.nielsvanhove.projectinfo.charts.Chart
import nl.nielsvanhove.projectinfo.measurements.MeasurementConfig
import java.io.File

data class ProjectConfig(
    val name: String,
    val repo: File,
    val branch: String,
    val filetypes: List<String>,
    val measurements: List<MeasurementConfig>,
    val charts: List<Chart>
)