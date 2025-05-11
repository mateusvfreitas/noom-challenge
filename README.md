# Sleep Tracking API

## Overview

This project is a RESTful API for tracking sleep patterns. It allows users to log their sleep times, record how they felt upon waking, and view statistics about their sleep habits. This project was developed as a solution to the Noom coding challenge.

## Features

-   Create sleep logs with bed time, wake time, and morning feeling.
-   Retrieve the most recent sleep log for a user.
-   View sleep statistics for the last 30 days for a user.
-   Data validation to ensure proper sleep log entries.
-   User-specific data access for all operations.
-   Robust error handling with specific HTTP status codes and detailed JSON error messages.

## Technologies

-   **Language**: Kotlin (JDK 11)
-   **Framework**: Spring Boot (v2.7.17)
-   **Database**: PostgreSQL (v13)
-   **Database Migrations**: Flyway
-   **Containerization**: Docker & Docker Compose
-   **Testing**: JUnit 5, Mockito, AssertJ, Spring Boot Test (includes `@DataJpaTest`, `@WebMvcTest`)
-   **Build Tool**: Gradle

## Getting Started

### Prerequisites

-   Docker and Docker Compose installed
-   Git installed
-   JDK 11 (if building/running outside of Docker)

### Setup and Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/mateusvfreitas/noom-challenge.git
    cd noom-challenge
    ```

2.  **Start the application using Docker Compose:**
    This command builds the Spring Boot application Docker image and starts both the application and PostgreSQL containers.
    ```bash
    docker-compose up --build
    ```
    The API will then be available at `http://localhost:8080`.

3.  **Create a test user:**
    After the application and database are running, you may need to manually create a user to interact with the API, as user creation endpoints are not part of this scope. The API expects a `userId` in the path. This command creates a user with ID `1`.
    ```bash
    docker exec -it postgres_db psql -U user -d postgres
    ```
    followed by
    ```
    INSERT INTO users (id) VALUES (1) ON CONFLICT (id) DO NOTHING;
    ```

## API Endpoints

All endpoints are prefixed with `/api/users/{userId}/`. Replace `{userId}` with an actual user ID (e.g., `1` if you used the command above).

### Create a Sleep Log

-   **Endpoint:** `POST /api/users/{userId}/sleep-logs`
-   **Description:** Creates a new sleep log for the specified user.
-   **Request Body (`application/json`):**
    ```json
    {
        "timeInBedStart": "2025-05-10T22:00:00Z",
        "timeInBedEnd": "2025-05-11T06:30:00Z",
        "morningFeeling": "GOOD"
    }
    ```
    * `timeInBedStart`, `timeInBedEnd`: Must be valid ISO 8601 Instants (UTC `Z` is recommended). `timeInBedEnd` must be after `timeInBedStart`. Duration must be between 1 minute and 24 hours.
    * `morningFeeling`: Must be one of `BAD`, `OK`, `GOOD`.
-   **Success Response:** `201 Created` with the created `SleepLogResponse` object.
-   **Error Responses:**
    * `400 Bad Request`: If input validation fails (e.g., missing fields, invalid times, invalid feeling).
    * `404 Not Found`: If the specified `userId` does not exist.
    * `409 Conflict`: If a sleep log for the derived `sleepDate` already exists for this user.

### Get Last Night's Sleep

-   **Endpoint:** `GET /api/users/{userId}/sleep-logs/last-night`
-   **Description:** Retrieves the most recent sleep log for the specified user, based on the derived `sleepDate` (the date the user woke up).
-   **Success Response:** `200 OK` with the `SleepLogResponse` object.
-   **Error Responses:**
    * `404 Not Found`: If the `userId` does not exist or if no sleep logs are found for the user.

### Get 30-Day Sleep Statistics

-   **Endpoint:** `GET /api/users/{userId}/sleep-logs/stats`
-   **Description:** Retrieves aggregated sleep statistics for the specified user over the last 30 days (inclusive of the current day).
-   **Success Response (`200 OK`) with `SleepStatsResponse`:**
    ```json
    {
        "startDate": "2025-04-12", // Example
        "endDate": "2025-05-11",   // Example
        "numberOfLogs": 15,
        "averageTimeInBedMinutes": 450.5,
        "averageBedTime": "23:10:30", // LocalTime format HH:mm:ss
        "averageWakeTime": "07:15:00",// LocalTime format HH:mm:ss
        "feelingFrequencies": {
            "GOOD": 8,
            "OK": 5,
            "BAD": 2
        }
    }
    ```
    * Returns stats with `numberOfLogs: 0` and `null` averages if no logs exist in the period for a valid user.
