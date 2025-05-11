package com.noom.interview.fullstack.sleep.dto

import com.noom.interview.fullstack.sleep.domain.Feeling
import java.time.LocalDate
import java.time.LocalTime

/**
 * Data transfer object for returning aggregated sleep statistics.
 * 
 * @property startDate Beginning of the statistics period
 * @property endDate End of the statistics period
 * @property numberOfLogs Count of sleep logs in the period
 * @property averageTimeInBedMinutes Average sleep duration in minutes
 * @property averageBedTime Average time the user went to bed
 * @property averageWakeTime Average time the user woke up
 * @property feelingFrequencies Count of each feeling type
 */
data class SleepStatsResponse(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val numberOfLogs: Int,
        val averageTimeInBedMinutes: Double?,
        val averageBedTime: LocalTime?,
        val averageWakeTime: LocalTime?,
        val feelingFrequencies: Map<Feeling, Int>
)
