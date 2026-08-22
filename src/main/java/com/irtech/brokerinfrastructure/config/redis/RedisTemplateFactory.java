package com.irtech.brokerinfrastructure.config.redis;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;


@Component
public class RedisTemplateFactory {

    private final RedisConnectionFactory connectionFactory;

    public RedisTemplateFactory(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }


    public <T> RedisTemplate<String, T> create(Class<T> clazz) {

        RedisTemplate<String, T> template =
                new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer =
                new StringRedisSerializer();

        JacksonJsonRedisSerializer<T> jsonSerializer =
                new JacksonJsonRedisSerializer<>(clazz);

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();

        return template;
    }
}
