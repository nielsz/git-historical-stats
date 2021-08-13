package nl.nielsvanhove.projectinfo.core

import nl.nielsvanhove.projectinfo.project.ProjectConfig
import java.time.OffsetDateTime

class GitWrapper(private val projectConfig: ProjectConfig, private val commandExecutor: CommandExecutor) {

    fun reset() {
        val c = "git reset --hard HEAD"
        val command = listOf("bash", "-c") + listOf(c)
        val output = commandExecutor.execute(command).trim()
    }

    fun log(): List<LogItem> {
        val c = "git log ${projectConfig.branch} --oneline --pretty=format:\"%h %cI %an\""
        val command = listOf("bash", "-c") + listOf(c)
        val commits = commandExecutor.execute(command)
            .lines()
            .map { logLine ->
                val splittedData = logLine.split(" ")
                LogItem(
                    commitHash = splittedData[0],
                    committerDate = OffsetDateTime.parse(splittedData[1]),
                    authorEmail = splittedData[2]
                )
            }

        return commits
    }

    fun checkout(hash: String) {
        commandExecutor.execute(listOf("git", "checkout", hash))
        commandExecutor.execute(listOf("git", "clean", "-fd"))

    }
}

data class LogItem(val commitHash: String, val committerDate: OffsetDateTime, val authorEmail: String)
