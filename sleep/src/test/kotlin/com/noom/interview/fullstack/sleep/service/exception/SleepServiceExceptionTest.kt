package com.noom.interview.fullstack.sleep.service.exception

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SleepServiceExceptionTest {

    @Test
    fun `should create exception with message only`() {
        val errorMessage = "Test error message"
        
        val exception = SleepServiceException(errorMessage)
        
        assertThat(exception.message).isEqualTo(errorMessage)
        assertThat(exception.cause).isNull()
    }
    
    @Test
    fun `should create exception with message and cause`() {
        val errorMessage = "Test error message"
        val cause = RuntimeException("Original cause")
        
        val exception = SleepServiceException(errorMessage, cause)
        
        assertThat(exception.message).isEqualTo(errorMessage)
        assertThat(exception.cause).isEqualTo(cause)
    }
}