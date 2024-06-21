package com.zqc.itineraryweb.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zqc.itineraryweb.entity.Result;
import com.zqc.itineraryweb.entity.login.LoginTokenVerifyRequest;
import com.zqc.itineraryweb.entity.login.TokenVerify;
import com.zqc.itineraryweb.service.LoginService;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class LoginServiceImpl implements LoginService {

    @Value("${jg.appKey}")
    private String appKey;

    @Value("${jg.masterSecret}")
    private String masterSecret;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

    public LoginServiceImpl(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Result<?>> verifyLoginToken(LoginTokenVerifyRequest request) {
        return Mono.defer(() -> {
            // Base64 编码
            String auth = appKey + ":" + masterSecret;
            String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            // 将 DTO 对象转换为 JSON 字符串
            String requestBody;
            try {
                requestBody = objectMapper.writeValueAsString(request);
            } catch (JsonProcessingException e) {
                logger.error("Error converting request object to JSON string", e);
                return Mono.just(Result.error("Error converting request object to JSON string"));
            }

            return webClient.post()
                    .uri("https://api.verification.jpush.cn/v1/web/loginTokenVerify")
                    .header("Authorization", "Basic " + base64Auth)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(TokenVerify.class)
                    .map(Result::success)
                    .onErrorResume(e -> {
                        logger.error("HTTP error: {}", e.getMessage());
                        if (e instanceof WebClientResponseException exception) {
                            logger.error("Status code: {}", exception.getStatusCode());
                            logger.error("Response body: {}", exception.getResponseBodyAsString());
                            ErrorResponse errorResponse = parseErrorMessage(exception.getResponseBodyAsString());
                            return Mono.just(new Result<>(errorResponse.getCode(), errorResponse.getContent(), null));
                        }
                        return Mono.just(Result.error("Unknown error occurred"));
                    });
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private ErrorResponse parseErrorMessage(String responseBody) {
        try {
            // 响应体是一个 JSON 对象，将其解析并提取错误信息
            return objectMapper.readValue(responseBody, ErrorResponse.class);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing error response body", e);
            return new ErrorResponse();
        }
    }

    @Setter
    @Getter
    private static class ErrorResponse {
        private int code;
        private String content;
    }
}