-   **Error Responses:**
    * `404 Not Found`: If the `userId` does not exist.

*(For all error responses, a standardized JSON object is returned providing details such as `timestamp`, `status`, `error`, `message`, `path`, and specific `errors` for validation failures.)*

## Project Structure

The project follows a standard layered architecture:

-   `com.noom.interview.fullstack.sleep.config`: Spring `@Configuration` classes (e.g., `DatabaseConfiguration.kt` if used for custom datasource beans).
-   `com.noom.interview.fullstack.sleep.controller`: REST API endpoints (`SleepLogController.kt`).
    -   `com.noom.interview.fullstack.sleep.controller.advice`: Global exception handling (`GlobalExceptionHandler.kt`).
-   `com.noom.interview.fullstack.sleep.domain`: Core domain models (`Feeling.kt` enum).
-   `com.noom.interview.fullstack.sleep.dto`: Data Transfer Objects for API requests/responses (e.g., `SleepLogRequest.kt`, `StandardErrorResponse.kt`).
-   `com.noom.interview.fullstack.sleep.entity`: JPA entity models (`SleepLog.kt`, `User.kt`).
-   `com.noom.interview.fullstack.sleep.service.exception`: Custom application exceptions (`SleepServiceExceptions.kt`).
-   `com.noom.interview.fullstack.sleep.repository`: Data access layer using Spring Data JPA (`SleepLogRepository.kt`, `UserRepository.kt`).
-   `com.noom.interview.fullstack.sleep.service`: Business logic layer (`SleepLogService.kt`).
-   `src/main/resources/db/migration`: Flyway database migration scripts (e.g., `V1.1__create_sleep_logs_table`).
-   `src/main/resources/application.properties`: Spring Boot application configuration.

## Testing

The project includes unit and integration tests to ensure code quality and correctness.

-   **Service Layer (`SleepLogServiceTest.kt`)**: Unit tests using JUnit 5 and Mockito, covering business logic, happy paths, edge cases, and error conditions.
-   **Repository Layer (`SleepLogRepositoryTest.kt`)**: Integration tests using `@DataJpaTest` with an H2 in-memory database to verify custom-derived query methods.
-   **Controller Layer (`SleepLogControllerTest.kt`)**: Unit tests using `@WebMvcTest` and `MockMvc` to validate request mapping, parameter binding, service delegation, and HTTP responses (including error handling via `GlobalExceptionHandler`).
-   **To run all tests (from the project root directory):**
    ```bash
    ./gradlew test
    ```
    Test reports are generated in `build/reports/tests/test/index.html`.

    Test coverage reports are generated in `build/reports/jacoco/test/html/index.html`.

## Key Design Decisions

-   **`sleepDate` Derivation**: `sleepDate` is derived from the date part of `timeInBedEnd` in the UTC timezone to ensure consistent daily logging.
-   **Average Bed/Wake Time Calculation**: Implemented logic in `SleepLogService` to average `LocalTime` values, specifically handling the common scenario of bedtimes crossing midnight by normalizing times around a logical day pivot before averaging. Calculations are based on UTC.
-   **Global Error Handling**: A centralized `@ControllerAdvice` (`GlobalExceptionHandler`) translates exceptions into standardized, informative JSON error responses.
-   **Immutability**: Preferred `val` and immutable data structures where practical.
-   **User-Awareness**: All API operations are scoped by `userId` passed as a path variable.

## API Testing

A Postman collection (`SleepLoggerAPI.postman_collection.json`) is included in the root of this repository to facilitate API testing.

1.  Import the collection into Postman.
2.  Ensure the `{{baseUrl}}` Postman environment variable is set (e.g., to `http://localhost:8080`).
3.  Use the predefined requests to interact with all API endpoints. Remember to replace `{{userId}}` with a valid user ID (e.g., `1` after running the setup command).
