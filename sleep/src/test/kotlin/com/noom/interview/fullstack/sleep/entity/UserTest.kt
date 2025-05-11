package com.noom.interview.fullstack.sleep.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import com.noom.interview.fullstack.sleep.domain.Feeling

class UserTest {

    @Test
    fun `should create user with default constructor`() {
        val user = User()
        
        assertThat(user.id).isNull()
        assertThat(user.createdAt).isNotNull
        assertThat(user.sleepLogs).isEmpty()
    }
    
    @Test
    fun `should create user with id`() {
        val id = 1L
        val createdAt = Instant.now().minus(1, ChronoUnit.DAYS)
        
        val user = User(id = id, createdAt = createdAt)
        
        assertThat(user.id).isEqualTo(id)
        assertThat(user.createdAt).isEqualTo(createdAt)
        assertThat(user.sleepLogs).isEmpty()
    }
    
    @Test
    fun `should add sleep log to user`() {
        val user = User(id = 1L)
        val sleepLog = SleepLog(
            user = user,
            sleepDate = java.time.LocalDate.now(),
            timeInBedStart = Instant.now().minus(8, ChronoUnit.HOURS),
            timeInBedEnd = Instant.now(),
            totalTimeInBedMinutes = 480,
            morningFeeling = Feeling.GOOD,
        )
        
        user.sleepLogs.add(sleepLog)
        
        assertThat(user.sleepLogs).hasSize(1)
        assertThat(user.sleepLogs).contains(sleepLog)
    }
}