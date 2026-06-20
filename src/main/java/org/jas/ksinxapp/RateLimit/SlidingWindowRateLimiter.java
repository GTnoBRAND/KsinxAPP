package org.jas.ksinxapp.RateLimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SlidingWindowRateLimiter {

    private final StringRedisTemplate redisTemplate;

    public boolean allowRequest(
            String clientId,
            int limit,
            Duration windowSize
            ){
        long windowMs = windowSize.toMillis();
        long now = System.currentTimeMillis();

        //step2 figure out which window we are in
        long currentWindow = now /windowMs;
        long previousWindow = currentWindow -1;

        //step 3 check how far into the current window we are
        long elapsedIntoWindow = now % windowMs;

        //step 4 compute the weight for the previous window
        double weight = (double) (windowMs - elapsedIntoWindow) /windowMs;

        //step 5 build the redis key for both windows
        String currentKey = String.format("rate:%s:%d",clientId, currentWindow);
        String previousKey = String.format("rate:%s:%d", clientId, previousWindow);


        //step 6 read previous windows count
        String previousRaw = redisTemplate.opsForValue().get(previousKey);
        long previousCount = previousRaw == null ? 0 : Long.parseLong(previousRaw);


        //step 7 register this req, by incr the current window
        Long currentCount = redisTemplate.opsForValue().increment(currentKey);

        //step 8 set an expiry the first time key gets created
        if(currentCount  != null && currentCount == 1L){
            redisTemplate.expire(currentKey, windowSize.multipliedBy(2));
        }

        //null safety
        long curr = currentCount == null ? 1 :currentCount;

        //actual sliding logic
        double estimated = curr + previousCount * weight;

        return estimated <= limit;
    }
}
