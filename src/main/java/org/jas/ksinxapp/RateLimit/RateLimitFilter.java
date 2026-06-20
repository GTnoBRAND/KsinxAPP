package org.jas.ksinxapp.RateLimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Order(60)
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final SlidingWindowRateLimiter slidingWindowRateLimiter;
    //the three tiers
    public enum Tier{
        STRICT(5, Duration.ofMinutes(15)),
        MODERATE(40, Duration.ofMinutes(1)),
        GENEROUS(200, Duration.ofMinutes(1));

        final int limit;
        final Duration window;
        Tier(int limit, Duration window){
            this.limit = limit;
            this.window = window;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        Tier tier = resolveTier(path, request.getMethod());

        String ip = clientIp(request);
        String key = tier.name() + " " + ip;

        boolean allowed = slidingWindowRateLimiter.allowRequest(key, tier.limit, tier.window);

        if(!allowed){
            response.setStatus(429);
            response.getWriter().write("Rate limit exceeded. Try again later");
            return;
        }
        filterChain.doFilter(request, response);
    }

    // ---- the rule table: first match wins, top to bottom ----
    private Tier resolveTier(String path, String method){
        //Strict: auth + memory
        if(path.equals("/api/v1/users/login")
            || path.equals("/api/v1/users/register")
            || path.equals("/api/v1/users/resend-verification")
            || (path.equals("/api/v1/payments"))){
            return Tier.STRICT;
        }

        //MODERATE
        if(method.equals("POST") || method.equals("PUT") || method.equals("DELETE")){
            return Tier.MODERATE;
        }

        //GENEROUS
        return Tier.GENEROUS;
    }

    // ---- extract the real client IP ----
    private String clientIp(HttpServletRequest request){
        String forwarded = request.getHeader("X-Forwarded-For");
        if(forwarded != null && !forwarded.isBlank()){
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
