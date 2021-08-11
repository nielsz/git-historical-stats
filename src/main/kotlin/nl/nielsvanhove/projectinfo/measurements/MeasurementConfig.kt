package nl.nielsvanhove.projectinfo.measurements

sealed class MeasurementConfig(open val key: String) {
    data class BashMeasurementConfig(override val key: String, val command: String) : MeasurementConfig(key)
    data class GrepMeasurementConfig(override val key: String, val pattern: String) : MeasurementConfig(key)
    data class ClocMeasurementConfig(override val key: String, val filetypes: List<String>, val folder: String) : MeasurementConfig(key)
}
