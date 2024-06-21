package com.zqc.itineraryweb.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginCheckInterceptor.class);
    private static final String ATTRIBUTE_ALREADY_FILTERED = "alreadyFiltered";

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        if (request.getAttribute(ATTRIBUTE_ALREADY_FILTERED) == null) {
            request.setAttribute(ATTRIBUTE_ALREADY_FILTERED, Boolean.TRUE);
            String url = request.getRequestURL().toString();
            logger.info("请求的URL: {}, Thread ID: {}", url, Thread.currentThread().getId());
        }
        return true;
    }
}