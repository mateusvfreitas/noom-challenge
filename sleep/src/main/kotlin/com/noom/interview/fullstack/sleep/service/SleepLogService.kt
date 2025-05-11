package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.SleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.dto.SleepStatsResponse
import com.noom.interview.fullstack.sleep.entity.SleepLog
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.repository.UserRepository
import com.noom.interview.fullstack.sleep.service.exception.DuplicateResourceException
import com.noom.interview.fullstack.sleep.service.exception.InvalidInputException
import com.noom.interview.fullstack.sleep.service.exception.SleepServiceException
import com.noom.interview.fullstack.sleep.service.exception.UserNotFoundException
import com.noom.interview.fullstack.sleep.service.exception.SleepLogNotFoundException
import com.noom.interview.fullstack.sleep.domain.Feeling
import java.time.Duration
import java.time.ZoneOffset
import java.time.LocalTime
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for managing sleep logs.
 *
 * This service handles the business logic for creating, retrieving, and analyzing sleep logs.
 */
@Service
class SleepLogService(
        private val sleepLogRepository: SleepLogRepository,
        private val userRepository: UserRepository
) {
    /**
     * Creates a new sleep log for a user.
     *
     * @param userId The ID of the user creating the sleep log
     * @param request The sleep log data
     * @return The created sleep log
     * @throws UserNotFoundException If the user doesn't exist
     * @throws InvalidInputException If the input data is invalid
     * @throws DuplicateResourceException If a sleep log already exists for the date
     * @throws SleepServiceException If there's an issue saving the sleep log
     */
    @Transactional
    fun createSleepLog(userId: Long, request: SleepLogRequest): SleepLogResponse {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException(userId) }

        if (!request.timeInBedEnd.isAfter(request.timeInBedStart)) {
            throw InvalidInputException("Time in bed end must be after time in bed start.")
        }

        val durationInBed = Duration.between(request.timeInBedStart, request.timeInBedEnd)
        val totalMinutes = durationInBed.toMinutes()

        if (totalMinutes < 1) {
            throw InvalidInputException("Total time in bed must be at least 1 minute.")
        }
        if (totalMinutes > 24 * 60) {
            throw InvalidInputException("Total time in bed cannot exceed 24 hours.")
        }

        val derivedSleepDate = request.timeInBedEnd.atZone(ZoneOffset.UTC).toLocalDate()

        if (sleepLogRepository.existsByUserAndSleepDate(user, derivedSleepDate)) {
            throw DuplicateResourceException(
                    "A sleep log for user ${user.id} on date $derivedSleepDate already exists."
            )
        }

        val sleepLog =
                SleepLog(
                        user = user,
                        sleepDate = derivedSleepDate,
                        timeInBedStart = request.timeInBedStart,
                        timeInBedEnd = request.timeInBedEnd,
                        totalTimeInBedMinutes = totalMinutes.toInt(),
                        morningFeeling = request.morningFeeling
                )

        try {
            val savedLog = sleepLogRepository.save(sleepLog)
            return mapToResponse(savedLog)
        } catch (ex: DataIntegrityViolationException) {
            throw SleepServiceException(
                    "Failed to save sleep log due to a data integrity issue.",
                    ex
            )
        }
    }

    /**
     * Retrieves the most recent sleep log for a user.
     *
     * @param userId The ID of the user
     * @return The most recent sleep log
     * @throws UserNotFoundException If the user doesn't exist
     * @throws SleepLogNotFoundException If no sleep logs exist for the user
     */
    fun getLastNightSleep(userId: Long): SleepLogResponse {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException(userId) }

        val lastSleepLog =
                sleepLogRepository.findFirstByUserOrderBySleepDateDesc(user)
                        ?: throw SleepLogNotFoundException(
                                "No sleep logs found for user ${user.id}."
                        )

        return mapToResponse(lastSleepLog)
    }

    /**
     * Calculates sleep statistics for the last 30 days.
     *
     * @param userId The ID of the user
     * @return Sleep statistics for the last 30 days
     * @throws UserNotFoundException If the user doesn't exist
     */
    fun getThirtyDayStats(userId: Long): SleepStatsResponse {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException(userId) }

        val endDate = LocalDate.now(ZoneOffset.UTC)
        val startDate = endDate.minusDays(29)

        val sleepLogs = sleepLogRepository.findByUserAndSleepDateBetween(user, startDate, endDate)

        val numberOfLogs = sleepLogs.size

        val avgTimeInBedMinutes: Double? =
                if (sleepLogs.isNotEmpty()) {
                    sleepLogs.map { it.totalTimeInBedMinutes }.average()
                } else {
                    null
                }

        val avgBedTime: LocalTime? =
                if (sleepLogs.isNotEmpty()) {
                    calculateAverageLocalTime(
                            sleepLogs.map { it.timeInBedStart },
                            isBedTime = true,
                            ZoneOffset.UTC
                    )
                } else {
                    null
                }

        val avgWakeTime: LocalTime? =
                if (sleepLogs.isNotEmpty()) {
                    calculateAverageLocalTime(
                            sleepLogs.map { it.timeInBedEnd },
                            isBedTime = false,
                            ZoneOffset.UTC
                    )
                } else {
                    null
                }

        val feelingFrequencies: Map<Feeling, Int> =
                if (sleepLogs.isNotEmpty()) {
                    sleepLogs.groupBy { it.morningFeeling }.mapValues { it.value.size }
                } else {
                    emptyMap()
                }

        return SleepStatsResponse(
                startDate = startDate,
                endDate = endDate,
                numberOfLogs = numberOfLogs,
                averageTimeInBedMinutes = avgTimeInBedMinutes,
                averageBedTime = avgBedTime,
                averageWakeTime = avgWakeTime,
                feelingFrequencies = feelingFrequencies
        )
    }

    /**
     * Calculates the average local time from a list of timestamps.
     *
     * @param timestamps List of timestamps to average
     * @param zoneId The time zone to use for the calculation
     * @return The average local time
     */
    private fun calculateAverageLocalTime(
            instants: List<Instant>,
            isBedTime: Boolean,
            zoneId: ZoneId
    ): LocalTime? {
        if (instants.isEmpty()) {
            return null
        }

        val secondsOfDay =
                instants.map { instant ->
                    var localTime = instant.atZone(zoneId).toLocalTime()
                    var seconds = localTime.toSecondOfDay().toLong()

                    // Handle bedtime wrap-around: if it's a bedtime and it's "early AM" (e.g.
                    // before 4 AM),
                    // shift it by 24 hours to correctly average with PM times.
                    // This pivot (4 AM) might need adjustment based on typical user patterns.
                    if (isBedTime && localTime.hour < 4) {
                        seconds += 24 * 3600
                    }
                    seconds
                }

        var averageSeconds = secondsOfDay.average().toLong()

        if (isBedTime) {
            averageSeconds %= (24 * 3600)
        }

        return LocalTime.ofSecondOfDay(averageSeconds)
    }

    private fun mapToResponse(sleepLog: SleepLog): SleepLogResponse {
        return SleepLogResponse(
                id = sleepLog.id
                                ?: throw IllegalStateException(
                                        "SleepLog ID cannot be null after saving"
                                ),
                sleepDate = sleepLog.sleepDate,
                timeInBedStart = sleepLog.timeInBedStart,
                timeInBedEnd = sleepLog.timeInBedEnd,
                totalTimeInBedMinutes = sleepLog.totalTimeInBedMinutes,
                morningFeeling = sleepLog.morningFeeling
        )
    }
}
