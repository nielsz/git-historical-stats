package nl.nielsvanhove.projectinfo.project

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.File

object ProjectDataReaderWriter {

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

        write(projectName, content)
    }

    fun write(projectName: String, specificCommit: JsonObject) {
        val hash = specificCommit["commitHash"]!!.jsonPrimitive.content
        val projectData = read(projectName)

        val updatedContent = (projectData.commits.filterNot { (it as JsonObject)["commitHash"]!!.jsonPrimitive.content == hash} + specificCommit).sortedBy { ((it as JsonObject)["committerDate"] as JsonPrimitive).content }

        val content = Json {
            prettyPrint = true
        }.encodeToString(updatedContent)

        write(projectName, content)
    }

    fun write(projectName: String, content: String) {
        val filename = "projects/$projectName.data.json"
        println("writing to $filename")
        File(filename).writeText(content)
    }
}