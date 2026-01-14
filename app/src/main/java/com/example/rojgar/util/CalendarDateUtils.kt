package com.example.rojgar.util

import java.util.*

object CalendarDateUtils {

    fun dayRangeMillis(year: Int, monthZeroBased: Int, day: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            set(year, monthZeroBased, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startMillis = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endMillis = calendar.timeInMillis

        return Pair(startMillis, endMillis)
    }

    fun monthRangeMillis(year: Int, monthZeroBased: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            set(year, monthZeroBased, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startMillis = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endMillis = calendar.timeInMillis

        return Pair(startMillis, endMillis)
    }
}
