package com.safemedi.app.sefemedi.domain.user.repository

import com.safemedi.app.sefemedi.domain.user.entity.UserHealthProfile
import org.springframework.data.jpa.repository.JpaRepository

interface UserHealthProfileRepository : JpaRepository<UserHealthProfile, Long>
