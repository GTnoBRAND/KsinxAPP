//package org.jas.ksinxapp.RateLimit;
//
//import lombok.AllArgsConstructor;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//
//@Service
//@RequiredArgsConstructor
//public class FixedWindowRateLimiter {
//
//    private final StringRedisTemplate stringRedisTemplate;
//
//    public boolean allowRequest(
//            String clientId,
//            int limit,
//            Duration windowSize
//    ){
//
//        long windowIndex = System.currentTimeMillis() / windowSize.toMillis();
//        String key = String.format("rate:%s%s", clientId, windowIndex);
//
//        //count the hints
//        Long hints = stringRedisTemplate.opsForValue()
//                .increment(key);
//        if(hints != null && hints == 1L){
//            stringRedisTemplate.expire(key, windowSize);
//        }
//
//        return hints != null && hints <= limit;
//    }
//}
