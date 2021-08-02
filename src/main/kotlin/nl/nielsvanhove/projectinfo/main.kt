package nl.nielsvanhove.projectinfo

import com.github.sh0nk.matplotlib4j.Plot
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import kotlinx.serialization.json.JsonObject
import nl.nielsvanhove.projectinfo.core.CommandExecutor
import nl.nielsvanhove.projectinfo.core.GitWrapper
import nl.nielsvanhove.projectinfo.core.ImportantCommitFilter
import nl.nielsvanhove.projectinfo.measurements.MeasurementExecutor
import nl.nielsvanhove.projectinfo.project.ProjectConfig
import nl.nielsvanhove.projectinfo.project.ProjectConfigReader
import nl.nielsvanhove.projectinfo.project.ProjectDataReaderWriter
import java.util.*
import kotlin.system.exitProcess


fun main(args: Array<String>) {

    val arguments = ArgParser(args).parseInto(::MyArgs)

    println("runAllMeasurements: " + arguments.runAllMeasurements)

    val projectConfig = ProjectConfigReader.read(arguments.project)
    val commandExecutor = CommandExecutor(projectConfig)
    val gitWrapper = GitWrapper(projectConfig, commandExecutor)

    syncCommits(projectConfig, gitWrapper)
    executeMeasurements(projectConfig, commandExecutor, gitWrapper, arguments.runAllMeasurements)
}

fun executeMeasurements(
    projectConfig: ProjectConfig,
    commandExecutor: CommandExecutor,
    gitWrapper: GitWrapper,
    runAllMeasurements: Boolean
) {
    val projectData = ProjectDataReaderWriter.read(projectConfig.name)
    val measurementExecutor = MeasurementExecutor(projectConfig, commandExecutor, gitWrapper)
    for (commit in projectData.commits) {
        val updatedCommit = measurementExecutor.executeIfNeeded(commit as JsonObject, runAllMeasurements)
        if (updatedCommit != null) {
            ProjectDataReaderWriter.write(projectConfig.name, updatedCommit)
        }
    }
}

fun syncCommits(projectConfig: ProjectConfig, gitWrapper: GitWrapper) {

    val commits = gitWrapper.log()
    val importantCommitFilter = ImportantCommitFilter(commits)
    val annotatedCommits = importantCommitFilter.filterImportantCommits()

    val projectData = ProjectDataReaderWriter.read(projectConfig.name)
    projectData.syncCommits(annotatedCommits)
    ProjectDataReaderWriter.write(projectConfig.name, projectData)
}


fun chart() {
    val plt = Plot.create()
    plt.plot()
        .add(Arrays.asList(1.3, 2))
        .label("label")
        .linestyle("--")
    plt.xlabel("xlabel")
    plt.ylabel("ylabel")
    plt.text(0.5, 0.2, "text")
    plt.title("Title!")
    plt.legend()
    //plt.show()
    plt.savefig("my_example_plot.png")

    // Don't miss this line to output the file!
    plt.executeSilently();
}


class MyArgs(parser: ArgParser) {
    val project by parser.storing("project name")
    val runAllMeasurements by parser.flagging("--runAllMeasurements", help = "(Re)run all measurements")
        .default(defaultValue = false)
}