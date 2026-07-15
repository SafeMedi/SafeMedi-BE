package com.safemedi.app.sefemedi.domain.family.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class FamilyInvitationConfig {

    @Bean
    fun utcClock(): Clock = Clock.systemUTC()
}
