package com.zqc.itineraryweb.service;

import com.zqc.itineraryweb.entity.Result;
import com.zqc.itineraryweb.entity.login.LoginTokenVerifyRequest;
import reactor.core.publisher.Mono;

public interface LoginService {
    Mono<Result<?>> verifyLoginToken(LoginTokenVerifyRequest request);
}