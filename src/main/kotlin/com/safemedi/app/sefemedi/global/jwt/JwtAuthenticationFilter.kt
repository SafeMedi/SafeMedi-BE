package com.safemedi.app.sefemedi.global.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(

    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)

        if (
            token != null &&
            SecurityContextHolder.getContext().authentication == null
        ) {
            when (val parseResult = jwtProvider.parseToken(token)) {
                is TokenParseResult.Success -> {
                    val authentication =
                        UsernamePasswordAuthenticationToken(
                            parseResult.subject,
                            null,
                            emptyList()
                        )

                    SecurityContextHolder.getContext().authentication =
                        authentication
                }
                TokenParseResult.Expired,
                TokenParseResult.Invalid -> Unit
            }
        }

        filterChain.doFilter(
            request,
            response
        )
    }

    private fun resolveToken(
        request: HttpServletRequest
    ): String? {
        val authorization =
            request.getHeader("Authorization")

        if (
            authorization == null ||
            !authorization.startsWith("Bearer ")
        ) {
            return null
        }

        return authorization.substring(7)
    }
}
