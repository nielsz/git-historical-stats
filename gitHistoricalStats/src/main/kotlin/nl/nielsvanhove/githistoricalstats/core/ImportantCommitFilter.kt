package nl.nielsvanhove.githistoricalstats.core

import nl.nielsvanhove.githistoricalstats.model.AnnotatedCommit
import java.time.temporal.IsoFields

/**
 * @param allCommits list of commits in reverse chronological order.
 */
class ImportantCommitFilter(val allCommits: List<LogItem>) {

    /**
     * Filters out all the commits that are not one of the following:
     * - The first commit
     * - The last commit
     * - The last commit of a year
     * - The last commit of a quarter
     * - The last commit of a month
     *
     */
    fun filterImportantCommits(): List<AnnotatedCommit> {
        val importantCommits = mutableListOf<AnnotatedCommit>()

        allCommits.forEachIndexed { index, logItem ->
            val date = logItem.committerDate

            val thisYearDoestExistYet = importantCommits.none { importantCommit ->
                importantCommit.committerDate.year == date.year
            }

            val thisQuarterDoestExistYet = importantCommits.none { importantCommit ->
                importantCommit.committerDate.get(IsoFields.QUARTER_OF_YEAR) == date.get(IsoFields.QUARTER_OF_YEAR) && importantCommit.committerDate.year == date.year
            }

            val thisMonthDoestExistYet = importantCommits.none { importantCommit ->
                importantCommit.committerDate.monthValue == date.monthValue && importantCommit.committerDate.year == date.year
            }

            val isLast = index == 0
            val isFirst = index == allCommits.size - 1



            if (isFirst || isLast || thisYearDoestExistYet || thisQuarterDoestExistYet || thisMonthDoestExistYet) {
                importantCommits.add(
                    AnnotatedCommit(
                        logItem.commitHash,
                        logItem.committerDate,
                        lastOfMonth = !isLast && thisMonthDoestExistYet,
                        lastOfQuarter = !isLast && thisQuarterDoestExistYet,
                        lastOfYear = !isLast && thisYearDoestExistYet,
                        isLastCommit = isLast,
                        isFirstCommit = isFirst
                    )
                )
            }
        }

        return importantCommits
    }

}