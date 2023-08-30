package com.zqc.itineraryweb.exception;

import com.zqc.itineraryweb.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Result<Object> ex(Exception exception) {
        logger.error("发生了错误: {}, 错误信息为: {}", exception, exception.getMessage());
        return Result.error("请求异常");
    }

}
