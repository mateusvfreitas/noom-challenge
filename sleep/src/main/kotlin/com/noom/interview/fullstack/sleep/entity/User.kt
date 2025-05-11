package com.noom.interview.fullstack.sleep.entity

import java.time.Instant
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

/**
 * Represents a user of the sleep tracking application.
 * 
 * Each user can have multiple sleep logs associated with their account.
 */
@Entity
@Table(name = "users")
class User(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
        @Column(name = "created_at") val createdAt: Instant = Instant.now(),
        @OneToMany(
                mappedBy = "user",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY,
                orphanRemoval = true
        )
        val sleepLogs: MutableList<SleepLog> = mutableListOf()
)
