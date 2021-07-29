package nl.nielsvanhove.projectinfo

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.File

object ProjectDataReader {

    fun read(projectName: String): ProjectData {
        val filename = "projects/$projectName.data.json"
        val content = File(filename).readText()
        val rootObject = Json.decodeFromString<JsonArray>(content)
        return ProjectData(commits = rootObject)
    }

    fun write(projectName: String, projectData: ProjectData) {
        val content = Json {
            prettyPrint = true
        }.encodeToString(projectData.commits)

        val filename = "projects/$projectName.data.json"
        File(filename).writeText(content)
    }
}