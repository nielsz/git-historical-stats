package nl.nielsvanhove.githistoricalstats.core

import nl.nielsvanhove.githistoricalstats.project.ProjectConfig
import java.io.File
import java.io.IOException

class CommandExecutor(val projectConfig: ProjectConfig) {

    @Throws(IOException::class)
    fun execute(command: List<String>, directory: File = projectConfig.repo, printError: Boolean = false): String {

        println("executing ${command.joinToString(" ")}")
        val process = ProcessBuilder(command)
            .directory(directory)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        if (printError) {
            println("Error: " + process.errorStream.bufferedReader().readText())
        }

        return process.inputStream.bufferedReader().readText()
    }
}