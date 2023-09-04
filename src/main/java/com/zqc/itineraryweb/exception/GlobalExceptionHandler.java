package com.zqc.itineraryweb.exception;

import com.zqc.itineraryweb.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 全局异常处理方法，处理所有未捕获的异常。
     *
     * @param exception 抛出的异常
     * @return 包含错误信息的 Result 对象
     */
    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception exception) {
        LOGGER.error("发生了错误: {}, 错误信息为: {}", exception, exception.getMessage());
        return Result.error("请求异常");
    }
}
