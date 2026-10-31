package com.askornilov.app.ui.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object Dates {
    private val ru = Locale.forLanguageTag("ru")
    private val short = DateTimeFormatter.ofPattern("d MMM", ru)
    private val full = DateTimeFormatter.ofPattern("d MMM yyyy", ru)

    fun today(): Long = LocalDate.now().toEpochDay()

    fun label(epochDay: Long): String {
        val d = LocalDate.ofEpochDay(epochDay)
        val now = LocalDate.now()
        return when (epochDay - now.toEpochDay()) {
            0L -> "Сегодня"
            1L -> "Завтра"
            -1L -> "Вчера"
            else -> if (d.year == now.year) d.format(short) else d.format(full)
        }
    }

    fun labelMillis(millis: Long): String =
        label(Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay())

    /** DatePicker работает в UTC-миллисекундах. */
    fun fromPicker(millis: Long): Long =
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()

    fun toPicker(epochDay: Long): Long =
        LocalDate.ofEpochDay(epochDay).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}
