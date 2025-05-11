package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.SleepApplication.Companion.UNIT_TEST_PROFILE
import com.noom.interview.fullstack.sleep.domain.Feeling
import com.noom.interview.fullstack.sleep.entity.SleepLog
import com.noom.interview.fullstack.sleep.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@DataJpaTest
@ActiveProfiles(UNIT_TEST_PROFILE)
class SleepLogRepositoryTest {

    @Autowired
    private lateinit var sleepLogRepository: SleepLogRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var user: User
    private lateinit var yesterday: LocalDate
    private lateinit var today: LocalDate
    private lateinit var tomorrow: LocalDate

    @BeforeEach
    fun setup() {
        sleepLogRepository.deleteAll()
        userRepository.deleteAll()
        
        user = userRepository.save(User())
        
        yesterday = LocalDate.now().minusDays(1)
        today = LocalDate.now()
        tomorrow = LocalDate.now().plusDays(1)
    }

    @Test
    fun `should find most recent sleep log by user`() {
        val yesterdaySleepLog = createSleepLog(user, yesterday)
        val todaySleepLog = createSleepLog(user, today)
        
        sleepLogRepository.save(yesterdaySleepLog)
        sleepLogRepository.save(todaySleepLog)
        
        val result = sleepLogRepository.findFirstByUserOrderBySleepDateDesc(user)
        
        assertThat(result).isNotNull
        assertThat(result!!.sleepDate).isEqualTo(today)
    }

    @Test
    fun `should find sleep logs between dates`() {
        val yesterdaySleepLog = createSleepLog(user, yesterday)
        val todaySleepLog = createSleepLog(user, today)
        val tomorrowSleepLog = createSleepLog(user, tomorrow)
        
        sleepLogRepository.save(yesterdaySleepLog)
        sleepLogRepository.save(todaySleepLog)
        sleepLogRepository.save(tomorrowSleepLog)
        
        val result = sleepLogRepository.findByUserAndSleepDateBetween(user, yesterday, today)
        
        assertThat(result).hasSize(2)
        assertThat(result.map { it.sleepDate }).containsExactlyInAnyOrder(yesterday, today)
    }

    @Test
    fun `should check if sleep log exists for user and date`() {
        val sleepLog = createSleepLog(user, today)
        sleepLogRepository.save(sleepLog)
        
        val exists = sleepLogRepository.existsByUserAndSleepDate(user, today)
        val doesNotExist = sleepLogRepository.existsByUserAndSleepDate(user, tomorrow)
        
        assertThat(exists).isTrue()
        assertThat(doesNotExist).isFalse()
    }

    private fun createSleepLog(user: User, sleepDate: LocalDate): SleepLog {
        val timeInBedStart = Instant.now().minus(8, ChronoUnit.HOURS)
        val timeInBedEnd = Instant.now()
        
        return SleepLog(
            user = user,
            sleepDate = sleepDate,
            timeInBedStart = timeInBedStart,
            timeInBedEnd = timeInBedEnd,
            totalTimeInBedMinutes = 480,
            morningFeeling = Feeling.GOOD
        )
    }
}