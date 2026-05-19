package com.safemedi.app.sefemedi.domain.drug.controller

import com.safemedi.app.sefemedi.domain.drug.dto.DrugSearchResponse
import com.safemedi.app.sefemedi.domain.drug.service.DrugSearchService
import com.safemedi.app.sefemedi.global.error.GlobalExceptionHandler
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class DrugSearchControllerTest {
    private lateinit var mockMvc: MockMvc
    private lateinit var drugSearchService: DrugSearchService

    @BeforeEach
    fun setUp() {
        drugSearchService = mock(DrugSearchService::class.java)
        mockMvc = MockMvcBuilders.standaloneSetup(DrugSearchController(drugSearchService))
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    }
    @Test
    fun `검색`() {
        // Given
        val keyword = "아목시실린"
        val mockResponse = listOf(
            DrugSearchResponse(
                atcCode = "J01CA04",
                drugName = "종근당아목시실린캡슐500mg",
            )
        )
        given(drugSearchService.search(keyword)).willReturn(mockResponse)

        // When
        val resultActions = mockMvc.perform(
            get("/api/v1/drugs/search")
                .param("keyword", keyword)
                .accept(MediaType.APPLICATION_JSON)
        )

        // Then
        resultActions
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].atcCode").value("J01CA04"))
            .andExpect(jsonPath("$[0].drugName").value("종근당아목시실린캡슐500mg"))
    }

    @Test
    fun `두글자 미만일때 에러`() {
        // Given
        val invalidKeyword = "아"

        // When
        val resultActions = mockMvc.perform(
            get("/api/v1/drugs/search")
                .param("keyword", invalidKeyword)
                .accept(MediaType.APPLICATION_JSON)
        )

        // Then
        resultActions
            .andExpect(status().isBadRequest)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code").value("VAL_005"))
            .andExpect(jsonPath("$.message").value("검색어는 최소 2글자 이상 입력해야 합니다."))
    }
}
