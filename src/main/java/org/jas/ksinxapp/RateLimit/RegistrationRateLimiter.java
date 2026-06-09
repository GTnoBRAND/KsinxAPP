package org.jas.ksinxapp.RateLimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class RegistrationRateLimiter {

    public final StringRedisTemplate redisTemplate;

    public boolean tryAcquire(String key, int maxPerHour){
        String redisKey = "rateLimit:register"+ key;
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if(count != null &&  count==1L){
            redisTemplate.expire(redisKey, Duration.ofHours(1));
        }
        return count != null && count <= maxPerHour;
    }
}
