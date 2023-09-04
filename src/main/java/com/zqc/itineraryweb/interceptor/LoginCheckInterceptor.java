package com.zqc.itineraryweb.interceptor;

import com.google.gson.Gson;
import com.zqc.itineraryweb.dao.UserRepository;
import com.zqc.itineraryweb.entity.Result;
import com.zqc.itineraryweb.utils.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginCheckInterceptor.class);
    private static final String AUTH_HEADER = "Authorization";
    private static final String LOGIN_URL = "/user/login";
    private static final String NULL_TOKEN_RESPONSE = "NULL_TOKEN";
    private static final String INVALID_TOKEN_RESPONSE = "INVALID_TOKEN";
    private static final String EXPIRED_TOKEN_RESPONSE = "EXPIRED_TOKEN";

    private final UserRepository userRepository;

    public LoginCheckInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {

        // 获取请求 url
        String url = request.getRequestURL().toString();
        LOGGER.info("发起请求的URL: {}", url);

        // 获取请求头中的令牌
        String token = request.getHeader(AUTH_HEADER);

        // 判断url路径段是否为登录请求
        if (url.endsWith(LOGIN_URL)) {
            LOGGER.info("当前URL为登录请求");

            // 获取请求参数
            String username = request.getParameter("username");
            int tokenByUsername = userRepository.findTokenByUsername(username);
            // 如果Token为null时可以放行请求，登录方法中会生成新的token存储返回
            if (tokenByUsername == 0) {
                return true;
            }

            // 检查令牌是否存在，不存在则拦截
            if (token.isEmpty()) {
                LOGGER.info("请求头Token为空，拦截请求");
                return handleUnauthorized(response, NULL_TOKEN_RESPONSE);
            }

            // 解析token，不合法则拦截
            try {
                JwtUtils.parseJwt(token);
            } catch (ExpiredJwtException e) {
                LOGGER.error("令牌已过有效期");
                // 令牌过期则删除数据库中对应jwt
                userRepository.updateTokenByUsername(null, username);
                return handleUnauthorized(response, EXPIRED_TOKEN_RESPONSE);
            } catch (JwtException e) {
                LOGGER.error("解析JWT时发生错误: {}, 错误信息为: {}", e, e.getMessage());
                // 令牌解析错误也需要删除数据库中对应jwt
                userRepository.updateTokenByUsername(null, username);
                return handleUnauthorized(response, INVALID_TOKEN_RESPONSE);
            }

        }

        // 最终在所有情况之外可以放行请求
        LOGGER.info("最终允许放行请求");
        return true;
    }

    private boolean handleUnauthorized(HttpServletResponse response, String msg) {
        Result<String> unauthorizedResult = Result.error(msg);
        String jsonResponse = new Gson().toJson(unauthorizedResult);
        try {
            response.getWriter().write(jsonResponse);
        } catch (IOException e) {
            LOGGER.error("发生了错误: {}, 错误信息为: {}", e, e.getMessage());
            throw new RuntimeException(e);
        }

        return false;
    }

}
