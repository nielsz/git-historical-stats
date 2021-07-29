package nl.nielsvanhove.projectinfo

import java.io.File

data class ProjectConfig(val name: String, val repo: File, val branch: String, val filetypes: List<String>,  val measurements: List<MeasurementConfig>)