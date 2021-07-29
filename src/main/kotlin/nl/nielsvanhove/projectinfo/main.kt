package nl.nielsvanhove.projectinfo

import com.github.sh0nk.matplotlib4j.Plot
import com.xenomachina.argparser.ArgParser
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.*
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    println("Hello World! " + args.joinToString(", "))

    val arguments = ArgParser(args).parseInto(::MyArgs)

    val projectConfig = ProjectConfigReader.read(arguments.project)
    val commandExecutor = CommandExecutor(projectConfig)
    val gitWrapper = GitWrapper(projectConfig, commandExecutor)

    //syncCommits(projectConfig, gitWrapper)
    executeMeasurements(projectConfig, commandExecutor, gitWrapper)
}

fun executeMeasurements(projectConfig: ProjectConfig, commandExecutor: CommandExecutor, gitWrapper: GitWrapper) {
    val projectData = ProjectDataReader.read(projectConfig.name)
    val measurementExecutor = MeasurementExecutor(projectConfig, commandExecutor, gitWrapper)
    for (commit in projectData.commits) {
        measurementExecutor.executeIfNeeded(commit)
    }


    println(projectData)
//
//    var shouldCheckout = false
//
//    for (commit in projectData.commits) {
//
//        println("Running on commit. $commit")
//        if (measurements == null) {
//            println("Measurements == null, should check out!")
//            shouldCheckout = true
//        } else {
//            for (measurement in projectConfig.measurements) {
//            }
//
//
//            if (shouldCheckout) {
//                gitWrapper.checkout(hash)
//                for (measurement in projectConfig.measurements) {
//                    val measured = commit["measurements"]
//
//                    println("qwerty1" + commit)
//                    println("qwerty2" + measured)
//                    if (measurement.type == "grep") {
//                        println("grepping?" + measurement)
//                    }
//                }
//
//
//                val command = listOf("grep", "--recursive", "-i", "-e", "import com.google.gson.Gson", ".")
//                val amount = commandExecutor.execute(command).lines().size
//                println("amount: $amount")
//
//                exitProcess(1)
//            }
//        }
//
//
//    }
}

fun syncCommits(projectConfig: ProjectConfig, gitWrapper: GitWrapper) {

    val commits = gitWrapper.log()

    val importantCommitFilter = ImportantCommitFilter(commits)
    val annotatedCommits = importantCommitFilter.filterImportantCommits()


    val projectData = ProjectDataReader.read(projectConfig.name)
    projectData.syncCommits(annotatedCommits)
    ProjectDataReader.write(projectConfig.name, projectData)
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
}