package com.sparse.launcher

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.time.Instant
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

        val now = System.currentTimeMillis()
        val end = now + 7L * 24 * 60 * 60 * 1000 // next 7 days

        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(now.toString())
            .appendPath(end.toString())
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
                events.add(
                    CalendarEvent(
                        id = it.getLong(0),
                        title = title,
                        startMs = it.getLong(2),
                        endMs = it.getLong(3),
                        allDay = it.getInt(4) != 0,
                    )
                )
            }
        }
        return events
    }
}
