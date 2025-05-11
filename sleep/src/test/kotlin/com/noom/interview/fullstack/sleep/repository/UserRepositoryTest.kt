package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.SleepApplication.Companion.UNIT_TEST_PROFILE
import com.noom.interview.fullstack.sleep.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles(UNIT_TEST_PROFILE)
class UserRepositoryTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `should save and retrieve user`() {
        val user = User()
        
        val savedUser = userRepository.save(user)
        val retrievedUser = userRepository.findById(savedUser.id!!).orElse(null)
        
        assertThat(retrievedUser).isNotNull
        assertThat(retrievedUser.id).isEqualTo(savedUser.id)
        assertThat(retrievedUser.createdAt).isNotNull()
    }
}