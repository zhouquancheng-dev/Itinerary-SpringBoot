package com.zqc.itineraryweb.interceptor;

import com.google.gson.Gson;
import com.zqc.itineraryweb.entity.Result;
import com.zqc.itineraryweb.service.UserService;
import com.zqc.itineraryweb.utils.JwtUtils;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginCheckInterceptor.class);
    private static final String AUTH_HEADER = "Authorization";
    private static final String LOGIN_URL = "/user/login";

    private final UserService userService;

    public LoginCheckInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {

        // 获取请求 url
        String url = request.getRequestURL().toString();
        log.info("发起请求的URL: {}", url);

        // 获取请求头中的令牌
        String jwtToken = request.getHeader(AUTH_HEADER);

        // 判断是否为登录请求
        if (url.contains(LOGIN_URL)) {
            log.info("当前URL为登录请求");

            // 获取请求参数
            String username = request.getParameter("username");
            int userByToken = userService.getUserByToken(username);

            // 如果用户之前没有令牌，或者令牌已过期，则生成新的令牌并更新数据库
            if (userByToken == 0) {
                return true;
            }

            // 检查令牌是否存在，不存在则拦截
            if (!StringUtils.hasLength(jwtToken)) {
                log.info("请求头Token为空，拦截请求");
                return handleUnauthorized(response, "NOT_TOKEN");
            }

            // 解析token，不合法则拦截
            try {
                JwtUtils.parseJwt(jwtToken);
            } catch (Exception e) {
                logger.error("发生了错误: {}, 错误信息为: {}", e, e.getMessage());
                log.info("令牌解析失败，拦截请求");
                // 解析失败或者过期则清空对应的 token 列字段
                userService.clearUserToken(username, null);

                return handleUnauthorized(response, "ERROR_TOKEN");
            }
        }

        // 最终在所有情况之外可以放行请求
        log.info("最终放行请求");
        return true;
    }

    private boolean handleUnauthorized(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("Content-Type", "application/json;charset=utf-8");

        Result<String> unauthorizedResult = Result.error(msg);
        String jsonResponse = new Gson().toJson(unauthorizedResult);
        try {
            response.getWriter().write(jsonResponse);
        } catch (java.io.IOException e) {
            logger.error("发生了错误: {}, 错误信息为: {}", e, e.getMessage());
            throw new RuntimeException(e);
        }

        return false;
    }

}
