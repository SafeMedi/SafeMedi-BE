package com.safemedi.app.sefemedi.domain.family.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.jpa.repository.EntityGraph

class FamilyInvitationRepositoryTest {

    @Test
    fun `findByTokenHash는 inviter를 함께 조회한다`() {
        val method = FamilyInvitationRepository::class.java.getMethod(
            "findByTokenHash",
            String::class.java,
        )

        val entityGraph = method.getAnnotation(EntityGraph::class.java)

        assertEquals(listOf("inviter"), entityGraph.attributePaths.toList())
        assertEquals(EntityGraph.EntityGraphType.FETCH, entityGraph.type)
    }
}
