package com.noom.interview.fullstack.sleep.entity

import com.noom.interview.fullstack.sleep.domain.Feeling
import java.time.Instant
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

/**
 * Represents a single sleep record for a user.
 * 
 * A sleep log tracks when a user went to bed, when they woke up,
 * and how they felt upon waking. The total time in bed is calculated
 * from the start and end times.
 */
@Entity
@Table(name = "sleep_logs")
class SleepLog(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false)
        val user: User,
        @Column(name = "sleep_date", nullable = false) val sleepDate: LocalDate,
        @Column(name = "time_in_bed_start", nullable = false) val timeInBedStart: Instant,
        @Column(name = "time_in_bed_end", nullable = false) val timeInBedEnd: Instant,
        @Column(name = "total_time_in_bed_minutes", nullable = false)
        val totalTimeInBedMinutes: Int,
        @Enumerated(EnumType.STRING)
        @Column(name = "morning_feeling", nullable = false)
        val morningFeeling: Feeling,
        @CreationTimestamp
        @Column(name = "created_at", nullable = false, updatable = false)
        val createdAt: Instant? = null,
        @UpdateTimestamp
        @Column(name = "updated_at", nullable = false)
        val updatedAt: Instant? = null
) {
    protected constructor() :
            this(
                    id = null,
                    user = User(),
                    sleepDate = LocalDate.now(),
                    timeInBedStart = Instant.now(),
                    timeInBedEnd = Instant.now(),
                    totalTimeInBedMinutes = 0,
                    morningFeeling = Feeling.GOOD
            )
}
