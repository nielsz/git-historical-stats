package nl.nielsvanhove.projectinfo

import java.time.OffsetDateTime
import kotlin.system.exitProcess

class GitWrapper(private val projectConfig: ProjectConfig, private val commandExecutor: CommandExecutor) {

    fun log(): List<LogItem> {
        val command = listOf("git", "log", projectConfig.branch, "--oneline", "--pretty=format:%h %cI %ae")
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
        println("checking out $hash")
        commandExecutor.execute(listOf("git", "checkout", hash))
        commandExecutor.execute(listOf("git", "clean", "-fd"))

    }
}

data class LogItem(val commitHash: String, val committerDate: OffsetDateTime, val authorEmail: String)
