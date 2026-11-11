package com.sparse.launcher

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class CalendarEvent(
    val id: Long,
    val title: String,
    val startMs: Long,
    val endMs: Long,
    val allDay: Boolean,
)

fun CalendarEvent.timeLabel(clock24h: Boolean): String {
    if (allDay) return "all day"
    val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startMs), ZoneId.systemDefault())
    val pattern = if (clock24h) "HH:mm" else "h:mm a"
    return ldt.format(DateTimeFormatter.ofPattern(pattern))
}

object CalendarRepository {
    fun upcoming(ctx: Context, maxEvents: Int = 5): List<CalendarEvent> {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CALENDAR)
            != PackageManager.PERMISSION_GRANTED) return emptyList()

        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        val endOfDay = LocalDate.now().plusDays(1)
            .atStartOfDay(zone).toInstant().toEpochMilli()

        // Query from start of today so all-day events (which begin at midnight) are included
        val startOfDay = LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(startOfDay.toString())
            .appendPath(endOfDay.toString())
            .build()

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
        )

        val cursor = ctx.contentResolver.query(
            uri, projection,
            null, null,
            "${CalendarContract.Instances.BEGIN} ASC"
        ) ?: return emptyList()

        val events = mutableListOf<CalendarEvent>()
        cursor.use {
            while (it.moveToNext() && events.size < maxEvents) {
                val title = it.getString(1) ?: continue
                if (title.isBlank()) continue
                val endMs = it.getLong(3)
                // Skip events that have already ended (but keep all-day events)
                val allDay = it.getInt(4) != 0
                if (!allDay && endMs < now) continue
                events.add(
                    CalendarEvent(
                        id = it.getLong(0),
                        title = title,
                        startMs = it.getLong(2),
                        endMs = endMs,
                        allDay = allDay,
                    )
                )
            }
        }
        return events
    }
}
