package nl.nielsvanhove.githistoricalstats.project

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.nielsvanhove.githistoricalstats.model.AnnotatedCommit


class ProjectData(var commits: JsonArray) {
    fun syncCommits(annotatedCommits: List<AnnotatedCommit>) {

        val finalCommits = mutableListOf<JsonObject>()
        for (commit in commits) {
            if (commit is JsonObject) {
                val commitHash = (commit["commitHash"] as JsonPrimitive).content
                val important = annotatedCommits.find { it.commitHash == commitHash }
                if (important != null) {

                    val extraData = mapOf<String, JsonElement>(
                        "isFirstCommit" to JsonPrimitive(important.isFirstCommit),
                        "isLastCommit" to JsonPrimitive(important.isLastCommit),
                        "isLastOfYear" to JsonPrimitive(important.lastOfYear),
                        "isLastOfQuarter" to JsonPrimitive(important.lastOfQuarter),
                        "isLastOfMonth" to JsonPrimitive(important.lastOfMonth),
                    )

                    finalCommits.add(JsonObject(commit + extraData))
                }
            }
        }

        for (annotatedCommit in annotatedCommits) {
            val alreadyExists =
                commits.any { ((it as JsonObject)["commitHash"] as JsonPrimitive).content == annotatedCommit.commitHash }
            if (!alreadyExists) {
                finalCommits.add(newJsonObject(annotatedCommit))
            }
        }

        finalCommits.sortBy { (it["committerDate"] as JsonPrimitive).content }

        commits = JsonArray(content = finalCommits)
    }

    fun newJsonObject(annotatedCommit: AnnotatedCommit): JsonObject {
        val map = mutableMapOf<String, JsonElement>()
        map["commitHash"] = JsonPrimitive(annotatedCommit.commitHash)
        map["committerDate"] = JsonPrimitive(annotatedCommit.committerDate.toString())
        map["isFirstCommit"] = JsonPrimitive(annotatedCommit.isFirstCommit)
        map["isLastCommit"] = JsonPrimitive(annotatedCommit.isLastCommit)
        map["isLastOfYear"] = JsonPrimitive(annotatedCommit.lastOfYear)
        map["isLastOfQuarter"] = JsonPrimitive(annotatedCommit.lastOfQuarter)
        map["isLastOfMonth"] = JsonPrimitive(annotatedCommit.lastOfMonth)
        map["measurements"] = JsonObject(emptyMap())
        return JsonObject(content = map)
    }

}