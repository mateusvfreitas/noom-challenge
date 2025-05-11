package com.noom.interview.fullstack.sleep.entity

import com.noom.interview.fullstack.sleep.domain.Feeling
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class SleepLogTest {

    @Test
    fun `should create sleep log with all properties`() {
        val user = User()
        val sleepDate = LocalDate.now()
        val timeInBedStart = Instant.now().minus(8, ChronoUnit.HOURS)
        val timeInBedEnd = Instant.now()
        val totalTimeInBedMinutes = 480
        val morningFeeling = Feeling.GOOD
        
        val sleepLog = SleepLog(
            id = 1L,
            user = user,
            sleepDate = sleepDate,
            timeInBedStart = timeInBedStart,
            timeInBedEnd = timeInBedEnd,
            totalTimeInBedMinutes = totalTimeInBedMinutes,
            morningFeeling = morningFeeling
        )
        
        assertThat(sleepLog.id).isEqualTo(1L)
        assertThat(sleepLog.user).isEqualTo(user)
        assertThat(sleepLog.sleepDate).isEqualTo(sleepDate)
        assertThat(sleepLog.timeInBedStart).isEqualTo(timeInBedStart)
        assertThat(sleepLog.timeInBedEnd).isEqualTo(timeInBedEnd)
        assertThat(sleepLog.totalTimeInBedMinutes).isEqualTo(totalTimeInBedMinutes)
        assertThat(sleepLog.morningFeeling).isEqualTo(morningFeeling)
        assertThat(sleepLog.createdAt).isNull()
        assertThat(sleepLog.updatedAt).isNull()
    }
    
    @Test
    fun `should create sleep log with default constructor`() {
        val constructor = SleepLog::class.java.getDeclaredConstructor()
        constructor.isAccessible = true
        val sleepLog = constructor.newInstance()
        
        assertThat(sleepLog).isNotNull
        assertThat(sleepLog.id).isNull()
        assertThat(sleepLog.user).isNotNull
        assertThat(sleepLog.sleepDate).isNotNull
        assertThat(sleepLog.timeInBedStart).isNotNull
        assertThat(sleepLog.timeInBedEnd).isNotNull
        assertThat(sleepLog.totalTimeInBedMinutes).isEqualTo(0)
        assertThat(sleepLog.morningFeeling).isEqualTo(Feeling.GOOD)
    }
}