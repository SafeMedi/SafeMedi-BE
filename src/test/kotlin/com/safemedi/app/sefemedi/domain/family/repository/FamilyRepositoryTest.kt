package com.safemedi.app.sefemedi.domain.family.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.jpa.repository.EntityGraph

class FamilyRepositoryTest {

    @Test
    fun `findWithConnectedUserById fetches connectedUser via entity graph`() {
        val method = FamilyRepository::class.java.getMethod(
            "findWithConnectedUserById",
            Long::class.javaPrimitiveType,
        )

        val entityGraph = method.getAnnotation(EntityGraph::class.java)

        assertEquals(listOf("connectedUser"), entityGraph.attributePaths.toList())
        assertEquals(EntityGraph.EntityGraphType.FETCH, entityGraph.type)
    }

    @Test
    fun `findAllByUser_IdOrderByCreatedAtAsc fetches connectedUser via entity graph`() {
        val method = FamilyRepository::class.java.getMethod(
            "findAllByUser_IdOrderByCreatedAtAsc",
            Long::class.javaPrimitiveType,
        )

        val entityGraph = method.getAnnotation(EntityGraph::class.java)

        assertEquals(listOf("connectedUser"), entityGraph.attributePaths.toList())
        assertEquals(EntityGraph.EntityGraphType.FETCH, entityGraph.type)
    }
}
