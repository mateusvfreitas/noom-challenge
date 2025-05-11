package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.entity.User
import org.springframework.data.jpa.repository.JpaRepository

/** JPA repository for User entity operations. */
interface UserRepository : JpaRepository<User, Long>
