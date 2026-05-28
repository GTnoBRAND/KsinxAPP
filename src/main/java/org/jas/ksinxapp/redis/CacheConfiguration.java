package org.jas.ksinxapp.redis;

import org.jas.ksinxapp.model.Course;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

@EnableCaching
@Configuration
public class CacheConfiguration {

    @Bean
    public RedisTemplate<String, Course> courseRedisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ){

        RedisTemplate<String, Course> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(connectionFactory);

        //serialize and deserialize cache
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        var serializer = new JacksonJsonRedisSerializer<>(objectMapper, Course.class);

        redisTemplate.setValueSerializer(serializer);

        redisTemplate.afterPropertiesSet();
        return  redisTemplate;

    }
}
