package com.noom.interview.fullstack.sleep.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.noom.interview.fullstack.sleep.SleepApplication.Companion.UNIT_TEST_PROFILE
import com.noom.interview.fullstack.sleep.domain.Feeling
import com.noom.interview.fullstack.sleep.dto.SleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.dto.SleepStatsResponse
import com.noom.interview.fullstack.sleep.service.SleepLogService
import com.noom.interview.fullstack.sleep.service.exception.UserNotFoundException
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@WebMvcTest(SleepLogController::class)
@ActiveProfiles(UNIT_TEST_PROFILE)
class SleepLogControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var sleepLogService: SleepLogService

    private val userId = 1L

    @Test
    fun `createSleepLog should return 201 with created sleep log`() {
        val timeInBedStart = Instant.now().minus(8, ChronoUnit.HOURS)
        val timeInBedEnd = Instant.now()
        val request = SleepLogRequest(
            timeInBedStart = timeInBedStart,
            timeInBedEnd = timeInBedEnd,
            morningFeeling = Feeling.GOOD
        )
        
        val response = SleepLogResponse(
            id = 1L,
            sleepDate = LocalDate.now(),
            timeInBedStart = timeInBedStart,
            timeInBedEnd = timeInBedEnd,
            totalTimeInBedMinutes = 480,
            morningFeeling = Feeling.GOOD
        )
        
        `when`(sleepLogService.createSleepLog(userId, request)).thenReturn(response)
        
        mockMvc.perform(
            post("/api/users/$userId/sleep-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.morningFeeling").value("GOOD"))
    }

    @Test
    fun `getLastNightSleep should return 200 with most recent sleep log`() {
        val response = SleepLogResponse(
            id = 1L,
            sleepDate = LocalDate.now(),
            timeInBedStart = Instant.now().minus(8, ChronoUnit.HOURS),
            timeInBedEnd = Instant.now(),
            totalTimeInBedMinutes = 480,
            morningFeeling = Feeling.GOOD
        )
        
        `when`(sleepLogService.getLastNightSleep(userId)).thenReturn(response)
        
        mockMvc.perform(get("/api/users/$userId/sleep-logs/last-night"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.morningFeeling").value("GOOD"))
    }

    @Test
    fun `getThirtyDayStats should return 200 with sleep statistics`() {
        val today = LocalDate.now()
        val startDate = today.minusDays(29)
        
        val response = SleepStatsResponse(
            startDate = startDate,
            endDate = today,
            numberOfLogs = 3,
            averageTimeInBedMinutes = 480.0,
            averageBedTime = LocalTime.of(22, 0),
            averageWakeTime = LocalTime.of(6, 0),
            feelingFrequencies = mapOf(
                Feeling.GOOD to 2,
                Feeling.OK to 1
            )
        )
        
        `when`(sleepLogService.getThirtyDayStats(userId)).thenReturn(response)
        
        mockMvc.perform(get("/api/users/$userId/sleep-logs/stats"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.numberOfLogs").value(3))
            .andExpect(jsonPath("$.averageTimeInBedMinutes").value(480.0))
            .andExpect(jsonPath("$.feelingFrequencies.GOOD").value(2))
            .andExpect(jsonPath("$.feelingFrequencies.OK").value(1))
    }

    @Test
    fun `should handle UserNotFoundException`() {
        `when`(sleepLogService.getLastNightSleep(userId))
            .thenThrow(UserNotFoundException(userId))
        
        mockMvc.perform(get("/api/users/$userId/sleep-logs/last-night"))
            .andExpect(status().isNotFound)
    }
}