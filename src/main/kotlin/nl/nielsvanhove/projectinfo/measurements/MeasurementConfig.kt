package nl.nielsvanhove.projectinfo.measurements

sealed class MeasurementConfig(open val key: String) {
    data class GrepMeasurementConfig(override val key: String, val pattern: String) : MeasurementConfig(key)
    data class ClocMeasurementConfig(override val key: String, val filetypes: List<String>, val folder: String) : MeasurementConfig(key)
    data class FilesInFolderMeasurementConfig(override val key: String, val folderPattern: String, val filePattern: String) : MeasurementConfig(key)
    data class LinesInFileMeasurementConfig(override val key: String, val filePattern: String, val linePattern: String) : MeasurementConfig(key)
}
