package com.noom.interview.fullstack.sleep.controller

import com.noom.interview.fullstack.sleep.dto.SleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.service.SleepLogService
import javax.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping

/**
 * REST controller for sleep log operations.
 *
 * This controller handles HTTP requests related to sleep logs.
 */
@RestController
@RequestMapping("/api/users/{userId}/sleep-logs")
class SleepLogController(private val sleepLogService: SleepLogService) {

    /**
     * Creates a new sleep log for a user.
     *
     * @param userId The ID of the user
     * @param request The sleep log data
     * @return The created sleep log with HTTP 201 (Created)
     */
    @PostMapping
    fun createSleepLog(
            @PathVariable userId: Long,
            @RequestBody @Valid request: SleepLogRequest
    ): ResponseEntity<SleepLogResponse> {
        val response = sleepLogService.createSleepLog(userId, request)
        return ResponseEntity(response, HttpStatus.CREATED)
    }

    /**
     * Retrieves the most recent sleep log for a user.
     *
     * @param userId The ID of the user
     * @return The most recent sleep log with HTTP 200 (OK)
     */
    @GetMapping("/last-night")
    fun getLastNightSleep(@PathVariable userId: Long): ResponseEntity<SleepLogResponse> {
        val response = sleepLogService.getLastNightSleep(userId)
        return ResponseEntity(response, HttpStatus.OK)
    }
}
