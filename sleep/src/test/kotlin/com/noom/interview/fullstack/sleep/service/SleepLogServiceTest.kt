package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.domain.Feeling
import com.noom.interview.fullstack.sleep.dto.SleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.entity.SleepLog
import com.noom.interview.fullstack.sleep.entity.User
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.repository.UserRepository
import com.noom.interview.fullstack.sleep.service.exception.DuplicateResourceException
import com.noom.interview.fullstack.sleep.service.exception.InvalidInputException
import com.noom.interview.fullstack.sleep.service.exception.SleepLogNotFoundException
import com.noom.interview.fullstack.sleep.service.exception.SleepServiceException
import com.noom.interview.fullstack.sleep.service.exception.UserNotFoundException
import java.lang.reflect.InvocationTargetException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Optional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.dao.DataIntegrityViolationException

class SleepLogServiceTest {

    private lateinit var sleepLogService: SleepLogService
    private lateinit var sleepLogRepository: SleepLogRepository
    private lateinit var userRepository: UserRepository

    private val userId = 1L
    private val user = User(id = userId)
    private val today = LocalDate.now(ZoneOffset.UTC)

    @BeforeEach
    fun setup() {
        sleepLogRepository = mock(SleepLogRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        sleepLogService = SleepLogService(sleepLogRepository, userRepository)

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
    }

    @Test
    fun `createSleepLog should create valid sleep log`() {
        val timeInBedStart = Instant.now().minus(8, ChronoUnit.HOURS)
        val timeInBedEnd = Instant.now()
        val request =
                SleepLogRequest(
                        timeInBedStart = timeInBedStart,
                        timeInBedEnd = timeInBedEnd,
                        morningFeeling = Feeling.GOOD
                )

        val sleepDate = timeInBedEnd.atZone(ZoneOffset.UTC).toLocalDate()
        `when`(sleepLogRepository.existsByUserAndSleepDate(user, sleepDate)).thenReturn(false)

        val savedSleepLog =
                SleepLog(
                        id = 1L,
                        user = user,
                        sleepDate = sleepDate,
                        timeInBedStart = timeInBedStart,
                        timeInBedEnd = timeInBedEnd,
                        totalTimeInBedMinutes = 480,
                        morningFeeling = Feeling.GOOD
                )
        `when`(sleepLogRepository.save(any())).thenReturn(savedSleepLog)

        val result = sleepLogService.createSleepLog(userId, request)

        assertThat(result.id).isEqualTo(1L)
        assertThat(result.sleepDate).isEqualTo(sleepDate)
        assertThat(result.timeInBedStart).isEqualTo(timeInBedStart)
        assertThat(result.timeInBedEnd).isEqualTo(timeInBedEnd)
        assertThat(result.morningFeeling).isEqualTo(Feeling.GOOD)

        verify(userRepository).findById(userId)
        verify(sleepLogRepository).existsByUserAndSleepDate(user, sleepDate)
        verify(sleepLogRepository).save(any())
    }

    @Test
    fun `createSleepLog should throw UserNotFoundException when user not found`() {
        val nonExistentUserId = 999L
        `when`(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty())

        val request =
                SleepLogRequest(
                        timeInBedStart = Instant.now().minus(8, ChronoUnit.HOURS),
                        timeInBedEnd = Instant.now(),
                        morningFeeling = Feeling.GOOD
                )

        assertThrows<UserNotFoundException> {
            sleepLogService.createSleepLog(nonExistentUserId, request)
        }

        verify(userRepository).findById(nonExistentUserId)
        verifyNoInteractions(sleepLogRepository)
    }

    @Test
    fun `createSleepLog should throw InvalidInputException when end time is before start time`() {
        val timeInBedEnd = Instant.now().minus(9, ChronoUnit.HOURS)
        val timeInBedStart = Instant.now()

        val request =
                SleepLogRequest(
                        timeInBedStart = timeInBedStart,
                        timeInBedEnd = timeInBedEnd,
                        morningFeeling = Feeling.GOOD
                )

        assertThrows<InvalidInputException> { sleepLogService.createSleepLog(userId, request) }

        verify(userRepository).findById(userId)
        verifyNoMoreInteractions(sleepLogRepository)
    }

    @Test
    fun `createSleepLog should throw InvalidInputException for too short duration`() {
        val timeInBedStart = Instant.parse("2025-05-10T23:00:00Z")
        val timeInBedEnd = Instant.parse("2025-05-10T23:00:30Z")
        val request = SleepLogRequest(timeInBedStart, timeInBedEnd, Feeling.OK)

        assertThrows<InvalidInputException> { sleepLogService.createSleepLog(userId, request) }
    }

    @Test
    fun `createSleepLog throws InvalidInputException when duration exceeds 24 hours`() {
        val timeInBedStart = Instant.parse("2025-05-10T23:00:00Z")
        val timeInBedEnd = Instant.parse("2025-05-12T23:00:30Z")
        val request = SleepLogRequest(timeInBedStart, timeInBedEnd, Feeling.OK)

        assertThrows<InvalidInputException> { sleepLogService.createSleepLog(userId, request) }
    }

    @Test
    fun `createSleepLog should throw DuplicateResourceException when log already exists for date`() {
        val timeInBedStart = Instant.now().minus(8, ChronoUnit.HOURS)
        val timeInBedEnd = Instant.now()
        val request =
                SleepLogRequest(
                        timeInBedStart = timeInBedStart,
                        timeInBedEnd = timeInBedEnd,
                        morningFeeling = Feeling.GOOD
                )

        val sleepDate = timeInBedEnd.atZone(ZoneOffset.UTC).toLocalDate()
        `when`(sleepLogRepository.existsByUserAndSleepDate(user, sleepDate)).thenReturn(true)

        assertThrows<DuplicateResourceException> { sleepLogService.createSleepLog(userId, request) }

        verify(userRepository).findById(userId)
        verify(sleepLogRepository).existsByUserAndSleepDate(user, sleepDate)
        verify(sleepLogRepository, never()).save(any())
    }

    @Test
    fun `getLastNightSleep should return most recent sleep log`() {
        val sleepLog =
                SleepLog(
                        id = 1L,
                        user = user,
                        sleepDate = today,
                        timeInBedStart = Instant.now().minus(8, ChronoUnit.HOURS),
                        timeInBedEnd = Instant.now(),
                        totalTimeInBedMinutes = 480,
                        morningFeeling = Feeling.GOOD
                )

        `when`(sleepLogRepository.findFirstByUserOrderBySleepDateDesc(user)).thenReturn(sleepLog)

        val result = sleepLogService.getLastNightSleep(userId)

        assertThat(result.id).isEqualTo(1L)
        assertThat(result.sleepDate).isEqualTo(today)

        verify(userRepository).findById(userId)
        verify(sleepLogRepository).findFirstByUserOrderBySleepDateDesc(user)
    }

    @Test
    fun `getLastNightSleep should throw SleepLogNotFoundException when no logs exist`() {
        `when`(sleepLogRepository.findFirstByUserOrderBySleepDateDesc(user)).thenReturn(null)

        assertThrows<SleepLogNotFoundException> { sleepLogService.getLastNightSleep(userId) }

        verify(userRepository).findById(userId)
        verify(sleepLogRepository).findFirstByUserOrderBySleepDateDesc(user)
    }

    @Test
    fun `getThirtyDayStats should return stats for last 30 days`() {
        val endDate = LocalDate.now(ZoneOffset.UTC)
        val startDate = endDate.minusDays(29)

        val sleepLogs =
                listOf(
                        createSleepLog(1L, endDate.minusDays(1), Feeling.GOOD),
                        createSleepLog(2L, endDate.minusDays(2), Feeling.OK),
                        createSleepLog(3L, endDate.minusDays(3), Feeling.BAD)
                )

        `when`(sleepLogRepository.findByUserAndSleepDateBetween(user, startDate, endDate))
                .thenReturn(sleepLogs)

        val result = sleepLogService.getThirtyDayStats(userId)

        assertThat(result.startDate).isEqualTo(startDate)
        assertThat(result.endDate).isEqualTo(endDate)
        assertThat(result.numberOfLogs).isEqualTo(3)
        assertThat(result.averageTimeInBedMinutes).isEqualTo(480.0)
        assertThat(result.feelingFrequencies).containsEntry(Feeling.GOOD, 1)
        assertThat(result.feelingFrequencies).containsEntry(Feeling.OK, 1)
        assertThat(result.feelingFrequencies).containsEntry(Feeling.BAD, 1)

        verify(userRepository).findById(userId)
        verify(sleepLogRepository).findByUserAndSleepDateBetween(user, startDate, endDate)
    }

    @Test
    fun `getThirtyDayStats should return empty stats when no logs exist`() {
        val endDate = LocalDate.now(ZoneOffset.UTC)
        val startDate = endDate.minusDays(29)

        `when`(sleepLogRepository.findByUserAndSleepDateBetween(user, startDate, endDate))
                .thenReturn(emptyList())

        val result = sleepLogService.getThirtyDayStats(userId)

        assertThat(result.startDate).isEqualTo(startDate)
        assertThat(result.endDate).isEqualTo(endDate)
        assertThat(result.numberOfLogs).isEqualTo(0)
        assertThat(result.averageTimeInBedMinutes).isNull()
        assertThat(result.averageBedTime).isNull()
        assertThat(result.averageWakeTime).isNull()
        assertThat(result.feelingFrequencies).isEmpty()

        verify(userRepository).findById(userId)
        verify(sleepLogRepository).findByUserAndSleepDateBetween(user, startDate, endDate)
    }

    private fun createSleepLog(id: Long, sleepDate: LocalDate, feeling: Feeling): SleepLog {
        return SleepLog(
                id = id,
                user = user,
                sleepDate = sleepDate,
                timeInBedStart = Instant.now().minus(8, ChronoUnit.HOURS),
                timeInBedEnd = Instant.now(),
                totalTimeInBedMinutes = 480,
                morningFeeling = feeling
        )
    }

    @Test
    fun `createSleepLog should throw SleepServiceException when DataIntegrityViolationException occurs`() {
        val timeInBedStart = Instant.now().minus(8, ChronoUnit.HOURS)
        val timeInBedEnd = Instant.now()
        val request =
                SleepLogRequest(
                        timeInBedStart = timeInBedStart,
                        timeInBedEnd = timeInBedEnd,
                        morningFeeling = Feeling.GOOD
                )

        val sleepDate = timeInBedEnd.atZone(ZoneOffset.UTC).toLocalDate()
        `when`(sleepLogRepository.existsByUserAndSleepDate(user, sleepDate)).thenReturn(false)

        `when`(sleepLogRepository.save(any()))
                .thenThrow(DataIntegrityViolationException("Test constraint violation"))

        val exception =
                assertThrows<SleepServiceException> {
                    sleepLogService.createSleepLog(userId, request)
                }

        assertThat(exception.message)
                .contains("Failed to save sleep log due to a data integrity issue")
        assertThat(exception.cause).isInstanceOf(DataIntegrityViolationException::class.java)

        verify(userRepository).findById(userId)
        verify(sleepLogRepository).existsByUserAndSleepDate(user, sleepDate)
        verify(sleepLogRepository).save(any())
    }

    @Test
    fun `calculateAverageLocalTime should correctly average evening bed times`() {
        val calculateAverageLocalTimeMethod =
                SleepLogService::class.java.getDeclaredMethod(
                        "calculateAverageLocalTime",
                        List::class.java,
                        Boolean::class.java,
                        ZoneId::class.java
                )
        calculateAverageLocalTimeMethod.isAccessible = true

        val bedTimes =
                listOf(
                        Instant.parse("2025-05-10T22:00:00Z"),
                        Instant.parse("2025-05-11T23:00:00Z"),
                        Instant.parse("2025-05-12T21:00:00Z")
                )

        val result =
                calculateAverageLocalTimeMethod.invoke(
                        sleepLogService,
                        bedTimes,
                        true,
                        ZoneOffset.UTC
                ) as
                        LocalTime

        assertThat(result.hour).isEqualTo(22)
        assertThat(result.minute).isEqualTo(0)
    }

    @Test
    fun `calculateAverageLocalTime should correctly handle early morning bed times`() {
        val calculateAverageLocalTimeMethod =
                SleepLogService::class.java.getDeclaredMethod(
                        "calculateAverageLocalTime",
                        List::class.java,
                        Boolean::class.java,
                        ZoneId::class.java
                )
        calculateAverageLocalTimeMethod.isAccessible = true

        val bedTimes =
                listOf(
                        Instant.parse("2025-05-10T23:00:00Z"),
                        Instant.parse("2025-05-11T01:00:00Z"),
                        Instant.parse("2025-05-12T02:00:00Z")
                )

        val result =
                calculateAverageLocalTimeMethod.invoke(
                        sleepLogService,
                        bedTimes,
                        true,
                        ZoneOffset.UTC
                ) as
                        LocalTime

        assertThat(result.hour).isIn(0, 1, 2)
    }

    @Test
    fun `calculateAverageLocalTime should correctly average wake times`() {
        val calculateAverageLocalTimeMethod =
                SleepLogService::class.java.getDeclaredMethod(
                        "calculateAverageLocalTime",
                        List::class.java,
                        Boolean::class.java,
                        ZoneId::class.java
                )
        calculateAverageLocalTimeMethod.isAccessible = true

        val wakeTimes =
                listOf(
                        Instant.parse("2025-05-10T06:00:00Z"),
                        Instant.parse("2025-05-11T07:00:00Z"),
                        Instant.parse("2025-05-12T08:00:00Z")
                )

        val result =
                calculateAverageLocalTimeMethod.invoke(
                        sleepLogService,
                        wakeTimes,
                        false,
                        ZoneOffset.UTC
                ) as
                        LocalTime

        assertThat(result.hour).isEqualTo(7)
        assertThat(result.minute).isEqualTo(0)
    }

    @Test
    fun `calculateAverageLocalTime should return null for empty list`() {
        val calculateAverageLocalTimeMethod =
                SleepLogService::class.java.getDeclaredMethod(
                        "calculateAverageLocalTime",
                        List::class.java,
                        Boolean::class.java,
                        ZoneId::class.java
                )
        calculateAverageLocalTimeMethod.isAccessible = true

        val result =
                calculateAverageLocalTimeMethod.invoke(
                        sleepLogService,
                        emptyList<Instant>(),
                        true,
                        ZoneOffset.UTC
                )

        assertThat(result).isNull()
    }

    @Test
    fun `mapToResponse should correctly map SleepLog to SleepLogResponse`() {
        val mapToResponseMethod =
                SleepLogService::class.java.getDeclaredMethod("mapToResponse", SleepLog::class.java)
        mapToResponseMethod.isAccessible = true

        val sleepLog =
                SleepLog(
                        id = 1L,
                        user = user,
                        sleepDate = LocalDate.now(),
                        timeInBedStart = Instant.now().minus(8, ChronoUnit.HOURS),
                        timeInBedEnd = Instant.now(),
                        totalTimeInBedMinutes = 480,
                        morningFeeling = Feeling.GOOD
                )

        val result = mapToResponseMethod.invoke(sleepLogService, sleepLog) as SleepLogResponse

        assertThat(result.id).isEqualTo(1L)
        assertThat(result.sleepDate).isEqualTo(sleepLog.sleepDate)
        assertThat(result.timeInBedStart).isEqualTo(sleepLog.timeInBedStart)
        assertThat(result.timeInBedEnd).isEqualTo(sleepLog.timeInBedEnd)
        assertThat(result.totalTimeInBedMinutes).isEqualTo(sleepLog.totalTimeInBedMinutes)
        assertThat(result.morningFeeling).isEqualTo(sleepLog.morningFeeling)
    }

    @Test
    fun `mapToResponse should throw IllegalStateException when SleepLog ID is null`() {
        val mapToResponseMethod =
                SleepLogService::class.java.getDeclaredMethod("mapToResponse", SleepLog::class.java)
        mapToResponseMethod.isAccessible = true

        val sleepLog =
                SleepLog(
                        id = null,
                        user = user,
                        sleepDate = LocalDate.now(),
                        timeInBedStart = Instant.now().minus(8, ChronoUnit.HOURS),
                        timeInBedEnd = Instant.now(),
                        totalTimeInBedMinutes = 480,
                        morningFeeling = Feeling.GOOD
                )

        val exception =
                assertThrows<InvocationTargetException> {
                    mapToResponseMethod.invoke(sleepLogService, sleepLog)
                }

        assertThat(exception.cause).isInstanceOf(IllegalStateException::class.java)
        assertThat(exception.cause?.message).contains("SleepLog ID cannot be null after saving")
    }

    @Test
    fun `SleepServiceException should be created with message only`() {
        val errorMessage = "Test error message"
        val exception = SleepServiceException(errorMessage)

        assertThat(exception.message).isEqualTo(errorMessage)
        assertThat(exception.cause).isNull()
    }

    @Test
    fun `SleepServiceException should be created with message and cause`() {
        val errorMessage = "Test error message"
        val cause = RuntimeException("Original cause")
        val exception = SleepServiceException(errorMessage, cause)

        assertThat(exception.message).isEqualTo(errorMessage)
        assertThat(exception.cause).isEqualTo(cause)
    }

    @Test
    fun `getLastNightSleep should throw UserNotFoundException when user does not exist`() {
        val nonExistentUserId = 999L
        `when`(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> { sleepLogService.getLastNightSleep(nonExistentUserId) }
                .also {
                    assertThat(it.message).contains("User with ID $nonExistentUserId not found")
                }

        verify(userRepository).findById(nonExistentUserId)
        verifyNoMoreInteractions(sleepLogRepository)
    }

    @Test
    fun `getThirtyDayStats should throw UserNotFoundException when user does not exist`() {
        val nonExistentUserId = 999L
        `when`(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty())

        assertThrows<UserNotFoundException> { sleepLogService.getThirtyDayStats(nonExistentUserId) }
                .also {
                    assertThat(it.message).contains("User with ID $nonExistentUserId not found")
                }

        verify(userRepository).findById(nonExistentUserId)
        verifyNoMoreInteractions(sleepLogRepository)
    }
}
