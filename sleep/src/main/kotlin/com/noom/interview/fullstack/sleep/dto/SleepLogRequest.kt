package com.noom.interview.fullstack.sleep.dto

import com.noom.interview.fullstack.sleep.domain.Feeling
import java.time.Instant
import javax.validation.constraints.NotNull

/**
 * Data transfer object for creating a new sleep log.
 * 
 * @property timeInBedStart The timestamp when the user went to bed
 * @property timeInBedEnd The timestamp when the user got out of bed
 * @property morningFeeling How the user felt upon waking
 */
data class SleepLogRequest(
        @field:NotNull(message = "Time in bed start must not be null.") val timeInBedStart: Instant,
        @field:NotNull(message = "Time in bed end must not be null.") val timeInBedEnd: Instant,
        @field:NotNull(message = "Morning feeling must not be null.") val morningFeeling: Feeling
)
