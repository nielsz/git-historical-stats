package nl.nielsvanhove.projectinfo

import java.time.OffsetDateTime

data class AnnotatedCommit(
    val commitHash: String,
    val committerDate: OffsetDateTime,
    val lastOfYear: Boolean = false,
    val lastOfQuarter: Boolean = false,
    val lastOfMonth: Boolean = false,
    val isFirstCommit: Boolean = false,
    val isLastCommit: Boolean = false
)