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


fun main(args: Array<String>) {

    val arguments = ArgParser(args).parseInto(::ApplicationArgs)
    val projectConfig = ProjectConfigReader.read(arguments.project)
    val commandExecutor = CommandExecutor(projectConfig)
    val gitWrapper = GitWrapper(projectConfig, commandExecutor)

    gitWrapper.reset()
    syncCommits(projectConfig, gitWrapper)
    executeMeasurements(projectConfig, commandExecutor, gitWrapper, arguments)
    generateCharts(projectConfig)
    gitWrapper.reset()
}

fun generateCharts(projectConfig: ProjectConfig) {
    val projectData = ProjectDataReaderWriter.read(projectConfig.name)

    val chartGenerator = ChartGenerator(projectConfig, projectData)
    for (chart in projectConfig.charts) {
        println("generating $chart")
        chartGenerator.generate(chart)
    }
}

fun executeMeasurements(
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

fun syncCommits(projectConfig: ProjectConfig, gitWrapper: GitWrapper) {

    val commits = gitWrapper.log()
    val importantCommitFilter = ImportantCommitFilter(commits)
    val annotatedCommits = importantCommitFilter.filterImportantCommits()

    val projectData = ProjectDataReaderWriter.read(projectConfig.name)
    projectData.syncCommits(annotatedCommits)
    ProjectDataReaderWriter.write(projectConfig.name, projectData)
}


class ApplicationArgs(parser: ArgParser) {

    val project by parser.storing("-p", "--project", help = "project name")

    val rerunMeasurement by parser.storing("--rerunMeasurement", help = "Rerun one measurement")
        .default(null)

    val runAllMeasurements by parser.flagging("--runAllMeasurements", help = "(Re)run all measurements")
        .default(defaultValue = false)
}