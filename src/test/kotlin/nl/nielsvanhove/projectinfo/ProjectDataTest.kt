package nl.nielsvanhove.projectinfo

import kotlinx.serialization.json.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime


class ProjectDataTest {

    @Test
    fun `When no initial commits and no new commits, Then the list will be empty`() {

        // Given
        //val commits = JsonArray(listOf(projectDataCommit("a"), projectDataCommit("b")))
        val commits = JsonArray(emptyList())
        val projectData = ProjectData(commits = commits)

        // When
        projectData.syncCommits(annotatedCommits = listOf())

        // Then
        assertEquals(emptyList<JsonArray>(), projectData.commits)
    }

    @Test
    fun `When two initial commits and no new commits, Then the list will be empty`() {

        // Given
        val commits = JsonArray(listOf(projectDataCommit("a"), projectDataCommit("b")))
        val projectData = ProjectData(commits = commits)

        // When
        projectData.syncCommits(annotatedCommits = listOf())

        // Then
        assertEquals(emptyList<JsonArray>(), projectData.commits)
    }

    @Test
    fun `When no initial commits and two new commits, Then the list will the two new items`() {

        // Given
        val commits = JsonArray(emptyList())
        val projectData = ProjectData(commits = commits)

        // When
        projectData.syncCommits(annotatedCommits = listOf(annotatedCommit("a"), annotatedCommit("b")))

        // Then
        assertEquals(2, projectData.commits.size)
        assertEquals("a", (projectData.commits[0] as JsonObject)["commitHash"]!!.jsonPrimitive.content)
        assertEquals("b", (projectData.commits[1] as JsonObject)["commitHash"]!!.jsonPrimitive.content)
    }

    @Test
    fun `When there is one initial commits with custom data, Then the list will retain that custom data`() {
        val content = mutableMapOf<String, JsonElement>()
        content["commitHash"] = JsonPrimitive("q")
        content["committerDate"] = JsonPrimitive(OffsetDateTime.now().toString())
        content["otherStuff"] = JsonArray(content = listOf(JsonPrimitive("p"), JsonPrimitive(18)))
        val jsonObject = JsonObject(content = content)

        // Given
        val commits = JsonArray(listOf(jsonObject))
        val projectData = ProjectData(commits = commits)

        // When
        projectData.syncCommits(annotatedCommits = listOf(annotatedCommit("a"), annotatedCommit("q")))

        println(projectData.commits)
        // Then
        assertEquals(2, projectData.commits.size)
        val q = (projectData.commits[0] as JsonObject)
        assertEquals("q", q["commitHash"]!!.jsonPrimitive.content)
        assertEquals("p", q["otherStuff"]!!.jsonArray[0].jsonPrimitive.content)
        assertEquals("18", q["otherStuff"]!!.jsonArray[1].jsonPrimitive.content)
        assertEquals("a", (projectData.commits[1] as JsonObject)["commitHash"]!!.jsonPrimitive.content)


    }

    private fun annotatedCommit(hash: String): AnnotatedCommit {
        return AnnotatedCommit(
            commitHash = hash,
            committerDate = OffsetDateTime.now(),
            lastOfYear = false,
            lastOfQuarter = false,
            lastOfMonth = false,
            isFirstCommit = false,
            isLastCommit = false
        )
    }

    private fun projectDataCommit(hash: String): JsonObject {
        val content = mutableMapOf<String, JsonPrimitive>()
        content["commitHash"] = JsonPrimitive(hash)
        val jsonObject = JsonObject(content = content)
        return jsonObject
    }
}