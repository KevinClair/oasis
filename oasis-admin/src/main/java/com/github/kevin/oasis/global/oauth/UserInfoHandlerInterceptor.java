package com.github.kevin.oasis.global.oauth;

import com.github.kevin.oasis.common.BusinessException;
import com.github.kevin.oasis.common.ResponseStatusEnums;
import com.github.kevin.oasis.models.base.UserInfo;
import com.github.kevin.oasis.utils.JwtTokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserInfoHandlerInterceptor implements HandlerInterceptor {

    private static final String HTTP_HEADER_AUTHORIZATION_TOKEN_PREFIX = "Bearer ";

    private final JwtTokenUtils jwtTokenUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果当前方法包含@Permission注解，就校验登录人信息
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Permission permission = handlerMethod.getMethodAnnotation(Permission.class);
        if (Objects.nonNull(permission)) {
            String token = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (token == null || token.isEmpty() || !token.startsWith(HTTP_HEADER_AUTHORIZATION_TOKEN_PREFIX)) {
                throw new BusinessException(ResponseStatusEnums.NEED_LOGIN.getCode(), ResponseStatusEnums.NEED_LOGIN.getMsg());
            }
            UserInfo userInfo = jwtTokenUtils.parseToken(token.substring(HTTP_HEADER_AUTHORIZATION_TOKEN_PREFIX.length()));
            if (Objects.isNull(userInfo)) {
                throw new BusinessException(ResponseStatusEnums.INVALID_TOKEN.getCode(), ResponseStatusEnums.INVALID_TOKEN.getMsg());
            }
            UserThreadLocal.setUserInfo(userInfo);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserThreadLocal.clear();
    }
}
