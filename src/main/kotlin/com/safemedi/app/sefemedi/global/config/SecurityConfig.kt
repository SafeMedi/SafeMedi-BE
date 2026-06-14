package com.safemedi.app.sefemedi.global.config

import com.safemedi.app.sefemedi.global.error.ErrorCode
import com.safemedi.app.sefemedi.global.error.ErrorResponse
import com.safemedi.app.sefemedi.global.jwt.JwtAuthenticationFilter
import com.safemedi.app.sefemedi.global.security.CustomOAuth2UserService
import com.safemedi.app.sefemedi.global.security.OAuth2SuccessHandler
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import tools.jackson.databind.json.JsonMapper

@Configuration
class SecurityConfig(

    private val jsonMapper: JsonMapper,
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {

    @Bean
    fun filterChain(
        http: HttpSecurity,
    ): SecurityFilterChain {

        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS,
                )
            }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/api/v1/login/kakao",
                    "/auth/reissue",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                ).permitAll()

                it.requestMatchers(
                    "/api/v1/users/me",
                    "/api/v1/users/me/tutorial",
                    "/api/v1/prescriptions/analyze",
                    "/api/v1/prescriptions",
                    "/api/v1/prescriptions/**",
                    "/api/v1/medication-records/today",
                    "/api/v1/drugs/search",
                ).authenticated()

                it.anyRequest().denyAll()
            }
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    writeErrorResponse(
                        response = response,
                    )
                }
                it.accessDeniedHandler { _, response, _ ->
                    response.status = HttpStatus.FORBIDDEN.value()
                }
            }

            .oauth2Login {

                it.authorizationEndpoint { authorization ->

                    authorization.baseUri(
                        "/api/v1/login",
                    )
                }

                it.userInfoEndpoint { userInfo ->

                    userInfo.userService(
                        customOAuth2UserService,
                    )
                }
                it.successHandler(
                    oAuth2SuccessHandler,
                )
            }
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java,
            )

        return http.build()
    }

    private fun writeErrorResponse(
        response: HttpServletResponse,
    ) {
        val errorCode = ErrorCode.INVALID_ACCESS_TOKEN
        response.status = errorCode.status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()
        jsonMapper.writeValue(
            response.writer,
            ErrorResponse(
                code = errorCode.code,
                message = errorCode.message,
            ),
        )
    }
}
