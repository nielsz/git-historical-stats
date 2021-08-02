package nl.nielsvanhove.projectinfo

import nl.nielsvanhove.projectinfo.core.ImportantCommitFilter
import nl.nielsvanhove.projectinfo.core.LogItem
import nl.nielsvanhove.projectinfo.model.AnnotatedCommit
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ImportantCommitFilterTest {

    @Test
    fun `When filtering an empty list, Then return an empty list`() {
        // When
        val result = ImportantCommitFilter(emptyList()).filterImportantCommits()

        // Then
        assertEquals(emptyList<AnnotatedCommit>(), result)
    }

    @Test
    fun `When filtering one item, Then it is both the first and the last`() {
        // Given
        val data = listOf(
            logItem("a", date(2021, 7, 1))
        )

        // When
        val result = ImportantCommitFilter(data).filterImportantCommits()

        // Then
        assertEquals(1, result.size)
        assertEquals("a", result[0].commitHash)
        assertTrue(result[0].isLastCommit)
        assertTrue(result[0].isFirstCommit)
    }

    @Test
    fun `When the first commit was in december, Then the first item is the last of the year`() {
        // Given
        val data = listOf(
            logItem("a", date(2022, 1, 1)),
            logItem("b", date(2021, 12, 31))
        )

        // When
        val result = ImportantCommitFilter(data).filterImportantCommits()

        // Then
        assertEquals(2, result.size)
        assertEquals("a", result[0].commitHash)
        assertTrue(result[0].isLastCommit)
        assertFalse(result[0].isFirstCommit)
        assertFalse(result[0].lastOfYear)
        assertFalse(result[0].lastOfQuarter)
        assertFalse(result[0].lastOfMonth)

        assertEquals("b", result[1].commitHash)
        assertFalse(result[1].isLastCommit)
        assertTrue(result[1].isFirstCommit)
        assertTrue(result[1].lastOfYear)
        assertTrue(result[1].lastOfQuarter)
        assertTrue(result[1].lastOfMonth)
    }


    @Test
    fun `When filtering one item, Then it is never the last of the year`() {
        // Given
        val data = listOf(
            logItem("a", date(2021, 12, 31))
        )

        // When
        val result = ImportantCommitFilter(data).filterImportantCommits()

        // Then
        assertEquals(1, result.size)
        assertEquals("a", result[0].commitHash)
        assertTrue(result[0].isLastCommit)
        assertFalse(result[0].lastOfYear)
        assertFalse(result[0].lastOfQuarter)
        assertFalse(result[0].lastOfMonth)
    }

    @Test
    fun `When filtering three items from the same month, Then only the latest is added`() {
        // Given
        val data = listOf(
            logItem("a", date(2021, 7, 3)),
            logItem("b", date(2021, 7, 2)),
            logItem("c", date(2021, 7, 1))
        )

        // When
        val result = ImportantCommitFilter(data).filterImportantCommits()

        // Then
        assertEquals(2, result.size)
        assertEquals("a", result[0].commitHash)
        assertTrue(result[0].isLastCommit)
        assertEquals("c", result[1].commitHash)
        assertTrue(result[1].isFirstCommit)
    }

    @Test
    fun `When filtering four items from two months, Then three items are added`() {
        // Given
        val data = listOf(
            logItem("a", date(2021, 7, 3)),
            logItem("b", date(2021, 7, 2)),
            logItem("c", date(2021, 6, 30)),
            logItem("d", date(2021, 6, 16))
        )

        // When
        val result = ImportantCommitFilter(data).filterImportantCommits()

        // Then
        assertEquals(3, result.size)
        assertEquals("a", result[0].commitHash)
        assertTrue(result[0].isLastCommit)
        assertFalse(result[0].lastOfYear)
        assertFalse(result[0].lastOfQuarter)
        assertFalse(result[0].lastOfMonth)
        assertEquals("c", result[1].commitHash)
        assertTrue(result[1].lastOfQuarter)
        assertFalse(result[1].lastOfYear)
        assertTrue(result[1].lastOfQuarter)
        assertTrue(result[1].lastOfMonth)
        assertEquals("d", result[2].commitHash)
        assertTrue(result[2].isFirstCommit)
        assertFalse(result[2].lastOfYear)
        assertFalse(result[2].lastOfQuarter)
        assertFalse(result[2].lastOfMonth)
    }

    @Test
    fun `When filtering five items from two months from different years, Then three items are added`() {
        // Given
        val data = listOf(
            logItem("a", date(2021, 7, 3)),
            logItem("b", date(2021, 6, 30)),
            logItem("c", date(2020, 6, 18)),
            logItem("d", date(2020, 1, 1))
        )

        // When
        val result = ImportantCommitFilter(data).filterImportantCommits()

        // Then
        assertEquals(4, result.size)
        assertEquals("a", result[0].commitHash)
        assertTrue(result[0].isLastCommit)
        assertEquals("b", result[1].commitHash)
        assertEquals("c", result[2].commitHash)
        assertEquals("d", result[3].commitHash)
        assertTrue(result[3].isFirstCommit)
    }


    fun date(year: Int, month: Int, day: Int) = OffsetDateTime.of(year, month, day, 0, 0, 0, 0, ZoneOffset.UTC)

    fun logItem(
        commitHash: String = List(8) {
            (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
        }.joinToString(""),
        committerDate: OffsetDateTime
    ) = LogItem(
        commitHash, committerDate = committerDate, authorEmail = "niels.vanhove@gmail.com"
    )
}