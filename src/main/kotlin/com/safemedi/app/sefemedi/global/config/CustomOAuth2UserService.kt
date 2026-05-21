package com.safemedi.app.sefemedi.global.security

import com.safemedi.app.sefemedi.domain.user.entity.User
import com.safemedi.app.sefemedi.domain.user.repository.UserRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService(

    private val userRepository: UserRepository

) : DefaultOAuth2UserService() {

    override fun loadUser(
        userRequest: OAuth2UserRequest
    ): OAuth2User {

        val oAuth2User = super.loadUser(userRequest)

        val kakaoId = oAuth2User.attributes["id"]
            .toString()
            .toLong()

        var user = userRepository.findByKakaoId(
            kakaoId
        )

        if (user == null) {

            user = userRepository.save(

                User(
                    kakaoId = kakaoId
                )
            )
        }

        return DefaultOAuth2User(
            emptyList(),
            oAuth2User.attributes,
            "id"
        )
    }
}