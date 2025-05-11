package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.entity.SleepLog
import com.noom.interview.fullstack.sleep.entity.User
import java.time.LocalDate
import org.springframework.data.jpa.repository.JpaRepository

/** JPA repository for SleepLog entity operations. */
interface SleepLogRepository : JpaRepository<SleepLog, Long> {
    /**
     * Finds the most recent sleep log for a user.
     *
     * @param user The user to find sleep logs for
     * @return The most recent sleep log, or null if none exists
     */
    fun findFirstByUserOrderBySleepDateDesc(user: User): SleepLog?

    /**
     * Finds sleep logs for a user within a date range.
     *
     * @param user The user to find sleep logs for
     * @param startDate The start of the date range (inclusive)
     * @param endDate The end of the date range (inclusive)
     * @return List of sleep logs within the date range
     */
    fun findByUserAndSleepDateBetween(
            user: User,
            startDate: LocalDate,
            endDate: LocalDate
    ): List<SleepLog>

    /**
     * Checks if a sleep log exists for a user on a specific date.
     *
     * @param user The user to check
     * @param sleepDate The date to check
     * @return true if a sleep log exists, false otherwise
     */
    fun existsByUserAndSleepDate(user: User, sleepDate: LocalDate): Boolean
}
