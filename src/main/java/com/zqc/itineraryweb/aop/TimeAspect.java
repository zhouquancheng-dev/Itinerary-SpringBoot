package com.zqc.itineraryweb.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
public class TimeAspect {

    private static final Logger logger = LoggerFactory.getLogger(TimeAspect.class);

    @Around("execution(* com.zqc.itineraryweb.service.*.*(..))")
    public Object recordTime(ProceedingJoinPoint joinPoint) {
        long begin = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            logger.error("发生了错误: {}, 错误信息为: {}", e, e.getMessage());
            throw new RuntimeException(e);
        }
        long end = System.currentTimeMillis();
        log.info("在包路径为: {} 中的方法: [{}], 执行耗时: {}ms", joinPoint.getSignature(), joinPoint.getSignature().getName(), end - begin);
        return result;
    }
}

