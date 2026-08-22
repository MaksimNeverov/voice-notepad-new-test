package com.example.emptyviewsactivity

import java.util.Calendar

object DateUtils {
    fun getStartOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getDateStringFromTimestamp(timestamp: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp

        val year = cal.get(Calendar.YEAR)
        val month = String.format("%02d", cal.get(Calendar.MONTH) + 1)
        val day = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))
        val hour = String.format("%02d", cal.get(Calendar.HOUR_OF_DAY))
        val minute = String.format("%02d", cal.get(Calendar.MINUTE))

        // Формат: ЧЧ:ММ ДД.ММ.ГГГГ
        return "$hour:$minute $day.$month.$year"
    }
}

