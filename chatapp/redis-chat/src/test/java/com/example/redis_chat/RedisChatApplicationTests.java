package com.example.redis_chat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379"
})
class RedisChatApplicationTests {

	@Test
	void contextLoads() {
		// This test will only pass if Redis is running
		// For CI/CD, you might want to use @Disabled or mock Redis
	}

}