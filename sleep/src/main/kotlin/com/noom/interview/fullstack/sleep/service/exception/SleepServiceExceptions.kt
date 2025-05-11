package com.noom.interview.fullstack.sleep.service.exception

open class ResourceNotFoundException(message: String) : RuntimeException(message)

class UserNotFoundException(userId: Long) :
        ResourceNotFoundException("User with ID $userId not found.")

class DuplicateResourceException(message: String) : RuntimeException(message)

class InvalidInputException(message: String) : RuntimeException(message)

class SleepServiceException(message: String, cause: Throwable? = null) :
        RuntimeException(message, cause)
