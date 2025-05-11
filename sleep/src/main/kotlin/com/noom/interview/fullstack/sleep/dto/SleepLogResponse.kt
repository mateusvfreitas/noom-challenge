package com.noom.interview.fullstack.sleep.dto

import com.noom.interview.fullstack.sleep.domain.Feeling
import java.time.Instant
import java.time.LocalDate

/**
 * Data transfer object for returning sleep log information.
 * 
 * @property id Unique identifier of the sleep log
 * @property sleepDate The date associated with the sleep record
 * @property timeInBedStart When the user went to bed
 * @property timeInBedEnd When the user got out of bed
 * @property totalTimeInBedMinutes Duration in minutes
 * @property morningFeeling How the user felt upon waking
 */
data class SleepLogResponse(
        val id: Long,
        val sleepDate: LocalDate,
        val timeInBedStart: Instant,
        val timeInBedEnd: Instant,
        val totalTimeInBedMinutes: Int,
        val morningFeeling: Feeling
)
