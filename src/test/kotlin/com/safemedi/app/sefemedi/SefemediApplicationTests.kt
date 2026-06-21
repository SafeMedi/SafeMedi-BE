package com.safemedi.app.sefemedi

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import tools.jackson.databind.json.JsonMapper

@SpringBootTest(
	properties = [
		"spring.datasource.url=jdbc:h2:mem:safemedi-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=none",
		"spring.flyway.enabled=false",
		"jwt.secret=abcdefghijklmnopqrstuvwxyz123456",
	]
)
class SefemediApplicationTests {

	@Test
	fun contextLoads() {
	}

	@TestConfiguration
	class TestBeans {
		@Bean
		@Primary
		fun jsonMapper(): JsonMapper {
			return JsonMapper.builder().build()
		}
	}
}
