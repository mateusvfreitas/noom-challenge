package com.noom.interview.fullstack.sleep.config

import com.noom.interview.fullstack.sleep.SleepApplication.Companion.UNIT_TEST_PROFILE
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.Connection
import javax.sql.DataSource

@Configuration
@Profile(UNIT_TEST_PROFILE)
class TestDatabaseConfiguration {
    
    @Bean
    fun dataSource(): DataSource {
        return DataSourceBuilder.create()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
            .username("sa")
            .password("")
            .build()
    }

    @Bean
    fun dbConnection(dataSource: DataSource): Connection = dataSource.connection

    @Bean
    fun namedParameterJdbcTemplate(dataSource: DataSource): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(dataSource)
    }
}