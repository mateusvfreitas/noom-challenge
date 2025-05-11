package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.SleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.entity.SleepLog
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.repository.UserRepository
import com.noom.interview.fullstack.sleep.service.exception.DuplicateResourceException
import com.noom.interview.fullstack.sleep.service.exception.InvalidInputException
import com.noom.interview.fullstack.sleep.service.exception.SleepServiceException
import com.noom.interview.fullstack.sleep.service.exception.UserNotFoundException
import java.time.Duration
import java.time.ZoneOffset
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
