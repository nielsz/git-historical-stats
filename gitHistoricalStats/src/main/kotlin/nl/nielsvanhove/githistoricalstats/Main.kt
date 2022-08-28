package nl.nielsvanhove.githistoricalstats

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import kotlinx.serialization.json.JsonObject
import nl.nielsvanhove.githistoricalstats.charts.ChartGenerator
import nl.nielsvanhove.githistoricalstats.core.CommandExecutor
import nl.nielsvanhove.githistoricalstats.core.GitWrapper
import nl.nielsvanhove.githistoricalstats.core.ImportantCommitFilter
import nl.nielsvanhove.githistoricalstats.measurements.MeasurementExecutor
import nl.nielsvanhove.githistoricalstats.project.ProjectConfig
import nl.nielsvanhove.githistoricalstats.project.ProjectConfigReader
import nl.nielsvanhove.githistoricalstats.project.ProjectDataReaderWriter
import java.io.File
import nl.nielsvanhove.githistoricalstats.measurements.MeasurementRequirementValidator

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            createFoldersIfNeeded()

            val arguments = ArgParser(args).parseInto(::ApplicationArgs)
            val projectConfig = ProjectConfigReader.read(arguments.project)
            projectConfig.validate()

            val commandExecutor = CommandExecutor(projectConfig)

            MeasurementRequirementValidator(projectConfig, commandExecutor).validate()

            val gitWrapper = GitWrapper(projectConfig, commandExecutor)
            gitWrapper.reset()

            syncCommits(projectConfig, gitWrapper)
            executeMeasurements(projectConfig, commandExecutor, gitWrapper, arguments)
            generateCharts(projectConfig)

            gitWrapper.reset()
        }
    }
}

private fun generateCharts(projectConfig: ProjectConfig) {
    val projectData = ProjectDataReaderWriter.read(projectConfig.name)

    val chartGenerator = ChartGenerator(projectConfig, projectData)
    for (chart in projectConfig.charts) {
        println("generating $chart")
        chartGenerator.generate(chart)
    }
}

private fun executeMeasurements(
    projectConfig: ProjectConfig,
    commandExecutor: CommandExecutor,
    gitWrapper: GitWrapper,
    args: ApplicationArgs
) {
    val projectData = ProjectDataReaderWriter.read(projectConfig.name)
    val measurementExecutor = MeasurementExecutor(projectConfig, commandExecutor, gitWrapper)
    for (commit in projectData.commits) {


        val updatedCommit =
            measurementExecutor.executeIfNeeded(
                commit as JsonObject,
                forceUpdateAll = args.runAllMeasurements,
                forceSpecificUpdate = args.rerunMeasurement
            )
        if (updatedCommit != null) {
            ProjectDataReaderWriter.write(projectConfig.name, updatedCommit)
        }
    }
}

private fun syncCommits(projectConfig: ProjectConfig, gitWrapper: GitWrapper) {

    val commits = gitWrapper.log()
    val importantCommitFilter = ImportantCommitFilter(commits)
    val annotatedCommits = importantCommitFilter.filterImportantCommits()

    val projectData = ProjectDataReaderWriter.read(projectConfig.name)
    projectData.syncCommits(annotatedCommits)
    ProjectDataReaderWriter.write(projectConfig.name, projectData)
}

private fun createFoldersIfNeeded() {
    val folders = listOf("projects", "repos", "output")
    folders.filter { !File(it).exists() }.forEach { File(it).mkdir() }
}

class ApplicationArgs(parser: ArgParser) {

    val project by parser.storing("-p", "--project", help = "project name")

    val rerunMeasurement by parser.storing("--rerunMeasurement", help = "Rerun one measurement")
        .default(null)

    val runAllMeasurements by parser.flagging("--runAllMeasurements", help = "(Re)run all measurements")
        .default(defaultValue = false)
}