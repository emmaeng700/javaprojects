package com.example.validatingforminput.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // Only rate-limit POST requests (form submissions)
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String clientIp = getClientIp(request);
        RequestCounter counter = requestCounts.computeIfAbsent(clientIp, k -> new RequestCounter());

        if (counter.isExpired()) {
            counter.reset();
        }

        if (counter.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(429);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(
                    "<html><body style='font-family:sans-serif;text-align:center;padding:60px;'>"
                    + "<h1>Too Many Requests</h1>"
                    + "<p>You have exceeded the rate limit. Please wait a moment and try again.</p>"
                    + "</body></html>");
            return false;
        }
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RequestCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        boolean isExpired() {
            return System.currentTimeMillis() - windowStart > 60_000;
        }

        void reset() {
            count.set(0);
            windowStart = System.currentTimeMillis();
        }

        int incrementAndGet() {
            return count.incrementAndGet();
        }
    }
}
