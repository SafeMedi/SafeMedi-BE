package com.safemedi.app.sefemedi.domain.drug.service

import com.safemedi.app.sefemedi.domain.drug.entity.DrugMaster
import com.safemedi.app.sefemedi.domain.drug.repository.DrugMasterRepository
import com.safemedi.app.sefemedi.global.error.BusinessException
import com.safemedi.app.sefemedi.global.error.ErrorCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DrugSearchServiceTest {
    private lateinit var drugMasterRepository: DrugMasterRepository
    private lateinit var drugSearchService: DrugSearchService

    @BeforeEach
    fun setUp() {
        drugMasterRepository = mock(DrugMasterRepository::class.java)
        drugSearchService = DrugSearchService(drugMasterRepository)
    }

    @Test
    fun `search returns paged drug search result`() {
        val drug = DrugMaster(
            drugCode = "D001",
            drugName = "Tylenol 500mg",
            atcCode = "N02BE01",
        )

        given(
            drugMasterRepository.findByDrugNameContainingAndAtcCodeIsNotNullOrderByDrugNameAsc(
                keyword = "tylenol",
                pageable = PageRequest.of(0, 20),
            )
        ).willReturn(SliceImpl(listOf(drug), PageRequest.of(0, 20), false))

        val response = drugSearchService.search(
            keyword = "tylenol",
            page = 0,
            size = 20,
        )

        assertEquals(0, response.page)
        assertEquals(20, response.size)
        assertEquals(true, response.isLast)
        assertEquals(1, response.content.size)

        val item = response.content.single()
        assertEquals("D001", item.drugCode)
        assertEquals("N02BE01", item.atcCode)
        assertEquals("Tylenol 500mg", item.drugName)
    }

    @Test
    fun `search throws when page is invalid`() {
        val exception = assertFailsWith<BusinessException> {
            drugSearchService.search(
                keyword = "tylenol",
                page = -1,
                size = 20,
            )
        }

        assertEquals(ErrorCode.INVALID_PAGE_REQUEST, exception.errorCode)
    }

    @Test
    fun `search throws when size is invalid`() {
        val exception = assertFailsWith<BusinessException> {
            drugSearchService.search(
                keyword = "tylenol",
                page = 0,
                size = 51,
            )
        }

        assertEquals(ErrorCode.INVALID_PAGE_REQUEST, exception.errorCode)
    }
}
