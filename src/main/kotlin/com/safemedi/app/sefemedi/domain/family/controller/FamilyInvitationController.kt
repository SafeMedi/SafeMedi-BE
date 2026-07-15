package com.safemedi.app.sefemedi.domain.family.controller

import com.safemedi.app.sefemedi.domain.family.dto.FamilyInvitationCreateResponse
import com.safemedi.app.sefemedi.domain.family.service.FamilyInvitationCreateService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/family-invitations")
class FamilyInvitationController(
    private val familyInvitationCreateService: FamilyInvitationCreateService,
) {

    @PostMapping
    fun createInvitation(authentication: Authentication): ResponseEntity<FamilyInvitationCreateResponse> {
        val result = familyInvitationCreateService.create(authentication.name)
        return ResponseEntity
            .status(if (result.created) HttpStatus.CREATED else HttpStatus.OK)
            .body(result.response)
    }
}
