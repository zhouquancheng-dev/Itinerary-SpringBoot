package com.zqc.itineraryweb.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimeAspect {

    private static final Logger logger = LoggerFactory.getLogger(TimeAspect.class);

    @Around("execution(* com.zqc.itineraryweb.service.*.*(..))")
    public Object recordTime(ProceedingJoinPoint joinPoint) {
        long startTime = System.currentTimeMillis();
        Object result;

        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            // 打印异常信息，然后继续执行方法
            logger.error("方法执行发生错误: {}", e.getMessage());
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                logger.error(stackTraceElement.toString());
            }
            // 设置result为null或者其他适当的默认值，以便方法继续执行
            result = null;
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getName();

            logger.info("执行方法: {}()，在类路径:{}中，执行耗时: {}ms", methodName, className, executionTime);
        }

        return result;
    }
}