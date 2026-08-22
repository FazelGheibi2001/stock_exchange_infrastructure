package com.irtech.brokerinfrastructure.redis.models.session;

import com.irtech.brokerinfrastructure.config.redis.RedisTemplateFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class LoginRedisConfig {

    @Bean
    RedisTemplate<String, LoginRedisModel> loginRedisTemplate(RedisTemplateFactory factory) {
        return factory.create(LoginRedisModel.class);
    }
}
