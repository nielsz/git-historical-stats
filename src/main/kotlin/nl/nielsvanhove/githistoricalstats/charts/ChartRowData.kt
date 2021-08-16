package nl.nielsvanhove.githistoricalstats.charts

import java.time.OffsetDateTime

data class ChartRowData(val date: OffsetDateTime, val data: Map<String, Int>)